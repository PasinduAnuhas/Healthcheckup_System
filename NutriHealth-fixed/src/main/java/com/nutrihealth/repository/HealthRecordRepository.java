package com.nutrihealth.repository;

import com.nutrihealth.entity.HealthRecord;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPatientOrderByRecordDateDesc(User patient);
    List<HealthRecord> findByPatientIdOrderByRecordDateDesc(Long patientId);
    Optional<HealthRecord> findFirstByPatientOrderByRecordDateDesc(User patient);
    long countByDoctor(User doctor);
}
