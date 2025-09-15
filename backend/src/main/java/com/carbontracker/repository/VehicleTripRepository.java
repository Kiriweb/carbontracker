package com.carbontracker.repository;

import com.carbontracker.model.VehicleTrip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleTripRepository extends JpaRepository<VehicleTrip, Long> {
    List<VehicleTrip> findByEmissionLogId(Long emissionLogId);
}

