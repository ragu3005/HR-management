package com.hrms.controller;

import com.hrms.dto.AuthDTOs;
import com.hrms.dto.CommonDTOs;
import com.hrms.security.UserPrincipal;
import com.hrms.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<CommonDTOs.ApiResponse<AuthDTOs.LoginResponse>> login(
            @RequestBody AuthDTOs.LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ip = getClientIp(httpRequest);
            AuthDTOs.LoginResponse response = authService.login(request, ip);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(CommonDTOs.ApiResponse.error("Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> logout() {
        // JWT is stateless – logout is handled client-side by deleting the token
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AuthDTOs.ChangePasswordRequest request) {
        try {
            authService.changePassword(principal.getId(), request);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Password changed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<CommonDTOs.ApiResponse<AuthDTOs.LoginResponse>> me(
            @AuthenticationPrincipal UserPrincipal principal) {
        String role = principal.getAuthorities().stream()
            .filter(a -> a.getAuthority().startsWith("ROLE_"))
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .findFirst().orElse("EMPLOYEE");

        AuthDTOs.LoginResponse response = AuthDTOs.LoginResponse.builder()
            .userId(principal.getId())
            .email(principal.getEmail())
            .username(principal.getUsername())
            .role(role)
            .build();

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(response));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) return xfHeader.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
