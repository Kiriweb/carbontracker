package com.carbontracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "electricity_use")
public class ElectricityUse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emission_log_id", nullable = false)
    private EmissionLog emissionLog;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "kwh_used", precision = 10, scale = 2)
    private BigDecimal kwhUsed;

    @Column(name = "emissions_kg", precision = 10, scale = 4)
    private BigDecimal emissionsKg;

    // --- getters & setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EmissionLog getEmissionLog() { return emissionLog; }
    public void setEmissionLog(EmissionLog emissionLog) { this.emissionLog = emissionLog; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public BigDecimal getKwhUsed() { return kwhUsed; }
    public void setKwhUsed(BigDecimal kwhUsed) { this.kwhUsed = kwhUsed; }

    public BigDecimal getEmissionsKg() { return emissionsKg; }
    public void setEmissionsKg(BigDecimal emissionsKg) { this.emissionsKg = emissionsKg; }
}
