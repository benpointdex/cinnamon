package com.henry.cinnamon.model;

public record DuplicateClusterResult(
    String filePathA,
    String functionNameA,
    String filePathB,
    String functionNameB,
    int lineCountA,
    int lineCountB,
    double similarityScore,
    String estimatedDuplicatedLines
) {}
