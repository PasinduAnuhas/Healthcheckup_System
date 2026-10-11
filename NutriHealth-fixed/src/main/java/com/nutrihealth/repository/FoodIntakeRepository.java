package com.nutrihealth.repository;

import com.nutrihealth.entity.FoodIntake;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodIntakeRepository extends JpaRepository<FoodIntake, Long> {
    List<FoodIntake> findByPatientAndLogDateOrderByCreatedAtAsc(User patient, LocalDate logDate);
    List<FoodIntake> findByPatientAndLogDateOrderByCreatedAtDesc(User patient, LocalDate logDate);
    List<FoodIntake> findByPatientOrderByLogDateDesc(User patient);
    List<FoodIntake> findByPatientIdOrderByLogDateDesc(Long patientId);

    Optional<FoodIntake> findByIdAndPatientId(Long id, Long patientId);

    @Query("SELECT COALESCE(SUM(f.calories), 0) FROM FoodIntake f WHERE f.patient = :patient AND f.logDate = :date")
    Integer getTotalCaloriesByPatientAndDate(@Param("patient") User patient, @Param("date") LocalDate date);
}
