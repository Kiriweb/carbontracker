package com.carbontracker.controller;

import com.carbontracker.dto.VehicleTripDTO;
import com.carbontracker.model.VehicleTrip;
import com.carbontracker.service.VehicleTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class VehicleTripController {

    private final VehicleTripService vehicleTripService;

    @Autowired
    public VehicleTripController(VehicleTripService vehicleTripService) {
        this.vehicleTripService = vehicleTripService;
    }

    @PostMapping
    public VehicleTrip addTrip(@RequestBody VehicleTripDTO dto) {
        return vehicleTripService.addTrip(dto);
    }

    @GetMapping("/{logId}")
    public List<VehicleTrip> getTrips(@PathVariable Long logId) {
        return vehicleTripService.getTripsByLog(logId);
    }
}