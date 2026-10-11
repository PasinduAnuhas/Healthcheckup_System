package com.nutrihealth.controller;

import com.nutrihealth.entity.UserRole;
import com.nutrihealth.service.AuditService;
import com.nutrihealth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuditService auditService;

    @Autowired
    public AuthController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please verify credentials.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been successfully logged out.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("roles", new UserRole[]{
                UserRole.ROLE_PATIENT,
                UserRole.ROLE_DOCTOR,
                UserRole.ROLE_NUTRITIONIST
        });
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam("fullName") String fullName,
                                 @RequestParam("email") String email,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("password") String password,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 @RequestParam("role") String roleStr,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String normalizedName = fullName == null ? "" : fullName.trim();
        String normalizedPhone = phone == null ? "" : phone.trim();

        if (normalizedName.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Full name is required.");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/register";
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long.");
            return "redirect:/register";
        }

        try {
            UserRole role = UserRole.valueOf(roleStr);
            userService.registerUser(normalizedEmail, password, normalizedName, normalizedPhone, role);
            // Audit logging must never make a successful registration appear to
            // have failed. The account has already been committed by UserService.
            try {
                auditService.log(normalizedEmail, role.name(), "USER_REGISTERED", "New user self-registered", request);
            } catch (Exception auditException) {
                System.err.println("Registration audit log skipped: " + auditException.getMessage());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
            redirectAttributes.addFlashAttribute("registeredEmail", normalizedEmail);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during registration. Please try again.");
            return "redirect:/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        try {
            String token = userService.generateResetToken(email);
            auditService.log(email, "UNKNOWN", "FORGOT_PASSWORD_REQUEST", "Password reset token requested", request);
            redirectAttributes.addFlashAttribute("successMessage", "A reset token has been generated. Demo Token: " + token);
            return "redirect:/reset-password?token=" + token;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "token", required = false) String token, Model model) {
        model.addAttribute("token", token != null ? token : "");
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/reset-password?token=" + token;
        }
        try {
            userService.resetPassword(token, password);
            auditService.log("unknown", "UNKNOWN", "PASSWORD_RESET_SUCCESS", "Password was reset via token", request);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully. Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "Access Denied: You do not possess the required clinical clearance to view this resource.");
        return "error/403";
    }
}
