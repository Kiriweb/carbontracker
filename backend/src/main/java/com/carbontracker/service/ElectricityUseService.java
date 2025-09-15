package com.carbontracker.service;

import com.carbontracker.dto.ElectricityUseDTO;
import com.carbontracker.model.ElectricityUse;

import java.util.List;

public interface ElectricityUseService {
    ElectricityUse addElectricityUse(ElectricityUseDTO dto);
    List<ElectricityUse> getElectricityUses(Long emissionLogId);
}