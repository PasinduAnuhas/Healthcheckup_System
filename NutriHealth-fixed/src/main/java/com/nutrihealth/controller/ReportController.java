package com.nutrihealth.controller;

import com.nutrihealth.entity.*;
import com.nutrihealth.repository.*;
import com.nutrihealth.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final DietPlanRepository dietPlanRepository;

    @Autowired
    public ReportController(UserService userService,
                            PatientService patientService,
                            DoctorService doctorService,
                            AppointmentService appointmentService,
                            DietPlanRepository dietPlanRepository) {
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.dietPlanRepository = dietPlanRepository;
    }

    @GetMapping("/patient/{id}/summary")
    public String patientSummaryReport(@PathVariable("id") Long patientId,
                                        Authentication authentication, Model model) {
        User requestedPatient = userService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Patients may only view their own dossier. Staff can use the shared
        // report endpoint for clinical/admin workflows.
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()))
                && !requestedPatient.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("Patients may only view their own health dossier.");
        }

        PatientProfile profile = patientService.getProfileByUser(requestedPatient)
                .orElseGet(() -> patientService.saveProfile(new PatientProfile(requestedPatient)));
        User patient = requestedPatient;
        List<HealthRecord> records = patientService.getHealthRecords(patient);
        List<HealthAssessment> assessments = doctorService.getPatientAssessments(patientId);
        DietPlan plan = patientService.getActiveDietPlan(patient).orElse(null);

        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile);
        model.addAttribute("records", records);
        model.addAttribute("assessments", assessments);
        model.addAttribute("plan", plan);
        model.addAttribute("generatedDate", LocalDate.now());
        return "reports/patient-summary";
    }

    @GetMapping("/patient/{id}/export-csv")
    public void exportPatientHealthRecordsCsv(@PathVariable("id") Long patientId,
                                              Authentication authentication,
                                              HttpServletResponse response) throws IOException {
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // A patient can export only their own health data. Clinical/admin staff
        // can still use the shared report endpoint for authorized workflows.
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()))
                && !patient.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("Patients may only export their own health records.");
        }

        List<HealthRecord> records = patientService.getHealthRecords(patient);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"patient_" + patientId + "_health_records.csv\"");

        PrintWriter writer = response.getWriter();
        writer.write("\uFEFF");
        writer.println("Record Date,Weight (kg),Height (cm),Systolic BP,Diastolic BP,Blood Sugar (mg/dL),Heart Rate (bpm),BMI,Classification,Clinical Notes");

        for (HealthRecord r : records) {
            writer.println(String.join(",",
                    csv(r.getRecordDate()),
                    csv(r.getWeightKg()),
                    csv(r.getHeightCm()),
                    csv(r.getSystolicBp()),
                    csv(r.getDiastolicBp()),
                    csv(r.getBloodSugarMgDl()),
                    csv(r.getHeartRateBpm()),
                    csv(r.getBmi()),
                    csv(r.getBmiCategory()),
                    csv(r.getNotes())
            ));
        }
        writer.flush();
    }

    private String csv(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    @GetMapping("/patient/{id}/export-pdf")
    public void exportPatientHealthRecordsPdf(@PathVariable("id") Long patientId,
                                              Authentication authentication,
                                              HttpServletResponse response) throws IOException {
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Patients can export only their own health records.
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()))
                && !patient.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Patients may only export their own health records.");
        }

        List<HealthRecord> records = patientService.getHealthRecords(patient);

        byte[] pdf = buildHealthRecordsPdf(patient, records);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"patient_" + patientId + "_health_records.pdf\"");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
    }

    /**
     * Builds a small, dependency-free PDF so the patient can download the
     * health-record export without adding another PDF library to the project.
     */
    private byte[] buildHealthRecordsPdf(User patient, List<HealthRecord> records) throws IOException {
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("NUTRIHEALTH - PATIENT HEALTH RECORDS");
        lines.add("Patient: " + safeText(patient.getFullName()));
        lines.add("Email: " + safeText(patient.getEmail()));
        lines.add("Generated: " + LocalDate.now());
        lines.add("");
        lines.add("Date | Doctor | Weight | BP | Glucose | Heart Rate | BMI | Status");
        lines.add("--------------------------------------------------------------------------");

        if (records.isEmpty()) {
            lines.add("No health records available.");
        } else {
            for (HealthRecord r : records) {
                String doctor = r.getDoctor() != null ? r.getDoctor().getFullName() : "Clinic Physician";
                lines.add(safeText(r.getRecordDate()) + " | "
                        + safeText(doctor) + " | "
                        + safeText(r.getWeightKg()) + " kg | "
                        + safeText(r.getSystolicBp()) + "/" + safeText(r.getDiastolicBp()) + " | "
                        + safeText(r.getBloodSugarMgDl()) + " | "
                        + safeText(r.getHeartRateBpm()) + " | "
                        + safeText(r.getBmi()) + " | "
                        + safeText(r.getBmiCategory()));
                if (r.getNotes() != null && !r.getNotes().isBlank()) {
                    lines.add("Notes: " + safeText(r.getNotes()));
                }
            }
        }
        return createSimplePdf(lines);
    }

    private String safeText(Object value) {
        if (value == null) return "-";
        return String.valueOf(value).replace('\n', ' ').replace('\r', ' ');
    }

    private byte[] createSimplePdf(java.util.List<String> sourceLines) throws IOException {
        java.util.List<String> wrapped = new java.util.ArrayList<>();
        for (String line : sourceLines) {
            String text = line == null ? "" : line;
            if (text.isEmpty()) {
                wrapped.add("");
                continue;
            }
            while (text.length() > 100) {
                int cut = text.lastIndexOf(' ', 100);
                if (cut < 20) cut = 100;
                wrapped.add(text.substring(0, cut));
                text = text.substring(cut).trim();
            }
            wrapped.add(text);
        }

        final int linesPerPage = 45;
        int pageCount = Math.max(1, (wrapped.size() + linesPerPage - 1) / linesPerPage);
        java.util.List<byte[]> objects = new java.util.ArrayList<>();

        // Object 1: catalog, object 2: pages, object 3: font.
        objects.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pageCount; i++) {
            int pageObj = 4 + i * 2;
            kids.append(pageObj).append(" 0 R ");
        }
        objects.add(ascii("<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount + " >>"));
        objects.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));

        for (int page = 0; page < pageCount; page++) {
            int start = page * linesPerPage;
            int end = Math.min(wrapped.size(), start + linesPerPage);
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n/F1 10 Tf\n50 760 Td\n14 TL\n");
            for (int i = start; i < end; i++) {
                stream.append("(").append(pdfEscape(wrapped.get(i))).append(") Tj T*\n");
            }
            stream.append("ET\n");
            byte[] content = ascii(stream.toString());
            int pageObj = 4 + page * 2;
            int contentObj = pageObj + 1;
            objects.add(ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> "
                    + "/Contents " + contentObj + " 0 R >>"));
            objects.add(concat(ascii("<< /Length " + content.length + " >>\nstream\n"), content, ascii("endstream")));
        }

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        out.write(ascii("%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n"));
        java.util.List<Integer> offsets = new java.util.ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            out.write(ascii((i + 1) + " 0 obj\n"));
            out.write(objects.get(i));
            out.write(ascii("\nendobj\n"));
        }
        int xref = out.size();
        out.write(ascii("xref\n0 " + (objects.size() + 1) + "\n"));
        out.write(ascii("0000000000 65535 f \n"));
        for (int i = 1; i < offsets.size(); i++) {
            out.write(ascii(String.format(java.util.Locale.ROOT, "%010d 00000 n \n", offsets.get(i))));
        }
        out.write(ascii("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n"));
        out.write(ascii("startxref\n" + xref + "\n%%EOF"));
        return out.toByteArray();
    }

    private byte[] ascii(String value) {
        return value.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    private byte[] concat(byte[] a, byte[] b, byte[] c) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        out.write(a); out.write(b); out.write('\n'); out.write(c);
        return out.toByteArray();
    }

    private String pdfEscape(String value) {
        StringBuilder out = new StringBuilder();
        for (char ch : value.toCharArray()) {
            if (ch == '\\' || ch == '(' || ch == ')') out.append('\\');
            if (ch >= 32 && ch <= 126) out.append(ch);
            else if (ch == '\t') out.append(' ');
            else out.append('?');
        }
        return out.toString();
    }

    @GetMapping("/diet-plan/{id}")
    public String dietPlanReport(@PathVariable("id") Long planId, Authentication authentication, Model model) {
        DietPlan plan = dietPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Diet plan not found"));
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()))
                && (plan.getPatient() == null || !plan.getPatient().getEmail().equalsIgnoreCase(authentication.getName()))) {
            throw new org.springframework.security.access.AccessDeniedException("Patients may only view their own diet plan.");
        }
        model.addAttribute("plan", plan);
        model.addAttribute("generatedDate", LocalDate.now());
        return "reports/diet-plan-report";
    }

    @GetMapping("/clinic-summary")
    public String clinicSummaryReport(Model model) {
        model.addAttribute("totalPatients", userService.countByRole(UserRole.ROLE_PATIENT));
        model.addAttribute("totalDoctors", userService.countByRole(UserRole.ROLE_DOCTOR));
        model.addAttribute("totalNutritionists", userService.countByRole(UserRole.ROLE_NUTRITIONIST));
        model.addAttribute("totalAppointments", appointmentService.countTotal());
        model.addAttribute("activePlans", dietPlanRepository.countByStatus("ACTIVE"));
        model.addAttribute("generatedDate", LocalDate.now());
        return "reports/clinic-summary";
    }
}
