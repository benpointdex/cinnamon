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
    private final GitRepositoryIngestionService gitIngestionService;

    public DejaCodeMcpTools(FunctionExtractor functionExtractor,
                            DuplicateDetectionCascade cascade,
                            IngestionJobService ingestionJobService,
                            DuplicateFindingRepository findingRepository,
                            DuplicateReportService reportService,
                            DuplicateClusterScanner clusterScanner,
                            GitRepositoryIngestionService gitIngestionService) {
        this.functionExtractor = functionExtractor;
        this.cascade = cascade;
        this.ingestionJobService = ingestionJobService;
        this.findingRepository = findingRepository;
        this.reportService = reportService;
        this.clusterScanner = clusterScanner;
        this.gitIngestionService = gitIngestionService;
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

        try {
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
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Tool 2: scan_repository_duplicates
     * Performs a server-side whole-repository vector self-join scan across all indexed code units (< 50ms).
     */
    @Tool(description = "Scans the entire repository in one shot using in-database pgvector similarity. Aggregates pairwise duplicates into N-way clusters with severity levels (CRITICAL, HIGH), line savings, and code snippets.")
    public RepositoryDuplicateScanResponse scanRepositoryDuplicates(
            @ToolParam(description = "Repository name e.g. 'cinnamon'") String repository,
            @ToolParam(description = "Minimum cosine similarity threshold (e.g. 0.85 for 85%). Default is 0.85", required = false) Double minSimilarity,
            @ToolParam(description = "Optional folder path filter e.g. 'src/routes/' to scope the scan", required = false) String pathPrefix,
            @ToolParam(description = "Maximum number of duplicate pairs to analyze. Default is 100", required = false) Integer limit) {

        String tenantId = TenantContext.get();
        return clusterScanner.scan(tenantId, repository, minSimilarity, pathPrefix, limit);
    }

    /**
     * Tool 3: ingest_github_repository (NEW)
     * Performs a one-click shallow Git clone, smart filtering, and vector indexing directly on the server.
     */
    @Tool(description = "Clones and indexes an entire public or private GitHub repository directly on the server in one click with zero code transferred over MCP. For private repos, provide githubToken (which can be obtained by running 'gh auth token' or a GitHub PAT).")
    public IngestJobHandle ingestGithubRepository(
            @ToolParam(description = "Git repository HTTPS or SSH clone URL (e.g. https://github.com/my-org/my-repo)") String repoUrl,
            @ToolParam(description = "Target repository name in DejaCode (defaults to repository name extracted from URL)", required = false) String repository,
            @ToolParam(description = "Branch name (defaults to 'main')", required = false) String branch,
            @ToolParam(description = "GitHub Access Token for private repositories (can be obtained via 'gh auth token' or a GitHub PAT)", required = false) String githubToken,
            @ToolParam(description = "Optional list of source folders to restrict scanning to e.g. ['src', 'lib']", required = false) List<String> sourceDirs) {

        String tenantId = TenantContext.get();
        String repoName = (repository != null && !repository.isBlank())
                ? repository.trim()
                : extractRepoNameFromUrl(repoUrl);

        IngestionJob job = gitIngestionService.createJob(tenantId, repoName);
        gitIngestionService.processGitCloneAsync(job.getId(), repoUrl, repoName, branch, githubToken, sourceDirs, tenantId);

        return new IngestJobHandle(job.getId(), "STARTED", 0,
                "Shallow cloning and indexing repository '" + repoName + "' in background with smart filtering");
    }

    /**
     * Tool 4: ingest_files
     * Starts asynchronous background indexing of a list of files.
     */
    @Tool(description = "Starts indexing a list of files into DejaCode. Handles payloads with internal auto-partitioning. Returns immediately with a job ID.")
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
     * Tool 5: get_ingestion_status
     * Checks progress and metrics of an indexing job.
     */
    @Tool(description = "Checks the progress and statistics of an indexing job started with ingest_files or ingest_github_repository.")
    public IngestionJob getIngestionStatus(
            @ToolParam(description = "Job ID returned by ingest_files or ingest_github_repository") String jobId) {
        return ingestionJobService.get(UUID.fromString(jobId));
    }

    /**
     * Tool 6: record_duplicate
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
     * Tool 7: get_duplicate_report
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

    private String extractRepoNameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "default-repo";
        }
        String clean = url.trim();
        if (clean.endsWith(".git")) {
            clean = clean.substring(0, clean.length() - 4);
        }
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < clean.length() - 1) {
            return clean.substring(lastSlash + 1);
        }
        return "repo-" + System.currentTimeMillis();
    }
}
