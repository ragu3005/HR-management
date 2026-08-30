package com.hrms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApplyRequest {
        private Long leaveTypeId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isHalfDay;
        private String halfDayType;
        private String reason;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActionRequest {
        private String comment;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private Long leaveTypeId;
        private String leaveTypeName;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalDays;
        private Boolean isHalfDay;
        private String halfDayType;
        private String reason;
        private String status;
        private String managerName;
        private LocalDateTime managerActionAt;
        private String managerComment;
        private String hrName;
        private LocalDateTime hrActionAt;
        private String hrComment;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BalanceResponse {
        private Long leaveTypeId;
        private String leaveTypeName;
        private String leaveTypeCode;
        private BigDecimal totalDays;
        private BigDecimal usedDays;
        private BigDecimal pendingDays;
        private BigDecimal remainingDays;
    }
}
