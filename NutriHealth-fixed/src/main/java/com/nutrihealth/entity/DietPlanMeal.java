package com.nutrihealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "diet_plan_meals")
public class DietPlanMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_plan_id", nullable = false)
    private DietPlan dietPlan;

    @Column(name = "meal_type", nullable = false, length = 50)
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK

    @Column(name = "food_name", nullable = false, length = 150)
    private String foodName;

    @Column(name = "portion_size", length = 100)
    private String portionSize;

    @Column(nullable = false)
    private Integer calories;

    @Column(name = "protein_g")
    private Double proteinG = 0.0;

    @Column(name = "carbs_g")
    private Double carbsG = 0.0;

    @Column(name = "fats_g")
    private Double fatsG = 0.0;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    public DietPlanMeal() {
    }

    public DietPlanMeal(String mealType, String foodName, String portionSize, int calories, double proteinG, double carbsG, double fatsG, String instructions) {
        this.mealType = mealType;
        this.foodName = foodName;
        this.portionSize = portionSize;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatsG = fatsG;
        this.instructions = instructions;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DietPlan getDietPlan() {
        return dietPlan;
    }

    public void setDietPlan(DietPlan dietPlan) {
        this.dietPlan = dietPlan;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(String portionSize) {
        this.portionSize = portionSize;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Double getProteinG() {
        return proteinG;
    }

    public void setProteinG(Double proteinG) {
        this.proteinG = proteinG;
    }

    public Double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(Double carbsG) {
        this.carbsG = carbsG;
    }

    public Double getFatsG() {
        return fatsG;
    }

    public void setFatsG(Double fatsG) {
        this.fatsG = fatsG;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
