package com.hrms.dto;

import com.hrms.model.Employee;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        private String firstName;
        private String lastName;
        private String personalEmail;
        private String officialEmail;
        private String phone;
        private LocalDate dateOfBirth;
        private String gender;
        private String address;
        private Long departmentId;
        private Long designationId;
        private Long reportingManagerId;
        private LocalDate joiningDate;
        private String employmentType;
        private String workLocation;
        private String professionalBio;
        // User account
        private String username;
        private String password;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String personalEmail;
        private String phone;
        private LocalDate dateOfBirth;
        private String gender;
        private String address;
        private Long departmentId;
        private Long designationId;
        private Long reportingManagerId;
        private String employmentType;
        private String employmentStatus;
        private String workLocation;
        private String professionalBio;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String employeeId;
        private Long userId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String personalEmail;
        private String officialEmail;
        private String phone;
        private LocalDate dateOfBirth;
        private String gender;
        private String address;
        private String profilePhotoUrl;
        private Long departmentId;
        private String departmentName;
        private Long designationId;
        private String designationName;
        private Long reportingManagerId;
        private String reportingManagerName;
        private LocalDate joiningDate;
        private String employmentType;
        private String employmentStatus;
        private String workLocation;
        private String professionalBio;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    // Public profile - hides sensitive information
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PublicProfile {
        private Long id;
        private String employeeId;
        private String fullName;
        private String profilePhotoUrl;
        private String departmentName;
        private String designationName;
        private String reportingManagerName;
        private String workLocation;
        private String professionalBio;
    }

    // Compact card for directory listing
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DirectoryCard {
        private Long id;
        private String employeeId;
        private String fullName;
        private String profilePhotoUrl;
        private String departmentName;
        private String designationName;
        private String officialEmail;
    }
}
