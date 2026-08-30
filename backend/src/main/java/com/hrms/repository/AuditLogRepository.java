package com.hrms.repository;

import com.hrms.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:module IS NULL OR a.module = :module) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:userId IS NULL OR a.userId = :userId)")
    Page<AuditLog> searchAuditLogs(
        @Param("module") String module,
        @Param("action") String action,
        @Param("userId") Long userId,
        Pageable pageable);
}
