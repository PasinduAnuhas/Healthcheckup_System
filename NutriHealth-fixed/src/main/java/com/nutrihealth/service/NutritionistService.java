package com.nutrihealth.service;

import com.nutrihealth.entity.*;
import com.nutrihealth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NutritionistService {

    private final NutritionistProfileRepository nutritionistProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DietPlanRepository dietPlanRepository;
    private final MealRecommendationRepository mealRecommendationRepository;
    private final FoodRepository foodRepository;
    private final FoodIntakeRepository foodIntakeRepository;

    @Autowired
    public NutritionistService(NutritionistProfileRepository nutritionistProfileRepository,
                               PatientProfileRepository patientProfileRepository,
                               DietPlanRepository dietPlanRepository,
                               MealRecommendationRepository mealRecommendationRepository,
                               FoodRepository foodRepository,
                               FoodIntakeRepository foodIntakeRepository) {
        this.nutritionistProfileRepository = nutritionistProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.dietPlanRepository = dietPlanRepository;
        this.mealRecommendationRepository = mealRecommendationRepository;
        this.foodRepository = foodRepository;
        this.foodIntakeRepository = foodIntakeRepository;
    }

    public Optional<NutritionistProfile> getProfileByUser(User user) {
        return nutritionistProfileRepository.findByUser(user);
    }

    public NutritionistProfile saveProfile(NutritionistProfile profile) {
        return nutritionistProfileRepository.save(profile);
    }

    public List<PatientProfile> getAssignedPatients(User nutritionist) {
        return patientProfileRepository.findByAssignedNutritionist(nutritionist);
    }

    public List<DietPlan> getDietPlansByNutritionist(User nutritionist) {
        return dietPlanRepository.findByNutritionistOrderByStartDateDesc(nutritionist);
    }

    public Optional<DietPlan> getDietPlanById(Long id) {
        return dietPlanRepository.findById(id);
    }

    public DietPlan saveDietPlan(DietPlan dietPlan) {
        return dietPlanRepository.save(dietPlan);
    }

    public DietPlan updateDietPlan(Long id, User nutritionist, String title, String objective,
                                   Integer dailyCalorieTarget, java.time.LocalDate startDate,
                                   java.time.LocalDate endDate, String status, String recommendedFoods,
                                   String restrictedFoods, String nutritionistNotes) {
        DietPlan plan = dietPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diet plan not found"));
        if (plan.getNutritionist() == null || !plan.getNutritionist().getId().equals(nutritionist.getId())) {
            throw new SecurityException("You are not authorized to edit this diet plan.");
        }
        plan.setTitle(title);
        plan.setObjective(objective);
        plan.setDailyCalorieTarget(dailyCalorieTarget);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setStatus(status);
        plan.setRecommendedFoods(recommendedFoods);
        plan.setRestrictedFoods(restrictedFoods);
        plan.setNutritionistNotes(nutritionistNotes);
        return dietPlanRepository.save(plan);
    }

    public void deleteDietPlan(Long id, User nutritionist) {
        DietPlan plan = dietPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diet plan not found"));
        if (plan.getNutritionist() == null || !plan.getNutritionist().getId().equals(nutritionist.getId())) {
            throw new SecurityException("You are not authorized to delete this diet plan.");
        }
        dietPlanRepository.delete(plan);
    }

    public MealRecommendation saveMealRecommendation(MealRecommendation recommendation) {
        if (recommendation == null || recommendation.getPatient() == null || recommendation.getNutritionist() == null) {
            throw new IllegalArgumentException("Patient and nutritionist are required for a meal recommendation.");
        }
        if (recommendation.getTitle() == null || recommendation.getTitle().isBlank()) {
            throw new IllegalArgumentException("Meal recommendation title is required.");
        }
        return mealRecommendationRepository.saveAndFlush(recommendation);
    }

    /**
     * Update only a recommendation owned by the currently authenticated nutritionist.
     * The repository lookup includes the owner, which avoids failures caused by
     * detached/stale User entity instances and keeps the ownership rule explicit.
     */
    public MealRecommendation updateMealRecommendation(Long id, User nutritionist, String mealType,
                                                        String title, String description, Integer calorieEstimate,
                                                        String nutritionBenefits, boolean restrictionFriendly) {
        if (nutritionist == null || nutritionist.getId() == null) {
            throw new SecurityException("Authenticated nutritionist is required.");
        }

        MealRecommendation rec = mealRecommendationRepository
                .findByIdAndNutritionistId(id, nutritionist.getId())
                .orElseThrow(() -> new SecurityException(
                        "This recommendation does not belong to the current nutritionist."));

        if (mealType == null || mealType.isBlank() || title == null || title.isBlank()) {
            throw new IllegalArgumentException("Meal type and title are required.");
        }
        if (calorieEstimate == null || calorieEstimate < 0) {
            throw new IllegalArgumentException("Calorie estimate must be a valid non-negative number.");
        }

        rec.setMealType(mealType.trim().toUpperCase());
        rec.setTitle(title.trim());
        rec.setDescription(description == null ? "" : description.trim());
        rec.setCalorieEstimate(calorieEstimate);
        rec.setNutritionBenefits(nutritionBenefits == null ? "" : nutritionBenefits.trim());
        rec.setDietaryRestrictionFriendly(restrictionFriendly);

        return mealRecommendationRepository.saveAndFlush(rec);
    }

    /** Delete only a recommendation owned by the current nutritionist. */
    public void deleteMealRecommendation(Long id, User nutritionist) {
        if (nutritionist == null || nutritionist.getId() == null) {
            throw new SecurityException("Authenticated nutritionist is required.");
        }

        MealRecommendation rec = mealRecommendationRepository
                .findByIdAndNutritionistId(id, nutritionist.getId())
                .orElseThrow(() -> new SecurityException(
                        "This recommendation does not belong to the current nutritionist."));

        mealRecommendationRepository.delete(rec);
        mealRecommendationRepository.flush();
    }

    public List<MealRecommendation> getRecommendationsByNutritionist(User nutritionist) {
        return mealRecommendationRepository.findByNutritionistOrderByCreatedAtDesc(nutritionist);
    }

    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    public Food saveFood(Food food) {
        return foodRepository.save(food);
    }

    public Food updateFood(Long id, String name, String category, String servingSize, Integer calories,
                           Double protein, Double carbs, Double fat, Double fiber, String description,
                           boolean healthy, String allergens) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food item not found"));
        food.setName(name);
        food.setCategory(category);
        food.setServingSize(servingSize);
        food.setCaloriesPer100g(calories);
        food.setProteinG(protein);
        food.setCarbsG(carbs);
        food.setFatG(fat);
        food.setFiberG(fiber);
        food.setDescription(description);
        food.setHealthy(healthy);
        food.setAllergens(allergens);
        return foodRepository.save(food);
    }

    public void deleteFood(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food item not found"));
        foodRepository.delete(food);
    }

    public List<FoodIntake> getPatientFoodIntakes(Long patientId) {
        return foodIntakeRepository.findByPatientIdOrderByLogDateDesc(patientId);
    }

    public long countActiveDietPlans() {
        return dietPlanRepository.countByStatus("ACTIVE");
    }

    public long countPlansByNutritionist(User nutritionist) {
        return dietPlanRepository.countByNutritionist(nutritionist);
    }
}
