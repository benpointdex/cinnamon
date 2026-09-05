package com.henry.cinnamon.services;

import com.henry.cinnamon.model.CodeUnit;
import com.henry.cinnamon.model.FileInput;
import com.henry.cinnamon.model.IngestionJob;
import com.henry.cinnamon.parser.FunctionExtractor;
import com.henry.cinnamon.parser.SourceFileFilter;
import com.henry.cinnamon.repository.CodeUnitRepository;
import com.henry.cinnamon.repository.IngestionJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IngestionJobService {

    private static final Logger log = LoggerFactory.getLogger(IngestionJobService.class);
    private static final int BATCH_SIZE = 50;

    private final FunctionExtractor functionExtractor;
    private final SourceFileFilter sourceFileFilter;
    private final CodeUnitRepository codeUnitRepository;
    private final IngestionJobRepository jobRepository;
    private final EmbeddingModel embeddingModel;
    private final MeterRegistry meterRegistry;

    @Autowired
    public IngestionJobService(FunctionExtractor functionExtractor,
                               SourceFileFilter sourceFileFilter,
                               CodeUnitRepository codeUnitRepository,
                               IngestionJobRepository jobRepository,
                               @Lazy EmbeddingModel embeddingModel,
                               MeterRegistry meterRegistry) {
        this.functionExtractor = functionExtractor;
        this.sourceFileFilter = sourceFileFilter;
        this.codeUnitRepository = codeUnitRepository;
        this.jobRepository = jobRepository;
        this.embeddingModel = embeddingModel;
        this.meterRegistry = meterRegistry != null ? meterRegistry : new SimpleMeterRegistry();
    }

    public IngestionJobService(FunctionExtractor functionExtractor,
                               SourceFileFilter sourceFileFilter,
                               CodeUnitRepository codeUnitRepository,
                               IngestionJobRepository jobRepository,
                               @Lazy EmbeddingModel embeddingModel) {
        this(functionExtractor, sourceFileFilter, codeUnitRepository, jobRepository, embeddingModel, new SimpleMeterRegistry());
    }

    /**
     * Creates and persists a new tracking job record.
     */
    public IngestionJob create(String tenantId, String repository, int totalFiles) {
        IngestionJob job = new IngestionJob(tenantId, repository, totalFiles);
        return jobRepository.save(job);
    }

    /**
     * Retrieves an ingestion job by ID.
     */
    public IngestionJob get(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Ingestion job not found: " + jobId));
    }

    /**
     * Runs asynchronously on the background thread pool so tool calls return immediately.
     */
    @Async("ingestionExecutor")
    public void processAsync(UUID jobId, List<FileInput> files, String tenantId, String repository, String authorTool) {
        IngestionJob job = get(jobId);
        job.setStatus("RUNNING");
        jobRepository.save(job);

        List<CodeUnit> pendingBatch = new ArrayList<>();
        int indexedCount = 0;
        int skippedCount = 0;
        int processedFilesCount = 0;

        try {
            MDC.put("jobId", jobId.toString());
            MDC.put("tenantId", tenantId);
            MDC.put("repo", repository);

            for (FileInput file : files) {
                // 1. Delete previous stale records for this modified file
                codeUnitRepository.deleteStaleFileUnits(tenantId, repository, file.path());

                // 2. Extract functions
                List<CodeUnit> extracted = functionExtractor.extractFunctions(file.content(), file.path(), repository);

                for (CodeUnit unit : extracted) {
                    // 3. Filter out trivial boilerplate (< 5 lines without logic, empty bodies, simple getters/setters)
                    if (!sourceFileFilter.isMeaningfulFunction(unit.getLineCount(), unit.getNormalizedText())) {
                        continue;
                    }

                    // 4. Skip unchanged function if content hash already exists
                    if (codeUnitRepository.existsByTenantIdAndRepositoryAndContentHash(tenantId, repository, unit.getContentHash())) {
                        skippedCount++;
                        continue;
                    }

                    unit.setTenantId(tenantId);
                    unit.setRepository(repository);
                    unit.setAuthorTool(authorTool);
                    pendingBatch.add(unit);
                }

                processedFilesCount++;

                // 5. Batch embed & save in chunks of 50
                if (pendingBatch.size() >= BATCH_SIZE) {
                    embedAndSaveBatch(pendingBatch);
                    indexedCount += pendingBatch.size();
                    pendingBatch.clear();

                    // Update live progress in DB
                    job.setProcessedFiles(processedFilesCount);
                    job.setFunctionsIndexed(indexedCount);
                    job.setFunctionsSkippedUnchanged(skippedCount);
                    jobRepository.save(job);
                } else if (processedFilesCount % 5 == 0) {
                    job.setProcessedFiles(processedFilesCount);
                    jobRepository.save(job);
                }
            }

            // Save any remaining units
            if (!pendingBatch.isEmpty()) {
                embedAndSaveBatch(pendingBatch);
                indexedCount += pendingBatch.size();
                pendingBatch.clear();
            }

            job.setStatus("COMPLETED");
            job.setCompletedAt(Instant.now());
        } catch (Exception e) {
            log.error("Error during ingestion job {}", jobId, e);
            job.setStatus("FAILED");
        } finally {
            try {
                job.setProcessedFiles(processedFilesCount);
                job.setFunctionsIndexed(indexedCount);
                job.setFunctionsSkippedUnchanged(skippedCount);
                jobRepository.save(job);
            } finally {
                MDC.clear();
            }
        }
    }

    private void embedAndSaveBatch(List<CodeUnit> units) {
        if (units == null || units.isEmpty()) {
            return;
        }

        List<String> normalizedTexts = units.stream()
                .map(CodeUnit::getNormalizedText)
                .toList();

        // Batch matrix inference via Spring AI ONNX Transformers
        Timer.Sample embSample = Timer.start(meterRegistry);
        List<float[]> vectors = embeddingModel.embed(normalizedTexts);
        embSample.stop(meterRegistry.timer("dejacode.embedding.batch.duration", "batch_size", String.valueOf(units.size())));

        for (int i = 0; i < units.size(); i++) {
            if (i < vectors.size()) {
                units.get(i).setEmbedding(vectors.get(i));
            }
        }

        // Bulk insert into Postgres
        codeUnitRepository.saveAll(units);
    }
}
