package com.knowflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class DocumentUploadRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Department is required")
    private String department;

    private String classification = "INTERNAL";

    @NotBlank(message = "Document content is required")
    private String content;

    private List<DocumentAclDto> acls;

    public DocumentUploadRequest() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<DocumentAclDto> getAcls() {
        return acls;
    }

    public void setAcls(List<DocumentAclDto> acls) {
        this.acls = acls;
    }
}
