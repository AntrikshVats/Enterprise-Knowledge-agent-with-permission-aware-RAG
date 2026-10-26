package com.knowflow.service;

import com.knowflow.entity.DocumentChunk;
import com.knowflow.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final double defaultThreshold;
    private final int defaultTopK;

    public record ScoredChunk(
        DocumentChunk chunk,
        double similarityScore
    ) {}

    public VectorSearchService(
        DocumentChunkRepository chunkRepository,
        EmbeddingService embeddingService,
        @Value("${app.rag.similarity-threshold:0.25}") double defaultThreshold,
        @Value("${app.rag.max-retrieved-chunks:5}") int defaultTopK
    ) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.defaultThreshold = defaultThreshold;
        this.defaultTopK = defaultTopK;
    }

    /**
     * Pre-retrieval Permission-Aware Vector Search.
     * Enforces document ACL boundaries at the retrieval query level BEFORE passages reach the LLM.
     */
    @Transactional(readOnly = true)
    public List<ScoredChunk> searchAuthorized(String query, Set<Long> authorizedDocIds, Integer topK, Double threshold) {
        if (query == null || query.isBlank() || authorizedDocIds == null || authorizedDocIds.isEmpty()) {
            log.debug("Search query empty or zero authorized documents in context. Returning empty results.");
            return List.of();
        }

        int limit = (topK != null && topK > 0) ? topK : defaultTopK;
        double minScore = (threshold != null && threshold >= 0) ? threshold : defaultThreshold;

        float[] queryEmbedding = embeddingService.generateEmbedding(query);

        // Pre-retrieval authorization boundary: Query ONLY chunks from authorized document IDs
        List<DocumentChunk> candidateChunks = chunkRepository.findAuthorizedChunks(authorizedDocIds);

        List<ScoredChunk> scoredList = new ArrayList<>();
        for (DocumentChunk chunk : candidateChunks) {
            float[] chunkVec = chunk.getEmbedding();
            if (chunkVec.length == 0) continue;

            double score = embeddingService.cosineSimilarity(queryEmbedding, chunkVec);
            if (score >= minScore) {
                scoredList.add(new ScoredChunk(chunk, score));
            }
        }

        // Rank by highest similarity score
        scoredList.sort((a, b) -> Double.compare(b.similarityScore(), a.similarityScore()));

        List<ScoredChunk> results = scoredList.stream().limit(limit).toList();

        log.info("Pre-retrieval search: query='{}' | authorizedDocCount={} | candidateChunks={} | matchedResults={}",
            query, authorizedDocIds.size(), candidateChunks.size(), results.size());

        return results;
    }
}
