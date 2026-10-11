package com.nutrihealth.repository;

import com.nutrihealth.entity.PatientProfile;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    Optional<PatientProfile> findByUser(User user);
    Optional<PatientProfile> findByUserId(Long userId);
    List<PatientProfile> findByAssignedDoctor(User doctor);
    List<PatientProfile> findByAssignedNutritionist(User nutritionist);
}
