package com.carbontracker.dto;

import java.math.BigDecimal;

public class FuelCombustionDTO {

    private Long emissionLogId;
    private String fuelType;
    private String unitType;   // <-- renamed from unit
    private BigDecimal amount;

    // --- getters & setters ---

    public Long getEmissionLogId() {
        return emissionLogId;
    }

    public void setEmissionLogId(Long emissionLogId) {
        this.emissionLogId = emissionLogId;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getUnitType() {   // <-- fixed getter
        return unitType;
    }

    public void setUnitType(String unitType) {   // <-- fixed setter
        this.unitType = unitType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
