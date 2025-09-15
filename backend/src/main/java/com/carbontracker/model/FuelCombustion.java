package com.carbontracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fuel_use")
public class FuelCombustion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> emission_logs.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emission_log_id", nullable = false)
    private EmissionLog emissionLog;

    @Column(name = "fuel_type", length = 100)
    private String fuelType;

    @Column(name = "unit_type", length = 20)
    private String unitType;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "emissions_kg", precision = 10, scale = 4)
    private BigDecimal emissionsKg;

    // --- getters & setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EmissionLog getEmissionLog() { return emissionLog; }
    public void setEmissionLog(EmissionLog emissionLog) { this.emissionLog = emissionLog; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getEmissionsKg() { return emissionsKg; }
    public void setEmissionsKg(BigDecimal emissionsKg) { this.emissionsKg = emissionsKg; }
}
