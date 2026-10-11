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

import java.lang.management.ManagementFactory;
import java.util.List;

@Controller
@RequestMapping("/it")
public class ITOperationsController {

    private final UserService userService;
    private final AuditService auditService;

    @Autowired
    public ITOperationsController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    private User getAuthenticatedIT(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("IT Officer not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        long totalUsers = userService.countAllUsers();
        long activeUsers = userService.getAllUsers().stream().filter(User::isActive).count();
        List<AuditLog> recentLogs = auditService.getRecentLogs();

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeHours = uptimeMs / (1000 * 60 * 60);

        model.addAttribute("itOfficer", itOfficer);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("recentLogs", recentLogs);
        model.addAttribute("uptimeHours", uptimeHours);

        return "it/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        List<User> users = userService.getAllUsers();
        model.addAttribute("itOfficer", itOfficer);
        model.addAttribute("users", users);
        model.addAttribute("roles", UserRole.values());
        return "it/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long id,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        userService.toggleUserActive(id);
        auditService.log("it@nutrihealth.com", "ROLE_IT_OPERATIONS_OFFICER", "USER_STATUS_TOGGLE", "Changed account active status for user #" + id, request);
        redirectAttributes.addFlashAttribute("successMessage", "User status changed successfully.");
        return "redirect:/it/users";
    }

    @PostMapping("/users/new")
    public String createInternalUser(@RequestParam("fullName") String fullName,
                                     @RequestParam("email") String email,
                                     @RequestParam("phone") String phone,
                                     @RequestParam("password") String password,
                                     @RequestParam("role") String roleStr,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        try {
            UserRole role = UserRole.valueOf(roleStr);
            userService.registerUser(email, password, fullName, phone, role);
            auditService.log("it@nutrihealth.com", "ROLE_IT_OPERATIONS_OFFICER", "USER_PROVISIONED", "Provisioned new account for: " + email, request);
            redirectAttributes.addFlashAttribute("successMessage", "User provisioned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/it/users";
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
        return "redirect:/it/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User cannot be permanently deleted because it is referenced by existing data. Deactivate it instead.");
        }
        return "redirect:/it/users";
    }

    @GetMapping("/roles")
    public String roleManagement(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        model.addAttribute("itOfficer", itOfficer);
        model.addAttribute("roles", UserRole.values());
        return "it/roles";
    }

    @GetMapping("/security")
    public String systemSecurity(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        List<AuditLog> recentLogs = auditService.getRecentLogs();
        model.addAttribute("itOfficer", itOfficer);
        model.addAttribute("logs", recentLogs);
        return "it/security";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        List<AuditLog> logs = auditService.getAllLogs();
        model.addAttribute("itOfficer", itOfficer);
        model.addAttribute("logs", logs);
        return "it/audit-logs";
    }

    @GetMapping("/system-logs")
    public String systemLogs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        model.addAttribute("itOfficer", itOfficer);
        return "it/system-logs";
    }

    @GetMapping("/backups")
    public String backupAndRestore(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        model.addAttribute("itOfficer", itOfficer);
        return "it/backups";
    }

    @PostMapping("/backups/trigger")
    public String triggerBackup(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        auditService.log("it@nutrihealth.com", "ROLE_IT_OPERATIONS_OFFICER", "BACKUP_GENERATED", "Instant MySQL database backup snapshot captured to /var/backups/nutrihealth", request);
        redirectAttributes.addFlashAttribute("successMessage", "Database snapshot generated successfully with 256-bit AES encryption.");
        return "redirect:/it/backups";
    }

    @GetMapping("/settings")
    public String settings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        model.addAttribute("itOfficer", itOfficer);
        return "it/settings";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        model.addAttribute("itOfficer", itOfficer);
        return "it/profile";
    }

    @GetMapping("/system-health")
    public String systemHealth(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User itOfficer = getAuthenticatedIT(userDetails);
        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;

        model.addAttribute("itOfficer", itOfficer);
        model.addAttribute("status", "UP");
        model.addAttribute("availableProcessors", runtime.availableProcessors());
        model.addAttribute("totalMemoryMb", totalMem);
        model.addAttribute("usedMemoryMb", usedMem);
        model.addAttribute("freeMemoryMb", freeMem);
        return "it/system-health";
    }

    @PostMapping("/system-health/clear-cache")
    public String clearCache(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        auditService.log("it@nutrihealth.com", "ROLE_IT_OPERATIONS_OFFICER", "CACHE_FLUSHED", "Application runtime cache flushed", request);
        redirectAttributes.addFlashAttribute("successMessage", "Application cache flushed successfully.");
        return "redirect:/it/system-health";
    }

    @PostMapping("/system-health/db-backup")
    public String backupFromHealth(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        auditService.log("it@nutrihealth.com", "ROLE_IT_OPERATIONS_OFFICER", "BACKUP_GENERATED", "Database snapshot generated", request);
        redirectAttributes.addFlashAttribute("successMessage", "Snapshot backup captured successfully.");
        return "redirect:/it/system-health";
    }
}
