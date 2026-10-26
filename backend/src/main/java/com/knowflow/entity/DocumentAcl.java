package com.knowflow.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document_acl", indexes = {
    @Index(name = "idx_acl_doc_id", columnList = "document_id"),
    @Index(name = "idx_acl_principal", columnList = "principal_type, principal_id")
})
public class DocumentAcl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "principal_type", nullable = false, length = 30)
    private String principalType; // ROLE, USER, DEPARTMENT, PUBLIC

    @Column(name = "principal_id", nullable = false, length = 100)
    private String principalId; // e.g. "ROLE_ENGINEERING", "1", "SECURITY", "*"

    @Column(nullable = false, length = 20)
    private String permission = "READ"; // READ, WRITE, ADMIN

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();

    public DocumentAcl() {}

    public DocumentAcl(Document document, String principalType, String principalId, String permission) {
        this.document = document;
        this.principalType = principalType;
        this.principalId = principalId;
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getPrincipalType() {
        return principalType;
    }

    public void setPrincipalType(String principalType) {
        this.principalType = principalType;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(Instant grantedAt) {
        this.grantedAt = grantedAt;
    }
}
