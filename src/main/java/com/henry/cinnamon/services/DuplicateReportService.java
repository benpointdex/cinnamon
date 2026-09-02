package com.henry.cinnamon.services;

import com.henry.cinnamon.model.DuplicateFinding;
import com.henry.cinnamon.model.DuplicateReport;
import com.henry.cinnamon.model.FileDuplicationSummary;
import com.henry.cinnamon.repository.DuplicateFindingRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicateReportService {

    private final DuplicateFindingRepository findingRepository;

    public DuplicateReportService(DuplicateFindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    public DuplicateReport summarize(String tenantId, String repository) {
        return summarize(tenantId, repository, null, null);
    }

    /**
     * Generates a duplicate summary report for a given tenant and repository with optional pathPrefix & minSimilarity filters.
     */
    public DuplicateReport summarize(String tenantId, String repository, String pathPrefix, Double minSimilarity) {
        List<DuplicateFinding> findings = findingRepository
                .findByTenantIdAndRepositoryOrderByFlaggedAtDesc(tenantId, repository);

        if (findings == null || findings.isEmpty()) {
            return new DuplicateReport(0, 0, List.of(), List.of());
        }

        // Apply pathPrefix and minSimilarity filters if provided
        double threshold = (minSimilarity != null && minSimilarity > 0.0) ? minSimilarity : 0.0;
        String prefix = (pathPrefix != null && !pathPrefix.isBlank()) ? pathPrefix.trim() : null;

        List<DuplicateFinding> filtered = findings.stream()
                .filter(f -> f.getSimilarityScore() >= threshold)
                .filter(f -> prefix == null || 
                        (f.getNewFilePath() != null && f.getNewFilePath().startsWith(prefix)) ||
                        (f.getMatchedFilePath() != null && f.getMatchedFilePath().startsWith(prefix)))
                .toList();

        if (filtered.isEmpty()) {
            return new DuplicateReport(0, 0, List.of(), List.of());
        }

        // Group findings by newFilePath to calculate worst offending files
        Map<String, List<DuplicateFinding>> byFile = filtered.stream()
                .filter(f -> f.getNewFilePath() != null)
                .collect(Collectors.groupingBy(DuplicateFinding::getNewFilePath));

        List<FileDuplicationSummary> topFiles = byFile.entrySet().stream()
                .map(entry -> {
                    String file = entry.getKey();
                    int count = entry.getValue().size();
                    int estimatedLines = count * 15; // approximate 15 lines per function
                    return new FileDuplicationSummary(file, count, estimatedLines);
                })
                .sorted(Comparator.comparingInt(FileDuplicationSummary::duplicateCount).reversed())
                .limit(10)
                .toList();

        int totalEstimatedLines = topFiles.stream()
                .mapToInt(FileDuplicationSummary::linesAffected)
                .sum();

        List<DuplicateFinding> recent = filtered.stream()
                .limit(20)
                .toList();

        return new DuplicateReport(filtered.size(), totalEstimatedLines, topFiles, recent);
    }
}
