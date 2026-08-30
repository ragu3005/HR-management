package com.hrms.controller;

import com.hrms.dto.AttendanceDTOs;
import com.hrms.dto.CommonDTOs;
import com.hrms.model.Attendance;
import com.hrms.security.UserPrincipal;
import com.hrms.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAuthority('attendance:check_in')")
    public ResponseEntity<CommonDTOs.ApiResponse<AttendanceDTOs.Response>> checkIn(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request) {
        try {
            String ip = getClientIp(request);
            Attendance attendance = attendanceService.checkIn(principal.getId(), ip);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Checked in successfully", attendanceService.toResponse(attendance)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAuthority('attendance:check_out')")
    public ResponseEntity<CommonDTOs.ApiResponse<AttendanceDTOs.Response>> checkOut(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request) {
        try {
            String ip = getClientIp(request);
            Attendance attendance = attendanceService.checkOut(principal.getId(), ip);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Checked out successfully", attendanceService.toResponse(attendance)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-status")
    public ResponseEntity<CommonDTOs.ApiResponse<AttendanceDTOs.TodayStatus>> getTodayStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success(attendanceService.getTodayStatus(principal.getId())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('attendance:read_all')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.PageResponse<AttendanceDTOs.Response>>> getAttendanceRecords(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Attendance.AttendanceStatus attStatus = null;
        if (status != null && !status.isBlank()) {
            try { attStatus = Attendance.AttendanceStatus.valueOf(status); } catch (Exception ignored) {}
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        Page<Attendance> result = attendanceService.searchAttendance(employeeId, departmentId, startDate, endDate, attStatus, pageable);

        CommonDTOs.PageResponse<AttendanceDTOs.Response> pageResponse = CommonDTOs.PageResponse.<AttendanceDTOs.Response>builder()
                .content(result.getContent().stream().map(attendanceService::toResponse).collect(Collectors.toList()))
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(pageResponse));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) return xfHeader.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
