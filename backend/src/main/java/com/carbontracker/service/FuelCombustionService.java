package com.carbontracker.service;

import com.carbontracker.dto.FuelCombustionDTO;
import com.carbontracker.model.FuelCombustion;

import java.util.List;

public interface FuelCombustionService {
    FuelCombustion addFuelCombustion(FuelCombustionDTO dto);
    List<FuelCombustion> getFuelCombinations(Long emissionLogId);
}