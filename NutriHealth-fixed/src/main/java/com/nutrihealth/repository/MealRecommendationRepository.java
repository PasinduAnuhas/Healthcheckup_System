package com.nutrihealth.repository;

import com.nutrihealth.entity.MealRecommendation;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealRecommendationRepository extends JpaRepository<MealRecommendation, Long> {
    List<MealRecommendation> findByPatientOrderByCreatedAtDesc(User patient);
    List<MealRecommendation> findByNutritionistOrderByCreatedAtDesc(User nutritionist);
    List<MealRecommendation> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    Optional<MealRecommendation> findByIdAndNutritionistId(Long id, Long nutritionistId);
}
