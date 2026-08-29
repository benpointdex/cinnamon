package com.henry.cinnamon.model;

public record SimilarFunctionResult(
    String filePath,
    String functionName,
    double similarityScore,
    int lineCount,
    String matchTier // "TIER_1_NORMALIZED" or "TIER_2_VECTOR"
) {}
