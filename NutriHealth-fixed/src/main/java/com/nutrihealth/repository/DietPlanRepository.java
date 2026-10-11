package com.nutrihealth.repository;

import com.nutrihealth.entity.DietPlan;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    /**
     * Load a complete diet plan for report rendering. The report template
     * iterates over meals, so the collection must be fetched with the plan
     * instead of relying on a lazy collection after the repository call.
     */
    @EntityGraph(attributePaths = {"patient", "nutritionist", "meals"})
    Optional<DietPlan> findById(Long id);
    List<DietPlan> findByPatientOrderByStartDateDesc(User patient);
    List<DietPlan> findByPatientIdOrderByStartDateDesc(Long patientId);
    List<DietPlan> findByNutritionistOrderByStartDateDesc(User nutritionist);
    @EntityGraph(attributePaths = {"meals", "nutritionist"})
    Optional<DietPlan> findFirstByPatientAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(
            User patient, String status, LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"meals", "nutritionist"})
    Optional<DietPlan> findFirstByPatientAndStatusOrderByStartDateDesc(User patient, String status);
    long countByStatus(String status);
    long countByNutritionist(User nutritionist);
}
