package com.carbontracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emission_logs")
public class EmissionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // each log belongs to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // your SQL: date DATE NOT NULL
    @Column(name = "date", nullable = false)
    private LocalDate date;

    // your SQL: total_emissions_kg DECIMAL(10,4)
    @Column(name = "total_emissions_kg", precision = 10, scale = 4)
    private BigDecimal totalEmissionsKg;

    // NEW columns you added in DB (optional but we’ve wired them through)
    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "description", length = 255)
    private String description;

    // DECIMAL(10,4) for consistency and precise sums
    @Column(name = "co2e", precision = 10, scale = 4)
    private BigDecimal co2e;

    // created_at TIMESTAMP (nullable is fine; DB can default it too)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // --- getters & setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getTotalEmissionsKg() {
        return totalEmissionsKg;
    }
    public void setTotalEmissionsKg(BigDecimal totalEmissionsKg) {
        this.totalEmissionsKg = totalEmissionsKg;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCo2e() {
        return co2e;
    }
    public void setCo2e(BigDecimal co2e) {
        this.co2e = co2e;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
