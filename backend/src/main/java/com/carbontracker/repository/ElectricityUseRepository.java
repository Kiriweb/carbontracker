package com.carbontracker.repository;

import com.carbontracker.model.ElectricityUse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElectricityUseRepository extends JpaRepository<ElectricityUse, Long> {
    List<ElectricityUse> findByEmissionLogId(Long emissionLogId);
}