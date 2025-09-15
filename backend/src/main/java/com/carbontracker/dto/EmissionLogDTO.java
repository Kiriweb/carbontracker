package com.carbontracker.dto;

import com.carbontracker.model.EmissionLog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmissionLogDTO {
    private Long id;
    private LocalDate date;               // existing column in your schema
    private BigDecimal totalEmissionsKg;  // existing column
    private String category;              // new
    private String description;           // new
    private BigDecimal co2e;              // new
    private LocalDateTime createdAt;      // new
    private Long userId;                  // useful on admin views

    public EmissionLogDTO() {}

    // Convenience mapper (static) — no extra class needed
    public static EmissionLogDTO fromEntity(EmissionLog e) {
        EmissionLogDTO d = new EmissionLogDTO();
        d.setId(e.getId());
        d.setDate(e.getDate());
        d.setTotalEmissionsKg(e.getTotalEmissionsKg());
        d.setCategory(e.getCategory());
        d.setDescription(e.getDescription());
        d.setCo2e(e.getCo2e());
        d.setCreatedAt(e.getCreatedAt());
        d.setUserId(e.getUser() != null ? e.getUser().getId() : null);
        return d;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getTotalEmissionsKg() { return totalEmissionsKg; }
    public void setTotalEmissionsKg(BigDecimal totalEmissionsKg) { this.totalEmissionsKg = totalEmissionsKg; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getCo2e() { return co2e; }
    public void setCo2e(BigDecimal co2e) { this.co2e = co2e; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
