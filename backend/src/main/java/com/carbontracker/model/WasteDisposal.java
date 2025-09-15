package com.carbontracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "waste_entries")
public class WasteDisposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> emission_logs.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emission_log_id", nullable = false)
    private EmissionLog emissionLog;

    @Column(name = "waste_type", length = 100)
    private String wasteType;

    @Column(name = "disposal_method", length = 50)
    private String disposalMethod;

    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "emissions_kg", precision = 10, scale = 4)
    private BigDecimal emissionsKg;

    // --- getters & setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EmissionLog getEmissionLog() { return emissionLog; }
    public void setEmissionLog(EmissionLog emissionLog) { this.emissionLog = emissionLog; }

    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }

    public String getDisposalMethod() { return disposalMethod; }
    public void setDisposalMethod(String disposalMethod) { this.disposalMethod = disposalMethod; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getEmissionsKg() { return emissionsKg; }
    public void setEmissionsKg(BigDecimal emissionsKg) { this.emissionsKg = emissionsKg; }
}
