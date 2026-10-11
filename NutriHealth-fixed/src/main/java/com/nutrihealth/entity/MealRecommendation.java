package com.nutrihealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_recommendations")
public class MealRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private User nutritionist;

    @Column(name = "meal_type", nullable = false, length = 50)
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "calorie_estimate")
    private Integer calorieEstimate;

    @Column(name = "nutrition_benefits", columnDefinition = "TEXT")
    private String nutritionBenefits;

    @Column(name = "dietary_restriction_friendly")
    private boolean dietaryRestrictionFriendly = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MealRecommendation() {
    }

    public MealRecommendation(User patient, User nutritionist, String mealType, String title, String description, int calories) {
        this.patient = patient;
        this.nutritionist = nutritionist;
        this.mealType = mealType;
        this.title = title;
        this.description = description;
        this.calorieEstimate = calories;
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

    public User getNutritionist() {
        return nutritionist;
    }

    public void setNutritionist(User nutritionist) {
        this.nutritionist = nutritionist;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCalorieEstimate() {
        return calorieEstimate;
    }

    public void setCalorieEstimate(Integer calorieEstimate) {
        this.calorieEstimate = calorieEstimate;
    }

    public String getNutritionBenefits() {
        return nutritionBenefits;
    }

    public void setNutritionBenefits(String nutritionBenefits) {
        this.nutritionBenefits = nutritionBenefits;
    }

    public boolean isDietaryRestrictionFriendly() {
        return dietaryRestrictionFriendly;
    }

    public void setDietaryRestrictionFriendly(boolean dietaryRestrictionFriendly) {
        this.dietaryRestrictionFriendly = dietaryRestrictionFriendly;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
