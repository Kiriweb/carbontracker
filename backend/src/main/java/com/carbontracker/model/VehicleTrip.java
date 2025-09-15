package com.carbontracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_trips")
public class VehicleTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> emission_logs.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emission_log_id", nullable = false)
    private EmissionLog emissionLog;

    @Column(name = "vehicle_type", length = 100)
    private String vehicleType;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "emissions_kg", precision = 10, scale = 4)
    private BigDecimal emissionsKg;

    // --- getters & setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EmissionLog getEmissionLog() { return emissionLog; }
    public void setEmissionLog(EmissionLog emissionLog) { this.emissionLog = emissionLog; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }

    public BigDecimal getEmissionsKg() { return emissionsKg; }
    public void setEmissionsKg(BigDecimal emissionsKg) { this.emissionsKg = emissionsKg; }
}
