package com.hrms.repository;

import com.hrms.model.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    @Query("SELECT l FROM LeaveRequest l WHERE " +
           "(:employeeId IS NULL OR l.employee.id = :employeeId) AND " +
           "(:status IS NULL OR l.status = :status)")
    Page<LeaveRequest> searchLeaveRequests(
        @Param("employeeId") Long employeeId,
        @Param("status") LeaveRequest.LeaveStatus status,
        Pageable pageable);

    long countByStatus(LeaveRequest.LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.reportingManager.id = :managerId AND l.status = 'PENDING'")
    List<LeaveRequest> findPendingByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT l FROM LeaveRequest l WHERE l.status IN ('PENDING','MANAGER_APPROVED') ORDER BY l.createdAt DESC")
    List<LeaveRequest> findPendingForHr();
}
