package com.hrms.service;

import com.hrms.dto.AuthDTOs;
import com.hrms.model.*;
import com.hrms.repository.*;
import com.hrms.security.JwtUtil;
import com.hrms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final RoleRepository roleRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeService employeeService;
    private final NotificationService notificationService;
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
    public AuthDTOs.LoginResponse register(AuthDTOs.RegisterRequest req, String ipAddress) {
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        if (req.getFirstName() == null || req.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (req.getLastName() == null || req.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }

        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered: " + email);
        }
        if (employeeRepository.findByOfficialEmail(email).isPresent()) {
            throw new RuntimeException("Official email is already registered: " + email);
        }

        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "");
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        // Determine Role (defaults to EMPLOYEE)
        String targetRole = (req.getRole() != null && !req.getRole().trim().isEmpty())
            ? req.getRole().trim().toUpperCase() : "EMPLOYEE";
        Role role = roleRepository.findByName(targetRole)
            .orElseGet(() -> roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default EMPLOYEE role not found")));

        // Create User entity
        java.util.Set<Role> userRoles = new java.util.HashSet<>();
        userRoles.add(role);

        User user = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(req.getPassword()))
            .isActive(true)
            .roles(userRoles)
            .build();
        User savedUser = userRepository.save(user);

        // Generate Employee ID
        String empId = generateEmployeeId();

        Department dept = req.getDepartmentId() != null ?
            departmentRepository.findById(req.getDepartmentId()).orElse(null) : null;
        Designation desig = req.getDesignationId() != null ?
            designationRepository.findById(req.getDesignationId()).orElse(null) : null;

        Employee.Gender gender = null;
        if (req.getGender() != null && !req.getGender().trim().isEmpty()) {
            try {
                gender = Employee.Gender.valueOf(req.getGender().trim().toUpperCase());
            } catch (Exception ignored) {}
        }

        // Create Employee entity
        Employee employee = Employee.builder()
            .employeeId(empId)
            .user(savedUser)
            .firstName(req.getFirstName().trim())
            .lastName(req.getLastName().trim())
            .officialEmail(email)
            .personalEmail(email)
            .phone(req.getPhone() != null ? req.getPhone().trim() : null)
            .gender(gender)
            .department(dept)
            .designation(desig)
            .joiningDate(LocalDate.now())
            .employmentType(Employee.EmploymentType.FULL_TIME)
            .employmentStatus(Employee.EmploymentStatus.ACTIVE)
            .isActive(true)
            .createdBy(savedUser.getId())
            .build();

        Employee savedEmp = employeeRepository.save(employee);

        // Initialize leave balances for all active leave types
        initializeLeaveBalances(savedEmp);

        // Send welcome notification
        try {
            notificationService.createNotification(savedUser, "Welcome to HRMS Pro!",
                "Your account has been registered successfully. Employee ID: " + empId,
                Notification.NotificationType.EMPLOYEE_ONBOARDING, savedEmp.getId());
        } catch (Exception e) {
            log.warn("Could not dispatch welcome notification: {}", e.getMessage());
        }

        // Audit Log
        try {
            auditLogService.log(savedUser.getId(), email, "USER_REGISTERED",
                "AUTH", "New user registered: " + email + " with role " + role.getName(), savedEmp.getId(), "EMPLOYEE", ipAddress);
        } catch (Exception e) {
            log.warn("Could not write audit log: {}", e.getMessage());
        }

        // Authenticate & generate JWT token for instant login
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, req.getPassword()));
        String token = jwtUtil.generateToken(auth);

        return AuthDTOs.LoginResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .userId(savedUser.getId())
            .email(savedUser.getEmail())
            .username(savedUser.getUsername())
            .role(role.getName())
            .fullName(savedEmp.getFullName())
            .employeeId(savedEmp.getId())
            .build();
    }

    public AuthDTOs.RegistrationOptionsResponse getRegistrationOptions() {
        List<AuthDTOs.DepartmentOption> deptOptions = departmentRepository.findAll().stream()
            .filter(d -> Boolean.TRUE.equals(d.getIsActive()))
            .map(d -> AuthDTOs.DepartmentOption.builder()
                .id(d.getId())
                .name(d.getName())
                .deptCode(d.getDeptCode())
                .build())
            .collect(Collectors.toList());

        List<AuthDTOs.RoleOption> roleOptions = roleRepository.findAll().stream()
            .filter(r -> !"SUPER_ADMIN".equalsIgnoreCase(r.getName())) // Exclude Super Admin from self-service registration
            .map(r -> AuthDTOs.RoleOption.builder()
                .name(r.getName())
                .displayName(r.getDisplayName())
                .description(r.getDescription())
                .build())
            .collect(Collectors.toList());

        return AuthDTOs.RegistrationOptionsResponse.builder()
            .departments(deptOptions)
            .roles(roleOptions)
            .build();
    }

    private void initializeLeaveBalances(Employee employee) {
        try {
            int currentYear = LocalDate.now().getYear();
            List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
            for (LeaveType lt : leaveTypes) {
                if (Boolean.TRUE.equals(lt.getIsActive())) {
                    boolean exists = leaveBalanceRepository
                        .findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), lt.getId(), currentYear)
                        .isPresent();
                    if (!exists) {
                        LeaveBalance balance = LeaveBalance.builder()
                            .employee(employee)
                            .leaveType(lt)
                            .year(currentYear)
                            .totalDays(BigDecimal.valueOf(lt.getMaxDaysPerYear() != null ? lt.getMaxDaysPerYear() : 0))
                            .usedDays(BigDecimal.ZERO)
                            .pendingDays(BigDecimal.ZERO)
                            .build();
                        leaveBalanceRepository.save(balance);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not initialize leave balances for employee {}: {}", employee.getId(), e.getMessage());
        }
    }

    private String generateEmployeeId() {
        String last = employeeRepository.findLastEmployeeId();
        if (last == null) return "EMP001";
        try {
            int num = Integer.parseInt(last.replace("EMP", ""));
            String nextId = String.format("EMP%03d", num + 1);
            while (employeeRepository.existsByEmployeeId(nextId)) {
                num++;
                nextId = String.format("EMP%03d", num + 1);
            }
            return nextId;
        } catch (Exception e) {
            return "EMP" + (employeeRepository.count() + 1);
        }
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

