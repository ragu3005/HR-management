package com.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CheckInRequest {
        private String notes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CheckOutRequest {
        private String notes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private LocalDate attendanceDate;
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private Integer totalWorkingMinutes;
        private Double totalWorkingHours;
        private Integer overtimeMinutes;
        private String status;
        private String notes;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TodayStatus {
        private boolean checkedIn;
        private boolean checkedOut;
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private Integer totalWorkingMinutes;
        private Double totalWorkingHours;
        private String status;
    }
}
