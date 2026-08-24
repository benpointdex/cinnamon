package com.henry.cinnamon.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "code_units", indexes = {
        @Index(name = "idx_code_units_tenant_repo", columnList = "tenantId, repository"),
        @Index(name = "idx_code_units_tenant_hash", columnList = "tenantId, contentHash")
})
public class CodeUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String repository;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String functionName;

    @Column(nullable = false)
    private String contentHash;

    @Column(columnDefinition = "vector(384)")
    private float[] embedding;

    private String authorTool;
    private int lineCount;
    private Instant lastModified;

    // Transient (in-memory only): used by the embedding model, never persisted to DB
    @Transient
    private String normalizedText;

    // Constructors
    public CodeUnit() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRepository() { return repository; }
    public void setRepository(String repository) { this.repository = repository; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    public String getAuthorTool() { return authorTool; }
    public void setAuthorTool(String authorTool) { this.authorTool = authorTool; }
    public int getLineCount() { return lineCount; }
    public void setLineCount(int lineCount) { this.lineCount = lineCount; }
    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
    public String getNormalizedText() { return normalizedText; }
    public void setNormalizedText(String normalizedText) { this.normalizedText = normalizedText; }
}
