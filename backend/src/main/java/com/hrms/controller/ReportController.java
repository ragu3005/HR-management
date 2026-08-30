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
}
