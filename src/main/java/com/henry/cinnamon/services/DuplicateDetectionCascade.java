package com.henry.cinnamon.services;

import com.henry.cinnamon.model.CodeUnit;
import com.henry.cinnamon.model.DetectionResult;
import com.henry.cinnamon.repository.CodeUnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DuplicateDetectionCascade {

    private static final Logger log = LoggerFactory.getLogger(DuplicateDetectionCascade.class);
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
        if (probe == null || probe.getNormalizedText() == null || probe.getNormalizedText().isBlank()) {
            return DetectionResult.noMatch();
        }

        try {
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
            if (probe.getEmbedding() == null) {
                float[] vector = embeddingModel.embed(probe.getNormalizedText());
                probe.setEmbedding(vector);
            }

            if (probe.getEmbedding() == null || probe.getEmbedding().length == 0) {
                return DetectionResult.noMatch();
            }

            String queryVectorStr = toPgVectorString(probe.getEmbedding());
            List<CodeUnit> nearestNeighbors;
            try {
                nearestNeighbors = codeUnitRepository.findNearestNeighbors(
                        tenantId, repository, queryVectorStr, TOP_K_CANDIDATES);
            } catch (Exception e) {
                // Return gracefully if table is empty or pgvector query returns zero rows
                return DetectionResult.noMatch();
            }

            if (nearestNeighbors == null || nearestNeighbors.isEmpty()) {
                return DetectionResult.noMatch();
            }

            // Filter candidates by cosine similarity threshold (>= 0.85)
            List<CodeUnit> aboveThreshold = nearestNeighbors.stream()
                    .filter(candidate -> candidate != null && candidate.getEmbedding() != null 
                            && cosineSimilarity(candidate.getEmbedding(), probe.getEmbedding()) >= SIMILARITY_THRESHOLD)
                    .toList();

            if (aboveThreshold.isEmpty()) {
                return DetectionResult.noMatch();
            }

            double highestScore = cosineSimilarity(aboveThreshold.get(0).getEmbedding(), probe.getEmbedding());
            return DetectionResult.candidatesFound(aboveThreshold, Math.round(highestScore * 1000.0) / 1000.0);
        } catch (Exception e) {
            log.warn("Non-fatal issue during duplicate cascade check for repo {}: {}", repository, e.getMessage());
            return DetectionResult.noMatch();
        }
    }

    private String toPgVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        return sb.append("]").toString();
    }

    private double cosineSimilarity(float[] vecA, float[] vecB) {
        if (vecA == null || vecB == null || vecA.length == 0 || vecB.length == 0) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < Math.min(vecA.length, vecB.length); i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        return (normA == 0.0 || normB == 0.0) ? 0.0 : (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}
