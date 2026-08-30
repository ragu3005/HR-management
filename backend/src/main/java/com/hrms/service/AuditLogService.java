package com.hrms.service;

import com.hrms.model.AuditLog;
import com.hrms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(Long userId, String userEmail, String action, String module,
                    String description, Long entityId, String entityType, String ipAddress) {
        AuditLog log = AuditLog.builder()
            .userId(userId)
            .userEmail(userEmail)
            .action(action)
            .module(module)
            .description(description)
            .entityId(entityId)
            .entityType(entityType)
            .ipAddress(ipAddress)
            .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLog> searchLogs(String module, String action, Long userId, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(module, action, userId, pageable);
    }
}
