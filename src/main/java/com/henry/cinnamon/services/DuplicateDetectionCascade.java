package com.henry.cinnamon.services;

import com.henry.cinnamon.model.CodeUnit;
import com.henry.cinnamon.model.DetectionResult;
import com.henry.cinnamon.repository.CodeUnitRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DuplicateDetectionCascade {

    private static final double SIMILARITY_THRESHOLD = 0.85; // 85% cosine similarity
    private static final int TOP_K_CANDIDATES = 5;

    private final CodeUnitRepository codeUnitRepository;
    private final EmbeddingModel embeddingModel; // In-process ONNX embedding model

    public DuplicateDetectionCascade(CodeUnitRepository codeUnitRepository, @Lazy EmbeddingModel embeddingModel) {
        this.codeUnitRepository = codeUnitRepository;
        this.embeddingModel = embeddingModel;
    }

    /**
     * Executes the detection cascade for a new probe function.
     */
    public DetectionResult detect(CodeUnit probe, String tenantId, String repository) {
        // --- Tier 1: Exact / Normalized Hash Match ---
        Optional<CodeUnit> exactMatch = codeUnitRepository
                .findByTenantIdAndRepositoryAndContentHash(tenantId, repository, probe.getContentHash());

        if (exactMatch.isPresent()) {
            return DetectionResult.confirmed(
                exactMatch.get(), 
                1.0, 
                "TIER_1_NORMALIZED", 
                "Exact normalized logic match found in " + exactMatch.get().getFilePath()
            );
        }

        // --- Tier 2: In-Process Local Vector Embedding + pgvector HNSW Search ---
        // 1. Generate 384-dim embedding in memory using local ONNX model
        if (probe.getEmbedding() == null) {
            float[] vector = embeddingModel.embed(probe.getNormalizedText());
            probe.setEmbedding(vector);
        }

        // 2. Query Postgres pgvector using cosine distance (<=>)
        String queryVectorStr = toPgVectorString(probe.getEmbedding());
        List<CodeUnit> nearestNeighbors = codeUnitRepository.findNearestNeighbors(
                tenantId, repository, queryVectorStr, TOP_K_CANDIDATES);

        // 3. Filter candidates by cosine similarity threshold (>= 0.85)
        List<CodeUnit> aboveThreshold = nearestNeighbors.stream()
                .filter(candidate -> candidate.getEmbedding() != null 
                        && cosineSimilarity(candidate.getEmbedding(), probe.getEmbedding()) >= SIMILARITY_THRESHOLD)
                .toList();

        if (aboveThreshold.isEmpty()) {
            return DetectionResult.noMatch();
        }

        double highestScore = cosineSimilarity(aboveThreshold.get(0).getEmbedding(), probe.getEmbedding());
        return DetectionResult.candidatesFound(aboveThreshold, highestScore);
    }

    // Helper: Formats float[] as Postgres vector literal "[0.123, -0.456, ...]"
    private String toPgVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        return sb.append("]").toString();
    }

    // Helper: Calculates Cosine Similarity between two 384-dim vectors
    private double cosineSimilarity(float[] vecA, float[] vecB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        return (normA == 0.0 || normB == 0.0) ? 0.0 : (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}
