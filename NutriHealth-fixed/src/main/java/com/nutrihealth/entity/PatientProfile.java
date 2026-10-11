package com.nutrihealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "patient_profiles")
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "height_cm")
    private Double heightCm = 175.0;

    @Column(name = "current_weight_kg")
    private Double currentWeightKg = 72.5;

    @Column(name = "target_weight_kg")
    private Double targetWeightKg = 68.0;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(name = "activity_level", length = 50)
    private String activityLevel = "MODERATE";

    @Column(name = "daily_calorie_target")
    private Integer dailyCalorieTarget = 2100;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_doctor_id")
    private User assignedDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_nutritionist_id")
    private User assignedNutritionist;

    public PatientProfile() {
    }

    public PatientProfile(User user) {
        this.user = user;
    }

    public Double calculateBmi() {
        if (heightCm != null && heightCm > 0 && currentWeightKg != null && currentWeightKg > 0) {
            double hMeter = heightCm / 100.0;
            return Math.round((currentWeightKg / (hMeter * hMeter)) * 10.0) / 10.0;
        }
        return 0.0;
    }

    /**
     * Thymeleaf/SpringEL-friendly BMI property.
     * This exposes calculateBmi() as the standard JavaBean property `bmi`,
     * which is used by the patient dashboard.
     */
    @Transient
    public Double getBmi() {
        return calculateBmi();
    }

    /**
     * Thymeleaf/SpringEL-friendly BMI category property.
     */
    @Transient
    public String getBmiCategory() {
        Double bmi = getBmi();
        if (bmi == null || bmi <= 0) return "N/A";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    // Getters and Setters
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

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public Double getCurrentWeightKg() {
        return currentWeightKg;
    }

    public void setCurrentWeightKg(Double currentWeightKg) {
        this.currentWeightKg = currentWeightKg;
    }

    public Double getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(Double targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public Integer getDailyCalorieTarget() {
        return dailyCalorieTarget;
    }

    public void setDailyCalorieTarget(Integer dailyCalorieTarget) {
        this.dailyCalorieTarget = dailyCalorieTarget;
    }

    public User getAssignedDoctor() {
        return assignedDoctor;
    }

    public void setAssignedDoctor(User assignedDoctor) {
        this.assignedDoctor = assignedDoctor;
    }

    public User getAssignedNutritionist() {
        return assignedNutritionist;
    }

    public void setAssignedNutritionist(User assignedNutritionist) {
        this.assignedNutritionist = assignedNutritionist;
    }
}
