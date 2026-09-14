package com.hrms.dto;

import lombok.*;

// ============================================================
// Authentication DTOs
// ============================================================

public class AuthDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        @Builder.Default
        private String tokenType = "Bearer";
        private Long userId;
        private String email;
        private String username;
        private String role;
        private Long employeeId;
        private String fullName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ForgotPasswordRequest {
        private String email;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String phone;
        private Long departmentId;
        private Long designationId;
        private String role; // "EMPLOYEE", "MANAGER", "HR_ADMIN", etc. Defaults to EMPLOYEE
        private String gender; // "MALE", "FEMALE", "OTHER"
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RegistrationOptionsResponse {
        private java.util.List<DepartmentOption> departments;
        private java.util.List<RoleOption> roles;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DepartmentOption {
        private Long id;
        private String name;
        private String deptCode;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RoleOption {
        private String name;
        private String displayName;
        private String description;
    }
}

