package com.henry.cinnamon.model;

import java.util.List;

public record DuplicateReport(
    int totalDuplicateFindings,
    int totalEstimatedLinesDuplicated,
    List<FileDuplicationSummary> topDuplicatedFiles,
    List<DuplicateFinding> recentFindings
) {}
