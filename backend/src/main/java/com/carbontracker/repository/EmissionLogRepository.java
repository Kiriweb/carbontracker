package com.carbontracker.repository;

import com.carbontracker.model.EmissionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmissionLogRepository extends JpaRepository<EmissionLog, Long> {
    List<EmissionLog> findByUserId(Long userId);
}
