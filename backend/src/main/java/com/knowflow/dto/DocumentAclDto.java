package com.knowflow.dto;

import java.time.Instant;

public class DocumentAclDto {
    private Long id;
    private String principalType; // ROLE, USER, DEPARTMENT, PUBLIC
    private String principalId;
    private String permission; // READ, WRITE, ADMIN
    private Instant grantedAt;

    public DocumentAclDto() {}

    public DocumentAclDto(Long id, String principalType, String principalId, String permission, Instant grantedAt) {
        this.id = id;
        this.principalType = principalType;
        this.principalId = principalId;
        this.permission = permission;
        this.grantedAt = grantedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
