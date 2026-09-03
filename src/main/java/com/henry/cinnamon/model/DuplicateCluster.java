package com.henry.cinnamon.model;

import java.util.List;

public record DuplicateCluster(
    String clusterId,
    double similarityScore,
    String severity,
    String actionRecommendation,
    int occurrences,
    int totalDuplicatedLines,
    int estimatedLinesSaved,
    List<DuplicateMember> members
) {}
