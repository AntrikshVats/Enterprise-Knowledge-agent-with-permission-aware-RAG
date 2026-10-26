package com.knowflow.controller;

import com.knowflow.dto.DocumentDto;
import com.knowflow.dto.DocumentUploadRequest;
import com.knowflow.dto.PermissionUpdateRequest;
import com.knowflow.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentDto> uploadDocument(@Valid @RequestBody DocumentUploadRequest request) {
        DocumentDto created = documentService.createDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> getAuthorizedDocuments() {
        return ResponseEntity.ok(documentService.getAuthorizedDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<DocumentDto> updatePermissions(
        @PathVariable Long id,
        @Valid @RequestBody PermissionUpdateRequest request
    ) {
        return ResponseEntity.ok(documentService.updatePermissions(id, request));
    }

    @PutMapping("/{id}/content")
    public ResponseEntity<DocumentDto> updateDocumentContent(
        @PathVariable Long id,
        @Valid @RequestBody DocumentUploadRequest request
    ) {
        return ResponseEntity.ok(documentService.updateDocumentContent(id, request));
    }
}
