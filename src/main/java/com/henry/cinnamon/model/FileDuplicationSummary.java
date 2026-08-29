package com.henry.cinnamon.model;

public record FileDuplicationSummary(
    String filePath,
    int duplicateCount,
    int linesAffected
) {}
