package com.hrms.service;

import com.hrms.model.EmployeeDocument;
import com.hrms.model.Employee;
import com.hrms.model.User;
import com.hrms.repository.EmployeeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeService employeeService;
    private final AuditLogService auditLogService;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.max-size-bytes}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "jpg", "jpeg", "png");

    @Transactional
    public EmployeeDocument uploadDocument(Long employeeId, MultipartFile file,
                                           String documentName, String category,
                                           User uploadedBy) throws IOException {
        Employee emp = employeeService.getById(employeeId);

        // Validate file
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName).toLowerCase();
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new RuntimeException("File type not allowed. Allowed: PDF, JPG, JPEG, PNG");
        }
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum allowed size of 10MB");
        }

        // Store file
        String storedName = UUID.randomUUID() + "." + ext;
        Path empDir = Paths.get(uploadDir, "employees", employeeId.toString());
        Files.createDirectories(empDir);
        Path destination = empDir.resolve(storedName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Parse category
        EmployeeDocument.DocumentCategory docCategory;
        try {
            docCategory = EmployeeDocument.DocumentCategory.valueOf(category.toUpperCase());
        } catch (Exception e) {
            docCategory = EmployeeDocument.DocumentCategory.OTHER;
        }

        EmployeeDocument doc = EmployeeDocument.builder()
            .employee(emp)
            .documentName(documentName)
            .documentCategory(docCategory)
            .originalFileName(originalName)
            .storedFileName(storedName)
            .fileType(ext)
            .fileSize(file.getSize())
            .filePath(destination.toString())
            .uploadedBy(uploadedBy)
            .build();

        EmployeeDocument saved = documentRepository.save(doc);

        auditLogService.log(uploadedBy.getId(), uploadedBy.getEmail(),
            "DOCUMENT_UPLOADED", "DOCUMENT",
            "Uploaded: " + documentName + " for employee " + emp.getEmployeeId(),
            saved.getId(), "DOCUMENT", null);

        return saved;
    }

    public Path getDocumentPath(Long documentId, Long requestingUserId, boolean isAdmin) {
        EmployeeDocument doc = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!isAdmin) {
            Employee emp = employeeService.getEmployeeByUserId(requestingUserId);
            if (!doc.getEmployee().getId().equals(emp.getId())) {
                throw new RuntimeException("Access denied to this document");
            }
        }

        return Paths.get(doc.getFilePath());
    }

    @Transactional
    public void deleteDocument(Long documentId, Long userId, boolean isAdmin) throws IOException {
        EmployeeDocument doc = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!isAdmin) {
            Employee emp = employeeService.getEmployeeByUserId(userId);
            if (!doc.getEmployee().getId().equals(emp.getId())) {
                throw new RuntimeException("Access denied");
            }
        }

        Files.deleteIfExists(Paths.get(doc.getFilePath()));
        documentRepository.delete(doc);

        auditLogService.log(userId, null, "DOCUMENT_DELETED", "DOCUMENT",
            "Deleted: " + doc.getDocumentName(), documentId, "DOCUMENT", null);
    }

    public List<EmployeeDocument> getEmployeeDocuments(Long employeeId) {
        return documentRepository.findByEmployeeId(employeeId);
    }

    public EmployeeDocument getDocument(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        else return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
