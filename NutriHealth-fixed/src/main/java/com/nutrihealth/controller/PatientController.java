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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final UserService userService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MessageAndNotificationService messageService;
    private final DoctorService doctorService;
    private final NutritionistService nutritionistService;
    private final AuditService auditService;
    private final HealthProgressService healthProgressService;

    @Autowired
    public PatientController(UserService userService,
                             PatientService patientService,
                             AppointmentService appointmentService,
                             MessageAndNotificationService messageService,
                             DoctorService doctorService,
                             NutritionistService nutritionistService,
                             AuditService auditService,
                             HealthProgressService healthProgressService) {
        this.userService = userService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.messageService = messageService;
        this.doctorService = doctorService;
        this.nutritionistService = nutritionistService;
        this.auditService = auditService;
        this.healthProgressService = healthProgressService;
    }

    private User getAuthenticatedPatient(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated patient not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        PatientProfile profile = patientService.getProfileByUser(patient).orElse(new PatientProfile(patient));

        HealthRecord latestRecord = patientService.getLatestHealthRecord(patient).orElse(null);
        DietPlan activePlan = patientService.getActiveDietPlan(patient).orElse(null);
        List<FoodIntake> todayIntakes = patientService.getTodayFoodIntakes(patient);
        Integer todayCalories = patientService.getTodayTotalCalories(patient);
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient);
        List<Notification> notifications = messageService.getNotifications(patient);
        List<HealthAssessment> assessments = doctorService.getPatientAssessments(patient.getId());
        List<MealRecommendation> recommendations = patientService.getMealRecommendations(patient);

        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile);
        model.addAttribute("latestRecord", latestRecord);
        model.addAttribute("activePlan", activePlan);
        model.addAttribute("todayIntakes", todayIntakes);
        model.addAttribute("todayCalories", todayCalories);
        model.addAttribute("appointments", appointments);
        model.addAttribute("notifications", notifications);
        model.addAttribute("assessments", assessments);
        model.addAttribute("recommendations", recommendations);

        return "patient/dashboard";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        PatientProfile profile = patientService.getProfileByUser(patient).orElse(new PatientProfile(patient));
        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile);
        return "patient/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam("address") String address,
                                @RequestParam("gender") String gender,
                                @RequestParam("bloodGroup") String bloodGroup,
                                @RequestParam("emergencyContact") String emergencyContact,
                                @RequestParam("heightCm") Double heightCm,
                                @RequestParam("currentWeightKg") Double currentWeightKg,
                                @RequestParam("targetWeightKg") Double targetWeightKg,
                                @RequestParam("allergies") String allergies,
                                @RequestParam("medicalConditions") String medicalConditions,
                                @RequestParam("activityLevel") String activityLevel,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        userService.updateUserProfile(patient.getId(), fullName, phone, address, gender);

        PatientProfile profile = patientService.getProfileByUser(patient).orElse(new PatientProfile(patient));
        profile.setBloodGroup(bloodGroup);
        profile.setEmergencyContact(emergencyContact);
        profile.setHeightCm(heightCm);
        profile.setCurrentWeightKg(currentWeightKg);
        profile.setTargetWeightKg(targetWeightKg);
        profile.setAllergies(allergies);
        profile.setMedicalConditions(medicalConditions);
        profile.setActivityLevel(activityLevel);
        patientService.saveProfile(profile);

        auditService.log(patient.getEmail(), "ROLE_PATIENT", "PROFILE_UPDATE", "Patient updated biometric profile", request);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/patient/profile";
    }

    @GetMapping("/health-records")
    public String healthRecords(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        List<HealthRecord> records = patientService.getHealthRecords(patient);
        List<HealthAssessment> assessments = doctorService.getPatientAssessments(patient.getId());
        model.addAttribute("patient", patient);
        model.addAttribute("records", records);
        model.addAttribute("assessments", assessments);
        return "patient/health-records";
    }

    @GetMapping("/diet-plan")
    public String dietPlan(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        DietPlan activePlan = patientService.getActiveDietPlan(patient).orElse(null);
        List<DietPlanMeal> meals = patientService.getDietPlanMeals(activePlan);
        model.addAttribute("patient", patient);
        model.addAttribute("activePlan", activePlan);
        model.addAttribute("meals", meals);
        return "patient/diet-plan";
    }

    @GetMapping("/food-intake")
    public String foodIntake(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(value = "date", required = false) String dateStr,
                             Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        PatientProfile profile = patientService.getProfileByUser(patient).orElse(new PatientProfile(patient));

        LocalDate selectedDate = LocalDate.now();
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateStr);
            } catch (java.time.format.DateTimeParseException ignored) {
                selectedDate = LocalDate.now();
            }
        }

        List<FoodIntake> selectedIntakes = patientService.getFoodIntakesByDate(patient, selectedDate);
        Integer totalCalories = patientService.getTotalCaloriesByDate(patient, selectedDate);
        List<Food> foods = nutritionistService.getAllFoods();

        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile);
        // Keep the existing template variable name for compatibility, but it now
        // represents the selected date rather than only today's records.
        model.addAttribute("todayIntakes", selectedIntakes);
        model.addAttribute("todayCalories", totalCalories);
        model.addAttribute("totalCaloriesDate", totalCalories);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("foods", foods);
        model.addAttribute("commonFoods", foods);
        return "patient/food-intake";
    }

    @PostMapping("/food-intake")
    public String logFoodIntake(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam("mealType") String mealType,
                                @RequestParam("foodItem") String foodItem,
                                @RequestParam(value = "quantity", required = false) String quantity,
                                @RequestParam(value = "portionSize", required = false) String portionSize,
                                @RequestParam("calories") Integer calories,
                                @RequestParam(value = "proteinG", defaultValue = "0") Double proteinG,
                                @RequestParam(value = "carbsG", defaultValue = "0") Double carbsG,
                                @RequestParam(value = "fatG", defaultValue = "0") Double fatG,
                                @RequestParam(value = "consumptionDate", required = false) String consumptionDate,
                                @RequestParam(value = "notes", required = false) String notes,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);

        String finalQuantity = (quantity != null && !quantity.isBlank()) ? quantity : portionSize;
        if (finalQuantity == null || finalQuantity.isBlank()) {
            finalQuantity = "1 serving";
        }

        LocalDate logDate = LocalDate.now();
        if (consumptionDate != null && !consumptionDate.isBlank()) {
            try {
                logDate = LocalDate.parse(consumptionDate);
            } catch (java.time.format.DateTimeParseException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid consumption date.");
                return "redirect:/patient/food-intake";
            }
        }

        FoodIntake intake = new FoodIntake(patient, mealType, foodItem, calories, notes);
        intake.setLogDate(logDate);
        intake.setQuantity(finalQuantity);
        intake.setProteinG(proteinG);
        intake.setCarbsG(carbsG);
        intake.setFatG(fatG);
        patientService.logFoodIntake(intake);

        auditService.log(patient.getEmail(), "ROLE_PATIENT", "FOOD_INTAKE_LOG", "Logged " + foodItem + " (" + calories + " kcal)", request);
        redirectAttributes.addFlashAttribute("successMessage", "Food intake logged successfully!");
        return "redirect:/patient/food-intake?date=" + logDate;
    }

    @PostMapping("/food-intake/update/{id}")
    public String updateFoodIntake(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable("id") Long id,
                                   @RequestParam("mealType") String mealType,
                                   @RequestParam("foodItem") String foodItem,
                                   @RequestParam(value = "quantity", required = false) String quantity,
                                   @RequestParam("calories") Integer calories,
                                   @RequestParam(value = "proteinG", defaultValue = "0") Double proteinG,
                                   @RequestParam(value = "carbsG", defaultValue = "0") Double carbsG,
                                   @RequestParam(value = "fatG", defaultValue = "0") Double fatG,
                                   @RequestParam(value = "notes", required = false) String notes,
                                   RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        String finalQuantity = (quantity == null || quantity.isBlank()) ? "1 serving" : quantity.trim();
        FoodIntake updated = patientService.updateFoodIntake(id, patient, mealType, foodItem, finalQuantity, calories, proteinG, carbsG, fatG, notes);
        redirectAttributes.addFlashAttribute("successMessage", "Food intake updated successfully.");
        return "redirect:/patient/food-intake?date=" + updated.getLogDate();
    }

    @PostMapping("/food-intake/delete/{id}")
    public String deleteFoodIntake(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable("id") Long id,
                                   RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        // Keep the user on the date that contained the deleted item.
        FoodIntake existing = patientService.getFoodIntakeForPatient(id, patient);
        LocalDate logDate = existing.getLogDate();
        patientService.deleteFoodIntake(id, patient);
        redirectAttributes.addFlashAttribute("successMessage", "Food record deleted successfully.");
        return "redirect:/patient/food-intake?date=" + logDate;
    }

    @GetMapping("/meal-recommendations")
    public String mealRecommendations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        List<MealRecommendation> recommendations = patientService.getMealRecommendations(patient);
        model.addAttribute("patient", patient);
        model.addAttribute("recommendations", recommendations);
        return "patient/meal-recommendations";
    }

    @GetMapping("/progress")
    public String progress(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        PatientProfile profile = patientService.getProfileByUser(patient).orElse(new PatientProfile(patient));
        List<HealthRecord> records = patientService.getHealthRecords(patient);
        List<HealthProgress> progressEntries = healthProgressService.getByPatient(patient);

        // Charts need real server-side data.  The previous template referenced
        // weightLabels/weightData/bmiData/bpLabels/systolicData/diastolicData
        // without ever adding them to the model, so the canvases stayed blank.
        List<HealthProgress> chartEntries = new ArrayList<>(progressEntries);
        Collections.reverse(chartEntries); // oldest -> newest for a time series

        List<String> weightLabels = new ArrayList<>();
        List<Double> weightData = new ArrayList<>();
        List<Double> bmiData = new ArrayList<>();
        List<String> bpLabels = new ArrayList<>();
        List<Integer> systolicData = new ArrayList<>();
        List<Integer> diastolicData = new ArrayList<>();

        for (HealthProgress entry : chartEntries) {
            String label = entry.getProgressDate() != null ? entry.getProgressDate().toString() : "";
            if (entry.getWeightKg() != null) {
                weightLabels.add(label);
                weightData.add(entry.getWeightKg());
                bmiData.add(entry.getBmi());
            }
            if (entry.getSystolicBp() != null || entry.getDiastolicBp() != null) {
                bpLabels.add(label);
                systolicData.add(entry.getSystolicBp());
                diastolicData.add(entry.getDiastolicBp());
            }
        }

        // If the patient has not entered progress records yet, use physician
        // health records so the telemetry charts still provide useful history.
        if (weightLabels.isEmpty() && records != null && !records.isEmpty()) {
            List<HealthRecord> chartRecords = new ArrayList<>(records);
            Collections.reverse(chartRecords);
            for (HealthRecord entry : chartRecords) {
                String label = entry.getRecordDate() != null ? entry.getRecordDate().toString() : "";
                if (entry.getWeightKg() != null) {
                    weightLabels.add(label);
                    weightData.add(entry.getWeightKg());
                    bmiData.add(entry.getBmi());
                }
                if (entry.getSystolicBp() != null || entry.getDiastolicBp() != null) {
                    bpLabels.add(label);
                    systolicData.add(entry.getSystolicBp());
                    diastolicData.add(entry.getDiastolicBp());
                }
            }
        }

        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile);
        model.addAttribute("records", records);
        model.addAttribute("progressEntries", progressEntries);
        model.addAttribute("weightLabels", weightLabels);
        model.addAttribute("weightData", weightData);
        model.addAttribute("bmiData", bmiData);
        model.addAttribute("bpLabels", bpLabels);
        model.addAttribute("systolicData", systolicData);
        model.addAttribute("diastolicData", diastolicData);
        return "patient/progress";
    }

    @PostMapping("/progress")
    public String createProgress(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String progressDate,
                                 @RequestParam Double weightKg,
                                 @RequestParam(required = false) Double heightCm,
                                 @RequestParam(required = false) Integer systolicBp,
                                 @RequestParam(required = false) Integer diastolicBp,
                                 @RequestParam(required = false) Double bloodSugarMgDl,
                                 @RequestParam(required = false) Integer heartRateBpm,
                                 @RequestParam(required = false) String notes,
                                 RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        HealthProgress progress = new HealthProgress();
        progress.setPatient(patient);
        progress.setProgressDate(LocalDate.parse(progressDate));
        progress.setWeightKg(weightKg);
        progress.setHeightCm(heightCm);
        progress.setSystolicBp(systolicBp);
        progress.setDiastolicBp(diastolicBp);
        progress.setBloodSugarMgDl(bloodSugarMgDl);
        progress.setHeartRateBpm(heartRateBpm);
        progress.setNotes(notes);
        healthProgressService.create(progress);
        redirectAttributes.addFlashAttribute("successMessage", "Health progress recorded successfully.");
        return "redirect:/patient/progress";
    }

    @PostMapping("/progress/{id}/update")
    public String updateProgress(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long id,
                                 @RequestParam String progressDate,
                                 @RequestParam Double weightKg,
                                 @RequestParam(required = false) Double heightCm,
                                 @RequestParam(required = false) Integer systolicBp,
                                 @RequestParam(required = false) Integer diastolicBp,
                                 @RequestParam(required = false) Double bloodSugarMgDl,
                                 @RequestParam(required = false) Integer heartRateBpm,
                                 @RequestParam(required = false) String notes,
                                 RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        healthProgressService.update(id, patient, LocalDate.parse(progressDate), weightKg, heightCm,
                systolicBp, diastolicBp, bloodSugarMgDl, heartRateBpm, notes);
        redirectAttributes.addFlashAttribute("successMessage", "Health progress updated successfully.");
        return "redirect:/patient/progress";
    }

    @PostMapping("/progress/{id}/delete")
    public String deleteProgress(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        healthProgressService.delete(id, patient);
        redirectAttributes.addFlashAttribute("successMessage", "Health progress entry deleted.");
        return "redirect:/patient/progress";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient);
        List<User> doctors = userService.getUsersByRole(UserRole.ROLE_DOCTOR);
        List<User> nutritionists = userService.getUsersByRole(UserRole.ROLE_NUTRITIONIST);

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctors", doctors);
        model.addAttribute("nutritionists", nutritionists);
        return "patient/appointments";
    }

    @PostMapping("/appointments")
    public String requestAppointment(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam(value = "doctorId", required = false) Long doctorId,
                                     @RequestParam(value = "nutritionistId", required = false) Long nutritionistId,
                                     @RequestParam("appointmentDate") String dateStr,
                                     @RequestParam("appointmentTime") String timeStr,
                                     @RequestParam("type") String type,
                                     @RequestParam("reason") String reason,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        Appointment apt = new Appointment();
        apt.setPatient(patient);
        apt.setAppointmentDate(LocalDate.parse(dateStr));
        apt.setAppointmentTime(timeStr);
        apt.setType(type);
        apt.setReason(reason);
        apt.setStatus("PENDING");

        if (doctorId != null) {
            userService.findById(doctorId).ifPresent(apt::setDoctor);
        }
        if (nutritionistId != null) {
            userService.findById(nutritionistId).ifPresent(apt::setNutritionist);
        }

        appointmentService.createAppointment(apt);
        auditService.log(patient.getEmail(), "ROLE_PATIENT", "APPOINTMENT_REQUEST", "Requested appointment on " + dateStr, request);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment booking requested! Awaiting clinic confirmation.");
        return "redirect:/patient/appointments";
    }

    @PostMapping("/appointments/{id}/update")
    public String updateAppointment(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                    @RequestParam("appointmentDate") String appointmentDate, @RequestParam("appointmentTime") String appointmentTime,
                                    @RequestParam("type") String type, @RequestParam("reason") String reason,
                                    @RequestParam(value = "notes", required = false) String notes, RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        appointmentService.updateAppointment(id, patient, LocalDate.parse(appointmentDate), appointmentTime, type, reason, notes);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment updated successfully.");
        return "redirect:/patient/appointments";
    }

    @PostMapping("/appointments/{id}/delete")
    public String deleteAppointment(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        appointmentService.deleteAppointment(id, patient);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment deleted successfully.");
        return "redirect:/patient/appointments";
    }

    @GetMapping("/calculator")
    public String calorieCalculator(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        PatientProfile profile = patientService.getProfileByUser(patient).orElse(new PatientProfile(patient));
        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile);
        return "patient/calculator";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable("id") Long appointmentId,
                                    RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        appointmentService.cancelAppointment(appointmentId, patient);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled successfully.");
        return "redirect:/patient/appointments";
    }

    @GetMapping("/messages")
    public String messages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        List<Message> inbox = messageService.getInbox(patient);
        List<Message> sent = messageService.getSent(patient);
        List<User> clinicStaff = userService.getUsersByRole(UserRole.ROLE_DOCTOR);
        clinicStaff.addAll(userService.getUsersByRole(UserRole.ROLE_NUTRITIONIST));

        model.addAttribute("patient", patient);
        model.addAttribute("inbox", inbox);
        model.addAttribute("sent", sent);
        model.addAttribute("recipients", clinicStaff);
        return "patient/messages";
    }

    @PostMapping("/messages")
    public String sendMessage(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("recipientId") Long recipientId,
                              @RequestParam("subject") String subject,
                              @RequestParam("content") String content,
                              RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        User recipient = userService.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
        messageService.sendMessage(patient, recipient, subject, content);
        redirectAttributes.addFlashAttribute("successMessage", "Message transmitted securely.");
        return "redirect:/patient/messages";
    }

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        List<Notification> notifications = messageService.getNotifications(patient);
        model.addAttribute("patient", patient);
        model.addAttribute("notifications", notifications);
        return "patient/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationRead(@AuthenticationPrincipal UserDetails userDetails,
                                       @PathVariable("id") Long notificationId,
                                       RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        boolean marked = messageService.markNotificationRead(notificationId, patient);
        if (marked) {
            redirectAttributes.addFlashAttribute("successMessage", "Notification marked as read.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Notification could not be found.");
        }
        return "redirect:/patient/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllNotificationsRead(@AuthenticationPrincipal UserDetails userDetails,
                                            RedirectAttributes redirectAttributes) {
        User patient = getAuthenticatedPatient(userDetails);
        messageService.markAllNotificationsRead(patient);
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/patient/notifications";
    }

    /**
     * Patient-owned clinical dossier.
     *
     * This route intentionally resolves the patient from the authenticated
     * session instead of accepting a user id from the browser. This prevents
     * missing-profile/incorrect-id errors and ensures a patient can only open
     * their own dossier.
     */
    @GetMapping("/health-dossier")
    public String healthDossier(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        PatientProfile profile = patientService.getProfileByUser(patient)
                .orElseGet(() -> {
                    PatientProfile newProfile = new PatientProfile(patient);
                    return patientService.saveProfile(newProfile);
                });

        List<HealthRecord> records = patientService.getHealthRecords(patient);
        List<HealthAssessment> assessments = doctorService.getPatientAssessments(patient.getId());
        DietPlan plan = patientService.getActiveDietPlan(patient).orElse(null);

        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile);
        model.addAttribute("records", records);
        model.addAttribute("assessments", assessments);
        model.addAttribute("plan", plan);
        model.addAttribute("generatedDate", LocalDate.now());
        return "reports/patient-summary";
    }

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User patient = getAuthenticatedPatient(userDetails);
        PatientProfile profile = patientService.getProfileByUser(patient).orElse(new PatientProfile(patient));
        List<HealthRecord> records = patientService.getHealthRecords(patient);
        DietPlan plan = patientService.getActiveDietPlan(patient).orElse(null);

        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile);
        model.addAttribute("records", records);
        model.addAttribute("plan", plan);
        return "patient/reports";
    }
}
