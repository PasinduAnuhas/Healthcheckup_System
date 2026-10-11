package com.nutrihealth.repository;

import com.nutrihealth.entity.DietPlan;
import com.nutrihealth.entity.DietPlanMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DietPlanMealRepository extends JpaRepository<DietPlanMeal, Long> {
    List<DietPlanMeal> findByDietPlanOrderByIdAsc(DietPlan dietPlan);
    List<DietPlanMeal> findByDietPlanAndMealType(DietPlan dietPlan, String mealType);
}
