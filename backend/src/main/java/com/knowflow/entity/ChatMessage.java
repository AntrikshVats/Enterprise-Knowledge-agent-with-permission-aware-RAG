package com.knowflow.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_msg_session_id", columnList = "session_id")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(name = "citations_json", columnDefinition = "TEXT")
    private String citationsJson;

    @Column(name = "authorized_doc_ids_queried", length = 500)
    private String authorizedDocIdsQueried;

    @Column(name = "model_used", length = 50)
    private String modelUsed = "local-grounded-agent";

    @Column(name = "latency_ms")
    private Long latencyMs = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ChatMessage() {}

    public ChatMessage(ChatSession session, String question, String answer, String citationsJson) {
        this.session = session;
        this.question = question;
        this.answer = answer;
        this.citationsJson = citationsJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCitationsJson() {
        return citationsJson;
    }

    public void setCitationsJson(String citationsJson) {
        this.citationsJson = citationsJson;
    }

    public String getAuthorizedDocIdsQueried() {
        return authorizedDocIdsQueried;
    }

    public void setAuthorizedDocIdsQueried(String authorizedDocIdsQueried) {
        this.authorizedDocIdsQueried = authorizedDocIdsQueried;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
