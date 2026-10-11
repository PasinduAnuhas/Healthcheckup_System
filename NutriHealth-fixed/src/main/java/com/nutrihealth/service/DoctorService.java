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
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthAssessmentRepository healthAssessmentRepository;

    @Autowired
    public DoctorService(DoctorProfileRepository doctorProfileRepository,
                         PatientProfileRepository patientProfileRepository,
                         HealthRecordRepository healthRecordRepository,
                         HealthAssessmentRepository healthAssessmentRepository) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.healthAssessmentRepository = healthAssessmentRepository;
    }

    private void validateAssignedPatient(User doctor, User patient) {
        if (doctor == null || doctor.getId() == null) {
            throw new SecurityException("Doctor account not found.");
        }
        if (patient == null || patient.getId() == null) {
            throw new IllegalArgumentException("Patient not found.");
        }

        PatientProfile patientProfile = patientProfileRepository.findByUser(patient)
                .orElseThrow(() -> new SecurityException("Patient record not found."));

        if (patientProfile.getAssignedDoctor() == null
                || !patientProfile.getAssignedDoctor().getId().equals(doctor.getId())) {
            throw new SecurityException("You are not authorized to manage this patient's chart.");
        }
    }

    private void validateHealthRecordOwnership(User doctor, HealthRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Health record not found");
        }
        validateAssignedPatient(doctor, record.getPatient());
        if (record.getDoctor() != null && !record.getDoctor().getId().equals(doctor.getId())) {
            throw new SecurityException("You are not authorized to edit this health record.");
        }
    }

    private void validateHealthAssessmentOwnership(User doctor, HealthAssessment assessment) {
        if (assessment == null) {
            throw new IllegalArgumentException("Health assessment not found");
        }
        validateAssignedPatient(doctor, assessment.getPatient());
        if (assessment.getDoctor() != null && !assessment.getDoctor().getId().equals(doctor.getId())) {
            throw new SecurityException("You are not authorized to edit this assessment.");
        }
    }

    public Optional<DoctorProfile> getProfileByUser(User user) {
        return doctorProfileRepository.findByUser(user);
    }

    public DoctorProfile saveProfile(DoctorProfile profile) {
        return doctorProfileRepository.save(profile);
    }

    public List<PatientProfile> getAssignedPatients(User doctor) {
        return patientProfileRepository.findByAssignedDoctor(doctor);
    }

    public List<HealthRecord> getPatientRecords(Long patientId) {
        return healthRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId);
    }

    public HealthRecord createHealthRecord(HealthRecord record) {
        if (record == null || record.getPatient() == null || record.getDoctor() == null) {
            throw new IllegalArgumentException("Health record requires a patient and doctor.");
        }
        validateAssignedPatient(record.getDoctor(), record.getPatient());
        return healthRecordRepository.save(record);
    }

    public HealthRecord updateHealthRecord(Long id, User doctor, Double weightKg, Double heightCm,
                                           Integer systolicBp, Integer diastolicBp, Double bloodSugarMgDl,
                                           Integer heartRateBpm, Double temperatureC, String notes) {
        HealthRecord record = healthRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Health record not found"));
        validateHealthRecordOwnership(doctor, record);
        record.setWeightKg(weightKg);
        record.setHeightCm(heightCm);
        record.setSystolicBp(systolicBp);
        record.setDiastolicBp(diastolicBp);
        record.setBloodSugarMgDl(bloodSugarMgDl);
        record.setHeartRateBpm(heartRateBpm);
        record.setTemperatureC(temperatureC);
        record.setNotes(notes);
        return healthRecordRepository.save(record);
    }

    public void deleteHealthRecord(Long id, User doctor) {
        HealthRecord record = healthRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Health record not found"));
        validateHealthRecordOwnership(doctor, record);
        healthRecordRepository.delete(record);
    }

    public HealthAssessment createHealthAssessment(HealthAssessment assessment) {
        if (assessment == null || assessment.getPatient() == null || assessment.getDoctor() == null) {
            throw new IllegalArgumentException("Health assessment requires a patient and doctor.");
        }
        validateAssignedPatient(assessment.getDoctor(), assessment.getPatient());
        return healthAssessmentRepository.save(assessment);
    }

    public HealthAssessment updateHealthAssessment(Long id, User doctor, String symptoms, String diagnosis,
                                                   String clinicalRecommendations, String medications,
                                                   LocalDate nextCheckupDate, String status) {
        HealthAssessment assessment = healthAssessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Health assessment not found"));
        validateHealthAssessmentOwnership(doctor, assessment);
        assessment.setSymptoms(symptoms);
        assessment.setDiagnosis(diagnosis);
        assessment.setClinicalRecommendations(clinicalRecommendations);
        assessment.setMedications(medications);
        assessment.setNextCheckupDate(nextCheckupDate);
        assessment.setStatus(status);
        return healthAssessmentRepository.save(assessment);
    }

    public void deleteHealthAssessment(Long id, User doctor) {
        HealthAssessment assessment = healthAssessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Health assessment not found"));
        validateHealthAssessmentOwnership(doctor, assessment);
        healthAssessmentRepository.delete(assessment);
    }

    public List<HealthAssessment> getAssessmentsByDoctor(User doctor) {
        return healthAssessmentRepository.findByDoctorOrderByAssessmentDateDesc(doctor);
    }

    public List<HealthAssessment> getPatientAssessments(Long patientId) {
        return healthAssessmentRepository.findByPatientIdOrderByAssessmentDateDesc(patientId);
    }

    public long countAssessmentsByDoctor(User doctor) {
        return healthAssessmentRepository.countByDoctor(doctor);
    }
}
