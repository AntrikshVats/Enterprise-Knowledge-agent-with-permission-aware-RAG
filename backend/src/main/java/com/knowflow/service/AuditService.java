package com.knowflow.service;

import com.knowflow.dto.AuditLogDto;
import com.knowflow.entity.AuditLog;
import com.knowflow.entity.User;
import com.knowflow.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logEvent(Long userId, String userEmail, String action, String resourceType, String resourceId, String details, String status) {
        log.info("[AUDIT] Action: {} | User: {} | Resource: {}:{} | Status: {} | Details: {}",
            action, userEmail, resourceType, resourceId, status, details);

        AuditLog auditLog = new AuditLog(userId, userEmail, action, resourceType, resourceId, details, status);
        return auditLogRepository.save(auditLog);
    }

    public void logAccessDenied(User user, String resourceType, String resourceId, String reason) {
        logEvent(
            user.getId(),
            user.getEmail(),
            "ACCESS_DENIED",
            resourceType,
            resourceId,
            reason,
            "DENIED"
        );
    }

    public void logIdorAttempt(User user, Long documentId, String details) {
        logEvent(
            user.getId(),
            user.getEmail(),
            "IDOR_ATTEMPT",
            "DOCUMENT",
            String.valueOf(documentId),
            details,
            "BLOCKED"
        );
    }

    public void logPromptInjection(User user, String query, String detectedPattern) {
        logEvent(
            user.getId(),
            user.getEmail(),
            "PROMPT_INJECTION_DETECTED",
            "CHAT",
            "N/A",
            "Query: '" + query + "' contained suspicious override instructions: " + detectedPattern,
            "WARNING"
        );
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .toList();
    }

    public AuditLogDto toDto(AuditLog log) {
        return new AuditLogDto(
            log.getId(),
            log.getUserId(),
            log.getUserEmail(),
            log.getAction(),
            log.getResourceType(),
            log.getResourceId(),
            log.getDetails(),
            log.getStatus(),
            log.getCreatedAt()
        );
    }
}
