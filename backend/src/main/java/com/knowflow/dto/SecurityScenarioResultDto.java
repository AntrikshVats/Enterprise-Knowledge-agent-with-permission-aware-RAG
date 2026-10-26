package com.knowflow.dto;

import java.util.List;

public class SecurityScenarioResultDto {
    private String scenarioId;
    private String title;
    private String actorName;
    private String actorRole;
    private String queryOrAction;
    private String expectedBehavior;
    private String actualBehavior;
    private boolean passed;
    private List<String> retrievedDocumentTitles;
    private List<Long> authorizedDocumentIds;
    private String explanation;

    public SecurityScenarioResultDto() {}

    public SecurityScenarioResultDto(String scenarioId, String title, String actorName, String actorRole, String queryOrAction, String expectedBehavior, String actualBehavior, boolean passed, List<String> retrievedDocumentTitles, List<Long> authorizedDocumentIds, String explanation) {
        this.scenarioId = scenarioId;
        this.title = title;
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.queryOrAction = queryOrAction;
        this.expectedBehavior = expectedBehavior;
        this.actualBehavior = actualBehavior;
        this.passed = passed;
        this.retrievedDocumentTitles = retrievedDocumentTitles;
        this.authorizedDocumentIds = authorizedDocumentIds;
        this.explanation = explanation;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getQueryOrAction() {
        return queryOrAction;
    }

    public void setQueryOrAction(String queryOrAction) {
        this.queryOrAction = queryOrAction;
    }

    public String getExpectedBehavior() {
        return expectedBehavior;
    }

    public void setExpectedBehavior(String expectedBehavior) {
        this.expectedBehavior = expectedBehavior;
    }

    public String getActualBehavior() {
        return actualBehavior;
    }

    public void setActualBehavior(String actualBehavior) {
        this.actualBehavior = actualBehavior;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<String> getRetrievedDocumentTitles() {
        return retrievedDocumentTitles;
    }

    public void setRetrievedDocumentTitles(List<String> retrievedDocumentTitles) {
        this.retrievedDocumentTitles = retrievedDocumentTitles;
    }

    public List<Long> getAuthorizedDocumentIds() {
        return authorizedDocumentIds;
    }

    public void setAuthorizedDocumentIds(List<Long> authorizedDocumentIds) {
        this.authorizedDocumentIds = authorizedDocumentIds;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
