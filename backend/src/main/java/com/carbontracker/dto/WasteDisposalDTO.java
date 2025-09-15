package com.carbontracker.dto;

import java.math.BigDecimal;

public class WasteDisposalDTO {
    private Long emissionLogId;
    private String wasteType;
    private String disposalMethod;   // <- align name with entity/DB
    private BigDecimal weightKg;

    public Long getEmissionLogId() { return emissionLogId; }
    public void setEmissionLogId(Long emissionLogId) { this.emissionLogId = emissionLogId; }

    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }

    public String getDisposalMethod() { return disposalMethod; }
    public void setDisposalMethod(String disposalMethod) { this.disposalMethod = disposalMethod; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
}
