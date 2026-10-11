package com.nutrihealth.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate = LocalDate.now();

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Column(name = "height_cm", nullable = false)
    private Double heightCm;

    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "blood_sugar_mg_dl")
    private Double bloodSugarMgDl;

    @Column(name = "heart_rate_bpm")
    private Integer heartRateBpm;

    @Column(name = "temperature_c")
    private Double temperatureC = 36.6;

    private Double bmi;

    @Column(name = "status_summary", length = 100)
    private String statusSummary = "STABLE";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public HealthRecord() {
    }

    @PrePersist
    @PreUpdate
    public void calculateBmiAndStatus() {
        if (heightCm != null && heightCm > 0 && weightKg != null && weightKg > 0) {
            double hM = heightCm / 100.0;
            this.bmi = Math.round((weightKg / (hM * hM)) * 10.0) / 10.0;
        }
        if (systolicBp != null && systolicBp >= 140) {
            this.statusSummary = "ELEVATED_BP";
        } else if (bloodSugarMgDl != null && bloodSugarMgDl >= 126) {
            this.statusSummary = "ELEVATED_GLUCOSE";
        } else {
            this.statusSummary = "NORMAL";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Double getBloodSugarMgDl() {
        return bloodSugarMgDl;
    }

    public void setBloodSugarMgDl(Double bloodSugarMgDl) {
        this.bloodSugarMgDl = bloodSugarMgDl;
    }

    public Integer getHeartRateBpm() {
        return heartRateBpm;
    }

    public void setHeartRateBpm(Integer heartRateBpm) {
        this.heartRateBpm = heartRateBpm;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public String getStatusSummary() {
        return statusSummary;
    }

    public void setStatusSummary(String statusSummary) {
        this.statusSummary = statusSummary;
    }

    public String getBmiCategory() {
        if (bmi == null) return "N/A";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
