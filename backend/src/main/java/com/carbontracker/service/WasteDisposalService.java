package com.carbontracker.service;

import com.carbontracker.dto.WasteDisposalDTO;
import com.carbontracker.model.WasteDisposal;

import java.util.List;

public interface WasteDisposalService {
    WasteDisposal addWasteDisposal(WasteDisposalDTO dto);
    List<WasteDisposal> getWasteDisposals(Long emissionLogId);
}

