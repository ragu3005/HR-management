package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.dto.EmployeeDTOs;
import com.hrms.model.Employee;
import com.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.PageResponse<EmployeeDTOs.DirectoryCard>>> getDirectory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long designationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<Employee> result = employeeService.searchEmployees(search, departmentId, designationId, Employee.EmploymentStatus.ACTIVE, pageable);

        CommonDTOs.PageResponse<EmployeeDTOs.DirectoryCard> pageResponse = CommonDTOs.PageResponse.<EmployeeDTOs.DirectoryCard>builder()
                .content(result.getContent().stream().map(employeeService::toDirectoryCard).collect(Collectors.toList()))
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(pageResponse));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<CommonDTOs.ApiResponse<EmployeeDTOs.PublicProfile>> getPublicProfile(
            @PathVariable String employeeId) {
        try {
            Long id = Long.parseLong(employeeId);
            Employee emp = employeeService.getById(id);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success(employeeService.toPublicProfile(emp)));
        } catch (NumberFormatException e) {
            // Check if string code e.g. EMP001
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
