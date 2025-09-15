package com.carbontracker.service.impl;

import com.carbontracker.dto.FuelCombustionDTO;
import com.carbontracker.model.EmissionLog;
import com.carbontracker.model.FuelCombustion;
import com.carbontracker.repository.EmissionLogRepository;
import com.carbontracker.repository.FuelCombustionRepository;
import com.carbontracker.service.FuelCombustionService;
import com.carbontracker.service.EmissionAggregationService;
import com.carbontracker.util.EmissionCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FuelCombustionServiceImpl implements FuelCombustionService {

    private final FuelCombustionRepository fuelCombustionRepository;
    private final EmissionLogRepository emissionLogRepository;
    private final EmissionCalculator emissionCalculator;
    private final EmissionAggregationService aggregationService;

    @Autowired
    public FuelCombustionServiceImpl(FuelCombustionRepository fuelCombustionRepository,
                                     EmissionLogRepository emissionLogRepository,
                                     EmissionCalculator emissionCalculator,
                                     EmissionAggregationService aggregationService) {
        this.fuelCombustionRepository = fuelCombustionRepository;
        this.emissionLogRepository = emissionLogRepository;
        this.emissionCalculator = emissionCalculator;
        this.aggregationService = aggregationService;
    }

    @Override
    public FuelCombustion addFuelCombustion(FuelCombustionDTO dto) {
        EmissionLog log = emissionLogRepository.findById(dto.getEmissionLogId())
                .orElseThrow(() -> new RuntimeException("Emission log not found"));

        double emissions = emissionCalculator.calculateFuelEmissions(
                dto.getFuelType(),
                dto.getUnitType(),
                dto.getAmount() != null ? dto.getAmount().doubleValue() : 0.0
        );

        FuelCombustion fuel = new FuelCombustion();
        fuel.setEmissionLog(log);
        fuel.setFuelType(dto.getFuelType());
        fuel.setUnitType(dto.getUnitType());  // <-- fixed
        fuel.setAmount(dto.getAmount());
        fuel.setEmissionsKg(BigDecimal.valueOf(emissions));

        FuelCombustion saved = fuelCombustionRepository.save(fuel);
        aggregationService.updateTotalEmissions(log.getId());
        return saved;
    }

    @Override
    public List<FuelCombustion> getFuelCombinations(Long emissionLogId) {
        return fuelCombustionRepository.findByEmissionLogId(emissionLogId);
    }
}
