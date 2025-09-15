package com.carbontracker.controller;

import com.carbontracker.dto.WasteDisposalDTO;
import com.carbontracker.model.WasteDisposal;
import com.carbontracker.service.WasteDisposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waste")
public class WasteDisposalController {

    private final WasteDisposalService wasteDisposalService;

    @Autowired
    public WasteDisposalController(WasteDisposalService wasteDisposalService) {
        this.wasteDisposalService = wasteDisposalService;
    }

    @PostMapping
    public WasteDisposal addDisposal(@RequestBody WasteDisposalDTO dto) {
        return wasteDisposalService.addWasteDisposal(dto);
    }

    @GetMapping("/{logId}")
    public List<WasteDisposal> getDisposals(@PathVariable Long logId) {
        return wasteDisposalService.getWasteDisposals(logId);
    }
}