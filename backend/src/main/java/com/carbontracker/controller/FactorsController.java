package com.carbontracker.controller;

import com.carbontracker.util.EmissionCalculator;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factors")
public class FactorsController {

    private final EmissionCalculator emissionCalculator;

    public FactorsController(EmissionCalculator emissionCalculator) {
        this.emissionCalculator = emissionCalculator;
    }

    @GetMapping("/vehicles")
    public List<String> vehicles() {
        return emissionCalculator.getVehicleKeys();
    }

    @GetMapping("/electricity-countries")
    public List<String> electricityCountries() {
        return emissionCalculator.getElectricityCountryCodes();
    }

    @GetMapping("/waste")
    public Map<String, List<String>> waste() {
        return emissionCalculator.getWasteTypesWithMethods();
    }

    @GetMapping("/fuels")
    public List<String> fuels() {
        return emissionCalculator.getFuelKeys();
    }
}
