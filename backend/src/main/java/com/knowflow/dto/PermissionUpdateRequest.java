package com.knowflow.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class PermissionUpdateRequest {
    @NotEmpty(message = "ACL entries cannot be empty")
    private List<DocumentAclDto> acls;

    public PermissionUpdateRequest() {}

    public PermissionUpdateRequest(List<DocumentAclDto> acls) {
        this.acls = acls;
    }

    public List<DocumentAclDto> getAcls() {
        return acls;
    }

    public void setAcls(List<DocumentAclDto> acls) {
        this.acls = acls;
    }
}
