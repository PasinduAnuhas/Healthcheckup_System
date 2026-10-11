package com.nutrihealth.repository;

import com.nutrihealth.entity.HealthAssessment;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment, Long> {
    List<HealthAssessment> findByPatientOrderByAssessmentDateDesc(User patient);
    List<HealthAssessment> findByDoctorOrderByAssessmentDateDesc(User doctor);
    List<HealthAssessment> findByPatientIdOrderByAssessmentDateDesc(Long patientId);
    long countByDoctor(User doctor);
}
