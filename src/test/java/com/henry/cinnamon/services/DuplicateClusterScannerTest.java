package com.henry.cinnamon.services;

import com.henry.cinnamon.model.DuplicateCluster;
import com.henry.cinnamon.model.DuplicatePairProjection;
import com.henry.cinnamon.model.RepositoryDuplicateScanResponse;
import com.henry.cinnamon.repository.CodeUnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateClusterScannerTest {

    @Mock
    private CodeUnitRepository codeUnitRepository;

    @InjectMocks
    private DuplicateClusterScanner scanner;

    @Test
    void shouldAggregatePairsIntoNWayClusterAndComputeMetrics() {
        // Mock 3 functions that form a single cluster: A matches B, and B matches C
        DuplicatePairProjection pair1 = createMockProjection(
                "src/routes/attendance.js", "verifyAttendance",
                "src/routes/hpc.js", "verifyHpc",
                40, 40, 0.96
        );

        DuplicatePairProjection pair2 = createMockProjection(
                "src/routes/hpc.js", "verifyHpc",
                "src/routes/fees.js", "verifyFees",
                40, 40, 0.92
        );

        when(codeUnitRepository.scanRepositoryDuplicates(anyString(), anyString(), anyDouble(), any(), anyInt()))
                .thenReturn(List.of(pair1, pair2));

        RepositoryDuplicateScanResponse response = scanner.scan("tenant-1", "repo-1", 0.85, null, 50);

        assertEquals("repo-1", response.repository());
        assertEquals(1, response.totalClustersFound());
        assertEquals(3, response.totalDuplicatedFunctions());
        // 3 functions of 40 lines = 120 total lines. Avg = 40. Saved = 120 - 40 = 80 lines!
        assertEquals(80, response.estimatedLinesSaved());

        DuplicateCluster cluster = response.clusters().get(0);
        assertEquals(3, cluster.occurrences());
        assertEquals(120, cluster.totalDuplicatedLines());
        assertEquals(80, cluster.estimatedLinesSaved());
        assertEquals(0.96, cluster.similarityScore());
        assertEquals("CRITICAL_COPY_PASTE", cluster.severity());
        assertEquals("MERGE_ENDPOINTS", cluster.actionRecommendation());
        assertEquals(3, cluster.members().size());
    }

    @Test
    void shouldDetectIntraFileDuplicatesAndRecommendRemoval() {
        // Real-world scenario: identical function duplicated in the exact same file
        DuplicatePairProjection sameFilePair = createMockProjection(
                "lib/features/missions/assignments_screen.dart", "_formatSubmittedDate",
                "lib/features/missions/assignments_screen.dart", "_formatSubmittedDateCopy",
                25, 25, 0.99
        );

        when(codeUnitRepository.scanRepositoryDuplicates(anyString(), anyString(), anyDouble(), any(), anyInt()))
                .thenReturn(List.of(sameFilePair));

        RepositoryDuplicateScanResponse response = scanner.scan("tenant-1", "atipriye", 0.85, null, 50);

        assertEquals(1, response.totalClustersFound());
        DuplicateCluster cluster = response.clusters().get(0);
        assertEquals("CRITICAL_COPY_PASTE", cluster.severity());
        assertEquals("REMOVE_SAME_FILE_DUPE", cluster.actionRecommendation());
        assertEquals(2, cluster.occurrences());
        assertEquals(25, cluster.estimatedLinesSaved());
    }

    @Test
    void shouldHandleZeroResultsGracefully() {
        when(codeUnitRepository.scanRepositoryDuplicates(anyString(), anyString(), anyDouble(), any(), anyInt()))
                .thenReturn(List.of());

        RepositoryDuplicateScanResponse response = scanner.scan("tenant-1", "empty-repo", 0.85, null, 50);

        assertEquals("empty-repo", response.repository());
        assertEquals(0, response.totalClustersFound());
        assertEquals(0, response.totalDuplicatedFunctions());
        assertEquals(0, response.estimatedLinesSaved());
        assertTrue(response.clusters().isEmpty());
    }

    private DuplicatePairProjection createMockProjection(
            String pathA, String funcA, String pathB, String funcB,
            int linesA, int linesB, double score) {

        return new DuplicatePairProjection() {
            @Override public String getFilePathA() { return pathA; }
            @Override public String getFunctionNameA() { return funcA; }
            @Override public String getFilePathB() { return pathB; }
            @Override public String getFunctionNameB() { return funcB; }
            @Override public Integer getLineCountA() { return linesA; }
            @Override public Integer getLineCountB() { return linesB; }
            @Override public Double getSimilarityScore() { return score; }
        };
    }
}
