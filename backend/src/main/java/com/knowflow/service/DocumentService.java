package com.knowflow.service;

import com.knowflow.dto.*;
import com.knowflow.entity.Document;
import com.knowflow.entity.DocumentAcl;
import com.knowflow.entity.DocumentChunk;
import com.knowflow.entity.User;
import com.knowflow.exception.ResourceNotFoundException;
import com.knowflow.repository.DocumentAclRepository;
import com.knowflow.repository.DocumentChunkRepository;
import com.knowflow.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentAclRepository aclRepository;
    private final DocumentChunkRepository chunkRepository;
    private final AuthService authService;
    private final AccessControlService accessControlService;
    private final EmbeddingService embeddingService;
    private final AuditService auditService;

    public DocumentService(
        DocumentRepository documentRepository,
        DocumentAclRepository aclRepository,
        DocumentChunkRepository chunkRepository,
        AuthService authService,
        AccessControlService accessControlService,
        EmbeddingService embeddingService,
        AuditService auditService
    ) {
        this.documentRepository = documentRepository;
        this.aclRepository = aclRepository;
        this.chunkRepository = chunkRepository;
        this.authService = authService;
        this.accessControlService = accessControlService;
        this.embeddingService = embeddingService;
        this.auditService = auditService;
    }

    @Transactional
    public DocumentDto createDocument(DocumentUploadRequest request) {
        User currentUser = authService.getCurrentUser();

        Document doc = new Document();
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        doc.setDepartment(request.getDepartment().toUpperCase());
        doc.setClassification(request.getClassification() != null ? request.getClassification().toUpperCase() : "INTERNAL");
        doc.setOwner(currentUser);
        doc.setStatus("ACTIVE");
        doc.setVersion(1);
        doc.setContentType("text/markdown");
        doc.setContentPreview(request.getContent().length() > 300 
            ? request.getContent().substring(0, 297) + "..." 
            : request.getContent());
        doc.setFileSize((long) request.getContent().getBytes().length);

        Document savedDoc = documentRepository.save(doc);

        // Add ACLs
        if (request.getAcls() != null && !request.getAcls().isEmpty()) {
            for (DocumentAclDto aclDto : request.getAcls()) {
                DocumentAcl acl = new DocumentAcl(
                    savedDoc,
                    aclDto.getPrincipalType().toUpperCase(),
                    aclDto.getPrincipalId(),
                    aclDto.getPermission() != null ? aclDto.getPermission().toUpperCase() : "READ"
                );
                savedDoc.addAcl(acl);
            }
        } else {
            // Default: owner write, department read
            savedDoc.addAcl(new DocumentAcl(savedDoc, "USER", String.valueOf(currentUser.getId()), "ADMIN"));
            savedDoc.addAcl(new DocumentAcl(savedDoc, "DEPARTMENT", request.getDepartment(), "READ"));
        }

        // Chunking and Indexing
        List<DocumentChunk> chunks = chunkAndEmbed(savedDoc, request.getContent());
        savedDoc.getChunks().addAll(chunks);
        documentRepository.save(savedDoc);

        auditService.logEvent(
            currentUser.getId(),
            currentUser.getEmail(),
            "DOC_UPLOAD",
            "DOCUMENT",
            String.valueOf(savedDoc.getId()),
            "Created document '" + savedDoc.getTitle() + "' with " + chunks.size() + " chunks",
            "SUCCESS"
        );

        return toDto(savedDoc);
    }

    @Transactional
    public DocumentDto updatePermissions(Long documentId, PermissionUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        Document doc = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        accessControlService.assertCanWrite(currentUser, doc);

        // Replace ACLs
        doc.getAcls().clear();
        for (DocumentAclDto aclDto : request.getAcls()) {
            DocumentAcl acl = new DocumentAcl(
                doc,
                aclDto.getPrincipalType().toUpperCase(),
                aclDto.getPrincipalId(),
                aclDto.getPermission() != null ? aclDto.getPermission().toUpperCase() : "READ"
            );
            doc.addAcl(acl);
        }

        doc.setUpdatedAt(Instant.now());
        Document saved = documentRepository.save(doc);

        auditService.logEvent(
            currentUser.getId(),
            currentUser.getEmail(),
            "ACL_UPDATED",
            "DOCUMENT",
            String.valueOf(saved.getId()),
            "Updated ACL permissions (" + request.getAcls().size() + " entries) for '" + saved.getTitle() + "'",
            "SUCCESS"
        );

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> getAuthorizedDocuments() {
        User currentUser = authService.getCurrentUser();
        Set<Long> authIds = accessControlService.getAuthorizedDocumentIds(currentUser);
        if (authIds.isEmpty()) return List.of();

        return documentRepository.findAllById(authIds).stream()
            .map(this::toDto)
            .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocumentById(Long id) {
        User currentUser = authService.getCurrentUser();
        Document doc = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + id));

        // Enforce access control - prevents IDOR attacks
        accessControlService.assertCanRead(currentUser, doc);

        return toDto(doc);
    }

    @Transactional
    public DocumentDto updateDocumentContent(Long id, DocumentUploadRequest request) {
        User currentUser = authService.getCurrentUser();
        Document doc = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + id));

        accessControlService.assertCanWrite(currentUser, doc);

        // Re-versioning
        doc.setVersion(doc.getVersion() + 1);
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        doc.setContentPreview(request.getContent().length() > 300 
            ? request.getContent().substring(0, 297) + "..." 
            : request.getContent());
        doc.setUpdatedAt(Instant.now());

        // Re-indexing: remove obsolete chunks and insert new chunks
        doc.getChunks().clear();
        List<DocumentChunk> newChunks = chunkAndEmbed(doc, request.getContent());
        doc.getChunks().addAll(newChunks);

        Document updated = documentRepository.save(doc);

        auditService.logEvent(
            currentUser.getId(),
            currentUser.getEmail(),
            "DOC_REINDEX",
            "DOCUMENT",
            String.valueOf(updated.getId()),
            "Re-indexed document to version " + updated.getVersion() + " with " + newChunks.size() + " chunks",
            "SUCCESS"
        );

        return toDto(updated);
    }

    /**
     * Splits document content semantically by headings or paragraphs,
     * maintaining section titles for grounded citations.
     */
    public List<DocumentChunk> chunkAndEmbed(Document doc, String content) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) return chunks;

        // Split by markdown headings: #, ##, ###
        Pattern sectionPattern = Pattern.compile("(?m)^(#{1,3}\\s+.+)$");
        Matcher matcher = sectionPattern.matcher(content);

        List<Integer> splitIndices = new ArrayList<>();
        List<String> sectionTitles = new ArrayList<>();

        while (matcher.find()) {
            splitIndices.add(matcher.start());
            sectionTitles.add(matcher.group(1).replaceAll("^#+\\s*", "").trim());
        }

        if (splitIndices.isEmpty()) {
            // Split by paragraph blocks
            String[] paragraphs = content.split("(?m)^\\s*$");
            int idx = 0;
            for (String p : paragraphs) {
                String trimmed = p.trim();
                if (trimmed.length() > 20) {
                    float[] emb = embeddingService.generateEmbedding(trimmed);
                    chunks.add(new DocumentChunk(doc, idx++, "General Overview", trimmed, emb));
                }
            }
        } else {
            for (int i = 0; i < splitIndices.size(); i++) {
                int start = splitIndices.get(i);
                int end = (i + 1 < splitIndices.size()) ? splitIndices.get(i + 1) : content.length();
                String sectionContent = content.substring(start, end).trim();
                String title = sectionTitles.get(i);

                if (!sectionContent.isBlank()) {
                    float[] emb = embeddingService.generateEmbedding(title + "\n" + sectionContent);
                    chunks.add(new DocumentChunk(doc, i, title, sectionContent, emb));
                }
            }
        }

        return chunks;
    }

    public DocumentDto toDto(Document doc) {
        DocumentDto dto = new DocumentDto();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        dto.setDepartment(doc.getDepartment());
        dto.setClassification(doc.getClassification());
        dto.setOwnerEmail(doc.getOwner() != null ? doc.getOwner().getEmail() : "system@knowflow.ai");
        dto.setStatus(doc.getStatus());
        dto.setVersion(doc.getVersion());
        dto.setChunkCount(doc.getChunks() != null ? doc.getChunks().size() : 0);
        dto.setContentPreview(doc.getContentPreview());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());

        if (doc.getAcls() != null) {
            dto.setAcls(doc.getAcls().stream()
                .map(a -> new DocumentAclDto(a.getId(), a.getPrincipalType(), a.getPrincipalId(), a.getPermission(), a.getGrantedAt()))
                .collect(Collectors.toList()));
        }
        return dto;
    }
}
