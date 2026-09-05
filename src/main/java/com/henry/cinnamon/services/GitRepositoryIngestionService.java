package com.henry.cinnamon.services;

import com.henry.cinnamon.model.CodeUnit;
import com.henry.cinnamon.model.IngestionJob;
import com.henry.cinnamon.parser.FunctionExtractor;
import com.henry.cinnamon.parser.SourceFileFilter;
import com.henry.cinnamon.repository.CodeUnitRepository;
import com.henry.cinnamon.repository.IngestionJobRepository;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;

@Service
public class GitRepositoryIngestionService {

    private static final Logger log = LoggerFactory.getLogger(GitRepositoryIngestionService.class);
    private static final int BATCH_SIZE = 50;
    private static final int CLONE_TIMEOUT_SECONDS = 60;

    private final SourceFileFilter sourceFileFilter;
    private final FunctionExtractor functionExtractor;
    private final CodeUnitRepository codeUnitRepository;
    private final IngestionJobRepository jobRepository;
    private final EmbeddingModel embeddingModel;
    private final MeterRegistry meterRegistry;

    @Autowired
    public GitRepositoryIngestionService(SourceFileFilter sourceFileFilter,
                                         FunctionExtractor functionExtractor,
                                         CodeUnitRepository codeUnitRepository,
                                         IngestionJobRepository jobRepository,
                                         @Lazy EmbeddingModel embeddingModel,
                                         MeterRegistry meterRegistry) {
        this.sourceFileFilter = sourceFileFilter;
        this.functionExtractor = functionExtractor;
        this.codeUnitRepository = codeUnitRepository;
        this.jobRepository = jobRepository;
        this.embeddingModel = embeddingModel;
        this.meterRegistry = meterRegistry != null ? meterRegistry : new SimpleMeterRegistry();
    }

    public GitRepositoryIngestionService(SourceFileFilter sourceFileFilter,
                                         FunctionExtractor functionExtractor,
                                         CodeUnitRepository codeUnitRepository,
                                         IngestionJobRepository jobRepository,
                                         @Lazy EmbeddingModel embeddingModel) {
        this(sourceFileFilter, functionExtractor, codeUnitRepository, jobRepository, embeddingModel, new SimpleMeterRegistry());
    }

    /**
     * Creates and initializes a tracking job for Git-native ingestion.
     */
    public IngestionJob createJob(String tenantId, String repository) {
        IngestionJob job = new IngestionJob(tenantId, repository, 0);
        job.setStatus("INITIALIZING");
        return jobRepository.save(job);
    }

    /**
     * Executes shallow Git clone, smart filtering, AST extraction, and batched vector indexing asynchronously.
     * Supports both public and private repositories securely via GitHub CLI tokens or PATs.
     */
    @Async("ingestionExecutor")
    public void processGitCloneAsync(UUID jobId, String repoUrl, String repository, String branch,
                                     String githubToken, List<String> sourceDirs, String tenantId) {

        IngestionJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        Path tempDir = null;
        int processedFiles = 0;
        int indexedFunctions = 0;
        int skippedFunctions = 0;

        try {
            MDC.put("jobId", jobId.toString());
            MDC.put("tenantId", tenantId);
            MDC.put("repo", repository);

            tempDir = Files.createTempDirectory("dejacode-git-");
            String normalizedUrl = normalizeGitUrl(repoUrl);

            log.info("Starting shallow Git clone for {} into temporary directory {}", normalizedUrl, tempDir);

            job.setStatus("CLONING");
            jobRepository.save(job);

            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(normalizedUrl)
                    .setDirectory(tempDir.toFile())
                    .setDepth(1)
                    .setTimeout(CLONE_TIMEOUT_SECONDS)
                    .setCloneAllBranches(false);

            if (branch != null && !branch.isBlank()) {
                cloneCommand.setBranch(branch.trim());
            }

            // Securely configure credentials for private repositories
            if (githubToken != null && !githubToken.isBlank()) {
                String cleanToken = githubToken.trim();
                if (cleanToken.contains(":")) {
                    String[] parts = cleanToken.split(":", 2);
                    cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(parts[0], parts[1]));
                } else {
                    // Standard GitHub PAT or 'gh auth token' OAuth token
                    cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("x-access-token", cleanToken));
                }
            }

            try (Git git = cloneCommand.call()) {
                log.info("Shallow Git clone completed successfully for {}", normalizedUrl);
            }

            job.setStatus("INDEXING");
            jobRepository.save(job);

            // 1. Discover all eligible code files using SourceFileFilter
            List<Path> candidateFiles = new ArrayList<>();
            final Path rootPath = tempDir;

            Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!sourceFileFilter.shouldTraverseDirectory(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (sourceFileFilter.isEligibleCodeFile(file, attrs.size())) {
                        String relativePath = rootPath.relativize(file).toString().replace('\\', '/');
                        if (matchesSourceDirs(relativePath, sourceDirs)) {
                            candidateFiles.add(file);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            job.setTotalFiles(candidateFiles.size());
            jobRepository.save(job);
            log.info("Discovered {} candidate code files to index for repository {}", candidateFiles.size(), repository);

            // 2. Stream, extract, filter, and batch embed
            List<CodeUnit> pendingBatch = new ArrayList<>();

            for (Path file : candidateFiles) {
                String relativePath = rootPath.relativize(file).toString().replace('\\', '/');
                String content;
                try {
                    content = Files.readString(file, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    continue; // Skip any file that cannot be decoded as UTF-8
                }

                // Delete previous stale records for this file path
                codeUnitRepository.deleteStaleFileUnits(tenantId, repository, relativePath);

                List<CodeUnit> extracted = functionExtractor.extractFunctions(content, relativePath, repository);

                for (CodeUnit unit : extracted) {
                    // Filter out trivial boilerplate (< 5 lines without logic, empty bodies, simple getters/setters)
                    if (!sourceFileFilter.isMeaningfulFunction(unit.getLineCount(), unit.getNormalizedText())) {
                        continue;
                    }

                    // Skip unchanged function if content hash already exists
                    if (codeUnitRepository.existsByTenantIdAndRepositoryAndContentHash(
                            tenantId, repository, unit.getContentHash())) {
                        skippedFunctions++;
                        continue;
                    }

                    unit.setTenantId(tenantId);
                    unit.setRepository(repository);
                    unit.setAuthorTool("git-clone-ingest");
                    pendingBatch.add(unit);
                }

                processedFiles++;

                if (pendingBatch.size() >= BATCH_SIZE) {
                    embedAndSaveBatch(pendingBatch);
                    indexedFunctions += pendingBatch.size();
                    pendingBatch.clear();

                    // Update live progress in DB
                    job.setProcessedFiles(processedFiles);
                    job.setFunctionsIndexed(indexedFunctions);
                    job.setFunctionsSkippedUnchanged(skippedFunctions);
                    jobRepository.save(job);
                } else if (processedFiles % 5 == 0) {
                    job.setProcessedFiles(processedFiles);
                    jobRepository.save(job);
                }
            }

            if (!pendingBatch.isEmpty()) {
                embedAndSaveBatch(pendingBatch);
                indexedFunctions += pendingBatch.size();
                pendingBatch.clear();
            }

            job.setStatus("COMPLETED");
            job.setCompletedAt(Instant.now());
            log.info("Git ingestion finished for {}. Files: {}, Functions indexed: {}, Skipped: {}",
                    repository, processedFiles, indexedFunctions, skippedFunctions);

        } catch (TransportException te) {
            log.error("Authentication or network error while cloning private repository {}: {}", repoUrl, te.getMessage());
            job.setStatus("FAILED");
        } catch (Exception e) {
            log.error("Fatal error during Git-native ingestion for repo {}", repoUrl, e);
            job.setStatus("FAILED");
        } finally {
            try {
                job.setProcessedFiles(processedFiles);
                job.setFunctionsIndexed(indexedFunctions);
                job.setFunctionsSkippedUnchanged(skippedFunctions);
                jobRepository.save(job);

                // Guaranteed cleanup of temporary cloned directory to prevent disk exhaustion
                if (tempDir != null) {
                    try {
                        FileSystemUtils.deleteRecursively(tempDir);
                        log.debug("Cleaned up temporary cloned repository folder at {}", tempDir);
                    } catch (IOException e) {
                        log.warn("Failed to delete temporary clone directory {}", tempDir, e);
                    }
                }
            } finally {
                MDC.clear();
            }
        }
    }

    private String normalizeGitUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String trimmed = url.trim();
        // Convert SSH URL format git@github.com:org/repo.git to HTTPS https://github.com/org/repo.git
        if (trimmed.startsWith("git@github.com:")) {
            return "https://github.com/" + trimmed.substring("git@github.com:".length());
        }
        return trimmed;
    }

    private boolean matchesSourceDirs(String relativePath, List<String> sourceDirs) {
        return sourceFileFilter.matchesSourceDirs(relativePath, sourceDirs);
    }

    private void embedAndSaveBatch(List<CodeUnit> units) {
        if (units == null || units.isEmpty()) {
            return;
        }

        List<String> normalizedTexts = units.stream()
                .map(CodeUnit::getNormalizedText)
                .toList();

        // Batched SIMD Matrix inference via Spring AI ONNX Transformers
        Timer.Sample embSample = Timer.start(meterRegistry);
        List<float[]> vectors = embeddingModel.embed(normalizedTexts);
        embSample.stop(meterRegistry.timer("dejacode.embedding.batch.duration", "batch_size", String.valueOf(units.size())));

        for (int i = 0; i < units.size(); i++) {
            if (i < vectors.size()) {
                units.get(i).setEmbedding(vectors.get(i));
            }
        }

        codeUnitRepository.saveAll(units);
    }
}
