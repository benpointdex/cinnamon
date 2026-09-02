package com.henry.cinnamon.services;

import com.henry.cinnamon.model.DuplicateClusterResult;
import com.henry.cinnamon.model.DuplicatePairProjection;
import com.henry.cinnamon.repository.CodeUnitRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DuplicateClusterScanner {

    private final CodeUnitRepository codeUnitRepository;

    public DuplicateClusterScanner(CodeUnitRepository codeUnitRepository) {
        this.codeUnitRepository = codeUnitRepository;
    }

    /**
     * Executes server-side whole-repository vector self-join scan across all functions in the index.
     */
    public List<DuplicateClusterResult> scan(String tenantId, String repository,
                                            Double minSimilarity, String pathPrefix, Integer limit) {
        double threshold = (minSimilarity != null && minSimilarity > 0.0) ? minSimilarity : 0.85;
        int maxResults = (limit != null && limit > 0) ? limit : 50;
        String prefix = (pathPrefix != null && !pathPrefix.isBlank()) ? pathPrefix.trim() : null;

        List<DuplicatePairProjection> pairs = codeUnitRepository.scanRepositoryDuplicates(
                tenantId, repository, threshold, prefix, maxResults);

        List<DuplicateClusterResult> results = new ArrayList<>();
        if (pairs == null || pairs.isEmpty()) {
            return results;
        }

        for (DuplicatePairProjection p : pairs) {
            int linesA = p.getLineCountA() != null ? p.getLineCountA() : 0;
            int linesB = p.getLineCountB() != null ? p.getLineCountB() : 0;
            int avgLines = Math.min(linesA, linesB);

            double rawScore = p.getSimilarityScore() != null ? p.getSimilarityScore() : threshold;
            double score = Math.round(rawScore * 1000.0) / 1000.0;

            results.add(new DuplicateClusterResult(
                    p.getFilePathA(),
                    p.getFunctionNameA(),
                    p.getFilePathB(),
                    p.getFunctionNameB(),
                    linesA,
                    linesB,
                    score,
                    "~" + avgLines + " lines"
            ));
        }
        return results;
    }
}
