package com.carbontracker.controller;

import com.carbontracker.dto.FuelCombustionDTO;
import com.carbontracker.model.FuelCombustion;
import com.carbontracker.service.FuelCombustionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuel")
public class FuelCombustionController {

    private final FuelCombustionService fuelCombustionService;

    @Autowired
    public FuelCombustionController(FuelCombustionService fuelCombustionService) {
        this.fuelCombustionService = fuelCombustionService;
    }

    @PostMapping
    public FuelCombustion addFuel(@RequestBody FuelCombustionDTO dto) {
        return fuelCombustionService.addFuelCombustion(dto);
    }

    @GetMapping("/{logId}")
    public List<FuelCombustion> getFuel(@PathVariable Long logId) {
        return fuelCombustionService.getFuelCombinations(logId);
    }
}