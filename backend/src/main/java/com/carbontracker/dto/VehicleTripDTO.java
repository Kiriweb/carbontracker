package com.carbontracker.dto;

import java.math.BigDecimal;

public class VehicleTripDTO {

    private Long emissionLogId;
    private String vehicleType;
    private String fuelType;
    private BigDecimal distanceKm;     // ✅ BigDecimal, not Double
    private BigDecimal emissionsKg;    // ✅ BigDecimal, not Double

    // --- getters & setters ---

    public Long getEmissionLogId() {
        return emissionLogId;
    }

    public void setEmissionLogId(Long emissionLogId) {
        this.emissionLogId = emissionLogId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getEmissionsKg() {
        return emissionsKg;
    }

    public void setEmissionsKg(BigDecimal emissionsKg) {
        this.emissionsKg = emissionsKg;
    }
}
