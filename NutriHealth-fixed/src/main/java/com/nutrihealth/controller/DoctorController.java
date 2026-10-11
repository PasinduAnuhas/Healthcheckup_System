package com.nutrihealth.controller;

import com.nutrihealth.entity.*;
import com.nutrihealth.service.*;
import com.nutrihealth.repository.PatientProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final UserService userService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MessageAndNotificationService messageService;
    private final AuditService auditService;
    private final PatientProfileRepository patientProfileRepository;

    @Autowired
    public DoctorController(UserService userService,
                            DoctorService doctorService,
                            PatientService patientService,
                            AppointmentService appointmentService,
                            MessageAndNotificationService messageService,
                            AuditService auditService,
                            PatientProfileRepository patientProfileRepository) {
        this.userService = userService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.messageService = messageService;
        this.auditService = auditService;
        this.patientProfileRepository = patientProfileRepository;
    }

    private User getAuthenticatedDoctor(UserDetails userDetails) {
        User doctor = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Doctor not found"));
        if (doctor.getRole() != UserRole.ROLE_DOCTOR) {
            throw new SecurityException("Access denied. Doctor role required.");
        }
        return doctor;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User doctor = getAuthenticatedDoctor(userDetails);
        DoctorProfile profile = doctorService.getProfileByUser(doctor).orElse(new DoctorProfile(doctor, "General", "LIC-000"));

        List<PatientProfile> assignedPatients = doctorService.getAssignedPatients(doctor);
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctor);
        List<HealthAssessment> assessments = doctorService.getAssessmentsByDoctor(doctor);

        model.addAttribute("doctor", doctor);
        model.addAttribute("profile", profile);
        model.addAttribute("assignedPatients", assignedPatients);
        model.addAttribute("appointments", appointments);
        model.addAttribute("assessments", assessments);
        model.addAttribute("totalPatients", assignedPatients.size());
        long pendingAppointmentsCount = appointments.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long completedAssessmentsCount = assessments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        model.addAttribute("pendingAppointments", pendingAppointmentsCount);
        model.addAttribute("pendingAppointmentsCount", pendingAppointmentsCount);
        model.addAttribute("completedAssessmentsCount", completedAssessmentsCount);

        return "doctor/dashboard";
    }

    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User doctor = getAuthenticatedDoctor(userDetails);
        List<PatientProfile> assignedPatients = doctorService.getAssignedPatients(doctor);
        model.addAttribute("doctor", doctor);
        model.addAttribute("patients", assignedPatients);
        return "doctor/patients";
    }

    @GetMapping("/patient/{patientUserId}")
    public String patientDetail(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable("patientUserId") Long patientUserId,
                                Model model) {
        User doctor = getAuthenticatedDoctor(userDetails);
        // The chart URL uses the patient's User ID consistently with all
        // health-record and assessment CRUD endpoints.
        User patient = userService.findById(patientUserId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        PatientProfile patientProfile = patientProfileRepository.findByUser(patient)
                .orElseThrow(() -> new IllegalArgumentException("Patient profile not found"));

        if (patientProfile.getAssignedDoctor() == null
                || !patientProfile.getAssignedDoctor().getId().equals(doctor.getId())) {
            throw new SecurityException("You are not authorized to view this patient's chart.");
        }

        List<HealthRecord> records = doctorService.getPatientRecords(patientUserId);
        List<HealthAssessment> assessments = doctorService.getPatientAssessments(patientUserId);
        DietPlan plan = patientService.getActiveDietPlan(patientProfile.getUser()).orElse(null);

        model.addAttribute("doctor", doctor);
        // The chart template expects both the Patient User (`patient`) and
        // the extended PatientProfile (`profile`/`patientProfile`).
        // Keep the IDs consistent: URLs/forms that operate on health data
        // use the User ID, while profile lookup is performed via the profile.
        model.addAttribute("patient", patientProfile.getUser());
        model.addAttribute("patientProfile", patientProfile);
        model.addAttribute("profile", patientProfile);
        model.addAttribute("records", records);
        model.addAttribute("assessments", assessments);
        model.addAttribute("dietPlan", plan);
        return "doctor/patient-detail";
    }

    @PostMapping("/health-records/new")
    public String addHealthRecord(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam("patientId") Long patientId,
                                  @RequestParam("weightKg") Double weightKg,
                                  @RequestParam("heightCm") Double heightCm,
                                  @RequestParam("systolicBp") Integer systolicBp,
                                  @RequestParam("diastolicBp") Integer diastolicBp,
                                  @RequestParam("bloodSugarMgDl") Double bloodSugarMgDl,
                                  @RequestParam("heartRateBpm") Integer heartRateBpm,
                                  @RequestParam("notes") String notes,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        User doctor = getAuthenticatedDoctor(userDetails);
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        HealthRecord record = new HealthRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setRecordDate(LocalDate.now());
        record.setWeightKg(weightKg);
        record.setHeightCm(heightCm);
        record.setSystolicBp(systolicBp);
        record.setDiastolicBp(diastolicBp);
        record.setBloodSugarMgDl(bloodSugarMgDl);
        record.setHeartRateBpm(heartRateBpm);
        record.setNotes(notes);

        doctorService.createHealthRecord(record);

        // Update patient current weight in profile
        patientService.getProfileByUser(patient).ifPresent(p -> {
            p.setCurrentWeightKg(weightKg);
            patientService.saveProfile(p);
        });

        auditService.log(doctor.getEmail(), "ROLE_DOCTOR", "HEALTH_RECORD_CREATE", "Recorded checkup telemetry for patient #" + patientId, request);
        messageService.sendNotification(patient, "New Health Record Added", "Dr. " + doctor.getFullName() + " added a new checkup record.", "HEALTH_RECORD", "/patient/health-records");

        redirectAttributes.addFlashAttribute("successMessage", "Health record recorded successfully.");
        return "redirect:/doctor/patient/" + patientId;
    }

    @PostMapping("/health-records/{id}/update")
    public String updateHealthRecord(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long id,
                                     @RequestParam Long patientId,
                                     @RequestParam Double weightKg, @RequestParam Double heightCm,
                                     @RequestParam Integer systolicBp, @RequestParam Integer diastolicBp,
                                     @RequestParam Double bloodSugarMgDl, @RequestParam Integer heartRateBpm,
                                     @RequestParam(required = false) Double temperatureC,
                                     @RequestParam(required = false) String notes,
                                     HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User doctor = getAuthenticatedDoctor(userDetails);
        doctorService.updateHealthRecord(id, doctor, weightKg, heightCm, systolicBp, diastolicBp, bloodSugarMgDl, heartRateBpm,
                temperatureC == null ? 36.6 : temperatureC, notes);
        auditService.log(doctor.getEmail(), "ROLE_DOCTOR", "HEALTH_RECORD_UPDATE", "Updated health record #" + id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Health record updated successfully.");
        return "redirect:/doctor/patient/" + patientId;
    }

    @PostMapping("/health-records/{id}/delete")
    public String deleteHealthRecord(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long id, @RequestParam Long patientId,
                                     HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User doctor = getAuthenticatedDoctor(userDetails);
        doctorService.deleteHealthRecord(id, doctor);
        auditService.log(doctor.getEmail(), "ROLE_DOCTOR", "HEALTH_RECORD_DELETE", "Deleted health record #" + id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Health record deleted successfully.");
        return "redirect:/doctor/patient/" + patientId;
    }

    @GetMapping("/health-assessments")
    public String healthAssessments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User doctor = getAuthenticatedDoctor(userDetails);
        List<HealthAssessment> assessments = doctorService.getAssessmentsByDoctor(doctor);
        List<PatientProfile> patients = doctorService.getAssignedPatients(doctor);

        model.addAttribute("doctor", doctor);
        model.addAttribute("assessments", assessments);
        model.addAttribute("patients", patients);
        return "doctor/assessments";
    }

    @PostMapping("/health-assessments")
    public String createAssessment(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam("patientId") Long patientId,
                                   @RequestParam("symptoms") String symptoms,
                                   @RequestParam("diagnosis") String diagnosis,
                                   @RequestParam("clinicalRecommendations") String clinicalRecommendations,
                                   @RequestParam("medications") String medications,
                                   @RequestParam(value = "nextCheckupDate", required = false) String nextCheckupDateStr,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        User doctor = getAuthenticatedDoctor(userDetails);
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        HealthAssessment assessment = new HealthAssessment();
        assessment.setPatient(patient);
        assessment.setDoctor(doctor);
        assessment.setAssessmentDate(LocalDate.now());
        assessment.setSymptoms(symptoms);
        assessment.setDiagnosis(diagnosis);
        assessment.setClinicalRecommendations(clinicalRecommendations);
        assessment.setMedications(medications);
        if (nextCheckupDateStr != null && !nextCheckupDateStr.isBlank()) {
            assessment.setNextCheckupDate(LocalDate.parse(nextCheckupDateStr));
        } else {
            // The form does not require a follow-up date. Keep the field optional
            // and use a sensible default so publishing never fails because the
            // date input was omitted.
            assessment.setNextCheckupDate(LocalDate.now().plusMonths(1));
        }

        // Persist the assessment before sending any notification.
        doctorService.createHealthAssessment(assessment);
        auditService.log(doctor.getEmail(), "ROLE_DOCTOR", "ASSESSMENT_CREATE", "Completed clinical diagnosis for patient #" + patientId, request);
        messageService.sendNotification(patient, "Clinical Assessment Ready", "Dr. " + doctor.getFullName() + " published your health diagnosis.", "HEALTH_RECORD", "/patient/health-records");

        redirectAttributes.addFlashAttribute("successMessage", "Assessment finalized and synchronized with patient chart.");
        return "redirect:/doctor/health-assessments";
    }

    @PostMapping("/health-assessments/{id}/update")
    public String updateAssessment(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable Long id, @RequestParam Long patientId,
                                   @RequestParam String symptoms, @RequestParam String diagnosis,
                                   @RequestParam String clinicalRecommendations, @RequestParam String medications,
                                   @RequestParam(required = false) String nextCheckupDate,
                                   @RequestParam(defaultValue = "COMPLETED") String status,
                                   RedirectAttributes redirectAttributes) {
        User doctor = getAuthenticatedDoctor(userDetails);
        LocalDate next = (nextCheckupDate == null || nextCheckupDate.isBlank()) ? null : LocalDate.parse(nextCheckupDate);
        doctorService.updateHealthAssessment(id, doctor, symptoms, diagnosis, clinicalRecommendations, medications, next, status);
        redirectAttributes.addFlashAttribute("successMessage", "Health assessment updated successfully.");
        return "redirect:/doctor/health-assessments";
    }

    @PostMapping("/health-assessments/{id}/delete")
    public String deleteAssessment(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        User doctor = getAuthenticatedDoctor(userDetails);
        doctorService.deleteHealthAssessment(id, doctor);
        redirectAttributes.addFlashAttribute("successMessage", "Health assessment deleted successfully.");
        return "redirect:/doctor/health-assessments";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User doctor = getAuthenticatedDoctor(userDetails);
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctor);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        return "doctor/appointments";
    }

    @PostMapping("/appointments/{id}/status")
    public String updateAppointmentStatus(@AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable("id") Long appointmentId,
                                          @RequestParam("status") String status,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        User doctor = getAuthenticatedDoctor(userDetails);
        appointmentService.updateStatus(appointmentId, doctor, status);
        appointmentService.getAppointmentById(appointmentId).ifPresent(apt -> {
            messageService.sendNotification(apt.getPatient(), "Appointment " + status, "Your appointment on " + apt.getAppointmentDate() + " is now " + status + ".", "APPOINTMENT", "/patient/appointments");
        });
        redirectAttributes.addFlashAttribute("successMessage", "Appointment status updated to " + status);
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User doctor = getAuthenticatedDoctor(userDetails);
        List<PatientProfile> patients = doctorService.getAssignedPatients(doctor);
        model.addAttribute("doctor", doctor);
        model.addAttribute("patients", patients);
        return "doctor/reports";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User doctor = getAuthenticatedDoctor(userDetails);
        DoctorProfile profile = doctorService.getProfileByUser(doctor).orElse(new DoctorProfile(doctor, "General Medicine", "LIC-001"));
        model.addAttribute("doctor", doctor);
        model.addAttribute("profile", profile);
        return "doctor/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(value = "fullName", required = false) String fullName,
                                @RequestParam(value = "phone", required = false) String phone,
                                @RequestParam("specialization") String specialization,
                                @RequestParam("licenseNumber") String licenseNumber,
                                @RequestParam(value = "qualification", required = false) String qualification,
                                @RequestParam(value = "department", required = false) String department,
                                @RequestParam(value = "consultationRoom", required = false) String consultationRoom,
                                RedirectAttributes redirectAttributes) {
        User doctor = getAuthenticatedDoctor(userDetails);

        // The name/email fields in the profile form are display-only/disabled,
        // so browsers do not submit them. Keep the existing values unless an
        // editable value is actually supplied.
        String updatedFullName = (fullName == null || fullName.isBlank())
                ? doctor.getFullName() : fullName.trim();
        String updatedPhone = (phone == null || phone.isBlank())
                ? doctor.getPhone() : phone.trim();

        userService.updateUserProfile(
                doctor.getId(),
                updatedFullName,
                updatedPhone,
                doctor.getAddress(),
                doctor.getGender()
        );

        // Reload the entity after the user update so the profile remains
        // attached to the managed/current doctor account.
        User currentDoctor = userService.findById(doctor.getId()).orElse(doctor);

        DoctorProfile profile = doctorService.getProfileByUser(currentDoctor)
                .orElseGet(() -> new DoctorProfile(currentDoctor, specialization.trim(), "LIC-000"));

        profile.setUser(currentDoctor);
        profile.setSpecialization(specialization.trim());
        profile.setLicenseNumber(licenseNumber.trim());

        // Qualification is not currently displayed in the form. Preserve the
        // stored value when no qualification was submitted.
        if (qualification != null && !qualification.isBlank()) {
            profile.setQualification(qualification.trim());
        }
        profile.setDepartment(department == null ? null : department.trim());
        profile.setConsultationRoom(consultationRoom == null ? null : consultationRoom.trim());

        doctorService.saveProfile(profile);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Doctor practice profile saved successfully."
        );
        return "redirect:/doctor/profile";
    }
}
