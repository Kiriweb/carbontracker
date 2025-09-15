package com.carbontracker.service.impl;

import com.carbontracker.dto.WasteDisposalDTO;
import com.carbontracker.model.EmissionLog;
import com.carbontracker.model.WasteDisposal;
import com.carbontracker.repository.EmissionLogRepository;
import com.carbontracker.repository.WasteDisposalRepository;
import com.carbontracker.service.EmissionAggregationService;
import com.carbontracker.service.WasteDisposalService;
import com.carbontracker.util.EmissionCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WasteDisposalServiceImpl implements WasteDisposalService {

    private final WasteDisposalRepository wasteDisposalRepository;
    private final EmissionLogRepository emissionLogRepository;
    private final EmissionCalculator emissionCalculator;
    private final EmissionAggregationService aggregationService;

    @Autowired
    public WasteDisposalServiceImpl(WasteDisposalRepository wasteDisposalRepository,
                                    EmissionLogRepository emissionLogRepository,
                                    EmissionCalculator emissionCalculator,
                                    EmissionAggregationService aggregationService) {
        this.wasteDisposalRepository = wasteDisposalRepository;
        this.emissionLogRepository = emissionLogRepository;
        this.emissionCalculator = emissionCalculator;
        this.aggregationService = aggregationService;
    }

    @Override
    public WasteDisposal addWasteDisposal(WasteDisposalDTO dto) {
        EmissionLog log = emissionLogRepository.findById(dto.getEmissionLogId())
                .orElseThrow(() -> new RuntimeException("Emission log not found"));

        double emissions = emissionCalculator.calculateWasteEmissions(
                dto.getWasteType(),
                dto.getDisposalMethod(),   // <- use disposalMethod
                dto.getWeightKg() != null ? dto.getWeightKg().doubleValue() : 0.0
        );

        WasteDisposal disposal = new WasteDisposal();
        disposal.setEmissionLog(log);
        disposal.setWasteType(dto.getWasteType());
        disposal.setDisposalMethod(dto.getDisposalMethod()); // <- correct setter
        disposal.setWeightKg(dto.getWeightKg());
        disposal.setEmissionsKg(BigDecimal.valueOf(emissions));

        WasteDisposal saved = wasteDisposalRepository.save(disposal);
        aggregationService.updateTotalEmissions(log.getId());
        return saved;
    }

    @Override
    public List<WasteDisposal> getWasteDisposals(Long emissionLogId) {
        return wasteDisposalRepository.findByEmissionLogId(emissionLogId);
    }
}
