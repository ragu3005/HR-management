package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.model.Organization;
import com.hrms.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    @GetMapping
    public ResponseEntity<CommonDTOs.ApiResponse<Organization>> getOrganization() {
        Organization org = organizationRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> organizationRepository.save(Organization.builder()
                        .name("HRMS Pro Corporation")
                        .email("info@hrmspro.com")
                        .phone("+91-9999999999")
                        .address("123 Business Park, Chennai, India")
                        .build()));
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(org));
    }

    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<Organization>> updateOrganization(
            @RequestBody Organization updateReq) {
        Organization org = organizationRepository.findFirstByOrderByIdAsc()
                .orElse(Organization.builder().build());

        if (updateReq.getName() != null) org.setName(updateReq.getName());
        if (updateReq.getEmail() != null) org.setEmail(updateReq.getEmail());
        if (updateReq.getPhone() != null) org.setPhone(updateReq.getPhone());
        if (updateReq.getAddress() != null) org.setAddress(updateReq.getAddress());
        if (updateReq.getWebsite() != null) org.setWebsite(updateReq.getWebsite());
        if (updateReq.getWorkStartTime() != null) org.setWorkStartTime(updateReq.getWorkStartTime());
        if (updateReq.getWorkEndTime() != null) org.setWorkEndTime(updateReq.getWorkEndTime());
        if (updateReq.getWorkingDays() != null) org.setWorkingDays(updateReq.getWorkingDays());
        if (updateReq.getLateCheckInMins() != null) org.setLateCheckInMins(updateReq.getLateCheckInMins());
        if (updateReq.getOvertimeThresholdMins() != null) org.setOvertimeThresholdMins(updateReq.getOvertimeThresholdMins());

        Organization saved = organizationRepository.save(org);
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Organization updated successfully", saved));
    }
}
