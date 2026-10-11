package com.nutrihealth.service;

import com.nutrihealth.entity.AuditLog;
import com.nutrihealth.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String email, String role, String action, String description, HttpServletRequest request) {
        String ip = (request != null) ? request.getRemoteAddr() : "127.0.0.1";
        AuditLog log = new AuditLog(email, role, action, description, ip);
        auditLogRepository.save(log);
    }

    /**
     * Login auditing must never block the authentication response.
     * The database write is executed asynchronously after authentication succeeds.
     */
    @Async
    public void logAsync(String email, String role, String action, String description, String ip) {
        try {
            auditLogRepository.save(new AuditLog(email, role, action, description, ip));
        } catch (Exception ignored) {
            // Auditing must not make an otherwise successful login fail.
        }
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
