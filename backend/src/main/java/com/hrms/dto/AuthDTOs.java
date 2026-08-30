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
}
