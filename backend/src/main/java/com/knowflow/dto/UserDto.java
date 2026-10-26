package com.knowflow.dto;

import java.util.Set;

public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String department;
    private Set<String> roles;
    private String status;

    public UserDto() {}

    public UserDto(Long id, String email, String fullName, String department, Set<String> roles, String status) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.department = department;
        this.roles = roles;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
