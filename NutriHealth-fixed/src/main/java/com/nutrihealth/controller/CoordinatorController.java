package com.nutrihealth.controller;

import com.nutrihealth.entity.*;
import com.nutrihealth.service.*;
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
@RequestMapping("/coordinator")
public class CoordinatorController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final MessageAndNotificationService messageService;
    private final AuditService auditService;

    @Autowired
    public CoordinatorController(UserService userService,
                                 AppointmentService appointmentService,
                                 MessageAndNotificationService messageService,
                                 AuditService auditService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.messageService = messageService;
        this.auditService = auditService;
    }

    private User getAuthenticatedCoordinator(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Coordinator not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User coordinator = getAuthenticatedCoordinator(userDetails);
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        List<User> patients = userService.getUsersByRole(UserRole.ROLE_PATIENT);
        List<User> doctors = userService.getUsersByRole(UserRole.ROLE_DOCTOR);
        List<User> nutritionists = userService.getUsersByRole(UserRole.ROLE_NUTRITIONIST);

        long pendingCount = allAppointments.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long confirmedCount = allAppointments.stream().filter(a -> "CONFIRMED".equals(a.getStatus())).count();

        model.addAttribute("coordinator", coordinator);
        model.addAttribute("appointments", allAppointments);
        model.addAttribute("patients", patients);
        model.addAttribute("doctors", doctors);
        model.addAttribute("nutritionists", nutritionists);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("totalPatients", patients.size());

        return "coordinator/dashboard";
    }

    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User coordinator = getAuthenticatedCoordinator(userDetails);
        List<User> patients = userService.getUsersByRole(UserRole.ROLE_PATIENT);
        model.addAttribute("coordinator", coordinator);
        model.addAttribute("patients", patients);
        return "coordinator/patients";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User coordinator = getAuthenticatedCoordinator(userDetails);
        List<Appointment> appointments = appointmentService.getAllAppointments();
        List<User> patients = userService.getUsersByRole(UserRole.ROLE_PATIENT);
        List<User> doctors = userService.getUsersByRole(UserRole.ROLE_DOCTOR);
        List<User> nutritionists = userService.getUsersByRole(UserRole.ROLE_NUTRITIONIST);

        model.addAttribute("coordinator", coordinator);
        model.addAttribute("appointments", appointments);
        model.addAttribute("patients", patients);
        model.addAttribute("doctors", doctors);
        model.addAttribute("nutritionists", nutritionists);
        return "coordinator/appointments";
    }

    @PostMapping("/appointments/status")
    public String updateStatus(@RequestParam("appointmentId") Long appointmentId,
                               @RequestParam("status") String status,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        appointmentService.updateStatus(appointmentId, status);
        appointmentService.getAppointmentById(appointmentId).ifPresent(apt -> {
            messageService.sendNotification(apt.getPatient(), "Appointment Status: " + status, "Your appointment has been updated to: " + status, "APPOINTMENT", "/patient/appointments");
        });
        redirectAttributes.addFlashAttribute("successMessage", "Appointment updated successfully.");
        return "redirect:/coordinator/appointments";
    }

    @PostMapping("/appointments/new")
    public String scheduleAppointment(@RequestParam("patientId") Long patientId,
                                      @RequestParam(value = "doctorId", required = false) Long doctorId,
                                      @RequestParam(value = "nutritionistId", required = false) Long nutritionistId,
                                      @RequestParam("appointmentDate") String dateStr,
                                      @RequestParam("appointmentTime") String timeStr,
                                      @RequestParam("type") String type,
                                      @RequestParam("reason") String reason,
                                      RedirectAttributes redirectAttributes) {
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        Appointment apt = new Appointment();
        apt.setPatient(patient);
        apt.setAppointmentDate(LocalDate.parse(dateStr));
        apt.setAppointmentTime(timeStr);
        apt.setType(type);
        apt.setReason(reason);
        apt.setStatus("CONFIRMED");

        if (doctorId != null) {
            userService.findById(doctorId).ifPresent(apt::setDoctor);
        }
        if (nutritionistId != null) {
            userService.findById(nutritionistId).ifPresent(apt::setNutritionist);
        }

        appointmentService.createAppointment(apt);
        messageService.sendNotification(patient, "Care Coordinator Scheduled Session", "A session on " + dateStr + " at " + timeStr + " has been confirmed.", "APPOINTMENT", "/patient/appointments");
        redirectAttributes.addFlashAttribute("successMessage", "Session scheduled and patient notified.");
        return "redirect:/coordinator/appointments";
    }

    @GetMapping("/followups")
    public String followups(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User coordinator = getAuthenticatedCoordinator(userDetails);
        List<Appointment> pendingFollowups = appointmentService.getUpcomingAppointments();
        model.addAttribute("coordinator", coordinator);
        model.addAttribute("followups", pendingFollowups);
        return "coordinator/followups";
    }

    @GetMapping("/messages")
    public String messages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User coordinator = getAuthenticatedCoordinator(userDetails);
        List<Message> inbox = messageService.getInbox(coordinator);
        List<Message> sent = messageService.getSent(coordinator);
        List<User> patients = userService.getUsersByRole(UserRole.ROLE_PATIENT);

        model.addAttribute("coordinator", coordinator);
        model.addAttribute("inbox", inbox);
        model.addAttribute("sent", sent);
        model.addAttribute("patients", patients);
        return "coordinator/messages";
    }

    @PostMapping("/messages")
    public String sendMessage(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("recipientId") Long recipientId,
                              @RequestParam("subject") String subject,
                              @RequestParam("content") String content,
                              RedirectAttributes redirectAttributes) {
        User coordinator = getAuthenticatedCoordinator(userDetails);
        User recipient = userService.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
        messageService.sendMessage(coordinator, recipient, subject, content);
        redirectAttributes.addFlashAttribute("successMessage", "Coordination message delivered.");
        return "redirect:/coordinator/messages";
    }

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User coordinator = getAuthenticatedCoordinator(userDetails);
        List<Appointment> appointments = appointmentService.getAllAppointments();
        model.addAttribute("coordinator", coordinator);
        model.addAttribute("appointments", appointments);
        return "coordinator/reports";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User coordinator = getAuthenticatedCoordinator(userDetails);
        model.addAttribute("coordinator", coordinator);
        return "coordinator/profile";
    }
}
