package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.model.Department;
import com.hrms.model.Designation;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.DesignationRepository;
import com.hrms.security.UserPrincipal;
import com.hrms.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<CommonDTOs.ApiResponse<List<CommonDTOs.DesignationDTO>>> getAllDesignations(
            @RequestParam(required = false) Long departmentId) {
        List<Designation> list = (departmentId != null) ?
                designationRepository.findByDepartmentId(departmentId) :
                designationRepository.findAll();

        List<CommonDTOs.DesignationDTO> dtos = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(dtos));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('designation:create')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.DesignationDTO>> createDesignation(
            @RequestBody CommonDTOs.DesignationDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (designationRepository.existsByName(request.getName())) {
                return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error("Designation name already exists"));
            }

            Department dept = null;
            if (request.getDepartmentId() != null) {
                dept = departmentRepository.findById(request.getDepartmentId()).orElse(null);
            }

            Designation desig = Designation.builder()
                    .name(request.getName())
                    .department(dept)
                    .description(request.getDescription())
                    .isActive(true)
                    .build();

            Designation saved = designationRepository.save(desig);
            auditLogService.log(principal.getId(), principal.getEmail(), "DESIGNATION_CREATED",
                    "DESIGNATION", "Created designation: " + saved.getName(), saved.getId(), "DESIGNATION", null);

            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Designation created successfully", toDto(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('designation:update')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.DesignationDTO>> updateDesignation(
            @PathVariable Long id,
            @RequestBody CommonDTOs.DesignationDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Designation desig = designationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Designation not found"));

            if (request.getName() != null) desig.setName(request.getName());
            if (request.getDescription() != null) desig.setDescription(request.getDescription());
            if (request.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(request.getDepartmentId()).orElse(null);
                desig.setDepartment(dept);
            }
            if (request.getIsActive() != null) desig.setIsActive(request.getIsActive());

            Designation saved = designationRepository.save(desig);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Designation updated successfully", toDto(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('designation:delete')")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> deleteDesignation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Designation desig = designationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Designation not found"));
            desig.setIsActive(false);
            designationRepository.save(desig);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Designation deactivated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    private CommonDTOs.DesignationDTO toDto(Designation d) {
        return CommonDTOs.DesignationDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .departmentId(d.getDepartment() != null ? d.getDepartment().getId() : null)
                .departmentName(d.getDepartment() != null ? d.getDepartment().getName() : null)
                .description(d.getDescription())
                .isActive(d.getIsActive())
                .build();
    }
}
