package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.model.Department;
import com.hrms.model.Employee;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.AuditLogService;
import com.hrms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<CommonDTOs.ApiResponse<List<CommonDTOs.DepartmentDTO>>> getAllDepartments() {
        List<CommonDTOs.DepartmentDTO> dtos = departmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(dtos));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('department:create')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.DepartmentDTO>> createDepartment(
            @RequestBody CommonDTOs.DepartmentDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (departmentRepository.existsByName(request.getName())) {
                return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error("Department name already exists"));
            }
            if (departmentRepository.existsByDeptCode(request.getDeptCode())) {
                return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error("Department code already exists"));
            }

            Employee manager = null;
            if (request.getManagerId() != null) {
                manager = employeeRepository.findById(request.getManagerId()).orElse(null);
            }

            Department dept = Department.builder()
                    .name(request.getName())
                    .deptCode(request.getDeptCode())
                    .description(request.getDescription())
                    .manager(manager)
                    .isActive(true)
                    .build();

            Department saved = departmentRepository.save(dept);
            auditLogService.log(principal.getId(), principal.getEmail(), "DEPARTMENT_CREATED",
                    "DEPARTMENT", "Created department: " + saved.getName(), saved.getId(), "DEPARTMENT", null);

            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Department created successfully", toDto(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('department:update')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.DepartmentDTO>> updateDepartment(
            @PathVariable Long id,
            @RequestBody CommonDTOs.DepartmentDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Department dept = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Department not found"));

            if (request.getName() != null) dept.setName(request.getName());
            if (request.getDescription() != null) dept.setDescription(request.getDescription());
            if (request.getManagerId() != null) {
                Employee manager = employeeRepository.findById(request.getManagerId()).orElse(null);
                dept.setManager(manager);
            }
            if (request.getIsActive() != null) dept.setIsActive(request.getIsActive());

            Department saved = departmentRepository.save(dept);
            auditLogService.log(principal.getId(), principal.getEmail(), "DEPARTMENT_UPDATED",
                    "DEPARTMENT", "Updated department: " + saved.getName(), saved.getId(), "DEPARTMENT", null);

            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Department updated successfully", toDto(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('department:delete')")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> deleteDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Department dept = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            dept.setIsActive(false);
            departmentRepository.save(dept);

            auditLogService.log(principal.getId(), principal.getEmail(), "DEPARTMENT_DEACTIVATED",
                    "DEPARTMENT", "Deactivated department: " + dept.getName(), id, "DEPARTMENT", null);

            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Department deactivated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    private CommonDTOs.DepartmentDTO toDto(Department d) {
        long count = employeeRepository.findByDepartmentIdAndIsActive(d.getId(), true).size();
        return CommonDTOs.DepartmentDTO.builder()
                .id(d.getId())
                .deptCode(d.getDeptCode())
                .name(d.getName())
                .description(d.getDescription())
                .managerId(d.getManager() != null ? d.getManager().getId() : null)
                .managerName(d.getManager() != null ? d.getManager().getFullName() : null)
                .isActive(d.getIsActive())
                .employeeCount(count)
                .build();
    }
}
