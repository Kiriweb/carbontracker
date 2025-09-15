package com.carbontracker.repository;

import com.carbontracker.model.WasteDisposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WasteDisposalRepository extends JpaRepository<WasteDisposal, Long> {
    List<WasteDisposal> findByEmissionLogId(Long emissionLogId);
}