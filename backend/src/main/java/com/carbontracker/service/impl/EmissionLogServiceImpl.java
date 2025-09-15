package com.carbontracker.service.impl;

import com.carbontracker.dto.EmissionLogDTO;
import com.carbontracker.dto.QuickEmissionRequest;
import com.carbontracker.model.EmissionLog;
import com.carbontracker.model.User;
import com.carbontracker.repository.EmissionLogRepository;
import com.carbontracker.repository.UserRepository;
import com.carbontracker.service.EmissionLogService;
import com.carbontracker.util.EmissionCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EmissionLogServiceImpl implements EmissionLogService {

    private final EmissionLogRepository emissionLogRepository;
    private final UserRepository userRepository;
    private final EmissionCalculator calculator;

    public EmissionLogServiceImpl(EmissionLogRepository emissionLogRepository,
                                  UserRepository userRepository,
                                  EmissionCalculator calculator) {
        this.emissionLogRepository = emissionLogRepository;
        this.userRepository = userRepository;
        this.calculator = calculator;
    }

    /**
     * Standard create from a full DTO payload.
     */
    @Override
    public EmissionLogDTO create(String userEmail, EmissionLogDTO dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        EmissionLog e = new EmissionLog();
        e.setUser(user);
        e.setDate(dto.getDate() != null ? dto.getDate() : LocalDate.now());
        e.setTotalEmissionsKg(scale(dto.getTotalEmissionsKg()));
        e.setCategory(dto.getCategory());
        e.setDescription(dto.getDescription());
        // If dto.co2e is null, fall back to totalEmissionsKg
        BigDecimal co2e = dto.getCo2e() != null ? dto.getCo2e() : dto.getTotalEmissionsKg();
        e.setCo2e(scale(co2e));
        e.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());

        EmissionLog saved = emissionLogRepository.save(e);
        return EmissionLogDTO.fromEntity(saved);
    }

    /**
     * Quick-entry convenience that computes totals and persists.
     */
    @Override
    public EmissionLogDTO createQuick(String userEmail, QuickEmissionRequest req) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        if (req == null || req.category() == null || req.category().isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }

        String category = req.category().trim().toLowerCase();
        double co2e;
        String details;

        switch (category) {
            case "vehicle trip" -> {
                double distance = nz(req.distanceKm());
                co2e = calculator.calculateVehicleEmissions(req.vehicleType(), req.vehicleFuel(), distance);
                details = String.format("Vehicle %s (%s), distance=%.3f km",
                        n(req.vehicleType()), n(req.vehicleFuel()), distance);
            }
            case "electricity use" -> {
                double kwh = nz(req.kwh());
                co2e = calculator.calculateElectricityEmissions(n(req.electricityCountry()), kwh);
                details = String.format("Electricity, country=%s, kWh=%.3f",
                        n(req.electricityCountry()), kwh);
            }
            case "waste disposal" -> {
                double weight = nz(req.wasteKg());
                co2e = calculator.calculateWasteEmissions(req.wasteType(), req.wasteMethod(), weight);
                details = String.format("Waste %s via %s, weight=%.3f kg",
                        n(req.wasteType()), n(req.wasteMethod()), weight);
            }
            case "fuel combustion" -> {
                double qty = nz(req.fuelQuantity());
                co2e = calculator.calculateFuelEmissions(req.fuelType(), req.fuelUnit(), qty);
                details = String.format("Fuel %s (%s), qty=%.3f",
                        n(req.fuelType()), n(req.fuelUnit()), qty);
            }
            default -> throw new IllegalArgumentException("Unknown category: " + req.category());
        }

        EmissionLog e = new EmissionLog();
        e.setUser(user);
        e.setDate(LocalDate.now()); // existing DATE column
        e.setTotalEmissionsKg(scale(BigDecimal.valueOf(co2e)));
        e.setCategory(req.category());
        e.setDescription(details);
        e.setCo2e(scale(BigDecimal.valueOf(co2e)));
        e.setCreatedAt(LocalDateTime.now());

        EmissionLog saved = emissionLogRepository.save(e);
        return EmissionLogDTO.fromEntity(saved);
    }

    /**
     * Current user’s logs (by email) as DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmissionLogDTO> getMyLogs(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        return emissionLogRepository.findByUserId(user.getId()).stream()
                .map(EmissionLogDTO::fromEntity)
                .toList();
    }

    /**
     * Admin/internal variant (by userId) as DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmissionLogDTO> getLogsByUserId(Long userId) {
        return emissionLogRepository.findByUserId(userId).stream()
                .map(EmissionLogDTO::fromEntity)
                .toList();
    }

    // --- helpers ---
    private static BigDecimal scale(BigDecimal v) {
        return v == null ? null : v.setScale(4, RoundingMode.HALF_UP);
    }

    private static String n(String s) {
        return s == null ? "-" : s;
    }

    private static double nz(Double d) {
        return d == null ? 0.0 : d;
    }
}
