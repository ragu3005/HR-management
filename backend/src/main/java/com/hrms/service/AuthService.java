package com.hrms.service;

import com.hrms.dto.AuthDTOs;
import com.hrms.model.User;
import com.hrms.repository.UserRepository;
import com.hrms.security.JwtUtil;
import com.hrms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeService employeeService;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthDTOs.LoginResponse login(AuthDTOs.LoginRequest request, String ipAddress) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtUtil.generateToken(auth);

        // Update last login
        userRepository.findById(principal.getId()).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });

        // Get employee info for response
        String fullName = "";
        Long empId = null;
        String role = principal.getAuthorities().stream()
            .filter(a -> a.getAuthority().startsWith("ROLE_"))
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .findFirst().orElse("EMPLOYEE");

        try {
            var emp = employeeService.getEmployeeByUserId(principal.getId());
            if (emp != null) {
                fullName = emp.getFullName();
                empId = emp.getId();
            }
        } catch (Exception ignored) {}

        auditLogService.log(principal.getId(), principal.getEmail(), "USER_LOGIN",
            "AUTH", "User logged in: " + request.getEmail(), null, null, ipAddress);

        return AuthDTOs.LoginResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .userId(principal.getId())
            .email(principal.getEmail())
            .username(principal.getUsername())
            .role(role)
            .fullName(fullName)
            .employeeId(empId)
            .build();
    }

    @Transactional
    public void changePassword(Long userId, AuthDTOs.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
