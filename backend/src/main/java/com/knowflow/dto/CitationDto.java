package com.knowflow.dto;

public class CitationDto {
    private Long documentId;
    private String documentTitle;
    private String sectionTitle;
    private String excerpt;
    private double similarityScore;
    private String classification;
    private String department;
    private int chunkIndex;

    public CitationDto() {}

    public CitationDto(Long documentId, String documentTitle, String sectionTitle, String excerpt, double similarityScore, String classification, String department, int chunkIndex) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.sectionTitle = sectionTitle;
        this.excerpt = excerpt;
        this.similarityScore = similarityScore;
        this.classification = classification;
        this.department = department;
        this.chunkIndex = chunkIndex;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }
}
