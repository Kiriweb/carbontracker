package com.carbontracker.service.impl;

import com.carbontracker.model.*;
import com.carbontracker.repository.*;
import com.carbontracker.service.EmissionAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EmissionAggregationServiceImpl implements EmissionAggregationService {

    @Autowired private EmissionLogRepository emissionLogRepository;
    @Autowired private VehicleTripRepository vehicleTripRepository;
    @Autowired private ElectricityUseRepository electricityUseRepository;
    @Autowired private WasteDisposalRepository wasteDisposalRepository;
    @Autowired private FuelCombustionRepository fuelCombustionRepository;

    @Override
    public void updateTotalEmissions(Long emissionLogId) {
        EmissionLog log = emissionLogRepository.findById(emissionLogId)
                .orElseThrow(() -> new RuntimeException("Emission log not found"));

        BigDecimal total = BigDecimal.ZERO;

        List<VehicleTrip> trips = vehicleTripRepository.findByEmissionLogId(emissionLogId);
        for (VehicleTrip t : trips) {
            if (t.getEmissionsKg() != null) {
                total = total.add(t.getEmissionsKg());
            }
        }

        List<ElectricityUse> usages = electricityUseRepository.findByEmissionLogId(emissionLogId);
        for (ElectricityUse u : usages) {
            if (u.getEmissionsKg() != null) {
                total = total.add(u.getEmissionsKg());
            }
        }

        List<WasteDisposal> wastes = wasteDisposalRepository.findByEmissionLogId(emissionLogId);
        for (WasteDisposal w : wastes) {
            if (w.getEmissionsKg() != null) {
                total = total.add(w.getEmissionsKg());
            }
        }

        List<FuelCombustion> fuels = fuelCombustionRepository.findByEmissionLogId(emissionLogId);
        for (FuelCombustion f : fuels) {
            if (f.getEmissionsKg() != null) {
                total = total.add(f.getEmissionsKg());
            }
        }

        log.setTotalEmissionsKg(total);
        emissionLogRepository.save(log);
    }
}
