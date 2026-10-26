package com.knowflow.service;

import com.knowflow.entity.Document;
import com.knowflow.entity.DocumentAcl;
import com.knowflow.entity.Role;
import com.knowflow.entity.User;
import com.knowflow.exception.AccessDeniedException;
import com.knowflow.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccessControlService {

    private final DocumentRepository documentRepository;
    private final AuditService auditService;

    public AccessControlService(DocumentRepository documentRepository, AuditService auditService) {
        this.documentRepository = documentRepository;
        this.auditService = auditService;
    }

    public boolean isAdmin(User user) {
        if (user == null || user.getRoles() == null) return false;
        return user.getRoles().stream()
            .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()) || "ADMIN".equalsIgnoreCase(r.getName()));
    }

    @Transactional(readOnly = true)
    public Set<Long> getAuthorizedDocumentIds(User user) {
        if (user == null) {
            return Set.of();
        }

        // Knowledge Administrators have organization-wide visibility
        if (isAdmin(user)) {
            return documentRepository.findByStatus("ACTIVE").stream()
                .map(Document::getId)
                .collect(Collectors.toSet());
        }

        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        // Also add non-prefixed role names if any (e.g. "ENGINEERING" vs "ROLE_ENGINEERING")
        Set<String> allRoleAliases = new HashSet<>(roleNames);
        for (String r : roleNames) {
            if (r.startsWith("ROLE_")) {
                allRoleAliases.add(r.substring(5));
            } else {
                allRoleAliases.add("ROLE_" + r);
            }
        }

        List<Long> docIds = documentRepository.findAuthorizedDocumentIds(
            user.getId(),
            String.valueOf(user.getId()),
            user.getDepartment(),
            allRoleAliases
        );

        return new HashSet<>(docIds);
    }

    public boolean canRead(User user, Document document) {
        if (user == null || document == null) return false;
        if (isAdmin(user)) return true;

        // Document owner always has access
        if (document.getOwner() != null && document.getOwner().getId().equals(user.getId())) {
            return true;
        }

        if (!"ACTIVE".equalsIgnoreCase(document.getStatus())) {
            return false;
        }

        Set<String> userRoles = user.getRoles().stream()
            .map(r -> r.getName().toUpperCase())
            .collect(Collectors.toSet());

        for (DocumentAcl acl : document.getAcls()) {
            String type = acl.getPrincipalType().toUpperCase();
            String id = acl.getPrincipalId();

            if ("PUBLIC".equals(type) || "*".equals(id)) {
                return true;
            }

            if ("USER".equals(type) && id.equals(String.valueOf(user.getId()))) {
                return true;
            }

            if ("DEPARTMENT".equals(type) && id.equalsIgnoreCase(user.getDepartment())) {
                return true;
            }

            if ("ROLE".equals(type)) {
                String normalizedId = id.toUpperCase();
                if (userRoles.contains(normalizedId) || 
                    userRoles.contains("ROLE_" + normalizedId) || 
                    (normalizedId.startsWith("ROLE_") && userRoles.contains(normalizedId.substring(5)))) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean canWrite(User user, Document document) {
        if (user == null || document == null) return false;
        if (isAdmin(user)) return true;
        if (document.getOwner() != null && document.getOwner().getId().equals(user.getId())) {
            return true;
        }

        Set<String> userRoles = user.getRoles().stream()
            .map(r -> r.getName().toUpperCase())
            .collect(Collectors.toSet());

        for (DocumentAcl acl : document.getAcls()) {
            if ("WRITE".equalsIgnoreCase(acl.getPermission()) || "ADMIN".equalsIgnoreCase(acl.getPermission())) {
                String type = acl.getPrincipalType().toUpperCase();
                String id = acl.getPrincipalId();

                if ("USER".equals(type) && id.equals(String.valueOf(user.getId()))) return true;
                if ("DEPARTMENT".equals(type) && id.equalsIgnoreCase(user.getDepartment())) return true;
                if ("ROLE".equals(type) && (userRoles.contains(id.toUpperCase()) || userRoles.contains("ROLE_" + id.toUpperCase()))) return true;
            }
        }

        return false;
    }

    public void assertCanRead(User user, Document document) {
        if (!canRead(user, document)) {
            auditService.logIdorAttempt(
                user,
                document.getId(),
                "Unauthorized document access attempt on '" + document.getTitle() + "' (ID: " + document.getId() + ")"
            );
            throw new AccessDeniedException(
                "Access Denied: You do not have permission to view document ID " + document.getId() + " ('" + document.getTitle() + "')"
            );
        }
    }

    public void assertCanWrite(User user, Document document) {
        if (!canWrite(user, document)) {
            auditService.logAccessDenied(
                user,
                "DOCUMENT",
                String.valueOf(document.getId()),
                "Unauthorized modification attempt on document ID " + document.getId()
            );
            throw new AccessDeniedException("Access Denied: You do not have permission to modify document ID " + document.getId());
        }
    }
}
