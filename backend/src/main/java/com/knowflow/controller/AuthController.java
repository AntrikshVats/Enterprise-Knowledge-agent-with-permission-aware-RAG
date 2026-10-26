package com.knowflow.controller;

import com.knowflow.dto.AuthRequest;
import com.knowflow.dto.AuthResponse;
import com.knowflow.dto.UserDto;
import com.knowflow.entity.User;
import com.knowflow.repository.UserRepository;
import com.knowflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(authService.toDto(user));
    }

    @GetMapping("/demo-users")
    public ResponseEntity<List<Map<String, Object>>> getDemoUsers() {
        List<Map<String, Object>> demoAccounts = List.of(
            Map.of(
                "email", "alice.engineer@knowflow.ai",
                "name", "Alice Chen",
                "role", "ROLE_ENGINEERING",
                "roleLabel", "Engineering Lead",
                "department", "ENGINEERING",
                "description", "Authorized for Engineering specs & public handbooks. Blocked from HR & Security files."
            ),
            Map.of(
                "email", "bob.hr@knowflow.ai",
                "name", "Bob Martinez",
                "role", "ROLE_HR",
                "roleLabel", "HR Business Partner",
                "department", "HR",
                "description", "Authorized for HR executive compensation & handbooks. Blocked from Engineering & Sec runbooks."
            ),
            Map.of(
                "email", "charlie.sec@knowflow.ai",
                "name", "Charlie Vance",
                "role", "ROLE_SECURITY",
                "roleLabel", "Security Operations Lead",
                "department", "SECURITY",
                "description", "Authorized for Security runbooks & handbooks. Blocked from HR compensation."
            ),
            Map.of(
                "email", "dave.admin@knowflow.ai",
                "name", "Dave Kowalski",
                "role", "ROLE_ADMIN",
                "roleLabel", "Knowledge Administrator",
                "department", "MANAGEMENT",
                "description", "Organization-wide administrative visibility. Can manage ACLs and view audit trail."
            )
        );
        return ResponseEntity.ok(demoAccounts);
    }
}
