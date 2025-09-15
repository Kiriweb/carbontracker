package com.carbontracker.service.impl;

import com.carbontracker.model.ApiKeyStorage;
import com.carbontracker.model.EmissionLog;
import com.carbontracker.model.User;
import com.carbontracker.repository.ApiKeyStorageRepository;
import com.carbontracker.repository.EmissionLogRepository;
import com.carbontracker.repository.UserRepository;
import com.carbontracker.service.AIAssistantService;
import com.carbontracker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Service
public class AIAssistantServiceImpl implements AIAssistantService {

    private final ApiKeyStorageRepository keyRepo;
    private final UserRepository userRepo;
    private final EmissionLogRepository logRepo;
    private final JwtUtil jwtUtil;

    @Value("${encryption.secret:}")
    private String secret; // available if you later add encryption

    public AIAssistantServiceImpl(ApiKeyStorageRepository keyRepo,
                                  UserRepository userRepo,
                                  EmissionLogRepository logRepo,
                                  JwtUtil jwtUtil) {
        this.keyRepo = keyRepo;
        this.userRepo = userRepo;
        this.logRepo = logRepo;
        this.jwtUtil = jwtUtil;
    }

    // --- key management ---

    @Override
    public boolean hasKey() {
        return keyRepo.findFirst().map(k -> k.getApiKey() != null && !k.getApiKey().isBlank()).orElse(false);
    }

    @Override
    public String maskedKey() {
        return keyRepo.findFirst().map(k -> {
            String v = k.getApiKey();
            if (v == null || v.length() < 8) return null;
            return v.substring(0, 4) + "…" + v.substring(v.length() - 4);
        }).orElse(null);
    }

    @Override
    public void storeKey(String apiKeyPlaintext) {
        ApiKeyStorage row = keyRepo.findFirst().orElseGet(ApiKeyStorage::new);
        // If you want, encrypt here with `secret`
        row.setApiKey(apiKeyPlaintext);
        keyRepo.save(row);
    }

    @Override
    public void deleteKey() {
        keyRepo.deleteAll();
    }

    // --- auth utility ---

    @Override
    public String emailFromJwt(String jwt) {
        return jwtUtil.validateToken(jwt); // returns email or null
    }

    // --- suggestions ---

    @Override
    public String generateSuggestionsForLog(String requesterEmail, Long logId) {
        // 1) resolve user + log
        User user = userRepo.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        EmissionLog log = logRepo.findById(logId)
                .orElseThrow(() -> new IllegalStateException("Log not found"));

        // If not admin, ensure ownership
        if (!"admin@carbontracker.com".equalsIgnoreCase(requesterEmail)) {
            if (log.getUser() == null || !log.getUser().getId().equals(user.getId())) {
                throw new IllegalStateException("Forbidden");
            }
        }

        // 2) build prompt
        String prompt = buildPrompt(log);

        // 3) call OpenAI
        String apiKey = keyRepo.findFirst()
                .map(ApiKeyStorage::getApiKey)
                .orElseThrow(() -> new IllegalStateException("OpenAI API key is not set"));

        try {
            String model = "gpt-4o-mini"; // cheaper & good enough for tips
            String requestJson = """
              {
                "model": "%s",
                "messages": [
                  {"role":"system","content":"You are a sustainability coach. Be concise, actionable, and numeric where possible."},
                  {"role":"user","content": %s }
                ],
                "temperature": 0.2
              }
            """.formatted(model, jsonEscape(prompt));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(40))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new IllegalStateException("OpenAI error " + resp.statusCode() + ": " + resp.body());
            }

            // naive parse to pull out content (avoid Gson dependency here)
            String body = resp.body();
            String marker = "\"content\":\"";
            int start = body.indexOf(marker);
            if (start >= 0) {
                start += marker.length();
                int end = body.indexOf("\"", start);
                if (end > start) {
                    return body.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"");
                }
            }
            // fallback: return full JSON
            return body;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to call OpenAI: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(EmissionLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append("Give concise, practical suggestions to reduce CO2e for this daily log.\n");
        sb.append("Total emissions (kg CO2e): ").append(log.getTotalEmissionsKg()).append("\n");
        if (log.getCategory() != null) sb.append("Category: ").append(log.getCategory()).append("\n");
        if (log.getDescription() != null) sb.append("Details: ").append(log.getDescription()).append("\n");
        sb.append("Limit to 6 bullets. Prioritize the largest-impact ideas first.\n");
        return sb.toString();
    }

    private static String jsonEscape(String s) {
        String q = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return "\"" + q + "\"";
    }
}
