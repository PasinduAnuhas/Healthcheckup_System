package com.nutrihealth.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_assessments")
public class HealthAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(length = 255)
    private String diagnosis;

    @Column(name = "clinical_recommendations", columnDefinition = "TEXT")
    private String clinicalRecommendations;

    @Column(columnDefinition = "TEXT")
    private String medications;

    @Column(name = "next_checkup_date")
    private LocalDate nextCheckupDate;

    @Column(length = 50)
    private String status = "COMPLETED";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public HealthAssessment() {
    }

    public HealthAssessment(User patient, User doctor, String diagnosis, String clinicalRecommendations) {
        this.patient = patient;
        this.doctor = doctor;
        this.diagnosis = diagnosis;
        this.clinicalRecommendations = clinicalRecommendations;
        this.assessmentDate = LocalDate.now();
        this.nextCheckupDate = LocalDate.now().plusMonths(1);
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

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getClinicalRecommendations() {
        return clinicalRecommendations;
    }

    public void setClinicalRecommendations(String clinicalRecommendations) {
        this.clinicalRecommendations = clinicalRecommendations;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public LocalDate getNextCheckupDate() {
        return nextCheckupDate;
    }

    public void setNextCheckupDate(LocalDate nextCheckupDate) {
        this.nextCheckupDate = nextCheckupDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
