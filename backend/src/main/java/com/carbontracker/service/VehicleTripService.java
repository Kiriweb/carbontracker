package com.carbontracker.service;

import com.carbontracker.dto.VehicleTripDTO;
import com.carbontracker.model.VehicleTrip;

import java.util.List;

public interface VehicleTripService {
    VehicleTrip addTrip(VehicleTripDTO dto);
    List<VehicleTrip> getTripsByLog(Long emissionLogId);
}

