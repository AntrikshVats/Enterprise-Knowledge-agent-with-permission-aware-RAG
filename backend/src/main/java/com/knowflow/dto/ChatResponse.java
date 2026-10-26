package com.knowflow.dto;

import java.time.Instant;
import java.util.List;

public class ChatResponse {
    private Long messageId;
    private Long sessionId;
    private String question;
    private String answer;
    private List<CitationDto> citations;
    private List<Long> authorizedDocIdsQueried;
    private String modelUsed;
    private Long latencyMs;
    private boolean accessRestricted;
    private String securityNote;
    private Instant timestamp;

    public ChatResponse() {}

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public List<CitationDto> getCitations() {
        return citations;
    }

    public void setCitations(List<CitationDto> citations) {
        this.citations = citations;
    }

    public List<Long> getAuthorizedDocIdsQueried() {
        return authorizedDocIdsQueried;
    }

    public void setAuthorizedDocIdsQueried(List<Long> authorizedDocIdsQueried) {
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

    public boolean isAccessRestricted() {
        return accessRestricted;
    }

    public void setAccessRestricted(boolean accessRestricted) {
        this.accessRestricted = accessRestricted;
    }

    public String getSecurityNote() {
        return securityNote;
    }

    public void setSecurityNote(String securityNote) {
        this.securityNote = securityNote;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
