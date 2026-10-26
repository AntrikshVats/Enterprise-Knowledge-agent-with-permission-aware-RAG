package com.knowflow;

import com.knowflow.entity.Document;
import com.knowflow.entity.DocumentChunk;
import com.knowflow.repository.DocumentChunkRepository;
import com.knowflow.service.EmbeddingService;
import com.knowflow.service.VectorSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionAwareRetrievalTest {

    @Mock
    private DocumentChunkRepository chunkRepository;

    private EmbeddingService embeddingService;
    private VectorSearchService vectorSearchService;

    private Document engDoc;
    private Document hrDoc;
    private DocumentChunk engChunk;
    private DocumentChunk hrChunk;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(384, "");
        vectorSearchService = new VectorSearchService(chunkRepository, embeddingService, 0.20, 5);

        engDoc = new Document();
        engDoc.setId(101L);
        engDoc.setTitle("Engineering Deployment Guide");
        engDoc.setDepartment("ENGINEERING");

        hrDoc = new Document();
        hrDoc.setId(102L);
        hrDoc.setTitle("Executive Compensation Framework");
        hrDoc.setDepartment("HR");

        float[] engEmb = embeddingService.generateEmbedding("Deployment procedures require CI checks and approval from release owner.");
        engChunk = new DocumentChunk(engDoc, 0, "CI Gates", "Deployment procedures require CI checks and approval from release owner.", engEmb);

        float[] hrEmb = embeddingService.generateEmbedding("Executive base salary and bonus compensation packages.");
        hrChunk = new DocumentChunk(hrDoc, 0, "Executive Pay", "Executive base salary and bonus compensation packages.", hrEmb);
    }

    @Test
    @DisplayName("Pre-retrieval vector search ONLY passes authorized document IDs to the database query")
    void testPreRetrievalBoundaryEnforced() {
        Set<Long> aliceAuthorizedIds = Set.of(101L); // Alice only has access to Engineering doc

        when(chunkRepository.findAuthorizedChunks(aliceAuthorizedIds)).thenReturn(List.of(engChunk));

        List<VectorSearchService.ScoredChunk> results = vectorSearchService.searchAuthorized(
            "What is the procedure for deployment?",
            aliceAuthorizedIds,
            5,
            0.15
        );

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(101L, results.get(0).chunk().getDocument().getId());
        verify(chunkRepository, times(1)).findAuthorizedChunks(aliceAuthorizedIds);
    }

    @Test
    @DisplayName("When user asks about restricted topics, unauthorized chunks are NEVER returned")
    void testRestrictedTopicRetrievalReturnsEmpty() {
        Set<Long> aliceAuthorizedIds = Set.of(101L); // Only engineering doc

        // The repository only returns chunks for authorized IDs (101L)
        when(chunkRepository.findAuthorizedChunks(aliceAuthorizedIds)).thenReturn(List.of(engChunk));

        // Alice queries about salary (which is in HR doc 102L, not authorized)
        List<VectorSearchService.ScoredChunk> results = vectorSearchService.searchAuthorized(
            "What is the executive bonus compensation?",
            aliceAuthorizedIds,
            5,
            0.40
        );

        // Verify no HR chunk was returned
        boolean hrLeaked = results.stream().anyMatch(sc -> sc.chunk().getDocument().getId().equals(102L));
        assertFalse(hrLeaked, "HR document must NEVER leak into Alice's results");
    }

    @Test
    @DisplayName("Empty authorized document set immediately terminates without querying chunks")
    void testEmptyAuthorizedSetReturnsEmpty() {
        List<VectorSearchService.ScoredChunk> results = vectorSearchService.searchAuthorized(
            "Any query",
            Set.of(),
            5,
            0.20
        );

        assertTrue(results.isEmpty());
        verifyNoInteractions(chunkRepository);
    }
}
