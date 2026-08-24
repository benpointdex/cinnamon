package com.henry.cinnamon.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingestion_jobs", indexes = {
    @Index(name = "idx_ingestion_jobs_tenant", columnList = "tenantId")
})
public class IngestionJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String repository;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING | RUNNING | COMPLETED | FAILED

    private int totalFiles;
    private int processedFiles = 0;
    private int functionsIndexed = 0;
    private int functionsSkippedUnchanged = 0;

    private Instant startedAt;
    private Instant completedAt;

    public IngestionJob() {}

    public IngestionJob(String tenantId, String repository, int totalFiles) {
        this.tenantId = tenantId;
        this.repository = repository;
        this.totalFiles = totalFiles;
        this.status = "PENDING";
        this.startedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRepository() { return repository; }
    public void setRepository(String repository) { this.repository = repository; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTotalFiles() { return totalFiles; }
    public void setTotalFiles(int totalFiles) { this.totalFiles = totalFiles; }
    public int getProcessedFiles() { return processedFiles; }
    public void setProcessedFiles(int processedFiles) { this.processedFiles = processedFiles; }
    public int getFunctionsIndexed() { return functionsIndexed; }
    public void setFunctionsIndexed(int functionsIndexed) { this.functionsIndexed = functionsIndexed; }
    public int getFunctionsSkippedUnchanged() { return functionsSkippedUnchanged; }
    public void setFunctionsSkippedUnchanged(int functionsSkippedUnchanged) { this.functionsSkippedUnchanged = functionsSkippedUnchanged; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
