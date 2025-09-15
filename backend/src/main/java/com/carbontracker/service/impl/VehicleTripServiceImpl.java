package com.carbontracker.service.impl;

import com.carbontracker.dto.VehicleTripDTO;
import com.carbontracker.model.EmissionLog;
import com.carbontracker.model.VehicleTrip;
import com.carbontracker.repository.EmissionLogRepository;
import com.carbontracker.repository.VehicleTripRepository;
import com.carbontracker.service.VehicleTripService;
import com.carbontracker.service.EmissionAggregationService;
import com.carbontracker.util.EmissionCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VehicleTripServiceImpl implements VehicleTripService {

    private final VehicleTripRepository vehicleTripRepository;
    private final EmissionLogRepository emissionLogRepository;
    private final EmissionCalculator emissionCalculator;
    private final EmissionAggregationService aggregationService;

    @Autowired
    public VehicleTripServiceImpl(VehicleTripRepository vehicleTripRepository,
                                  EmissionLogRepository emissionLogRepository,
                                  EmissionCalculator emissionCalculator,
                                  EmissionAggregationService aggregationService) {
        this.vehicleTripRepository = vehicleTripRepository;
        this.emissionLogRepository = emissionLogRepository;
        this.emissionCalculator = emissionCalculator;
        this.aggregationService = aggregationService;
    }

    @Override
    public VehicleTrip addTrip(VehicleTripDTO dto) {
        EmissionLog log = emissionLogRepository.findById(dto.getEmissionLogId())
                .orElseThrow(() -> new RuntimeException("Emission log not found"));

        double emissions = emissionCalculator.calculateVehicleEmissions(
                dto.getVehicleType(),
                dto.getFuelType(),
                dto.getDistanceKm() != null ? dto.getDistanceKm().doubleValue() : 0.0
        );

        VehicleTrip trip = new VehicleTrip();
        trip.setEmissionLog(log);
        trip.setVehicleType(dto.getVehicleType());
        trip.setFuelType(dto.getFuelType());
        trip.setDistanceKm(dto.getDistanceKm());
        trip.setEmissionsKg(BigDecimal.valueOf(emissions));

        VehicleTrip saved = vehicleTripRepository.save(trip);
        aggregationService.updateTotalEmissions(log.getId());
        return saved;
    }

    @Override
    public List<VehicleTrip> getTripsByLog(Long emissionLogId) {   // ✅ matches interface
        return vehicleTripRepository.findByEmissionLogId(emissionLogId);
    }
}
