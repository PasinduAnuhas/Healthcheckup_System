package com.nutrihealth.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diet_plans")
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private User nutritionist;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 255)
    private String objective;

    @Column(name = "daily_calorie_target", nullable = false)
    private Integer dailyCalorieTarget;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 50)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, SUSPENDED

    @Column(name = "recommended_foods", columnDefinition = "TEXT")
    private String recommendedFoods;

    @Column(name = "restricted_foods", columnDefinition = "TEXT")
    private String restrictedFoods;

    @Column(name = "nutritionist_notes", columnDefinition = "TEXT")
    private String nutritionistNotes;

    @OneToMany(mappedBy = "dietPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DietPlanMeal> meals = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DietPlan() {
    }

    public void addMeal(DietPlanMeal meal) {
        meals.add(meal);
        meal.setDietPlan(this);
    }

    public void removeMeal(DietPlanMeal meal) {
        meals.remove(meal);
        meal.setDietPlan(null);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public Integer getDailyCalorieTarget() {
        return dailyCalorieTarget;
    }

    public void setDailyCalorieTarget(Integer dailyCalorieTarget) {
        this.dailyCalorieTarget = dailyCalorieTarget;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecommendedFoods() {
        return recommendedFoods;
    }

    public void setRecommendedFoods(String recommendedFoods) {
        this.recommendedFoods = recommendedFoods;
    }

    public String getRestrictedFoods() {
        return restrictedFoods;
    }

    public void setRestrictedFoods(String restrictedFoods) {
        this.restrictedFoods = restrictedFoods;
    }

    public String getNutritionistNotes() {
        return nutritionistNotes;
    }

    public void setNutritionistNotes(String nutritionistNotes) {
        this.nutritionistNotes = nutritionistNotes;
    }

    public List<DietPlanMeal> getMeals() {
        return meals;
    }

    public void setMeals(List<DietPlanMeal> meals) {
        this.meals = meals;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
