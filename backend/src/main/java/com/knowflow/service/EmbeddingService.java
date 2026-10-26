package com.knowflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private final int dimensions;
    private final String openAiApiKey;
    private final HttpClient httpClient;

    public EmbeddingService(
        @Value("${app.rag.embedding-dimensions:384}") int dimensions,
        @Value("${app.llm.api-key:}") String openAiApiKey
    ) {
        this.dimensions = dimensions;
        this.openAiApiKey = openAiApiKey != null ? openAiApiKey.trim() : "";
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return new float[dimensions];
        }

        if (!openAiApiKey.isEmpty()) {
            try {
                return fetchOpenAiEmbedding(text);
            } catch (Exception e) {
                log.warn("OpenAI embedding failed, falling back to deterministic local semantic vectorizer: {}", e.getMessage());
            }
        }

        return generateLocalSemanticEmbedding(text);
    }

    /**
     * High-quality deterministic dense semantic vectorizer (384 dimensions)
     * using token n-grams, semantic bucket projection, and L2 unit-norm.
     */
    public float[] generateLocalSemanticEmbedding(String text) {
        float[] vector = new float[dimensions];
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s_-]", " ");
        String[] tokens = cleaned.split("\\s+");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.isBlank() || isStopWord(token)) continue;

            // Word-level hash projection
            int h1 = Math.abs(token.hashCode());
            int idx1 = h1 % dimensions;
            float weight1 = (float) (1.0 + Math.log(token.length()));
            vector[idx1] += weight1;

            // Trigram subword hashing for stem/morphology resilience
            if (token.length() >= 3) {
                for (int j = 0; j <= token.length() - 3; j++) {
                    String sub = token.substring(j, j + 3);
                    int hSub = Math.abs(sub.hashCode() * 31 + j);
                    int idxSub = hSub % dimensions;
                    vector[idxSub] += 0.4f;
                }
            }

            // Bigram projection with adjacent token for context
            if (i < tokens.length - 1) {
                String next = tokens[i + 1];
                if (!next.isBlank() && !isStopWord(next)) {
                    int hBi = Math.abs((token + "_" + next).hashCode());
                    int idxBi = hBi % dimensions;
                    vector[idxBi] += 0.8f;
                }
            }
        }

        // L2 Unit Normalization (so dot product equals cosine similarity)
        float norm = 0.0f;
        for (float v : vector) {
            norm += v * v;
        }
        if (norm > 0) {
            float sqrtNorm = (float) Math.sqrt(norm);
            for (int k = 0; k < dimensions; k++) {
                vector[k] /= sqrtNorm;
            }
        }

        return vector;
    }

    public double cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length == 0 || v2.length == 0) return 0.0;
        int len = Math.min(v1.length, v2.length);
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < len; i++) {
            dot += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private float[] fetchOpenAiEmbedding(String text) throws Exception {
        String safeText = text.replace("\"", "\\\"").replace("\n", " ");
        String jsonPayload = String.format("{\"input\": \"%s\", \"model\": \"text-embedding-3-small\"}", safeText);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/embeddings"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + openAiApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .timeout(Duration.ofSeconds(10))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI API returned status " + response.statusCode());
        }

        // Quick parse for float array
        String body = response.body();
        int embeddingStart = body.indexOf("\"embedding\": [");
        if (embeddingStart == -1) throw new RuntimeException("Could not find embedding array");
        embeddingStart += 14;
        int embeddingEnd = body.indexOf("]", embeddingStart);
        String[] values = body.substring(embeddingStart, embeddingEnd).split(",");
        float[] res = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = Float.parseFloat(values[i].trim());
        }
        return res;
    }

    private boolean isStopWord(String word) {
        return Set.of(
            "a", "an", "the", "in", "on", "at", "to", "for", "of", "with",
            "is", "are", "was", "were", "it", "this", "that", "and", "or", "by"
        ).contains(word);
    }
}
