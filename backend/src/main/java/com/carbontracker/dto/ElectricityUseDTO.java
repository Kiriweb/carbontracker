package com.carbontracker.dto;

import java.math.BigDecimal;

public class ElectricityUseDTO {
    private Long emissionLogId;
    private String countryCode;
    private BigDecimal kwhUsed;

    // Getters & Setters
    public Long getEmissionLogId() { return emissionLogId; }
    public void setEmissionLogId(Long emissionLogId) { this.emissionLogId = emissionLogId; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public BigDecimal getKwhUsed() { return kwhUsed; }
    public void setKwhUsed(BigDecimal kwhUsed) { this.kwhUsed = kwhUsed; }
}
