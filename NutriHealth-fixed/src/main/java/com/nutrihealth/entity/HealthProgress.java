package com.nutrihealth.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_progress")
public class HealthProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(name = "progress_date", nullable = false)
    private LocalDate progressDate = LocalDate.now();

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Column(name = "height_cm")
    private Double heightCm;

    private Double bmi;

    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "blood_sugar_mg_dl")
    private Double bloodSugarMgDl;

    @Column(name = "heart_rate_bpm")
    private Integer heartRateBpm;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void calculateBmi() {
        if (heightCm != null && heightCm > 0 && weightKg != null && weightKg > 0) {
            double h = heightCm / 100.0;
            bmi = Math.round((weightKg / (h * h)) * 10.0) / 10.0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }
    public LocalDate getProgressDate() { return progressDate; }
    public void setProgressDate(LocalDate progressDate) { this.progressDate = progressDate; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }
    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }
    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }
    public Double getBloodSugarMgDl() { return bloodSugarMgDl; }
    public void setBloodSugarMgDl(Double bloodSugarMgDl) { this.bloodSugarMgDl = bloodSugarMgDl; }
    public Integer getHeartRateBpm() { return heartRateBpm; }
    public void setHeartRateBpm(Integer heartRateBpm) { this.heartRateBpm = heartRateBpm; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getBmiCategory() {
        if (bmi == null) return "N/A";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }
}
