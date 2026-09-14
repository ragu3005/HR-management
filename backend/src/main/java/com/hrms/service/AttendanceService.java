package com.hrms.service;

import com.hrms.dto.AttendanceDTOs;
import com.hrms.model.Attendance;
import com.hrms.model.Employee;
import com.hrms.model.Organization;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeService employeeService;
    private final OrganizationRepository orgRepository;
    private final AuditLogService auditLogService;

    public EmployeeService getEmployeeService() {
        return employeeService;
    }

    @Transactional
    public Attendance checkIn(Long userId, String ipAddress) {
        Employee emp = employeeService.getEmployeeByUserId(userId);
        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(emp.getId(), today)) {
            throw new RuntimeException("Already checked in for today");
        }

        Organization org = orgRepository.findFirstByOrderByIdAsc().orElse(null);
        LocalTime workStart = org != null ? parseTimeSafely(org.getWorkStartTime(), LocalTime.of(9, 0)) : LocalTime.of(9, 0);
        LocalTime workStartWithGrace = workStart.plusMinutes(org != null && org.getLateCheckInMins() != null ? org.getLateCheckInMins() : 15);
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        Attendance.AttendanceStatus status = currentTime.isAfter(workStartWithGrace)
            ? Attendance.AttendanceStatus.LATE : Attendance.AttendanceStatus.PRESENT;

        Attendance attendance = Attendance.builder()
            .employee(emp)
            .attendanceDate(today)
            .checkInTime(now)
            .status(status)
            .totalWorkingMinutes(0)
            .overtimeMinutes(0)
            .build();

        Attendance saved = attendanceRepository.save(attendance);
        auditLogService.log(userId, emp.getOfficialEmail(), "CHECK_IN",
            "ATTENDANCE", "Check-in at " + now, saved.getId(), "ATTENDANCE", ipAddress);
        return saved;
    }

    @Transactional
    public Attendance checkOut(Long userId, String ipAddress) {
        Employee emp = employeeService.getEmployeeByUserId(userId);
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
            .findByEmployeeIdAndAttendanceDate(emp.getId(), today)
            .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out for today");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);

        // Calculate working minutes
        long workingMinutes = Duration.between(attendance.getCheckInTime(), now).toMinutes();
        attendance.setTotalWorkingMinutes((int) workingMinutes);

        // Calculate overtime (standard 8 hours = 480 minutes)
        Organization org = orgRepository.findFirstByOrderByIdAsc().orElse(null);
        int threshold = org != null ? org.getOvertimeThresholdMins() : 480;
        int overtime = (int) Math.max(0, workingMinutes - threshold);
        attendance.setOvertimeMinutes(overtime);

        // Determine half-day
        if (workingMinutes < 240) {
            attendance.setStatus(Attendance.AttendanceStatus.HALF_DAY);
        }

        Attendance saved = attendanceRepository.save(attendance);
        auditLogService.log(userId, emp.getOfficialEmail(), "CHECK_OUT",
            "ATTENDANCE", "Check-out at " + now + " (" + workingMinutes + " min)", saved.getId(), "ATTENDANCE", ipAddress);
        return saved;
    }

    public AttendanceDTOs.TodayStatus getTodayStatus(Long userId) {
        Employee emp = employeeService.getEmployeeByUserId(userId);
        Optional<Attendance> opt = attendanceRepository
            .findByEmployeeIdAndAttendanceDate(emp.getId(), LocalDate.now());

        if (opt.isEmpty()) {
            return AttendanceDTOs.TodayStatus.builder()
                .checkedIn(false).checkedOut(false).build();
        }

        Attendance a = opt.get();
        return AttendanceDTOs.TodayStatus.builder()
            .checkedIn(true)
            .checkedOut(a.getCheckOutTime() != null)
            .checkInTime(a.getCheckInTime())
            .checkOutTime(a.getCheckOutTime())
            .totalWorkingMinutes(a.getTotalWorkingMinutes())
            .totalWorkingHours(a.getTotalWorkingHours())
            .status(a.getStatus().name())
            .build();
    }

    public Page<Attendance> searchAttendance(Long employeeId, Long deptId,
                                              LocalDate start, LocalDate end,
                                              Attendance.AttendanceStatus status, Pageable pageable) {
        return attendanceRepository.searchAttendance(employeeId, deptId, start, end, status, pageable);
    }

    public AttendanceDTOs.Response toResponse(Attendance a) {
        return AttendanceDTOs.Response.builder()
            .id(a.getId())
            .employeeId(a.getEmployee().getId())
            .employeeName(a.getEmployee().getFullName())
            .employeeCode(a.getEmployee().getEmployeeId())
            .attendanceDate(a.getAttendanceDate())
            .checkInTime(a.getCheckInTime())
            .checkOutTime(a.getCheckOutTime())
            .totalWorkingMinutes(a.getTotalWorkingMinutes())
            .totalWorkingHours(a.getTotalWorkingHours())
            .overtimeMinutes(a.getOvertimeMinutes())
            .status(a.getStatus().name())
            .notes(a.getNotes())
            .createdAt(a.getCreatedAt())
            .build();
    }

    private LocalTime parseTimeSafely(String timeStr, LocalTime defaultTime) {
        if (timeStr == null || timeStr.isBlank()) return defaultTime;
        try {
            timeStr = timeStr.trim();
            if (timeStr.length() == 5) return LocalTime.parse(timeStr);
            if (timeStr.length() >= 8) return LocalTime.parse(timeStr.substring(0, 8));
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return defaultTime;
        }
    }
}
