package com.carbontracker.service;

import com.carbontracker.dto.EmissionLogDTO;
import com.carbontracker.dto.QuickEmissionRequest;

import java.util.List;

public interface EmissionLogService {

    /** Standard create from a full DTO payload. */
    EmissionLogDTO create(String userEmail, EmissionLogDTO dto);

    /** Quick-entry convenience that computes totals and persists. */
    EmissionLogDTO createQuick(String userEmail, QuickEmissionRequest req);

    /** Current user’s logs (by email). */
    List<EmissionLogDTO> getMyLogs(String userEmail);

    /** Utility overload for admin or internal use. */
    List<EmissionLogDTO> getLogsByUserId(Long userId);
}


