package com.carbontracker.controller;

import com.carbontracker.dto.ElectricityUseDTO;
import com.carbontracker.model.ElectricityUse;
import com.carbontracker.service.ElectricityUseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/electricity")
public class ElectricityUseController {

    private final ElectricityUseService electricityUseService;

    @Autowired
    public ElectricityUseController(ElectricityUseService electricityUseService) {
        this.electricityUseService = electricityUseService;
    }

    @PostMapping
    public ElectricityUse addUsage(@RequestBody ElectricityUseDTO dto) {
        return electricityUseService.addElectricityUse(dto);
    }

    @GetMapping("/{logId}")
    public List<ElectricityUse> getUsage(@PathVariable Long logId) {
        return electricityUseService.getElectricityUses(logId);
    }
}

