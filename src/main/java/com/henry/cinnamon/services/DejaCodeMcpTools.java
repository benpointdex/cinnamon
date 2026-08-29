package com.henry.cinnamon.services;

import com.henry.cinnamon.model.*;
import com.henry.cinnamon.parser.FunctionExtractor;
import com.henry.cinnamon.repository.DuplicateFindingRepository;
import com.henry.cinnamon.security.TenantContext;
import com.henry.cinnamon.services.DuplicateDetectionCascade;
import com.henry.cinnamon.services.DuplicateReportService;
import com.henry.cinnamon.services.IngestionJobService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DejaCodeMcpTools {

    private final FunctionExtractor functionExtractor;
    private final DuplicateDetectionCascade cascade;
    private final IngestionJobService ingestionJobService;
    private final DuplicateFindingRepository findingRepository;
    private final DuplicateReportService reportService;

    public DejaCodeMcpTools(FunctionExtractor functionExtractor,
                            DuplicateDetectionCascade cascade,
                            IngestionJobService ingestionJobService,
                            DuplicateFindingRepository findingRepository,
                            DuplicateReportService reportService) {
        this.functionExtractor = functionExtractor;
        this.cascade = cascade;
        this.ingestionJobService = ingestionJobService;
        this.findingRepository = findingRepository;
        this.reportService = reportService;
    }

    /**
     * Tool 1: find_similar_functions
     * Calculates similarity scores and returns candidate duplicate functions.
     */
    @Tool(description = "Given source code and repository name, calculates and returns structurally or semantically similar functions already indexed (file path, function name, cosine similarity score). Call this before writing new code.")
    public List<SimilarFunctionResult> findSimilarFunctions(String repository, String sourceCode, String filePath) {
        String tenantId = TenantContext.get();
        CodeUnit probe = functionExtractor.extractSingle(sourceCode, filePath, repository);
        DetectionResult result = cascade.detect(probe, tenantId, repository);

        if (!result.matchFound()) {
            return List.of();
        }

        List<SimilarFunctionResult> results = new ArrayList<>();
        for (CodeUnit candidate : result.candidates()) {
            results.add(new SimilarFunctionResult(
                candidate.getFilePath(),
                candidate.getFunctionName(),
                result.similarityScore(),
                candidate.getLineCount(),
                result.matchTier()
            ));
        }
        return results;
    }

    /**
     * Tool 2: ingest_files
     * Starts asynchronous background indexing of a batch of files (20-50 files recommended).
     */
    @Tool(description = "Starts indexing a batch of files into this repository's duplicate index. Pass files in chunks of 20-50 files. Returns immediately with a job ID.")
    public IngestJobHandle ingestFiles(String repository, List<FileInput> files) {
        String tenantId = TenantContext.get();
        IngestionJob job = ingestionJobService.create(tenantId, repository, files.size());
        ingestionJobService.processAsync(job.getId(), files, tenantId, repository, "mcp-agent");
        return new IngestJobHandle(job.getId(), "STARTED", files.size(), "Indexing batch in background");
    }

    /**
     * Tool 3: get_ingestion_status
     * Checks progress and metrics of an indexing job.
     */
    @Tool(description = "Checks the progress and statistics of an indexing job started with ingest_files.")
    public IngestionJob getIngestionStatus(String jobId) {
        return ingestionJobService.get(UUID.fromString(jobId));
    }

    /**
     * Tool 4: record_duplicate
     * Called by the client AI agent (Claude Code / Cursor) when it confirms duplicate logic using its own model.
     */
    @Tool(description = "Records a confirmed duplicate finding in this repository after your agent confirms duplicate logic. Persists finding and metrics.")
    public DuplicateFinding recordDuplicate(
            String repository,
            String newFilePath,
            String newFunctionName,
            String matchedFilePath,
            String matchedFunctionName,
            double similarityScore,
            String reasoning,
            String commitSha) {

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
     * Tool 5: get_duplicate_report
     * Returns duplication metrics, consolidated lines, and worst offender files for the repository.
     */
    @Tool(description = "Returns a report of confirmed duplicate findings for a repository — total count, estimated lines of duplicated code, and most affected files.")
    public DuplicateReport getDuplicateReport(String repository) {
        String tenantId = TenantContext.get();
        return reportService.summarize(tenantId, repository);
    }
}
