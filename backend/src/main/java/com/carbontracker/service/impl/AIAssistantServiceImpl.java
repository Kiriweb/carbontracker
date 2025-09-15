package com.carbontracker.service.impl;

import com.carbontracker.model.*;
import com.carbontracker.repository.*;
import com.carbontracker.service.AIAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Service
public class AIAssistantServiceImpl implements AIAssistantService {

    private final EmissionLogRepository emissionLogRepository;
    private final VehicleTripRepository vehicleTripRepository;
    private final ElectricityUseRepository electricityUseRepository;
    private final WasteDisposalRepository wasteDisposalRepository;
    private final FuelCombustionRepository fuelCombustionRepository;
    private final ApiKeyStorageRepository apiKeyStorageRepository;

    private final String secretKey;

    @Autowired
    public AIAssistantServiceImpl(EmissionLogRepository emissionLogRepository,
                                  VehicleTripRepository vehicleTripRepository,
                                  ElectricityUseRepository electricityUseRepository,
                                  WasteDisposalRepository wasteDisposalRepository,
                                  FuelCombustionRepository fuelCombustionRepository,
                                  ApiKeyStorageRepository apiKeyStorageRepository,
                                  @Value("${encryption.secret}") String secretKey) {
        this.emissionLogRepository = emissionLogRepository;
        this.vehicleTripRepository = vehicleTripRepository;
        this.electricityUseRepository = electricityUseRepository;
        this.wasteDisposalRepository = wasteDisposalRepository;
        this.fuelCombustionRepository = fuelCombustionRepository;
        this.apiKeyStorageRepository = apiKeyStorageRepository;
        this.secretKey = secretKey;
    }

    @Override
    public void setApiKey(String apiKey) {
        String encrypted = encrypt(apiKey);
        ApiKeyStorage record = apiKeyStorageRepository.findByName("openai")
                .orElse(new ApiKeyStorage());
        record.setName("openai");
        record.setEncryptedKey(encrypted);
        apiKeyStorageRepository.save(record);
    }

    @Override
    public String getMaskedApiKey() {
        return apiKeyStorageRepository.findByName("openai")
                .map(ApiKeyStorage::getEncryptedKey)
                .map(this::decrypt)
                .map(key -> key.length() > 4 ? "************" + key.substring(key.length() - 4) : "")
                .orElse("");
    }

    private String getDecryptedKey() {
        return apiKeyStorageRepository.findByName("openai")
                .map(ApiKeyStorage::getEncryptedKey)
                .map(this::decrypt)
                .orElseThrow(() -> new RuntimeException("API key not set"));
    }

    @Override
    public String generateSuggestions(Long logId) {
        String apiKey = getDecryptedKey();

        EmissionLog log = emissionLogRepository.findById(logId).orElseThrow();
        List<VehicleTrip> trips = vehicleTripRepository.findByEmissionLogId(logId);
        List<ElectricityUse> electricity = electricityUseRepository.findByEmissionLogId(logId);
        List<WasteDisposal> waste = wasteDisposalRepository.findByEmissionLogId(logId);
        List<FuelCombustion> fuel = fuelCombustionRepository.findByEmissionLogId(logId);

        String prompt = "Based on the following carbon emission log, give advice to reduce emissions:\n" +
                "Total emissions: " + log.getTotalEmissionsKg() + " kg\n" +
                "Trips: " + trips.size() + ", Electricity: " + electricity.size() + ", Waste: " + waste.size() + ", Fuel: " + fuel.size();

        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            String body = new ObjectMapper().writeValueAsString(new Object() {
                public final String model = "gpt-3.5-turbo";
                public final Object[] messages = new Object[]{
                        new Object() {
                            public final String role = "user";
                            public final String content = prompt;
                        }
                };
            });
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(RequestBody.create(body, mediaType))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new RuntimeException("OpenAI API error");

            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while contacting AI service.";
        }
    }

    private String encrypt(String strToEncrypt) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    private String decrypt(String strToDecrypt) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting", e);
        }
    }
}