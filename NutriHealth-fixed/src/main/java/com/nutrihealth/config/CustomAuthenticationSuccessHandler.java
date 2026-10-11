package com.nutrihealth.config;

import com.nutrihealth.service.AuditService;
import com.nutrihealth.security.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuditService auditService;

    @Autowired
    public CustomAuthenticationSuccessHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String redirectUrl = "/";
        String userEmail = authentication.getName();
        String userRole = "UNKNOWN";

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            userRole = userDetails.getRoleName();
        }

        // Do not block the login response on an audit-log database write.
        // The audit is persisted asynchronously so the user is redirected immediately.
        auditService.logAsync(
                userEmail,
                userRole,
                "LOGIN_SUCCESS",
                "User logged in successfully",
                request.getRemoteAddr()
        );

        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority == null || authority.getAuthority() == null) continue;
                String role = authority.getAuthority().trim().toUpperCase();
                if (!role.startsWith("ROLE_")) role = "ROLE_" + role;
                switch (role) {
                case "ROLE_PATIENT" -> redirectUrl = "/patient/dashboard";
                case "ROLE_DOCTOR" -> redirectUrl = "/doctor/dashboard";
                case "ROLE_NUTRITIONIST" -> redirectUrl = "/nutritionist/dashboard";
                case "ROLE_PATIENT_CARE_COORDINATOR" -> redirectUrl = "/coordinator/dashboard";
                case "ROLE_CLINIC_ADMIN" -> redirectUrl = "/admin/dashboard";
                    case "ROLE_IT_OPERATIONS_OFFICER" -> redirectUrl = "/it/dashboard";
                }
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
