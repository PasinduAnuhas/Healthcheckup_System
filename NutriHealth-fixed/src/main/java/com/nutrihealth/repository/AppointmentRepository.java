package com.nutrihealth.repository;

import com.nutrihealth.entity.Appointment;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientOrderByAppointmentDateDesc(User patient);
    java.util.Optional<Appointment> findByIdAndPatientId(Long id, Long patientId);
    List<Appointment> findByDoctorOrderByAppointmentDateDesc(User doctor);
    List<Appointment> findByNutritionistOrderByAppointmentDateDesc(User nutritionist);
    List<Appointment> findAllByOrderByAppointmentDateDesc();
    List<Appointment> findByStatus(String status);
    List<Appointment> findByAppointmentDateGreaterThanEqualOrderByAppointmentDateAsc(LocalDate date);
    long countByStatus(String status);
    long countByDoctor(User doctor);
    long countByNutritionist(User nutritionist);
}
