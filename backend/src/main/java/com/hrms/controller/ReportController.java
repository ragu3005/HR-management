package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.model.Employee;
import com.hrms.model.LeaveRequest;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.DashboardStats>> getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        long presentToday = attendanceRepository.countPresentByDate(today);
        long onLeaveToday = attendanceRepository.countOnLeaveByDate(today);
        long absentToday = Math.max(0, activeEmployees - presentToday - onLeaveToday);
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.PENDING) +
                leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.MANAGER_APPROVED);
        long newEmployees = employeeRepository.countNewJoiners(startOfMonth);
        long totalDepartments = departmentRepository.count();

        CommonDTOs.DashboardStats stats = CommonDTOs.DashboardStats.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .onLeaveToday(onLeaveToday)
                .pendingLeaveRequests(pendingLeaves)
                .newEmployeesThisMonth(newEmployees)
                .totalDepartments(totalDepartments)
                .build();

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(stats));
    }

    @GetMapping("/department-distribution")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<List<Map<String, Object>>>> getDepartmentDistribution() {
        List<Map<String, Object>> result = departmentRepository.findAll().stream()
                .map(dept -> {
                    long count = employeeRepository.findByDepartmentIdAndIsActive(dept.getId(), true).size();
                    Map<String, Object> map = new HashMap<>();
                    map.put("departmentId", dept.getId());
                    map.put("departmentName", dept.getName());
                    map.put("deptCode", dept.getDeptCode());
                    map.put("employeeCount", count);
                    return map;
                })
                .toList();

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(result));
    }

    @GetMapping("/attendance-trends")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<List<Map<String, Object>>>> getAttendanceTrends(
            @RequestParam(defaultValue = "7") int days) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> trends = new java.util.ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long present = attendanceRepository.countPresentByDate(date);
            long onLeave = attendanceRepository.countOnLeaveByDate(date);
            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());
            point.put("present", present);
            point.put("onLeave", onLeave);
            trends.add(point);
        }

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(trends));
    }

    @GetMapping({"/headcount", "/headcount-summary"})
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<Map<String, Object>>> getHeadcountSummary() {
        long total = employeeRepository.count();
        long active = employeeRepository.countByEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        long inactive = employeeRepository.countByEmploymentStatus(Employee.EmploymentStatus.INACTIVE);
        long onNotice = employeeRepository.countByEmploymentStatus(Employee.EmploymentStatus.ON_NOTICE);
        long terminated = employeeRepository.countByEmploymentStatus(Employee.EmploymentStatus.TERMINATED);

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("active", active);
        data.put("inactive", inactive);
        data.put("onNotice", onNotice);
        data.put("terminated", terminated);
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(data));
    }

    @GetMapping("/attendance-summary")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<Map<String, Object>>> getAttendanceSummary() {
        LocalDate today = LocalDate.now();
        long present = attendanceRepository.countPresentByDate(today);
        long onLeave = attendanceRepository.countOnLeaveByDate(today);
        long active = employeeRepository.countByEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        long absent = Math.max(0, active - present - onLeave);

        Map<String, Object> data = new HashMap<>();
        data.put("presentToday", present);
        data.put("onLeaveToday", onLeave);
        data.put("absentToday", absent);
        data.put("totalActive", active);
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(data));
    }

    @GetMapping("/leave-summary")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<Map<String, Object>>> getLeaveSummary() {
        long pending = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.PENDING);
        long managerApproved = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.MANAGER_APPROVED);
        long approved = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.APPROVED);
        long rejected = leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.REJECTED);

        Map<String, Object> data = new HashMap<>();
        data.put("pending", pending);
        data.put("managerApproved", managerApproved);
        data.put("approved", approved);
        data.put("rejected", rejected);
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(data));
    }
}
