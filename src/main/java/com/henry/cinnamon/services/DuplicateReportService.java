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

    /**
     * Generates a duplicate summary report for a given tenant and repository.
     */
    public DuplicateReport summarize(String tenantId, String repository) {
        List<DuplicateFinding> findings = findingRepository
                .findByTenantIdAndRepositoryOrderByFlaggedAtDesc(tenantId, repository);

        if (findings.isEmpty()) {
            return new DuplicateReport(0, 0, List.of(), List.of());
        }

        // Group findings by newFilePath to calculate worst offending files
        Map<String, List<DuplicateFinding>> byFile = findings.stream()
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

        List<DuplicateFinding> recent = findings.stream()
                .limit(20)
                .toList();

        return new DuplicateReport(findings.size(), totalEstimatedLines, topFiles, recent);
    }
}
