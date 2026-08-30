package com.hrms.service;

import com.hrms.dto.EmployeeDTOs;
import com.hrms.model.*;
import com.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public Employee createEmployee(EmployeeDTOs.CreateRequest req, Long createdByUserId, String createdByEmail) {
        // Generate unique employee ID
        String empId = generateEmployeeId();

        // Create user account
        User user = User.builder()
            .username(req.getUsername() != null ? req.getUsername() : req.getOfficialEmail().split("@")[0])
            .email(req.getOfficialEmail())
            .password(passwordEncoder.encode(req.getPassword() != null ? req.getPassword() : "Employee@123"))
            .isActive(true)
            .build();

        // Assign employee role
        roleRepository.findByName("EMPLOYEE").ifPresent(role -> user.getRoles().add(role));
        User savedUser = userRepository.save(user);

        // Resolve relations
        Department dept = req.getDepartmentId() != null ?
            departmentRepository.findById(req.getDepartmentId()).orElse(null) : null;
        Designation desig = req.getDesignationId() != null ?
            designationRepository.findById(req.getDesignationId()).orElse(null) : null;
        Employee manager = req.getReportingManagerId() != null ?
            employeeRepository.findById(req.getReportingManagerId()).orElse(null) : null;

        Employee employee = Employee.builder()
            .employeeId(empId)
            .user(savedUser)
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .personalEmail(req.getPersonalEmail())
            .officialEmail(req.getOfficialEmail())
            .phone(req.getPhone())
            .dateOfBirth(req.getDateOfBirth())
            .gender(req.getGender() != null ? Employee.Gender.valueOf(req.getGender()) : null)
            .address(req.getAddress())
            .department(dept)
            .designation(desig)
            .reportingManager(manager)
            .joiningDate(req.getJoiningDate())
            .employmentType(req.getEmploymentType() != null ?
                Employee.EmploymentType.valueOf(req.getEmploymentType()) : Employee.EmploymentType.FULL_TIME)
            .workLocation(req.getWorkLocation())
            .professionalBio(req.getProfessionalBio())
            .isActive(true)
            .createdBy(createdByUserId)
            .build();

        Employee saved = employeeRepository.save(employee);

        // Send onboarding notification
        notificationService.createNotification(savedUser, "Welcome to HRMS Pro!",
            "Your account has been created. Employee ID: " + empId,
            Notification.NotificationType.EMPLOYEE_ONBOARDING, saved.getId());

        auditLogService.log(createdByUserId, createdByEmail, "EMPLOYEE_CREATED",
            "EMPLOYEE", "Created employee: " + empId + " - " + req.getFirstName(), saved.getId(), "EMPLOYEE", null);

        return saved;
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeDTOs.UpdateRequest req, Long updatedByUserId, String updatedByEmail) {
        Employee emp = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found: " + id));

        if (req.getFirstName() != null) emp.setFirstName(req.getFirstName());
        if (req.getLastName() != null) emp.setLastName(req.getLastName());
        if (req.getPersonalEmail() != null) emp.setPersonalEmail(req.getPersonalEmail());
        if (req.getPhone() != null) emp.setPhone(req.getPhone());
        if (req.getDateOfBirth() != null) emp.setDateOfBirth(req.getDateOfBirth());
        if (req.getGender() != null) emp.setGender(Employee.Gender.valueOf(req.getGender()));
        if (req.getAddress() != null) emp.setAddress(req.getAddress());
        if (req.getDepartmentId() != null)
            emp.setDepartment(departmentRepository.findById(req.getDepartmentId()).orElse(null));
        if (req.getDesignationId() != null)
            emp.setDesignation(designationRepository.findById(req.getDesignationId()).orElse(null));
        if (req.getReportingManagerId() != null)
            emp.setReportingManager(employeeRepository.findById(req.getReportingManagerId()).orElse(null));
        if (req.getEmploymentType() != null)
            emp.setEmploymentType(Employee.EmploymentType.valueOf(req.getEmploymentType()));
        if (req.getEmploymentStatus() != null)
            emp.setEmploymentStatus(Employee.EmploymentStatus.valueOf(req.getEmploymentStatus()));
        if (req.getWorkLocation() != null) emp.setWorkLocation(req.getWorkLocation());
        if (req.getProfessionalBio() != null) emp.setProfessionalBio(req.getProfessionalBio());

        Employee updated = employeeRepository.save(emp);
        auditLogService.log(updatedByUserId, updatedByEmail, "EMPLOYEE_UPDATED",
            "EMPLOYEE", "Updated employee: " + emp.getEmployeeId(), id, "EMPLOYEE", null);
        return updated;
    }

    public Page<Employee> searchEmployees(String search, Long deptId, Long desigId,
                                          Employee.EmploymentStatus status, Pageable pageable) {
        return employeeRepository.searchEmployees(search, deptId, desigId, status, pageable);
    }

    public Employee getById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
    }

    public Employee getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Employee profile not found for user: " + userId));
    }

    @Transactional
    public void toggleActive(Long id, boolean active, Long userId, String email) {
        Employee emp = getById(id);
        emp.setIsActive(active);
        emp.setEmploymentStatus(active ? Employee.EmploymentStatus.ACTIVE : Employee.EmploymentStatus.INACTIVE);
        if (emp.getUser() != null) {
            emp.getUser().setIsActive(active);
            userRepository.save(emp.getUser());
        }
        employeeRepository.save(emp);
        auditLogService.log(userId, email,
            active ? "EMPLOYEE_ACTIVATED" : "EMPLOYEE_DEACTIVATED",
            "EMPLOYEE", "Employee " + emp.getEmployeeId() + " status changed", id, "EMPLOYEE", null);
    }

    public EmployeeDTOs.Response toResponse(Employee e) {
        return EmployeeDTOs.Response.builder()
            .id(e.getId())
            .employeeId(e.getEmployeeId())
            .userId(e.getUser() != null ? e.getUser().getId() : null)
            .firstName(e.getFirstName())
            .lastName(e.getLastName())
            .fullName(e.getFullName())
            .personalEmail(e.getPersonalEmail())
            .officialEmail(e.getOfficialEmail())
            .phone(e.getPhone())
            .dateOfBirth(e.getDateOfBirth())
            .gender(e.getGender() != null ? e.getGender().name() : null)
            .address(e.getAddress())
            .profilePhotoUrl(e.getProfilePhotoPath() != null ? "/api/files/" + e.getProfilePhotoPath() : null)
            .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
            .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
            .designationId(e.getDesignation() != null ? e.getDesignation().getId() : null)
            .designationName(e.getDesignation() != null ? e.getDesignation().getName() : null)
            .reportingManagerId(e.getReportingManager() != null ? e.getReportingManager().getId() : null)
            .reportingManagerName(e.getReportingManager() != null ? e.getReportingManager().getFullName() : null)
            .joiningDate(e.getJoiningDate())
            .employmentType(e.getEmploymentType() != null ? e.getEmploymentType().name() : null)
            .employmentStatus(e.getEmploymentStatus() != null ? e.getEmploymentStatus().name() : null)
            .workLocation(e.getWorkLocation())
            .professionalBio(e.getProfessionalBio())
            .isActive(e.getIsActive())
            .createdAt(e.getCreatedAt())
            .build();
    }

    public EmployeeDTOs.PublicProfile toPublicProfile(Employee e) {
        return EmployeeDTOs.PublicProfile.builder()
            .id(e.getId())
            .employeeId(e.getEmployeeId())
            .fullName(e.getFullName())
            .profilePhotoUrl(e.getProfilePhotoPath() != null ? "/api/files/" + e.getProfilePhotoPath() : null)
            .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
            .designationName(e.getDesignation() != null ? e.getDesignation().getName() : null)
            .reportingManagerName(e.getReportingManager() != null ? e.getReportingManager().getFullName() : null)
            .workLocation(e.getWorkLocation())
            .professionalBio(e.getProfessionalBio())
            .build();
    }

    public EmployeeDTOs.DirectoryCard toDirectoryCard(Employee e) {
        return EmployeeDTOs.DirectoryCard.builder()
            .id(e.getId())
            .employeeId(e.getEmployeeId())
            .fullName(e.getFullName())
            .profilePhotoUrl(e.getProfilePhotoPath() != null ? "/api/files/" + e.getProfilePhotoPath() : null)
            .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
            .designationName(e.getDesignation() != null ? e.getDesignation().getName() : null)
            .officialEmail(e.getOfficialEmail())
            .build();
    }

    private String generateEmployeeId() {
        String last = employeeRepository.findLastEmployeeId();
        if (last == null) return "EMP001";
        try {
            int num = Integer.parseInt(last.replace("EMP", ""));
            return String.format("EMP%03d", num + 1);
        } catch (NumberFormatException e) {
            return "EMP" + (employeeRepository.count() + 1);
        }
    }
}
