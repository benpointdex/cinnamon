package com.henry.cinnamon.services;

import com.henry.cinnamon.model.DuplicateCluster;
import com.henry.cinnamon.model.DuplicateMember;
import com.henry.cinnamon.model.DuplicatePairProjection;
import com.henry.cinnamon.model.RepositoryDuplicateScanResponse;
import com.henry.cinnamon.repository.CodeUnitRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DuplicateClusterScanner {

    private final CodeUnitRepository codeUnitRepository;

    public DuplicateClusterScanner(CodeUnitRepository codeUnitRepository) {
        this.codeUnitRepository = codeUnitRepository;
    }

    /**
     * Executes server-side whole-repository vector self-join scan, groups pairwise duplicates into
     * actionable N-way clusters via Disjoint Set Union (Union-Find), and computes impact metrics.
     */
    public RepositoryDuplicateScanResponse scan(String tenantId, String repository,
                                                Double minSimilarity, String pathPrefix, Integer limit) {

        double threshold = (minSimilarity != null && minSimilarity > 0.0) ? minSimilarity : 0.85;
        int maxResults = (limit != null && limit > 0) ? limit : 100;
        String prefix = (pathPrefix != null && !pathPrefix.isBlank()) ? pathPrefix.trim() : null;

        List<DuplicatePairProjection> pairs = codeUnitRepository.scanRepositoryDuplicates(
                tenantId, repository, threshold, prefix, maxResults);

        if (pairs == null || pairs.isEmpty()) {
            return new RepositoryDuplicateScanResponse(repository, 0, 0, 0, List.of());
        }

        // 1. Union-Find Disjoint Set to aggregate pairwise matches into N-Way clusters
        Map<String, String> parent = new HashMap<>();
        Map<String, DuplicateMember> memberMap = new HashMap<>();
        Map<String, Double> clusterMaxScore = new HashMap<>();
        Map<String, String> clusterSnippet = new HashMap<>();

        for (DuplicatePairProjection p : pairs) {
            String keyA = formatKey(p.getFilePathA(), p.getFunctionNameA());
            String keyB = formatKey(p.getFilePathB(), p.getFunctionNameB());

            int linesA = p.getLineCountA() != null ? p.getLineCountA() : 10;
            int linesB = p.getLineCountB() != null ? p.getLineCountB() : 10;
            double score = p.getSimilarityScore() != null ? p.getSimilarityScore() : threshold;

            memberMap.putIfAbsent(keyA, new DuplicateMember(p.getFilePathA(), p.getFunctionNameA(), linesA));
            memberMap.putIfAbsent(keyB, new DuplicateMember(p.getFilePathB(), p.getFunctionNameB(), linesB));

            union(parent, clusterMaxScore, clusterSnippet, keyA, keyB, score, p.getSnippetA());
        }

        // 2. Group members by their root cluster
        Map<String, List<DuplicateMember>> groups = new HashMap<>();
        for (String key : memberMap.keySet()) {
            String root = find(parent, key);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(memberMap.get(key));
        }

        // 3. Assemble and rank clusters
        List<DuplicateCluster> clusters = new ArrayList<>();
        int clusterCounter = 1;
        int totalSavedLines = 0;
        int totalDuplicatedFunctions = 0;

        for (Map.Entry<String, List<DuplicateMember>> entry : groups.entrySet()) {
            String root = entry.getKey();
            List<DuplicateMember> members = entry.getValue();
            if (members.size() < 2) {
                continue; // Ignore isolated items
            }

            int occurrences = members.size();
            int totalLines = members.stream().mapToInt(DuplicateMember::lineCount).sum();
            int avgLines = totalLines / occurrences;
            int linesSaved = totalLines - avgLines;

            double maxScore = clusterMaxScore.getOrDefault(root, threshold);
            double roundedScore = Math.round(maxScore * 1000.0) / 1000.0;

            String severity = roundedScore >= 0.95 ? "CRITICAL_COPY_PASTE"
                    : roundedScore >= 0.85 ? "HIGH_DUPLICATION" : "STRUCTURAL_SIMILARITY";

            String snippet = clusterSnippet.getOrDefault(root, "");
            if (snippet.length() > 500) {
                snippet = snippet.substring(0, 500) + "...";
            }

            clusters.add(new DuplicateCluster(
                    "cluster_" + clusterCounter++,
                    roundedScore,
                    severity,
                    occurrences,
                    totalLines,
                    linesSaved,
                    members,
                    snippet
            ));

            totalSavedLines += linesSaved;
            totalDuplicatedFunctions += occurrences;
        }

        // Sort clusters by highest impact (totalDuplicatedLines descending)
        clusters.sort(Comparator.comparingInt(DuplicateCluster::totalDuplicatedLines).reversed());

        return new RepositoryDuplicateScanResponse(
                repository,
                clusters.size(),
                totalDuplicatedFunctions,
                totalSavedLines,
                clusters
        );
    }

    private String formatKey(String filePath, String functionName) {
        return (filePath != null ? filePath : "") + "::" + (functionName != null ? functionName : "anonymous");
    }

    private String find(Map<String, String> parent, String node) {
        parent.putIfAbsent(node, node);
        if (!parent.get(node).equals(node)) {
            parent.put(node, find(parent, parent.get(node))); // Path compression
        }
        return parent.get(node);
    }

    private void union(Map<String, String> parent,
                       Map<String, Double> clusterMaxScore,
                       Map<String, String> clusterSnippet,
                       String a, String b, double score, String snippet) {

        String rootA = find(parent, a);
        String rootB = find(parent, b);

        if (!rootA.equals(rootB)) {
            parent.put(rootA, rootB);

            // Merge metadata from rootA into rootB
            Double scoreA = clusterMaxScore.remove(rootA);
            if (scoreA != null) {
                clusterMaxScore.merge(rootB, scoreA, Math::max);
            }
            String snippetA = clusterSnippet.remove(rootA);
            if (snippetA != null) {
                clusterSnippet.putIfAbsent(rootB, snippetA);
            }
        }

        clusterMaxScore.merge(rootB, score, Math::max);
        if (snippet != null && !snippet.isBlank()) {
            clusterSnippet.putIfAbsent(rootB, snippet.trim());
        }
    }
}
