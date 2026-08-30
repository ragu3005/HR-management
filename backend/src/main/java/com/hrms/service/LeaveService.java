package com.hrms.service;

import com.hrms.dto.LeaveDTOs;
import com.hrms.model.*;
import com.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeService employeeService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public LeaveRequest applyLeave(Long userId, LeaveDTOs.ApplyRequest req) {
        Employee emp = employeeService.getEmployeeByUserId(userId);

        LeaveType leaveType = leaveTypeRepository.findById(req.getLeaveTypeId())
            .orElseThrow(() -> new RuntimeException("Leave type not found"));

        // Calculate days
        BigDecimal days;
        if (Boolean.TRUE.equals(req.getIsHalfDay())) {
            days = BigDecimal.valueOf(0.5);
        } else {
            long between = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
            days = BigDecimal.valueOf(between);
        }

        // Check balance
        int year = LocalDate.now().getYear();
        LeaveBalance balance = leaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeIdAndYear(emp.getId(), leaveType.getId(), year)
            .orElseThrow(() -> new RuntimeException("No leave balance found for this type"));

        if (balance.getRemainingDays().compareTo(days) < 0) {
            throw new RuntimeException("Insufficient leave balance. Available: " + balance.getRemainingDays());
        }

        LeaveRequest request = LeaveRequest.builder()
            .employee(emp)
            .leaveType(leaveType)
            .startDate(req.getStartDate())
            .endDate(req.getEndDate())
            .totalDays(days)
            .isHalfDay(req.getIsHalfDay() != null && req.getIsHalfDay())
            .halfDayType(req.getHalfDayType() != null ?
                LeaveRequest.HalfDayType.valueOf(req.getHalfDayType()) : null)
            .reason(req.getReason())
            .status(LeaveRequest.LeaveStatus.PENDING)
            .build();

        // Update pending balance
        balance.setPendingDays(balance.getPendingDays().add(days));
        leaveBalanceRepository.save(balance);

        LeaveRequest saved = leaveRequestRepository.save(request);

        // Notify manager
        if (emp.getReportingManager() != null && emp.getReportingManager().getUser() != null) {
            notificationService.createNotification(
                emp.getReportingManager().getUser(),
                "Leave Request Pending",
                emp.getFullName() + " has applied for " + leaveType.getName() +
                    " from " + req.getStartDate() + " to " + req.getEndDate(),
                Notification.NotificationType.LEAVE_PENDING, saved.getId());
        }

        auditLogService.log(userId, emp.getOfficialEmail(), "LEAVE_APPLIED",
            "LEAVE", "Applied for " + leaveType.getName() + " (" + days + " days)",
            saved.getId(), "LEAVE", null);

        return saved;
    }

    @Transactional
    public LeaveRequest managerAction(Long requestId, Long managerId, boolean approve, String comment) {
        LeaveRequest req = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (req.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request is not in PENDING status");
        }

        Employee managerEmp = employeeService.getById(managerId);
        req.setManager(managerEmp);
        req.setManagerActionAt(LocalDateTime.now());
        req.setManagerComment(comment);
        req.setStatus(approve ? LeaveRequest.LeaveStatus.MANAGER_APPROVED : LeaveRequest.LeaveStatus.REJECTED);

        if (!approve) {
            revertPendingBalance(req);
        }

        LeaveRequest saved = leaveRequestRepository.save(req);

        // Notify employee
        if (req.getEmployee().getUser() != null) {
            notificationService.createNotification(
                req.getEmployee().getUser(),
                approve ? "Leave Request Approved by Manager" : "Leave Request Rejected",
                "Your " + req.getLeaveType().getName() + " request has been " +
                    (approve ? "approved by " : "rejected by ") + managerEmp.getFullName(),
                approve ? Notification.NotificationType.LEAVE_APPROVED : Notification.NotificationType.LEAVE_REJECTED,
                requestId);
        }

        return saved;
    }

    @Transactional
    public LeaveRequest hrAction(Long requestId, Long hrUserId, boolean approve, String comment) {
        LeaveRequest req = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (req.getStatus() != LeaveRequest.LeaveStatus.MANAGER_APPROVED &&
            req.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request cannot be acted upon in current status");
        }

        Employee hrEmp = employeeService.getEmployeeByUserId(hrUserId);
        req.setHr(hrEmp);
        req.setHrActionAt(LocalDateTime.now());
        req.setHrComment(comment);
        req.setStatus(approve ? LeaveRequest.LeaveStatus.APPROVED : LeaveRequest.LeaveStatus.REJECTED);

        if (approve) {
            // Move from pending to used
            leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                req.getEmployee().getId(), req.getLeaveType().getId(), LocalDate.now().getYear())
                .ifPresent(balance -> {
                    balance.setPendingDays(balance.getPendingDays().subtract(req.getTotalDays()));
                    balance.setUsedDays(balance.getUsedDays().add(req.getTotalDays()));
                    leaveBalanceRepository.save(balance);
                });
        } else {
            revertPendingBalance(req);
        }

        LeaveRequest saved = leaveRequestRepository.save(req);

        if (req.getEmployee().getUser() != null) {
            notificationService.createNotification(
                req.getEmployee().getUser(),
                approve ? "Leave Request Approved" : "Leave Request Rejected",
                "Your " + req.getLeaveType().getName() + " has been " +
                    (approve ? "approved!" : "rejected by HR"),
                approve ? Notification.NotificationType.LEAVE_APPROVED : Notification.NotificationType.LEAVE_REJECTED,
                requestId);
        }

        return saved;
    }

    @Transactional
    public void cancelLeave(Long requestId, Long userId) {
        LeaveRequest req = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        Employee emp = employeeService.getEmployeeByUserId(userId);
        if (!req.getEmployee().getId().equals(emp.getId())) {
            throw new RuntimeException("Cannot cancel another employee's leave");
        }

        if (req.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new RuntimeException("Only PENDING leave requests can be cancelled");
        }

        req.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        revertPendingBalance(req);
        leaveRequestRepository.save(req);
    }

    public List<LeaveDTOs.BalanceResponse> getLeaveBalances(Long userId) {
        Employee emp = employeeService.getEmployeeByUserId(userId);
        int year = LocalDate.now().getYear();
        return leaveBalanceRepository.findByEmployeeIdAndYear(emp.getId(), year).stream()
            .map(b -> LeaveDTOs.BalanceResponse.builder()
                .leaveTypeId(b.getLeaveType().getId())
                .leaveTypeName(b.getLeaveType().getName())
                .leaveTypeCode(b.getLeaveType().getCode())
                .totalDays(b.getTotalDays())
                .usedDays(b.getUsedDays())
                .pendingDays(b.getPendingDays())
                .remainingDays(b.getRemainingDays())
                .build())
            .collect(Collectors.toList());
    }

    public Page<LeaveRequest> searchLeaveRequests(Long empId, LeaveRequest.LeaveStatus status, Pageable pageable) {
        return leaveRequestRepository.searchLeaveRequests(empId, status, pageable);
    }

    public List<LeaveRequest> getMyLeaves(Long userId) {
        Employee emp = employeeService.getEmployeeByUserId(userId);
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(emp.getId());
    }

    public LeaveDTOs.Response toResponse(LeaveRequest r) {
        return LeaveDTOs.Response.builder()
            .id(r.getId())
            .employeeId(r.getEmployee().getId())
            .employeeName(r.getEmployee().getFullName())
            .employeeCode(r.getEmployee().getEmployeeId())
            .leaveTypeId(r.getLeaveType().getId())
            .leaveTypeName(r.getLeaveType().getName())
            .startDate(r.getStartDate())
            .endDate(r.getEndDate())
            .totalDays(r.getTotalDays())
            .isHalfDay(r.getIsHalfDay())
            .halfDayType(r.getHalfDayType() != null ? r.getHalfDayType().name() : null)
            .reason(r.getReason())
            .status(r.getStatus().name())
            .managerName(r.getManager() != null ? r.getManager().getFullName() : null)
            .managerActionAt(r.getManagerActionAt())
            .managerComment(r.getManagerComment())
            .hrName(r.getHr() != null ? r.getHr().getFullName() : null)
            .hrActionAt(r.getHrActionAt())
            .hrComment(r.getHrComment())
            .createdAt(r.getCreatedAt())
            .build();
    }

    private void revertPendingBalance(LeaveRequest req) {
        leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
            req.getEmployee().getId(), req.getLeaveType().getId(), LocalDate.now().getYear())
            .ifPresent(balance -> {
                balance.setPendingDays(balance.getPendingDays().subtract(req.getTotalDays()));
                leaveBalanceRepository.save(balance);
            });
    }
}
