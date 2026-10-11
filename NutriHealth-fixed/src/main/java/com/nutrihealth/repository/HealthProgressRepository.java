package com.nutrihealth.repository;

import com.nutrihealth.entity.HealthProgress;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthProgressRepository extends JpaRepository<HealthProgress, Long> {
    List<HealthProgress> findByPatientOrderByProgressDateDesc(User patient);
    List<HealthProgress> findByPatientIdOrderByProgressDateDesc(Long patientId);
}

//it seperate database operations from business logic