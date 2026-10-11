package com.nutrihealth.service;

import com.nutrihealth.entity.*;
import com.nutrihealth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PatientService {

    private final PatientProfileRepository patientProfileRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final FoodIntakeRepository foodIntakeRepository;
    private final DietPlanRepository dietPlanRepository;
    private final DietPlanMealRepository dietPlanMealRepository;
    private final MealRecommendationRepository mealRecommendationRepository;

    @Autowired
    public PatientService(PatientProfileRepository patientProfileRepository,
                          HealthRecordRepository healthRecordRepository,
                          FoodIntakeRepository foodIntakeRepository,
                          DietPlanRepository dietPlanRepository,
                          DietPlanMealRepository dietPlanMealRepository,
                          MealRecommendationRepository mealRecommendationRepository) {
        this.patientProfileRepository = patientProfileRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.foodIntakeRepository = foodIntakeRepository;
        this.dietPlanRepository = dietPlanRepository;
        this.dietPlanMealRepository = dietPlanMealRepository;
        this.mealRecommendationRepository = mealRecommendationRepository;
    }

    public Optional<PatientProfile> getProfileByUser(User user) {
        return patientProfileRepository.findByUser(user);
    }

    public Optional<PatientProfile> getProfileByUserId(Long userId) {
        return patientProfileRepository.findByUserId(userId);
    }

    public PatientProfile saveProfile(PatientProfile profile) {
        return patientProfileRepository.save(profile);
    }

    public List<HealthRecord> getHealthRecords(User patient) {
        return healthRecordRepository.findByPatientOrderByRecordDateDesc(patient);
    }

    public Optional<HealthRecord> getLatestHealthRecord(User patient) {
        return healthRecordRepository.findFirstByPatientOrderByRecordDateDesc(patient);
    }

    public HealthRecord addHealthRecord(HealthRecord record) {
        return healthRecordRepository.save(record);
    }

    public List<FoodIntake> getTodayFoodIntakes(User patient) {
        return getFoodIntakesByDate(patient, LocalDate.now());
    }

    public List<FoodIntake> getFoodIntakesByDate(User patient, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return foodIntakeRepository.findByPatientAndLogDateOrderByCreatedAtAsc(patient, date);
    }

    public Integer getTodayTotalCalories(User patient) {
        return getTotalCaloriesByDate(patient, LocalDate.now());
    }

    public Integer getTotalCaloriesByDate(User patient, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        Integer calories = foodIntakeRepository.getTotalCaloriesByPatientAndDate(patient, date);
        return calories != null ? calories : 0;
    }

    public FoodIntake logFoodIntake(FoodIntake foodIntake) {
        return foodIntakeRepository.saveAndFlush(foodIntake);
    }

    public FoodIntake updateFoodIntake(Long id, User patient, String mealType, String foodItem, String quantity,
                                        Integer calories, Double proteinG, Double carbsG, Double fatG, String notes) {
        FoodIntake intake = foodIntakeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food intake log not found"));
        if (!intake.getPatient().getId().equals(patient.getId())) {
            throw new SecurityException("Unauthorized to edit this record.");
        }
        intake.setMealType(mealType);
        intake.setFoodItem(foodItem);
        intake.setQuantity(quantity);
        intake.setCalories(calories);
        intake.setProteinG(proteinG);
        intake.setCarbsG(carbsG);
        intake.setFatG(fatG);
        intake.setNotes(notes);
        return foodIntakeRepository.saveAndFlush(intake);
    }

    public FoodIntake getFoodIntakeForPatient(Long id, User patient) {
        if (patient == null || patient.getId() == null) {
            throw new SecurityException("Authenticated patient is required.");
        }
        return foodIntakeRepository.findByIdAndPatientId(id, patient.getId())
                .orElseThrow(() -> new IllegalArgumentException("Food intake log not found or access denied."));
    }

    public void deleteFoodIntake(Long id, User patient) {
        if (id == null || patient == null || patient.getId() == null) {
            throw new SecurityException("Authenticated patient is required.");
        }

        // Load the record through the patient-scoped query first. This both
        // verifies ownership and avoids relying on a bulk JPQL DELETE, which
        // can behave differently across database providers.
        FoodIntake intake = foodIntakeRepository.findByIdAndPatientId(id, patient.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Food intake log not found or access denied."));

        foodIntakeRepository.delete(intake);
        foodIntakeRepository.flush();
    }

    public Optional<DietPlan> getActiveDietPlan(User patient) {
        LocalDate today = LocalDate.now();
        // Prefer a plan whose configured validity period actually includes today.
        // Fall back to the latest ACTIVE plan so older seed data still remains visible.
        return dietPlanRepository
                .findFirstByPatientAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(
                        patient, "ACTIVE", today, today)
                .or(() -> dietPlanRepository.findFirstByPatientAndStatusOrderByStartDateDesc(patient, "ACTIVE"));
    }

    public List<DietPlanMeal> getDietPlanMeals(DietPlan plan) {
        if (plan == null) return List.of();
        return dietPlanMealRepository.findByDietPlanOrderByIdAsc(plan);
    }

    public List<MealRecommendation> getMealRecommendations(User patient) {
        return mealRecommendationRepository.findByPatientOrderByCreatedAtDesc(patient);
    }

    /**
     * Harris-Benedict BMR and TDEE calorie estimate formula
     */
    public int calculateEstimatedDailyCalories(int age, String gender, double heightCm, double weightKg, String activityLevel) {
        double bmr;
        if ("FEMALE".equalsIgnoreCase(gender)) {
            bmr = 655.1 + (9.563 * weightKg) + (1.850 * heightCm) - (4.676 * age);
        } else {
            bmr = 66.5 + (13.75 * weightKg) + (5.003 * heightCm) - (6.75 * age);
        }

        double multiplier = switch (activityLevel != null ? activityLevel.toUpperCase() : "SEDENTARY") {
            case "LIGHT" -> 1.375;
            case "MODERATE" -> 1.55;
            case "VERY_ACTIVE" -> 1.725;
            case "EXTRA_ACTIVE" -> 1.9;
            default -> 1.2;
        };

        return (int) Math.round(bmr * multiplier);
    }
}
