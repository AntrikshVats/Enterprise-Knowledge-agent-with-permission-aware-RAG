package com.knowflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowflow.dto.ChatRequest;
import com.knowflow.dto.ChatResponse;
import com.knowflow.dto.CitationDto;
import com.knowflow.entity.ChatMessage;
import com.knowflow.entity.ChatSession;
import com.knowflow.entity.Document;
import com.knowflow.entity.DocumentChunk;
import com.knowflow.entity.User;
import com.knowflow.exception.ResourceNotFoundException;
import com.knowflow.repository.ChatMessageRepository;
import com.knowflow.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeAgentService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeAgentService.class);

    private final AuthService authService;
    private final AccessControlService accessControlService;
    private final VectorSearchService vectorSearchService;
    private final LlmService llmService;
    private final AuditService auditService;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public KnowledgeAgentService(
        AuthService authService,
        AccessControlService accessControlService,
        VectorSearchService vectorSearchService,
        LlmService llmService,
        AuditService auditService,
        ChatSessionRepository sessionRepository,
        ChatMessageRepository messageRepository
    ) {
        this.authService = authService;
        this.accessControlService = accessControlService;
        this.vectorSearchService = vectorSearchService;
        this.llmService = llmService;
        this.auditService = auditService;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public ChatResponse processQuestion(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        // Step 1: Authenticate employee
        User user = authService.getCurrentUser();

        // Resolve or create ChatSession
        ChatSession session;
        if (request.getSessionId() != null) {
            session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + request.getSessionId()));
            if (!session.getUser().getId().equals(user.getId()) && !accessControlService.isAdmin(user)) {
                auditService.logIdorAttempt(user, request.getSessionId(), "Attempted to access another user's chat session");
                throw new com.knowflow.exception.AccessDeniedException("Unauthorized access to chat session");
            }
        } else {
            String title = request.getQuestion().length() > 50 
                ? request.getQuestion().substring(0, 47) + "..." 
                : request.getQuestion();
            session = sessionRepository.save(new ChatSession(user, title));
        }

        // Step 2 & 3: Enforce access permissions during retrieval
        Set<Long> authorizedDocIds = accessControlService.getAuthorizedDocumentIds(user);

        List<VectorSearchService.ScoredChunk> retrievedChunks = 
            vectorSearchService.searchAuthorized(request.getQuestion(), authorizedDocIds, 5, 0.25);

        // Step 4: Grounded generation with prompt injection resistance
        LlmService.LlmResult llmResult = llmService.generateGroundedAnswer(request.getQuestion(), retrievedChunks);

        if (llmResult.injectionAttemptDetected()) {
            auditService.logPromptInjection(user, request.getQuestion(), "Detected instruction override pattern in chat prompt or context");
        }

        // Step 5: Build verified citations
        List<CitationDto> citations = new ArrayList<>();
        for (VectorSearchService.ScoredChunk sc : retrievedChunks) {
            DocumentChunk c = sc.chunk();
            Document d = c.getDocument();
            
            // Double check: re-verify user can read this cited document
            if (accessControlService.canRead(user, d)) {
                String excerpt = c.getContent().length() > 220 
                    ? c.getContent().substring(0, 217) + "..." 
                    : c.getContent();
                citations.add(new CitationDto(
                    d.getId(),
                    d.getTitle(),
                    c.getSectionTitle(),
                    excerpt,
                    Math.round(sc.similarityScore() * 100.0) / 100.0,
                    d.getClassification(),
                    d.getDepartment(),
                    c.getChunkIndex()
                ));
            }
        }

        long latencyMs = System.currentTimeMillis() - startTime;

        // Persist message
        String citationsJson = "[]";
        try {
            citationsJson = objectMapper.writeValueAsString(citations);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize citations JSON", e);
        }

        ChatMessage message = new ChatMessage(session, request.getQuestion(), llmResult.answer(), citationsJson);
        message.setAuthorizedDocIdsQueried(authorizedDocIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(",")));
        message.setModelUsed(llmResult.modelName());
        message.setLatencyMs(latencyMs);
        messageRepository.save(message);

        session.setUpdatedAt(Instant.now());
        sessionRepository.save(session);

        // Audit Trail
        String auditDetails = String.format("Query: '%s' | AuthorizedDocIds: %s | RetrievedChunks: %d | Latency: %dms",
            request.getQuestion(), authorizedDocIds, citations.size(), latencyMs);
        auditService.logEvent(user.getId(), user.getEmail(), "QUERY", "CHAT", String.valueOf(session.getId()), auditDetails, "SUCCESS");

        ChatResponse response = new ChatResponse();
        response.setMessageId(message.getId());
        response.setSessionId(session.getId());
        response.setQuestion(request.getQuestion());
        response.setAnswer(llmResult.answer());
        response.setCitations(citations);
        response.setAuthorizedDocIdsQueried(new ArrayList<>(authorizedDocIds));
        response.setModelUsed(llmResult.modelName());
        response.setLatencyMs(latencyMs);
        response.setAccessRestricted(citations.isEmpty());
        response.setSecurityNote(citations.isEmpty() 
            ? "Zero unauthorized documents were queried. Retrieval query was pre-filtered to user's authorized document IDs." 
            : "Authorized retrieval successful: " + citations.size() + " citation(s) verified against security context.");
        response.setTimestamp(Instant.now());

        return response;
    }
}
