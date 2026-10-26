package com.knowflow.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowflow.dto.ChatRequest;
import com.knowflow.dto.ChatResponse;
import com.knowflow.dto.CitationDto;
import com.knowflow.entity.ChatMessage;
import com.knowflow.entity.ChatSession;
import com.knowflow.entity.User;
import com.knowflow.exception.AccessDeniedException;
import com.knowflow.exception.ResourceNotFoundException;
import com.knowflow.repository.ChatMessageRepository;
import com.knowflow.repository.ChatSessionRepository;
import com.knowflow.service.AccessControlService;
import com.knowflow.service.AuthService;
import com.knowflow.service.KnowledgeAgentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final KnowledgeAgentService knowledgeAgentService;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final AuthService authService;
    private final AccessControlService accessControlService;
    private final ObjectMapper objectMapper;

    public ChatController(
        KnowledgeAgentService knowledgeAgentService,
        ChatSessionRepository sessionRepository,
        ChatMessageRepository messageRepository,
        AuthService authService,
        AccessControlService accessControlService
    ) {
        this.knowledgeAgentService = knowledgeAgentService;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.authService = authService;
        this.accessControlService = accessControlService;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(knowledgeAgentService.processQuestion(request));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<Map<String, Object>>> getSessions() {
        User user = authService.getCurrentUser();
        List<ChatSession> sessions = sessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());

        List<Map<String, Object>> result = sessions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("title", s.getTitle());
            map.put("createdAt", s.getCreatedAt());
            map.put("updatedAt", s.getUpdatedAt());
            map.put("messageCount", s.getMessages() != null ? s.getMessages().size() : 0);
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<Map<String, Object>> getSessionDetails(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        ChatSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + id));

        if (!session.getUser().getId().equals(user.getId()) && !accessControlService.isAdmin(user)) {
            throw new AccessDeniedException("You are not authorized to view this chat session");
        }

        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(id);
        List<Map<String, Object>> messageDtos = messages.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("question", m.getQuestion());
            map.put("answer", m.getAnswer());
            map.put("modelUsed", m.getModelUsed());
            map.put("latencyMs", m.getLatencyMs());
            map.put("createdAt", m.getCreatedAt());

            List<CitationDto> citations = List.of();
            if (m.getCitationsJson() != null && !m.getCitationsJson().isBlank()) {
                try {
                    citations = objectMapper.readValue(m.getCitationsJson(), new TypeReference<List<CitationDto>>() {});
                } catch (Exception ignored) {}
            }
            map.put("citations", citations);
            return map;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("id", session.getId());
        response.put("title", session.getTitle());
        response.put("createdAt", session.getCreatedAt());
        response.put("messages", messageDtos);

        return ResponseEntity.ok(response);
    }
}
