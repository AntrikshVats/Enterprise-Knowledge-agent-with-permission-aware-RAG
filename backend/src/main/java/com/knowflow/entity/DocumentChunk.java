package com.knowflow.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Arrays;

@Entity
@Table(name = "document_chunks", indexes = {
    @Index(name = "idx_chunk_doc_id", columnList = "document_id"),
    @Index(name = "idx_chunk_index", columnList = "document_id, chunk_index")
})
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "section_title", length = 200)
    private String sectionTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "token_count")
    private Integer tokenCount = 0;

    /**
     * Stored as comma-separated floats or JSON string for cross-database portability
     * (H2, PostgreSQL without vector extension, or mapped to vector in PostgreSQL).
     */
    @Column(name = "embedding_data", columnDefinition = "TEXT")
    private String embeddingData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public DocumentChunk() {}

    public DocumentChunk(Document document, Integer chunkIndex, String sectionTitle, String content, float[] embedding) {
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.sectionTitle = sectionTitle;
        this.content = content;
        setEmbedding(embedding);
        this.tokenCount = content != null ? content.split("\\s+").length : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.tokenCount = content != null ? content.split("\\s+").length : 0;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getEmbeddingData() {
        return embeddingData;
    }

    public void setEmbeddingData(String embeddingData) {
        this.embeddingData = embeddingData;
    }

    public float[] getEmbedding() {
        if (embeddingData == null || embeddingData.isBlank()) {
            return new float[0];
        }
        String[] parts = embeddingData.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Float.parseFloat(parts[i].trim());
            } catch (NumberFormatException e) {
                result[i] = 0.0f;
            }
        }
        return result;
    }

    public void setEmbedding(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            this.embeddingData = "";
            return;
        }
        StringBuilder sb = new StringBuilder(embedding.length * 8);
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        this.embeddingData = sb.toString();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
