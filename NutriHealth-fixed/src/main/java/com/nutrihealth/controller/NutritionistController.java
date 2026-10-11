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
@RequestMapping("/nutritionist")
public class NutritionistController {

    private final UserService userService;
    private final NutritionistService nutritionistService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MessageAndNotificationService messageService;
    private final AuditService auditService;

    @Autowired
    public NutritionistController(UserService userService,
                                  NutritionistService nutritionistService,
                                  PatientService patientService,
                                  AppointmentService appointmentService,
                                  MessageAndNotificationService messageService,
                                  AuditService auditService) {
        this.userService = userService;
        this.nutritionistService = nutritionistService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.messageService = messageService;
        this.auditService = auditService;
    }

    private User getAuthenticatedNutritionist(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Nutritionist not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        NutritionistProfile profile = nutritionistService.getProfileByUser(nutritionist).orElse(new NutritionistProfile(nutritionist, "RD", "NUT-001"));

        List<PatientProfile> assignedPatients = nutritionistService.getAssignedPatients(nutritionist);
        List<DietPlan> dietPlans = nutritionistService.getDietPlansByNutritionist(nutritionist);
        List<Appointment> appointments = appointmentService.getAppointmentsByNutritionist(nutritionist);
        List<MealRecommendation> recommendations = nutritionistService.getRecommendationsByNutritionist(nutritionist);

        model.addAttribute("nutritionist", nutritionist);
        model.addAttribute("profile", profile);
        model.addAttribute("assignedPatients", assignedPatients);
        model.addAttribute("dietPlans", dietPlans);
        model.addAttribute("appointments", appointments);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("activePlansCount", dietPlans.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count());

        return "nutritionist/dashboard";
    }

    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        List<PatientProfile> assignedPatients = nutritionistService.getAssignedPatients(nutritionist);
        model.addAttribute("nutritionist", nutritionist);
        model.addAttribute("patients", assignedPatients);
        return "nutritionist/patients";
    }

    @GetMapping("/diet-plans")
    public String dietPlans(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        List<DietPlan> plans = nutritionistService.getDietPlansByNutritionist(nutritionist);
        List<PatientProfile> patients = nutritionistService.getAssignedPatients(nutritionist);

        model.addAttribute("nutritionist", nutritionist);
        model.addAttribute("plans", plans);
        model.addAttribute("patients", patients);
        return "nutritionist/diet-plans";
    }

    @PostMapping("/diet-plans")
    public String createDietPlan(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam("patientId") Long patientId,
                                 @RequestParam("title") String title,
                                 @RequestParam("objective") String objective,
                                 @RequestParam("dailyCalorieTarget") Integer dailyCalorieTarget,
                                 @RequestParam("startDate") String startDateStr,
                                 @RequestParam("endDate") String endDateStr,
                                 @RequestParam("recommendedFoods") String recommendedFoods,
                                 @RequestParam("restrictedFoods") String restrictedFoods,
                                 @RequestParam("nutritionistNotes") String nutritionistNotes,
                                 @RequestParam(value = "breakfastFood", required = false) String bFood,
                                 @RequestParam(value = "breakfastCalories", defaultValue = "400") Integer bCal,
                                 @RequestParam(value = "lunchFood", required = false) String lFood,
                                 @RequestParam(value = "lunchCalories", defaultValue = "600") Integer lCal,
                                 @RequestParam(value = "dinnerFood", required = false) String dFood,
                                 @RequestParam(value = "dinnerCalories", defaultValue = "500") Integer dCal,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        DietPlan plan = new DietPlan();
        plan.setPatient(patient);
        plan.setNutritionist(nutritionist);
        plan.setTitle(title);
        plan.setObjective(objective);
        plan.setDailyCalorieTarget(dailyCalorieTarget);
        plan.setStartDate(LocalDate.parse(startDateStr));
        plan.setEndDate(LocalDate.parse(endDateStr));
        plan.setStatus("ACTIVE");
        plan.setRecommendedFoods(recommendedFoods);
        plan.setRestrictedFoods(restrictedFoods);
        plan.setNutritionistNotes(nutritionistNotes);

        if (bFood != null && !bFood.isEmpty()) {
            DietPlanMeal bMeal = new DietPlanMeal("BREAKFAST", bFood, "1 serving", bCal, 20.0, 50.0, 10.0, "Prepare fresh");
            plan.addMeal(bMeal);
        }
        if (lFood != null && !lFood.isEmpty()) {
            DietPlanMeal lMeal = new DietPlanMeal("LUNCH", lFood, "1 serving", lCal, 35.0, 60.0, 15.0, "Balanced macro plate");
            plan.addMeal(lMeal);
        }
        if (dFood != null && !dFood.isEmpty()) {
            DietPlanMeal dMeal = new DietPlanMeal("DINNER", dFood, "1 serving", dCal, 30.0, 40.0, 12.0, "Light evening digestion");
            plan.addMeal(dMeal);
        }

        nutritionistService.saveDietPlan(plan);
        auditService.log(nutritionist.getEmail(), "ROLE_NUTRITIONIST", "DIET_PLAN_CREATE", "Created customized diet plan: " + title, request);
        messageService.sendNotification(patient, "New Diet Plan Published", "Nutritionist " + nutritionist.getFullName() + " created a new plan: " + title, "DIET_PLAN", "/patient/diet-plan");

        redirectAttributes.addFlashAttribute("successMessage", "Custom diet plan created and assigned successfully!");
        return "redirect:/nutritionist/diet-plans";
    }

    @PostMapping("/diet-plans/{id}/update")
    public String updateDietPlan(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                 @RequestParam String title, @RequestParam String objective,
                                 @RequestParam Integer dailyCalorieTarget, @RequestParam String startDate,
                                 @RequestParam String endDate, @RequestParam String status,
                                 @RequestParam String recommendedFoods, @RequestParam String restrictedFoods,
                                 @RequestParam String nutritionistNotes, RedirectAttributes redirectAttributes) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        nutritionistService.updateDietPlan(id, nutritionist, title, objective, dailyCalorieTarget, LocalDate.parse(startDate),
                LocalDate.parse(endDate), status, recommendedFoods, restrictedFoods, nutritionistNotes);
        redirectAttributes.addFlashAttribute("successMessage", "Diet plan updated successfully.");
        return "redirect:/nutritionist/diet-plans";
    }

    @PostMapping("/diet-plans/{id}/delete")
    public String deleteDietPlan(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        nutritionistService.deleteDietPlan(id, nutritionist);
        redirectAttributes.addFlashAttribute("successMessage", "Diet plan deleted successfully.");
        return "redirect:/nutritionist/diet-plans";
    }

    @GetMapping("/meal-recommendations")
    public String mealRecommendations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        List<MealRecommendation> recommendations = nutritionistService.getRecommendationsByNutritionist(nutritionist);
        List<PatientProfile> patients = nutritionistService.getAssignedPatients(nutritionist);

        model.addAttribute("nutritionist", nutritionist);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("patients", patients);
        return "nutritionist/meal-recommendations";
    }

    @PostMapping("/meal-recommendations")
    public String createRecommendation(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam("patientId") Long patientId,
                                       @RequestParam("mealType") String mealType,
                                       @RequestParam("title") String title,
                                       @RequestParam("description") String description,
                                       @RequestParam("calorieEstimate") Integer calorieEstimate,
                                       @RequestParam("nutritionBenefits") String nutritionBenefits,
                                       RedirectAttributes redirectAttributes) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        MealRecommendation rec = new MealRecommendation(patient, nutritionist, mealType, title, description, calorieEstimate);
        rec.setNutritionBenefits(nutritionBenefits);
        nutritionistService.saveMealRecommendation(rec);

        messageService.sendNotification(patient, "New Meal Idea", "Nutritionist recommended: " + title, "DIET_PLAN", "/patient/meal-recommendations");
        redirectAttributes.addFlashAttribute("successMessage", "Meal recommendation submitted.");
        return "redirect:/nutritionist/meal-recommendations";
    }

    @PostMapping("/meal-recommendations/{id}/update")
    public String updateRecommendation(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                       @RequestParam String mealType, @RequestParam String title,
                                       @RequestParam String description, @RequestParam Integer calorieEstimate,
                                       @RequestParam(value = "nutritionBenefits", required = false) String nutritionBenefits,
                                       @RequestParam(value = "dietaryRestrictionFriendly", defaultValue = "false") boolean dietaryRestrictionFriendly,
                                       RedirectAttributes redirectAttributes) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        nutritionistService.updateMealRecommendation(id, nutritionist, mealType, title, description, calorieEstimate, nutritionBenefits, dietaryRestrictionFriendly);
        redirectAttributes.addFlashAttribute("successMessage", "Meal recommendation updated successfully.");
        return "redirect:/nutritionist/meal-recommendations";
    }

    @PostMapping("/meal-recommendations/{id}/delete")
    public String deleteRecommendation(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        nutritionistService.deleteMealRecommendation(id, nutritionist);
        redirectAttributes.addFlashAttribute("successMessage", "Meal recommendation deleted successfully.");
        return "redirect:/nutritionist/meal-recommendations";
    }

    @GetMapping("/food-database")
    public String foodDatabase(Model model) {
        List<Food> foods = nutritionistService.getAllFoods();
        model.addAttribute("foods", foods);
        return "nutritionist/food-database";
    }

    @PostMapping("/food-database")
    public String addFoodItem(@RequestParam("name") String name,
                              @RequestParam("category") String category,
                              @RequestParam("servingSize") String servingSize,
                              @RequestParam("caloriesPer100g") Integer calories,
                              @RequestParam("proteinG") Double protein,
                              @RequestParam("carbsG") Double carbs,
                              @RequestParam("fatG") Double fat,
                              @RequestParam("fiberG") Double fiber,
                              @RequestParam("description") String description,
                              RedirectAttributes redirectAttributes) {
        Food food = new Food(name, category, calories, protein, carbs, fat, description);
        food.setServingSize(servingSize);
        food.setFiberG(fiber);
        nutritionistService.saveFood(food);
        redirectAttributes.addFlashAttribute("successMessage", "New food added to institutional database.");
        return "redirect:/nutritionist/food-database";
    }

    @PostMapping("/food-database/{id}/update")
    public String updateFood(@PathVariable Long id, @RequestParam String name, @RequestParam String category,
                             @RequestParam String servingSize, @RequestParam Integer caloriesPer100g,
                             @RequestParam Double proteinG, @RequestParam Double carbsG, @RequestParam Double fatG,
                             @RequestParam Double fiberG, @RequestParam String description,
                             @RequestParam(defaultValue = "false") boolean healthy,
                             @RequestParam(required = false) String allergens, RedirectAttributes redirectAttributes) {
        nutritionistService.updateFood(id, name, category, servingSize, caloriesPer100g, proteinG, carbsG, fatG, fiberG, description, healthy, allergens);
        redirectAttributes.addFlashAttribute("successMessage", "Food item updated successfully.");
        return "redirect:/nutritionist/food-database";
    }

    @PostMapping("/food-database/{id}/delete")
    public String deleteFood(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        nutritionistService.deleteFood(id);
        redirectAttributes.addFlashAttribute("successMessage", "Food item deleted successfully.");
        return "redirect:/nutritionist/food-database";
    }

    @GetMapping("/food-intake")
    public String foodIntakeReview(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam(value = "patientId", required = false) Long patientId,
                                   Model model) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        List<PatientProfile> patients = nutritionistService.getAssignedPatients(nutritionist);
        List<FoodIntake> intakes = java.util.Collections.emptyList();

        if (patientId != null) {
            final Long requestedPatientId = patientId;
            boolean assigned = patients.stream()
                    .anyMatch(p -> p.getUser() != null
                            && requestedPatientId.equals(p.getUser().getId()));

            if (!assigned) {
                throw new SecurityException("You are not authorized to review this patient's food intake.");
            }

            intakes = nutritionistService.getPatientFoodIntakes(patientId);
        } else if (!patients.isEmpty()) {
            patientId = patients.get(0).getUser().getId();
            intakes = nutritionistService.getPatientFoodIntakes(patientId);
        }

        model.addAttribute("nutritionist", nutritionist);
        model.addAttribute("patients", patients);
        model.addAttribute("selectedPatientId", patientId);
        model.addAttribute("intakes", intakes);
        return "nutritionist/food-intake";
    }

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        List<PatientProfile> patients = nutritionistService.getAssignedPatients(nutritionist);
        model.addAttribute("nutritionist", nutritionist);
        model.addAttribute("patients", patients);
        return "nutritionist/reports";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User nutritionist = getAuthenticatedNutritionist(userDetails);
        NutritionistProfile profile = nutritionistService.getProfileByUser(nutritionist).orElse(new NutritionistProfile(nutritionist, "Clinical RD", "NUT-001"));
        model.addAttribute("nutritionist", nutritionist);
        model.addAttribute("profile", profile);
        return "nutritionist/profile";
    }
}
