package com.carbontracker.service.impl;

import com.carbontracker.dto.ElectricityUseDTO;
import com.carbontracker.model.ElectricityUse;
import com.carbontracker.model.EmissionLog;
import com.carbontracker.repository.ElectricityUseRepository;
import com.carbontracker.repository.EmissionLogRepository;
import com.carbontracker.service.ElectricityUseService;
import com.carbontracker.service.EmissionAggregationService;
import com.carbontracker.util.EmissionCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ElectricityUseServiceImpl implements ElectricityUseService {

    private final ElectricityUseRepository electricityUseRepository;
    private final EmissionLogRepository emissionLogRepository;
    private final EmissionCalculator emissionCalculator;
    private final EmissionAggregationService aggregationService;

    @Autowired
    public ElectricityUseServiceImpl(ElectricityUseRepository electricityUseRepository,
                                     EmissionLogRepository emissionLogRepository,
                                     EmissionCalculator emissionCalculator,
                                     EmissionAggregationService aggregationService) {
        this.electricityUseRepository = electricityUseRepository;
        this.emissionLogRepository = emissionLogRepository;
        this.emissionCalculator = emissionCalculator;
        this.aggregationService = aggregationService;
    }

    @Override
    public ElectricityUse addElectricityUse(ElectricityUseDTO dto) {
        EmissionLog log = emissionLogRepository.findById(dto.getEmissionLogId())
                .orElseThrow(() -> new RuntimeException("Emission log not found"));

        double emissions = emissionCalculator.calculateElectricityEmissions(
                dto.getCountryCode(),
                dto.getKwhUsed() != null ? dto.getKwhUsed().doubleValue() : 0.0
        );

        ElectricityUse use = new ElectricityUse();
        use.setEmissionLog(log);
        use.setCountryCode(dto.getCountryCode());
        use.setKwhUsed(dto.getKwhUsed());
        use.setEmissionsKg(BigDecimal.valueOf(emissions));

        ElectricityUse saved = electricityUseRepository.save(use);
        aggregationService.updateTotalEmissions(log.getId());
        return saved;
    }

    @Override
    public List<ElectricityUse> getElectricityUses(Long emissionLogId) {
        return electricityUseRepository.findByEmissionLogId(emissionLogId);
    }
}
