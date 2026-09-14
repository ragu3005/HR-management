package com.hrms.controller;

import com.hrms.dto.CommonDTOs;
import com.hrms.model.EmployeeDocument;
import com.hrms.model.User;
import com.hrms.repository.UserRepository;
import com.hrms.security.UserPrincipal;
import com.hrms.service.DocumentService;
import com.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final EmployeeService employeeService;
    private final UserRepository userRepository;

    @PostMapping("/upload/{employeeId}")
    @PreAuthorize("hasAuthority('document:upload')")
    public ResponseEntity<CommonDTOs.ApiResponse<CommonDTOs.DocumentDTO>> uploadDocument(
            @PathVariable Long employeeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String documentName,
            @RequestParam("category") String category,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            User user = userRepository.findById(principal.getId()).orElseThrow();
            EmployeeDocument doc = documentService.uploadDocument(employeeId, file, documentName, category, user);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Document uploaded successfully", toDto(doc)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping({"", "/all"})
    @PreAuthorize("hasAuthority('document:read_all') or hasRole('SUPER_ADMIN') or hasRole('HR_ADMIN')")
    public ResponseEntity<CommonDTOs.ApiResponse<List<CommonDTOs.DocumentDTO>>> getAllDocuments() {
        List<CommonDTOs.DocumentDTO> docs = documentService.getAllDocuments().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CommonDTOs.ApiResponse.success(docs));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('document:read_own')")
    public ResponseEntity<CommonDTOs.ApiResponse<List<CommonDTOs.DocumentDTO>>> getMyDocuments(
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            List<CommonDTOs.DocumentDTO> docs = documentService.getMyDocuments(principal.getId()).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success(docs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('document:read_own') or hasAuthority('document:read_all')")
    public ResponseEntity<CommonDTOs.ApiResponse<List<CommonDTOs.DocumentDTO>>> getEmployeeDocuments(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            boolean isAdmin = principal.hasRole("SUPER_ADMIN") || principal.hasRole("HR_ADMIN");
            if (!isAdmin) {
                var emp = employeeService.getEmployeeByUserId(principal.getId());
                if (!emp.getId().equals(employeeId)) {
                    return ResponseEntity.status(403).body(CommonDTOs.ApiResponse.error("Access denied"));
                }
            }
            List<CommonDTOs.DocumentDTO> docs = documentService.getEmployeeDocuments(employeeId).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success(docs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            boolean isAdmin = principal.hasRole("SUPER_ADMIN") || principal.hasRole("HR_ADMIN");
            Path path = documentService.getDocumentPath(id, principal.getId(), isAdmin);
            Resource resource = new UrlResource(path.toUri());

            EmployeeDocument doc = documentService.getDocument(id);
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if ("pdf".equalsIgnoreCase(doc.getFileType())) {
                mediaType = MediaType.APPLICATION_PDF;
            } else if ("png".equalsIgnoreCase(doc.getFileType())) {
                mediaType = MediaType.IMAGE_PNG;
            } else if ("jpg".equalsIgnoreCase(doc.getFileType()) || "jpeg".equalsIgnoreCase(doc.getFileType())) {
                mediaType = MediaType.IMAGE_JPEG;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            boolean isAdmin = principal.hasRole("SUPER_ADMIN") || principal.hasRole("HR_ADMIN");
            Path path = documentService.getDocumentPath(id, principal.getId(), isAdmin);
            Resource resource = new UrlResource(path.toUri());
            EmployeeDocument doc = documentService.getDocument(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('document:delete_own') or hasAuthority('document:delete_all')")
    public ResponseEntity<CommonDTOs.ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            boolean isAdmin = principal.hasRole("SUPER_ADMIN") || principal.hasRole("HR_ADMIN");
            documentService.deleteDocument(id, principal.getId(), isAdmin);
            return ResponseEntity.ok(CommonDTOs.ApiResponse.success("Document deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CommonDTOs.ApiResponse.error(e.getMessage()));
        }
    }

    private CommonDTOs.DocumentDTO toDto(EmployeeDocument d) {
        return CommonDTOs.DocumentDTO.builder()
                .id(d.getId())
                .employeeId(d.getEmployee().getId())
                .documentName(d.getDocumentName())
                .documentCategory(d.getDocumentCategory().name())
                .originalFileName(d.getOriginalFileName())
                .fileType(d.getFileType())
                .fileSize(d.getFileSize())
                .fileSizeFormatted(documentService.formatFileSize(d.getFileSize()))
                .uploadedByName(d.getUploadedBy() != null ? d.getUploadedBy().getUsername() : "System")
                .uploadedAt(d.getUploadedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
