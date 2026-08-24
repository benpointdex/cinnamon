package com.henry.cinnamon.model;

import java.util.List;

public record DetectionResult(
    boolean matchFound,
    String matchTier, // "TIER_1_NORMALIZED" or "TIER_2_VECTOR"
    double similarityScore,
    String reason,
    List<CodeUnit> candidates
) {
    public static DetectionResult confirmed(CodeUnit match, double score, String tier, String reason) {
        return new DetectionResult(true, tier, score, reason, List.of(match));
    }

    public static DetectionResult candidatesFound(List<CodeUnit> candidates, double topScore) {
        return new DetectionResult(true, "TIER_2_VECTOR", topScore, "Semantically similar functions found", candidates);
    }

    public static DetectionResult noMatch() {
        return new DetectionResult(false, "NONE", 0.0, "No similar functions found", List.of());
    }
}
