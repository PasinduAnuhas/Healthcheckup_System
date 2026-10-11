package com.nutrihealth.config;

import com.nutrihealth.entity.*;
import com.nutrihealth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final NutritionistProfileRepository nutritionistProfileRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthAssessmentRepository healthAssessmentRepository;
    private final FoodRepository foodRepository;
    private final DietPlanRepository dietPlanRepository;
    private final DietPlanMealRepository dietPlanMealRepository;
    private final FoodIntakeRepository foodIntakeRepository;
    private final MealRecommendationRepository mealRecommendationRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           PatientProfileRepository patientProfileRepository,
                           DoctorProfileRepository doctorProfileRepository,
                           NutritionistProfileRepository nutritionistProfileRepository,
                           HealthRecordRepository healthRecordRepository,
                           HealthAssessmentRepository healthAssessmentRepository,
                           FoodRepository foodRepository,
                           DietPlanRepository dietPlanRepository,
                           DietPlanMealRepository dietPlanMealRepository,
                           FoodIntakeRepository foodIntakeRepository,
                           MealRecommendationRepository mealRecommendationRepository,
                           AppointmentRepository appointmentRepository,
                           NotificationRepository notificationRepository,
                           AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.nutritionistProfileRepository = nutritionistProfileRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.healthAssessmentRepository = healthAssessmentRepository;
        this.foodRepository = foodRepository;
        this.dietPlanRepository = dietPlanRepository;
        this.dietPlanMealRepository = dietPlanMealRepository;
        this.foodIntakeRepository = foodIntakeRepository;
        this.mealRecommendationRepository = mealRecommendationRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        /*
         * Keep the demo login accounts available even when the application is
         * connected to an existing MySQL database that already contains users.
         * The old "if user count > 0 return" check could leave one or more demo
         * accounts missing, which made the demo buttons fail to authenticate.
         */
        if (userRepository.count() > 0) {
            ensureDemoAccounts();
            return;
        }

        System.out.println(">>> Initializing NutriHealth demo accounts and sample clinical dataset...");

        // 1. Create Demo Users
        User itOfficer = new User("it@nutrihealth.com", passwordEncoder.encode("IT@123"), "Marcus Vance", "+1 555-0101", UserRole.ROLE_IT_OPERATIONS_OFFICER);
        itOfficer.setAddress("742 Evergreen Terrace, Sector 4");
        itOfficer.setGender("MALE");
        userRepository.save(itOfficer);

        User clinicAdmin = new User("admin@nutrihealth.com", passwordEncoder.encode("Admin@123"), "Dr. Eleanor Vance", "+1 555-0102", UserRole.ROLE_CLINIC_ADMIN);
        clinicAdmin.setAddress("100 Medical Center Blvd, Suite 400");
        clinicAdmin.setGender("FEMALE");
        userRepository.save(clinicAdmin);

        User coordinator = new User("coordinator@nutrihealth.com", passwordEncoder.encode("Coordinator@123"), "Sophia Reynolds", "+1 555-0103", UserRole.ROLE_PATIENT_CARE_COORDINATOR);
        coordinator.setAddress("45 Care Way, East Wing");
        coordinator.setGender("FEMALE");
        userRepository.save(coordinator);

        User doctor = new User("doctor@nutrihealth.com", passwordEncoder.encode("Doctor@123"), "Dr. Julian Hayes, MD", "+1 555-0104", UserRole.ROLE_DOCTOR);
        doctor.setAddress("12 Clinic Plaza, Floor 3");
        doctor.setGender("MALE");
        userRepository.save(doctor);
        DoctorProfile docProfile = new DoctorProfile(doctor, "Cardiometabolic & Preventive Medicine", "MD-98421-US");
        docProfile.setQualification("MD, Harvard Medical School; FACC");
        docProfile.setDepartment("Internal Medicine & Preventive Cardiology");
        docProfile.setConsultationRoom("Suite 302");
        doctorProfileRepository.save(docProfile);

        User nutritionist = new User("nutritionist@nutrihealth.com", passwordEncoder.encode("Nutritionist@123"), "Clara Oswald, MS, RD", "+1 555-0105", UserRole.ROLE_NUTRITIONIST);
        nutritionist.setAddress("12 Clinic Plaza, Floor 2");
        nutritionist.setGender("FEMALE");
        userRepository.save(nutritionist);
        NutritionistProfile nutProfile = new NutritionistProfile(nutritionist, "M.S. Clinical Nutrition, RD, LDN", "RD-77312-C");
        nutProfile.setExperienceYears(8);
        nutProfile.setConsultationRoom("Suite 210");
        nutProfile.setSpecializationAreas("Glycemic Control, Weight Optimization & Renal Dietetics");
        nutritionistProfileRepository.save(nutProfile);

        User patient = new User("patient@nutrihealth.com", passwordEncoder.encode("Patient@123"), "Alexander Wright", "+1 555-0106", UserRole.ROLE_PATIENT);
        patient.setDateOfBirth(LocalDate.of(1992, 4, 15));
        patient.setAddress("88 Maple Avenue, Apt 4B");
        patient.setGender("MALE");
        userRepository.save(patient);

        PatientProfile patProfile = new PatientProfile(patient);
        patProfile.setBloodGroup("O+");
        patProfile.setEmergencyContact("Sarah Wright (+1 555-0199)");
        patProfile.setHeightCm(178.0);
        patProfile.setCurrentWeightKg(73.4);
        patProfile.setTargetWeightKg(70.0);
        patProfile.setAllergies("Shellfish, Peanuts");
        patProfile.setMedicalConditions("Mild Pre-hypertension, Borderline LDL");
        patProfile.setActivityLevel("MODERATE");
        patProfile.setDailyCalorieTarget(2150);
        patProfile.setAssignedDoctor(doctor);
        patProfile.setAssignedNutritionist(nutritionist);
        patientProfileRepository.save(patProfile);

        // 2. Initialize Seed Foods
        Food oats = new Food("Steel Cut Oats", "GRAINS", 375, 12.5, 68.0, 6.5, "Slow-digesting whole grain with rich soluble beta-glucan.");
        foodRepository.save(oats);
        Food berries = new Food("Organic Blueberries", "FRUITS", 57, 0.7, 14.5, 0.3, "High anthocyanin antioxidant profile for cardiovascular health.");
        foodRepository.save(berries);
        Food salmon = new Food("Atlantic Salmon Fillet", "PROTEIN", 208, 20.4, 0.0, 13.4, "Rich EPA and DHA essential marine fatty acids.");
        foodRepository.save(salmon);
        Food broccoli = new Food("Steamed Florets of Broccoli", "VEGETABLES", 35, 2.4, 7.0, 0.4, "Potent sulforaphane source supporting hepatic detox pathways.");
        foodRepository.save(broccoli);
        Food yogurt = new Food("Greek Yogurt 2%", "DAIRY", 97, 10.0, 3.6, 5.0, "Bioactive probiotics and high bioavailable casein.");
        foodRepository.save(yogurt);
        Food almonds = new Food("Roasted Unsalted Almonds", "HEALTHY_FATS", 579, 21.0, 21.5, 49.5, "Vitamin E and magnesium dense healthy snack.");
        foodRepository.save(almonds);

        // 3. Initialize Patient Health Records
        HealthRecord r1 = new HealthRecord();
        r1.setPatient(patient);
        r1.setDoctor(doctor);
        r1.setRecordDate(LocalDate.now().minusWeeks(4));
        r1.setHeightCm(178.0);
        r1.setWeightKg(76.5);
        r1.setSystolicBp(134);
        r1.setDiastolicBp(86);
        r1.setBloodSugarMgDl(108.0);
        r1.setHeartRateBpm(74);
        r1.setNotes("Initial baseline consultation. Recommended dietary sodium reduction and aerobic exercise.");
        healthRecordRepository.save(r1);

        HealthRecord r2 = new HealthRecord();
        r2.setPatient(patient);
        r2.setDoctor(doctor);
        r2.setRecordDate(LocalDate.now().minusWeeks(2));
        r2.setHeightCm(178.0);
        r2.setWeightKg(74.8);
        r2.setSystolicBp(126);
        r2.setDiastolicBp(82);
        r2.setBloodSugarMgDl(99.0);
        r2.setHeartRateBpm(68);
        r2.setNotes("Follow-up checkup. Notable improvement in blood pressure and fasting blood glucose.");
        healthRecordRepository.save(r2);

        HealthRecord r3 = new HealthRecord();
        r3.setPatient(patient);
        r3.setDoctor(doctor);
        r3.setRecordDate(LocalDate.now());
        r3.setHeightCm(178.0);
        r3.setWeightKg(73.4);
        r3.setSystolicBp(120);
        r3.setDiastolicBp(78);
        r3.setBloodSugarMgDl(94.0);
        r3.setHeartRateBpm(65);
        r3.setNotes("Patient in optimal range. Cardiovascular markers normalizing with balanced diet.");
        healthRecordRepository.save(r3);

        // 4. Clinical Health Assessment
        HealthAssessment assessment = new HealthAssessment();
        assessment.setPatient(patient);
        assessment.setDoctor(doctor);
        assessment.setAssessmentDate(LocalDate.now().minusWeeks(1));
        assessment.setSymptoms("Occasional afternoon fatigue, mild postural dizziness previously reported.");
        assessment.setDiagnosis("Metabolic Profile Optimization - Pre-hypertension Resolving");
        assessment.setClinicalRecommendations("Maintain current potassium-rich dietary intake. 150 minutes weekly moderate cardio. Hydration target 2.5L/day.");
        assessment.setMedications("Omega-3 1000mg daily, Vitamin D3 2000 IU with breakfast.");
        assessment.setNextCheckupDate(LocalDate.now().plusMonths(2));
        assessment.setStatus("COMPLETED");
        healthAssessmentRepository.save(assessment);

        // 5. Active Diet Plan with Meals
        DietPlan plan = new DietPlan();
        plan.setPatient(patient);
        plan.setNutritionist(nutritionist);
        plan.setTitle("Cardiometabolic Vitality & Lean Mass Retention Plan");
        plan.setObjective("Reduce visceral adiposity, maintain steady postprandial glucose, support vascular endothelial elasticity.");
        plan.setDailyCalorieTarget(2150);
        plan.setStartDate(LocalDate.now().minusDays(10));
        plan.setEndDate(LocalDate.now().plusDays(50));
        plan.setStatus("ACTIVE");
        plan.setRecommendedFoods("Berries, leafy greens, wild salmon, chia seeds, extra virgin olive oil, quinoa.");
        plan.setRestrictedFoods("Ultra-processed meats, trans-fats, refined white flour, high fructose corn syrup, peanuts.");
        plan.setNutritionistNotes("Monitor morning fasting blood glucose twice weekly. Prioritize lean protein in breakfast.");
        dietPlanRepository.save(plan);

        DietPlanMeal m1 = new DietPlanMeal("BREAKFAST", "Steel Cut Oats with Blueberries & Greek Yogurt", "1 bowl (250g)", 480, 24.0, 68.0, 10.0, "Cook oats with water, top with fresh berries and swirl in Greek yogurt.");
        m1.setDietPlan(plan);
        dietPlanMealRepository.save(m1);

        DietPlanMeal m2 = new DietPlanMeal("LUNCH", "Wild Salmon with Quinoa & Steamed Broccoli", "1 plate (350g)", 620, 42.0, 52.0, 22.0, "Pan-sear salmon in 1 tsp olive oil, serve with warm seasoned quinoa and steamed greens.");
        m2.setDietPlan(plan);
        dietPlanMealRepository.save(m2);

        DietPlanMeal m3 = new DietPlanMeal("SNACK", "Unsalted Almonds & Green Apple", "1 serving (80g)", 240, 6.0, 22.0, 14.0, "Pair sliced crisp apple with a handful of raw almonds.");
        m3.setDietPlan(plan);
        dietPlanMealRepository.save(m3);

        DietPlanMeal m4 = new DietPlanMeal("DINNER", "Herb-Grilled Chicken Breast with Roasted Mediterranean Medley", "1 plate (320g)", 560, 48.0, 36.0, 16.0, "Season with rosemary, thyme, garlic and bake with zucchini, bell peppers.");
        m4.setDietPlan(plan);
        dietPlanMealRepository.save(m4);

        // 6. Food Intake Logs for Today
        FoodIntake f1 = new FoodIntake(patient, "BREAKFAST", "Steel Cut Oats with Berries", 480, "Felt satiated for over 4 hours");
        foodIntakeRepository.save(f1);

        FoodIntake f2 = new FoodIntake(patient, "LUNCH", "Salmon & Quinoa Bowl", 620, "Enjoyed at clinic cafeteria");
        foodIntakeRepository.save(f2);

        // 7. Meal Recommendations
        MealRecommendation rec1 = new MealRecommendation(patient, nutritionist, "SNACK", "Chia Seed Pudding with Cinnamon & Raspberries", "Nutrient-packed fiber snack with low glycemic impact", 210);
        mealRecommendationRepository.save(rec1);

        MealRecommendation rec2 = new MealRecommendation(patient, nutritionist, "DINNER", "Tofu Stir Fry with Bok Choy & Shiitake Mushrooms", "Plant-based phyto-nutrient rich alternative for light digestion", 450);
        mealRecommendationRepository.save(rec2);

        // 8. Clinical Appointments
        Appointment apt1 = new Appointment(patient, doctor, LocalDate.now().plusDays(3), "10:30 AM", "Routine Biometric & Blood Pressure Review");
        apt1.setStatus("CONFIRMED");
        apt1.setNotes("Please fast for 8 hours prior to appointment for lipid panel re-test.");
        appointmentRepository.save(apt1);

        Appointment apt2 = new Appointment(patient, null, LocalDate.now().plusDays(7), "02:00 PM", "Dietary Strategy & Macro Adjustment Consultation");
        apt2.setNutritionist(nutritionist);
        apt2.setType("NUTRITION_REVIEW");
        apt2.setStatus("CONFIRMED");
        appointmentRepository.save(apt2);

        // 9. Initial Notifications
        notificationRepository.save(new Notification(patient, "Upcoming Checkup", "Your consultation with Dr. Julian Hayes is confirmed for " + LocalDate.now().plusDays(3) + " at 10:30 AM.", "APPOINTMENT", "/patient/appointments"));
        notificationRepository.save(new Notification(patient, "Diet Plan Active", "Nutritionist Clara Oswald updated your meal recommendations.", "DIET_PLAN", "/patient/diet-plan"));

        // 10. Audit Logs
        auditLogRepository.save(new AuditLog("system", "SYSTEM", "SYSTEM_INIT", "Database seed completed with 6 demo roles and sample medical telemetry.", "127.0.0.1"));
        auditLogRepository.save(new AuditLog("admin@nutrihealth.com", "ROLE_CLINIC_ADMIN", "ADMIN_BOOTSTRAP", "Clinic Administrator security clearance verified.", "127.0.0.1"));

        System.out.println(">>> NutriHealth initialization complete! Ready for logins.");
    }

    /**
     * Always reconcile the six documented demo accounts.
     *
     * This is intentionally idempotent: it does not delete application data.
     * It only creates missing demo users or repairs their demo credentials,
     * role and active state. This makes the one-click login buttons reliable
     * with both a fresh H2 database and an existing MySQL database.
     */
    private void ensureDemoAccounts() {
        User itOfficer = ensureDemoAccount(
                "it@nutrihealth.com", "IT@123", "Marcus Vance",
                "+1 555-0101", UserRole.ROLE_IT_OPERATIONS_OFFICER);

        User clinicAdmin = ensureDemoAccount(
                "admin@nutrihealth.com", "Admin@123", "Dr. Eleanor Vance",
                "+1 555-0102", UserRole.ROLE_CLINIC_ADMIN);

        User coordinator = ensureDemoAccount(
                "coordinator@nutrihealth.com", "Coordinator@123", "Sophia Reynolds",
                "+1 555-0103", UserRole.ROLE_PATIENT_CARE_COORDINATOR);

        User doctor = ensureDemoAccount(
                "doctor@nutrihealth.com", "Doctor@123", "Dr. Julian Hayes, MD",
                "+1 555-0104", UserRole.ROLE_DOCTOR);

        User nutritionist = ensureDemoAccount(
                "nutritionist@nutrihealth.com", "Nutritionist@123", "Clara Oswald, MS, RD",
                "+1 555-0105", UserRole.ROLE_NUTRITIONIST);

        User patient = ensureDemoAccount(
                "patient@nutrihealth.com", "Patient@123", "Alexander Wright",
                "+1 555-0106", UserRole.ROLE_PATIENT);

        ensureDoctorProfile(doctor);
        ensureNutritionistProfile(nutritionist);
        ensurePatientProfile(patient);
        ensureDemoDietPlan(patient, nutritionist);

        System.out.println(">>> NutriHealth demo accounts verified: 6/6");
    }

    private User ensureDemoAccount(
            String email,
            String rawPassword,
            String fullName,
            String phone,
            UserRole role) {

        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElseGet(User::new);

        user.setEmail(email.toLowerCase());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(role);
        user.setActive(true);

        User saved = userRepository.saveAndFlush(user);
        System.out.println(">>> Demo account ready: " + email + " [" + role.name() + "]");
        return saved;
    }

    private void ensureDoctorProfile(User doctor) {
        if (doctor == null) return;

        DoctorProfile profile = doctorProfileRepository.findByUser(doctor)
                .orElseGet(() -> new DoctorProfile(
                        doctor,
                        "Cardiometabolic & Preventive Medicine",
                        "MD-DEMO-98421"));

        if (profile.getSpecialization() == null || profile.getSpecialization().isBlank()) {
            profile.setSpecialization("General Medicine");
        }
        if (profile.getLicenseNumber() == null || profile.getLicenseNumber().isBlank()) {
            profile.setLicenseNumber("MD-DEMO-98421");
        }
        if (profile.getQualification() == null || profile.getQualification().isBlank()) {
            profile.setQualification("MD, Preventive Medicine");
        }
        if (profile.getDepartment() == null || profile.getDepartment().isBlank()) {
            profile.setDepartment("Internal Medicine");
        }
        if (profile.getConsultationRoom() == null || profile.getConsultationRoom().isBlank()) {
            profile.setConsultationRoom("Suite 302");
        }

        doctorProfileRepository.save(profile);
    }

    private void ensureNutritionistProfile(User nutritionist) {
        if (nutritionist == null) return;

        NutritionistProfile profile = nutritionistProfileRepository.findByUser(nutritionist)
                .orElseGet(() -> new NutritionistProfile(
                        nutritionist,
                        "M.S. Clinical Nutrition, RD",
                        "RD-DEMO-77312"));

        if (profile.getQualification() == null || profile.getQualification().isBlank()) {
            profile.setQualification("M.S. Clinical Nutrition, RD");
        }
        if (profile.getCertificationNumber() == null || profile.getCertificationNumber().isBlank()) {
            profile.setCertificationNumber("RD-DEMO-77312");
        }
        if (profile.getConsultationRoom() == null || profile.getConsultationRoom().isBlank()) {
            profile.setConsultationRoom("Suite 210");
        }

        nutritionistProfileRepository.save(profile);
    }


    /**
     * Repairs the demo patient's active diet plan when the application is
     * started against an existing database.
     *
     * Older databases may contain the plan itself but only one meal (or no
     * meals at all) because the original seed data was skipped once users
     * already existed.  The patient portal expects a complete daily schedule.
     * This method is idempotent: existing meals are preserved and only missing
     * meal types are added.
     */
    private void ensureDemoDietPlan(User patient, User nutritionist) {
        if (patient == null || nutritionist == null) return;

        LocalDate today = LocalDate.now();

        DietPlan plan = dietPlanRepository
                .findFirstByPatientAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(
                        patient, "ACTIVE", today, today)
                .orElseGet(() -> dietPlanRepository
                        .findFirstByPatientAndStatusOrderByStartDateDesc(patient, "ACTIVE")
                        .orElse(null));

        if (plan == null) {
            plan = new DietPlan();
            plan.setPatient(patient);
            plan.setNutritionist(nutritionist);
            plan.setTitle("Cardiometabolic Vitality & Lean Mass Retention Plan");
            plan.setObjective("Reduce visceral adiposity, maintain steady postprandial glucose, support vascular endothelial elasticity.");
            plan.setDailyCalorieTarget(2150);
            plan.setStartDate(today.minusDays(10));
            plan.setEndDate(today.plusDays(50));
            plan.setStatus("ACTIVE");
            plan.setRecommendedFoods("Berries, leafy greens, wild salmon, chia seeds, extra virgin olive oil, quinoa.");
            plan.setRestrictedFoods("Ultra-processed meats, trans-fats, refined white flour, high fructose corn syrup, peanuts.");
            plan.setNutritionistNotes("Monitor morning fasting blood glucose twice weekly. Prioritize lean protein in breakfast.");
            plan = dietPlanRepository.saveAndFlush(plan);
        } else if (plan.getNutritionist() == null) {
            plan.setNutritionist(nutritionist);
            dietPlanRepository.saveAndFlush(plan);
        }

        java.util.Set<String> existingTypes = new java.util.HashSet<>(
                dietPlanMealRepository.findByDietPlanOrderByIdAsc(plan)
                        .stream()
                        .map(DietPlanMeal::getMealType)
                        .filter(java.util.Objects::nonNull)
                        .map(String::toUpperCase)
                        .collect(java.util.stream.Collectors.toSet())
        );

        addMissingDemoMeal(plan, existingTypes, "BREAKFAST",
                "Steel Cut Oats with Blueberries & Greek Yogurt", "1 bowl (250g)",
                480, 24.0, 68.0, 10.0,
                "Cook oats with water, top with fresh berries and swirl in Greek yogurt.");

        addMissingDemoMeal(plan, existingTypes, "LUNCH",
                "Wild Salmon with Quinoa & Steamed Broccoli", "1 plate (350g)",
                620, 42.0, 52.0, 22.0,
                "Pan-sear salmon in 1 tsp olive oil, serve with warm seasoned quinoa and steamed greens.");

        addMissingDemoMeal(plan, existingTypes, "SNACK",
                "Unsalted Almonds & Green Apple", "1 serving (80g)",
                240, 6.0, 22.0, 14.0,
                "Pair sliced crisp apple with a handful of raw almonds.");

        addMissingDemoMeal(plan, existingTypes, "DINNER",
                "Herb-Grilled Chicken Breast with Roasted Mediterranean Medley", "1 plate (320g)",
                560, 48.0, 36.0, 16.0,
                "Season with rosemary, thyme, garlic and bake with zucchini, bell peppers.");
    }

    private void addMissingDemoMeal(
            DietPlan plan,
            java.util.Set<String> existingTypes,
            String mealType,
            String foodName,
            String portionSize,
            int calories,
            double proteinG,
            double carbsG,
            double fatsG,
            String instructions) {

        if (existingTypes.contains(mealType)) return;

        DietPlanMeal meal = new DietPlanMeal(
                mealType, foodName, portionSize, calories,
                proteinG, carbsG, fatsG, instructions);
        meal.setDietPlan(plan);
        dietPlanMealRepository.saveAndFlush(meal);
        existingTypes.add(mealType);
    }

    private void ensurePatientProfile(User patient) {
        if (patient == null) return;

        PatientProfile profile = patientProfileRepository.findByUser(patient)
                .orElseGet(() -> new PatientProfile(patient));

        User assignedDoctor = userRepository.findByEmailIgnoreCase("doctor@nutrihealth.com").orElse(null);
        User assignedNutritionist = userRepository.findByEmailIgnoreCase("nutritionist@nutrihealth.com").orElse(null);

        if (profile.getAssignedDoctor() == null && assignedDoctor != null) {
            profile.setAssignedDoctor(assignedDoctor);
        }
        if (profile.getAssignedNutritionist() == null && assignedNutritionist != null) {
            profile.setAssignedNutritionist(assignedNutritionist);
        }
        if (profile.getHeightCm() == null) profile.setHeightCm(178.0);
        if (profile.getCurrentWeightKg() == null) profile.setCurrentWeightKg(73.4);
        if (profile.getTargetWeightKg() == null) profile.setTargetWeightKg(70.0);
        if (profile.getActivityLevel() == null || profile.getActivityLevel().isBlank()) {
            profile.setActivityLevel("MODERATE");
        }
        if (profile.getDailyCalorieTarget() == null) profile.setDailyCalorieTarget(2150);
        if (profile.getAllergies() == null) profile.setAllergies("Not specified");
        if (profile.getMedicalConditions() == null) profile.setMedicalConditions("Not specified");

        patientProfileRepository.save(profile);
    }
}
