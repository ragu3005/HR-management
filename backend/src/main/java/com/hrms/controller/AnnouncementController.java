package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.model.Announcement;
import com.hrms.model.Notification;
import com.hrms.model.User;
import com.hrms.repository.AnnouncementRepository;
import com.hrms.repository.UserRepository;
import com.hrms.security.UserPrincipal;
import com.hrms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<CommonDTOs.ApiResponse<List<CommonDTOs.AnnouncementDTO>>> getPublishedAnnouncements() {
        List<CommonDTOs.AnnouncementDTO> dtos = announcementRepository.findByIsPublishedTrueOrderByPublishedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(dtos));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('announcement:create')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.PageResponse<CommonDTOs.AnnouncementDTO>>> getAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Announcement> result = announcementRepository.findAllByOrderByCreatedAtDesc(pageable);

        CommonDTOs.PageResponse<CommonDTOs.AnnouncementDTO> pageResponse = CommonDTOs.PageResponse.<CommonDTOs.AnnouncementDTO>builder()
                .content(result.getContent().stream().map(this::toDto).collect(Collectors.toList()))
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(pageResponse));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('announcement:create')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.AnnouncementDTO>> createAnnouncement(
            @RequestBody CommonDTOs.AnnouncementDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            User user = userRepository.findById(principal.getId()).orElseThrow();
            Announcement.Priority priority = Announcement.Priority.MEDIUM;
            if (request.getPriority() != null) {
                try { priority = Announcement.Priority.valueOf(request.getPriority()); } catch (Exception ignored) {}
            }

            boolean publish = Boolean.TRUE.equals(request.getIsPublished());

            Announcement announcement = Announcement.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .priority(priority)
                    .isPublished(publish)
                    .publishedAt(publish ? LocalDateTime.now() : null)
                    .createdBy(user)
                    .build();

            Announcement saved = announcementRepository.save(announcement);

            // Notify all active users if published
            if (publish) {
                List<User> activeUsers = userRepository.findAll().stream().filter(User::getIsActive).toList();
                for (User u : activeUsers) {
                    notificationService.createNotification(u, "New Announcement: " + saved.getTitle(),
                            saved.getContent().length() > 80 ? saved.getContent().substring(0, 77) + "..." : saved.getContent(),
                            Notification.NotificationType.ANNOUNCEMENT, saved.getId());
                }
            }

            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Announcement created", toDto(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('announcement:delete')")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        announcementRepository.deleteById(id);
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Announcement deleted", null));
    }

    private CommonDTOs.AnnouncementDTO toDto(Announcement a) {
        return CommonDTOs.AnnouncementDTO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .priority(a.getPriority().name())
                .isPublished(a.getIsPublished())
                .publishedAt(a.getPublishedAt())
                .createdByName(a.getCreatedBy() != null ? a.getCreatedBy().getUsername() : "System")
                .createdAt(a.getCreatedAt())
                .build();
    }
}
