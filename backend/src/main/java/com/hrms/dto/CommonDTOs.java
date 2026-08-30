package com.hrms.dto;

import lombok.*;
import java.time.LocalDateTime;

public class CommonDTOs {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder().success(true).message(message).data(data).build();
        }

        public static <T> ApiResponse<T> success(T data) {
            return success("Success", data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder().success(false).message(message).build();
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PageResponse<T> {
        private java.util.List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DashboardStats {
        private long totalEmployees;
        private long activeEmployees;
        private long presentToday;
        private long absentToday;
        private long onLeaveToday;
        private long pendingLeaveRequests;
        private long newEmployeesThisMonth;
        private long totalDepartments;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NotificationDTO {
        private Long id;
        private String title;
        private String message;
        private String type;
        private Long referenceId;
        private Boolean isRead;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AnnouncementDTO {
        private Long id;
        private String title;
        private String content;
        private String priority;
        private Boolean isPublished;
        private LocalDateTime publishedAt;
        private String createdByName;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DepartmentDTO {
        private Long id;
        private String deptCode;
        private String name;
        private String description;
        private Long managerId;
        private String managerName;
        private Boolean isActive;
        private long employeeCount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DesignationDTO {
        private Long id;
        private String name;
        private Long departmentId;
        private String departmentName;
        private String description;
        private Boolean isActive;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HolidayDTO {
        private Long id;
        private String name;
        private java.time.LocalDate holidayDate;
        private String type;
        private String description;
        private Integer year;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DocumentDTO {
        private Long id;
        private Long employeeId;
        private String documentName;
        private String documentCategory;
        private String originalFileName;
        private String fileType;
        private Long fileSize;
        private String fileSizeFormatted;
        private String uploadedByName;
        private LocalDateTime uploadedAt;
        private LocalDateTime updatedAt;
    }
}
