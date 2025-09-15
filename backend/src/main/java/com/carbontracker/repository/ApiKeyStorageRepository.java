package com.carbontracker.repository;

import com.carbontracker.model.ApiKeyStorage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyStorageRepository extends JpaRepository<ApiKeyStorage, Long> {
    Optional<ApiKeyStorage> findByName(String name);
}

