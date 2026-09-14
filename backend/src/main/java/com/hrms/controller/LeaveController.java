package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.dto.LeaveDTOs;
import com.hrms.model.LeaveRequest;
import com.hrms.security.UserPrincipal;
import com.hrms.service.EmployeeService;
import com.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeService employeeService;

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('leave:apply')")
    public ResponseEntity<CommonDTOs.ApiResponse<LeaveDTOs.Response>> applyLeave(
            @RequestBody LeaveDTOs.ApplyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            LeaveRequest leaveRequest = leaveService.applyLeave(principal.getId(), request);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Leave applied successfully", leaveService.toResponse(leaveRequest)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('leave:read_own')")
    public ResponseEntity<CommonDTOs.ApiResponse<List<LeaveDTOs.Response>>> getMyLeaves(
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            List<LeaveDTOs.Response> responses = leaveService.getMyLeaves(principal.getId()).stream()
                    .map(leaveService::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/balances")
    @PreAuthorize("hasAuthority('leave:read_own')")
    public ResponseEntity<CommonDTOs.ApiResponse<List<LeaveDTOs.BalanceResponse>>> getMyBalances(
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success(leaveService.getLeaveBalances(principal.getId())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/types")
    public ResponseEntity<CommonDTOs.ApiResponse<List<com.hrms.model.LeaveType>>> getLeaveTypes() {
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(leaveService.getLeaveTypes()));
    }

    @GetMapping({"", "/all"})
    @PreAuthorize("hasAuthority('leave:read_all') or hasAuthority('leave:approve_hr') or hasAuthority('leave:approve_manager')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.PageResponse<LeaveDTOs.Response>>> getAllLeaves(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        LeaveRequest.LeaveStatus leaveStatus = null;
        if (status != null && !status.isBlank()) {
            try { leaveStatus = LeaveRequest.LeaveStatus.valueOf(status); } catch (Exception ignored) {}
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LeaveRequest> result = leaveService.searchLeaveRequests(employeeId, leaveStatus, pageable);

        CommonDTOs.PageResponse<LeaveDTOs.Response> pageResponse = CommonDTOs.PageResponse.<LeaveDTOs.Response>builder()
                .content(result.getContent().stream().map(leaveService::toResponse).collect(Collectors.toList()))
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(pageResponse));
    }

    @PutMapping("/{id}/manager-action")
    @PreAuthorize("hasAuthority('leave:approve_manager')")
    public ResponseEntity<CommonDTOs.ApiResponse<LeaveDTOs.Response>> managerAction(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @RequestBody(required = false) LeaveDTOs.ActionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            var emp = employeeService.getEmployeeByUserId(principal.getId());
            String comment = request != null ? request.getComment() : "";
            LeaveRequest updated = leaveService.managerAction(id, emp.getId(), approve, comment);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Action processed", leaveService.toResponse(updated)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/hr-action")
    @PreAuthorize("hasAuthority('leave:approve_hr')")
    public ResponseEntity<CommonDTOs.ApiResponse<LeaveDTOs.Response>> hrAction(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @RequestBody(required = false) LeaveDTOs.ActionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            String comment = request != null ? request.getComment() : "";
            LeaveRequest updated = leaveService.hrAction(id, principal.getId(), approve, comment);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Action processed", leaveService.toResponse(updated)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('leave:cancel')")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            leaveService.cancelLeave(id, principal.getId());
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Leave cancelled", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }
}
