package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.dto.EmployeeDTOs;
import com.hrms.model.Employee;
import com.hrms.security.UserPrincipal;
import com.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('employee:create')")
    public ResponseEntity<CommonDTOs.ApiResponse<EmployeeDTOs.Response>> createEmployee(
            @RequestBody EmployeeDTOs.CreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Employee emp = employeeService.createEmployee(request, principal.getId(), principal.getEmail());
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Employee created successfully",
                employeeService.toResponse(emp)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('employee:read_all')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.PageResponse<EmployeeDTOs.Response>>> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long designationId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Employee.EmploymentStatus empStatus = null;
        if (status != null && !status.isBlank()) {
            try { empStatus = Employee.EmploymentStatus.valueOf(status); } catch (Exception ignored) {}
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Employee> result = employeeService.searchEmployees(search, departmentId, designationId, empStatus, pageable);

        CommonDTOs.PageResponse<EmployeeDTOs.Response> pageResponse = CommonDTOs.PageResponse.<EmployeeDTOs.Response>builder()
            .content(result.getContent().stream().map(employeeService::toResponse).collect(Collectors.toList()))
            .page(result.getNumber())
            .size(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .last(result.isLast())
            .build();

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(pageResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<CommonDTOs.ApiResponse<EmployeeDTOs.Response>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Employee emp = employeeService.getEmployeeByUserId(principal.getId());
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success(employeeService.toResponse(emp)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:read') or hasAuthority('employee:read_all')")
    public ResponseEntity<CommonDTOs.ApiResponse<EmployeeDTOs.Response>> getEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Employee emp = employeeService.getById(id);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success(employeeService.toResponse(emp)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:update')")
    public ResponseEntity<CommonDTOs.ApiResponse<EmployeeDTOs.Response>> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeDTOs.UpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Employee emp = employeeService.updateEmployee(id, request, principal.getId(), principal.getEmail());
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Updated successfully",
                employeeService.toResponse(emp)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('employee:update')")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> activateEmployee(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        employeeService.toggleActive(id, true, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Employee activated", null));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('employee:update')")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> deactivateEmployee(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        employeeService.toggleActive(id, false, principal.getId(), principal.getEmail());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Employee deactivated", null));
    }
}
