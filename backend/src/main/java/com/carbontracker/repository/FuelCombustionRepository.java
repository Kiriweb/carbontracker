package com.carbontracker.repository;

import com.carbontracker.model.FuelCombustion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuelCombustionRepository extends JpaRepository<FuelCombustion, Long> {
    List<FuelCombustion> findByEmissionLogId(Long emissionLogId);
}