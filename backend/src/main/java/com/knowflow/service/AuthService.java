package com.knowflow.service;

import com.knowflow.config.JwtTokenProvider;
import com.knowflow.dto.AuthRequest;
import com.knowflow.dto.AuthResponse;
import com.knowflow.dto.UserDto;
import com.knowflow.entity.Role;
import com.knowflow.entity.User;
import com.knowflow.exception.AccessDeniedException;
import com.knowflow.exception.ResourceNotFoundException;
import com.knowflow.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AccessDeniedException("User account is inactive or disabled");
        }

        List<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .toList();

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), roleNames);
        return new AuthResponse(token, toDto(user));
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("No authenticated security context found");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }

    public UserDto toDto(User user) {
        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getDepartment(),
            roleNames,
            user.getStatus()
        );
    }
}
