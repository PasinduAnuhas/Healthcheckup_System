package com.nutrihealth.service;

import com.nutrihealth.entity.Appointment;
import com.nutrihealth.entity.User;
import com.nutrihealth.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> getAppointmentsByPatient(User patient) {
        return appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient);
    }

    public List<Appointment> getAppointmentsByDoctor(User doctor) {
        return appointmentRepository.findByDoctorOrderByAppointmentDateDesc(doctor);
    }

    public List<Appointment> getAppointmentsByNutritionist(User nutritionist) {
        return appointmentRepository.findByNutritionistOrderByAppointmentDateDesc(nutritionist);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentDateDesc();
    }

    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository.findByAppointmentDateGreaterThanEqualOrderByAppointmentDateAsc(LocalDate.now());
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Long id, User owner, LocalDate date, String time, String type, String reason, String notes) {
        if (owner == null || owner.getId() == null) {
            throw new SecurityException("Authenticated patient not found.");
        }
        Appointment apt = appointmentRepository.findByIdAndPatientId(id, owner.getId())
                .orElseThrow(() -> new SecurityException("Appointment not found or you are not authorized to edit it."));

        if (date == null || time == null || time.isBlank() || type == null || type.isBlank()
                || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Appointment date, time, type and reason are required.");
        }

        apt.setAppointmentDate(date);
        apt.setAppointmentTime(time.trim());
        apt.setType(type.trim());
        apt.setReason(reason.trim());
        apt.setNotes(notes == null ? null : notes.trim());
        return appointmentRepository.saveAndFlush(apt);
    }

    public void deleteAppointment(Long id, User owner) {
        if (owner == null || owner.getId() == null) {
            throw new SecurityException("Authenticated patient not found.");
        }
        Appointment apt = appointmentRepository.findByIdAndPatientId(id, owner.getId())
                .orElseThrow(() -> new SecurityException("Appointment not found or you are not authorized to delete it."));
        appointmentRepository.delete(apt);
        appointmentRepository.flush();
    }

    public Appointment updateAppointmentByAdmin(Long id, LocalDate date, String time, String type, String reason, String notes, String status) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        apt.setAppointmentDate(date);
        apt.setAppointmentTime(time);
        apt.setType(type);
        apt.setReason(reason);
        apt.setNotes(notes);
        apt.setStatus(status);
        return appointmentRepository.save(apt);
    }

    public void deleteAppointmentByAdmin(Long id) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appointmentRepository.delete(apt);
    }

    public Appointment updateStatus(Long appointmentId, User doctor, String newStatus) {
        Appointment apt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (apt.getDoctor() == null || !apt.getDoctor().getId().equals(doctor.getId())) {
            throw new SecurityException("You are not authorized to update this appointment.");
        }
        apt.setStatus(newStatus);
        return appointmentRepository.save(apt);
    }

    public Appointment updateStatus(Long appointmentId, String newStatus) {
        Appointment apt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        apt.setStatus(newStatus);
        return appointmentRepository.save(apt);
    }

    public void cancelAppointment(Long appointmentId, User owner) {
        if (owner == null || owner.getId() == null) {
            throw new SecurityException("Authenticated patient not found.");
        }
        Appointment apt = appointmentRepository.findByIdAndPatientId(appointmentId, owner.getId())
                .orElseThrow(() -> new SecurityException("Appointment not found or you are not authorized to cancel it."));
        apt.setStatus("CANCELLED");
        appointmentRepository.saveAndFlush(apt);
    }

    /** Backwards-compatible internal status update used by staff workflows. */
    public void cancelAppointment(Long appointmentId) {
        updateStatus(appointmentId, "CANCELLED");
    }

    public long countByStatus(String status) {
        return appointmentRepository.countByStatus(status);
    }

    public long countTotal() {
        return appointmentRepository.count();
    }
}
