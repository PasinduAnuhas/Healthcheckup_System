package com.nutrihealth.service;

import com.nutrihealth.entity.HealthProgress;
import com.nutrihealth.entity.User;
import com.nutrihealth.repository.HealthProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class HealthProgressService {
    private final HealthProgressRepository repository;

    public HealthProgressService(HealthProgressRepository repository) {
        this.repository = repository;
    }

    public List<HealthProgress> getByPatient(User patient) {
        return repository.findByPatientOrderByProgressDateDesc(patient);
    }

    public List<HealthProgress> getByPatientId(Long patientId) {
        return repository.findByPatientIdOrderByProgressDateDesc(patientId);
    }

    public HealthProgress create(HealthProgress progress) {
        return repository.save(progress);
    }

    public HealthProgress update(Long id, User patient, LocalDate date, Double weight, Double height,
                                 Integer systolic, Integer diastolic, Double sugar, Integer heartRate, String notes) {
        HealthProgress p = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Progress entry not found"));
        if (p.getPatient() == null || !p.getPatient().getId().equals(patient.getId())) {
            throw new SecurityException("You are not authorized to edit this progress entry.");
        }
        p.setProgressDate(date);
        p.setWeightKg(weight);
        p.setHeightCm(height);
        p.setSystolicBp(systolic);
        p.setDiastolicBp(diastolic);
        p.setBloodSugarMgDl(sugar);
        p.setHeartRateBpm(heartRate);
        p.setNotes(notes);
        return repository.saveAndFlush(p);
    }

    public void delete(Long id, User patient) {
        HealthProgress p = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Progress entry not found"));
        if (p.getPatient() == null || !p.getPatient().getId().equals(patient.getId())) {
            throw new SecurityException("You are not authorized to delete this progress entry.");
        }
        repository.delete(p);
        repository.flush();
    }
}
