package com.nutrihealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String category; // GRAINS, PROTEIN, VEGETABLES, FRUITS, DAIRY, HEALTHY_FATS, BEVERAGES

    @Column(name = "serving_size", length = 100)
    private String servingSize = "100g";

    @Column(name = "calories_per_100g", nullable = false)
    private Integer caloriesPer100g;

    @Column(name = "protein_g")
    private Double proteinG = 0.0;

    @Column(name = "carbs_g")
    private Double carbsG = 0.0;

    @Column(name = "fat_g")
    private Double fatG = 0.0;

    @Column(name = "fiber_g")
    private Double fiberG = 0.0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_healthy")
    private boolean healthy = true;

    @Column(length = 255)
    private String allergens;

    public Food() {
    }

    public Food(String name, String category, int calories, double protein, double carbs, double fat, String description) {
        this.name = name;
        this.category = category;
        this.caloriesPer100g = calories;
        this.proteinG = protein;
        this.carbsG = carbs;
        this.fatG = fat;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public Integer getCaloriesPer100g() {
        return caloriesPer100g;
    }

    public void setCaloriesPer100g(Integer caloriesPer100g) {
        this.caloriesPer100g = caloriesPer100g;
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

    public Double getFatG() {
        return fatG;
    }

    public void setFatG(Double fatG) {
        this.fatG = fatG;
    }

    public Double getFiberG() {
        return fiberG;
    }

    public void setFiberG(Double fiberG) {
        this.fiberG = fiberG;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }
}
