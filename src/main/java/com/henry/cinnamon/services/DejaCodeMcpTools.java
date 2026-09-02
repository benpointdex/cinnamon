package com.henry.cinnamon.services;

import com.henry.cinnamon.model.*;
import com.henry.cinnamon.parser.FunctionExtractor;
import com.henry.cinnamon.repository.DuplicateFindingRepository;
import com.henry.cinnamon.security.TenantContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DejaCodeMcpTools {

    private final FunctionExtractor functionExtractor;
    private final DuplicateDetectionCascade cascade;
    private final IngestionJobService ingestionJobService;
    private final DuplicateFindingRepository findingRepository;
    private final DuplicateReportService reportService;
    private final DuplicateClusterScanner clusterScanner;

    public DejaCodeMcpTools(FunctionExtractor functionExtractor,
                            DuplicateDetectionCascade cascade,
                            IngestionJobService ingestionJobService,
                            DuplicateFindingRepository findingRepository,
                            DuplicateReportService reportService,
                            DuplicateClusterScanner clusterScanner) {
        this.functionExtractor = functionExtractor;
        this.cascade = cascade;
        this.ingestionJobService = ingestionJobService;
        this.findingRepository = findingRepository;
        this.reportService = reportService;
        this.clusterScanner = clusterScanner;
    }

    /**
     * Tool 1: find_similar_functions
     * Calculates similarity scores and returns candidate duplicate functions.
     */
    @Tool(description = "Given source code and repository name, calculates and returns structurally or semantically similar functions already indexed (file path, function name, cosine similarity score). Call this before writing new code.")
    public List<SimilarFunctionResult> findSimilarFunctions(
            @ToolParam(description = "Repository name e.g. 'cinnamon'") String repository,
            @ToolParam(description = "Source code snippet of the function to check") String sourceCode,
            @ToolParam(description = "File path or target file e.g. 'src/utils/math.ts'") String filePath) {

        String tenantId = TenantContext.get();
        if (sourceCode == null || sourceCode.isBlank()) {
            return List.of();
        }

        Optional<CodeUnit> probeOpt = functionExtractor.extractSingle(sourceCode, filePath, repository);
        if (probeOpt.isEmpty()) {
            return List.of(); // Safely return empty list if no function structure was found
        }

        DetectionResult result = cascade.detect(probeOpt.get(), tenantId, repository);
        if (result == null || !result.matchFound()) {
            return List.of();
        }

        List<SimilarFunctionResult> results = new ArrayList<>();
        for (CodeUnit candidate : result.candidates()) {
            if (candidate != null) {
                results.add(new SimilarFunctionResult(
                    candidate.getFilePath(),
                    candidate.getFunctionName(),
                    result.similarityScore(),
                    candidate.getLineCount(),
                    result.matchTier()
                ));
            }
        }
        return results;
    }

    /**
     * Tool 2: scan_repository_duplicates (NEW)
     * Performs a server-side whole-repository vector self-join scan across all indexed code units (< 50ms).
     */
    @Tool(description = "Scans the entire repository in one shot using in-database pgvector similarity to discover and rank all duplicate clusters across all files. Returns ranked duplicate pairs without requiring client-side batching.")
    public List<DuplicateClusterResult> scanRepositoryDuplicates(
            @ToolParam(description = "Repository name e.g. 'cinnamon'") String repository,
            @ToolParam(description = "Minimum cosine similarity threshold (e.g. 0.85 for 85%). Default is 0.85", required = false) Double minSimilarity,
            @ToolParam(description = "Optional folder path filter e.g. 'src/routes/' to scope the scan", required = false) String pathPrefix,
            @ToolParam(description = "Maximum number of duplicate pairs to return. Default is 50", required = false) Integer limit) {

        String tenantId = TenantContext.get();
        return clusterScanner.scan(tenantId, repository, minSimilarity, pathPrefix, limit);
    }

    /**
     * Tool 3: ingest_files
     * Starts asynchronous background indexing of any batch or entire repository.
     */
    @Tool(description = "Starts indexing a list of files or an entire repository into DejaCode. Handles large payloads with internal auto-partitioning. Returns immediately with a job ID.")
    public IngestJobHandle ingestFiles(
            @ToolParam(description = "Repository name e.g. 'cinnamon'") String repository,
            @ToolParam(description = "List of file paths and their contents") List<FileInput> files) {

        String tenantId = TenantContext.get();
        int count = (files != null) ? files.size() : 0;
        IngestionJob job = ingestionJobService.create(tenantId, repository, count);
        if (files != null && !files.isEmpty()) {
            ingestionJobService.processAsync(job.getId(), files, tenantId, repository, "mcp-agent");
        }
        return new IngestJobHandle(job.getId(), "STARTED", count, "Indexing files in background with auto-partitioning");
    }

    /**
     * Tool 4: get_ingestion_status
     * Checks progress and metrics of an indexing job.
     */
    @Tool(description = "Checks the progress and statistics of an indexing job started with ingest_files.")
    public IngestionJob getIngestionStatus(
            @ToolParam(description = "Job ID returned by ingest_files") String jobId) {
        return ingestionJobService.get(UUID.fromString(jobId));
    }

    /**
     * Tool 5: record_duplicate
     * Called by the client AI agent (Claude Code / Cursor) when it confirms duplicate logic using its own model.
     */
    @Tool(description = "Records a confirmed duplicate finding in this repository after your agent confirms duplicate logic. Persists finding and metrics.")
    public DuplicateFinding recordDuplicate(
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "File path of the new function") String newFilePath,
            @ToolParam(description = "Name of the new function") String newFunctionName,
            @ToolParam(description = "File path of the matched duplicate function") String matchedFilePath,
            @ToolParam(description = "Name of the matched duplicate function") String matchedFunctionName,
            @ToolParam(description = "Cosine similarity score (0.0 - 1.0)") double similarityScore,
            @ToolParam(description = "One-sentence technical reasoning explaining why logic is duplicate") String reasoning,
            @ToolParam(description = "Git commit SHA") String commitSha) {

        String tenantId = TenantContext.get();

        DuplicateFinding finding = new DuplicateFinding();
        finding.setTenantId(tenantId);
        finding.setRepository(repository);
        finding.setNewFilePath(newFilePath);
        finding.setNewFunctionName(newFunctionName);
        finding.setMatchedFilePath(matchedFilePath);
        finding.setMatchedFunctionName(matchedFunctionName);
        finding.setSimilarityScore(similarityScore);
        finding.setConfirmedDuplicate(true);
        finding.setJudgmentReasoning(reasoning);
        finding.setCommitSha(commitSha);

        return findingRepository.save(finding);
    }

    /**
     * Tool 6: get_duplicate_report
     * Returns duplication metrics, consolidated lines, and worst offender files for the repository with optional path and similarity filtering.
     */
    @Tool(description = "Returns a report of confirmed duplicate findings for a repository — total count, estimated lines of duplicated code, and most affected files, with optional pathPrefix and minSimilarity filters.")
    public DuplicateReport getDuplicateReport(
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Optional folder prefix filter e.g. 'src/routes/'", required = false) String pathPrefix,
            @ToolParam(description = "Optional minimum similarity threshold e.g. 0.90", required = false) Double minSimilarity) {

        String tenantId = TenantContext.get();
        return reportService.summarize(tenantId, repository, pathPrefix, minSimilarity);
    }
}
