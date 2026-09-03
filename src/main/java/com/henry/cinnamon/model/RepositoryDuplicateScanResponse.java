package com.henry.cinnamon.model;

import java.util.List;

public record RepositoryDuplicateScanResponse(
    String repository,
    int totalClustersFound,
    int totalDuplicatedFunctions,
    int estimatedLinesSaved,
    List<DuplicateCluster> clusters
) {}
