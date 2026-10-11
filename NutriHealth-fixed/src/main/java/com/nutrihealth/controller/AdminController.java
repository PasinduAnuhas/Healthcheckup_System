package com.nutrihealth.controller;

import com.nutrihealth.entity.*;
import com.nutrihealth.repository.DietPlanRepository;
import com.nutrihealth.service.*;
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
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final DietPlanRepository dietPlanRepository;
    private final AuditService auditService;
    private final ClinicReportService clinicReportService;

    @Autowired
    public AdminController(UserService userService,
                           AppointmentService appointmentService,
                           DietPlanRepository dietPlanRepository,
                           AuditService auditService,
                           ClinicReportService clinicReportService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.dietPlanRepository = dietPlanRepository;
        this.auditService = auditService;
        this.clinicReportService = clinicReportService;
    }

    private User getAuthenticatedAdmin(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Admin not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        long patientCount = userService.countByRole(UserRole.ROLE_PATIENT);
        long doctorCount = userService.countByRole(UserRole.ROLE_DOCTOR);
        long nutritionistCount = userService.countByRole(UserRole.ROLE_NUTRITIONIST);
        long appointmentCount = appointmentService.countTotal();
        long activeDietPlans = dietPlanRepository.countByStatus("ACTIVE");
        List<Appointment> recentAppointments = appointmentService.getAllAppointments();
        List<AuditLog> recentLogs = auditService.getRecentLogs();

        model.addAttribute("admin", admin);
        model.addAttribute("patientCount", patientCount);
        model.addAttribute("doctorCount", doctorCount);
        model.addAttribute("nutritionistCount", nutritionistCount);
        model.addAttribute("appointmentCount", appointmentCount);
        model.addAttribute("activeDietPlans", activeDietPlans);
        model.addAttribute("recentAppointments", recentAppointments);
        model.addAttribute("recentLogs", recentLogs);

        return "admin/dashboard";
    }

    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        List<User> patients = userService.getUsersByRole(UserRole.ROLE_PATIENT);
        model.addAttribute("admin", admin);
        model.addAttribute("patients", patients);
        return "admin/patients";
    }

    @GetMapping("/doctors")
    public String doctors(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        List<User> doctors = userService.getUsersByRole(UserRole.ROLE_DOCTOR);
        model.addAttribute("admin", admin);
        model.addAttribute("doctors", doctors);
        return "admin/doctors";
    }

    @GetMapping("/nutritionists")
    public String nutritionists(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        List<User> nutritionists = userService.getUsersByRole(UserRole.ROLE_NUTRITIONIST);
        model.addAttribute("admin", admin);
        model.addAttribute("nutritionists", nutritionists);
        return "admin/nutritionists";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id, @RequestParam String fullName, @RequestParam String email,
                             @RequestParam(required = false) String phone, @RequestParam(required = false) String address,
                             @RequestParam(required = false) String gender, @RequestParam String role,
                             @RequestParam(defaultValue = "false") boolean active, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, fullName, email, phone, address, gender, UserRole.valueOf(role), active);
            redirectAttributes.addFlashAttribute("successMessage", "User account updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User cannot be deleted because it is referenced by existing clinical data. Deactivate the account instead.");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        List<Appointment> appointments = appointmentService.getAllAppointments();
        model.addAttribute("admin", admin);
        model.addAttribute("appointments", appointments);
        model.addAttribute("patients", userService.getUsersByRole(UserRole.ROLE_PATIENT));
        model.addAttribute("doctors", userService.getUsersByRole(UserRole.ROLE_DOCTOR));
        model.addAttribute("nutritionists", userService.getUsersByRole(UserRole.ROLE_NUTRITIONIST));
        return "admin/appointments";
    }

    @PostMapping("/appointments/new")
    public String createAppointment(@RequestParam Long patientId,
                                    @RequestParam(required = false) Long doctorId,
                                    @RequestParam(required = false) Long nutritionistId,
                                    @RequestParam String appointmentDate,
                                    @RequestParam String appointmentTime,
                                    @RequestParam String type,
                                    @RequestParam String reason,
                                    @RequestParam(defaultValue = "CONFIRMED") String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            User patient = userService.findById(patientId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
            Appointment apt = new Appointment();
            apt.setPatient(patient);
            apt.setAppointmentDate(LocalDate.parse(appointmentDate));
            apt.setAppointmentTime(appointmentTime);
            apt.setType(type);
            apt.setReason(reason);
            apt.setStatus(status);
            if (doctorId != null) userService.findById(doctorId).ifPresent(apt::setDoctor);
            if (nutritionistId != null) userService.findById(nutritionistId).ifPresent(apt::setNutritionist);
            appointmentService.createAppointment(apt);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/{id}/update")
    public String updateAppointment(@PathVariable Long id, @RequestParam String appointmentDate,
                                    @RequestParam String appointmentTime, @RequestParam String type,
                                    @RequestParam String reason, @RequestParam(required = false) String notes,
                                    @RequestParam String status, RedirectAttributes redirectAttributes) {
        appointmentService.updateAppointmentByAdmin(id, LocalDate.parse(appointmentDate), appointmentTime, type, reason, notes, status);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment updated successfully.");
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/{id}/delete")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appointmentService.deleteAppointmentByAdmin(id);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment deleted successfully.");
        return "redirect:/admin/appointments";
    }

    @PostMapping("/diet-plans/{id}/delete")
    public String deleteDietPlan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dietPlanRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Diet plan deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to delete this diet plan.");
        }
        return "redirect:/admin/diet-plans";
    }

    @GetMapping("/diet-plans")
    public String dietPlans(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        List<DietPlan> plans = dietPlanRepository.findAll();
        model.addAttribute("admin", admin);
        model.addAttribute("plans", plans);
        return "admin/diet-plans";
    }

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        long patientCount = userService.countByRole(UserRole.ROLE_PATIENT);
        long doctorCount = userService.countByRole(UserRole.ROLE_DOCTOR);
        long nutritionistCount = userService.countByRole(UserRole.ROLE_NUTRITIONIST);
        long totalAppointments = appointmentService.countTotal();

        model.addAttribute("admin", admin);
        model.addAttribute("patientCount", patientCount);
        model.addAttribute("doctorCount", doctorCount);
        model.addAttribute("nutritionistCount", nutritionistCount);
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("reports", clinicReportService.findAll());
        return "admin/reports";
    }

    @PostMapping("/reports")
    public String createReport(@RequestParam String title,
                               @RequestParam String reportType,
                               @RequestParam(required = false) String description,
                               RedirectAttributes redirectAttributes) {
        ClinicReport report = new ClinicReport();
        report.setTitle(title.trim());
        report.setReportType(reportType);
        report.setDescription(description);
        clinicReportService.save(report);
        redirectAttributes.addFlashAttribute("successMessage", "Clinic report saved successfully.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/{id}/update")
    public String updateReport(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam String reportType,
                               @RequestParam(required = false) String description,
                               RedirectAttributes redirectAttributes) {
        try {
            ClinicReport report = clinicReportService.findById(id);
            report.setTitle(title.trim());
            report.setReportType(reportType);
            report.setDescription(description);
            clinicReportService.save(report);
            redirectAttributes.addFlashAttribute("successMessage", "Clinic report updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/{id}/delete")
    public String deleteReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clinicReportService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Clinic report deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @GetMapping("/activities")
    public String activities(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        List<AuditLog> logs = auditService.getAllLogs();
        model.addAttribute("admin", admin);
        model.addAttribute("logs", logs);
        return "admin/activities";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getAuthenticatedAdmin(userDetails);
        model.addAttribute("admin", admin);
        return "admin/profile";
    }
}
