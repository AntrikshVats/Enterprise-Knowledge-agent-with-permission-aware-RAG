package com.knowflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ChatRequest {
    private Long sessionId;

    @NotBlank(message = "Question cannot be blank")
    private String question;

    public ChatRequest() {}

    public ChatRequest(Long sessionId, String question) {
        this.sessionId = sessionId;
        this.question = question;
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
}
