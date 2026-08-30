package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.model.Notification;
import com.hrms.security.UserPrincipal;
import com.hrms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<CommonDTOs.ApiResponse<List<CommonDTOs.NotificationDTO>>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CommonDTOs.NotificationDTO> dtos = notificationService.getUserNotifications(principal.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(dtos));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<CommonDTOs.ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal.getId());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success("All notifications marked as read", null));
    }

    private CommonDTOs.NotificationDTO toDto(Notification n) {
        return CommonDTOs.NotificationDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType().name())
                .referenceId(n.getReferenceId())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
