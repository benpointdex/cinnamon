package com.henry.cinnamon.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "duplicate_findings", indexes = {
    @Index(name = "idx_duplicate_findings_tenant_repo", columnList = "tenantId, repository")
})
public class DuplicateFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String repository;

    private String newFilePath;
    private String newFunctionName;
    private String matchedFilePath;
    private String matchedFunctionName;

    private double similarityScore;
    private boolean confirmedDuplicate;

    @Column(columnDefinition = "TEXT")
    private String judgmentReasoning;

    private Instant flaggedAt = Instant.now();
    private String commitSha;

    public DuplicateFinding() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRepository() { return repository; }
    public void setRepository(String repository) { this.repository = repository; }
    public String getNewFilePath() { return newFilePath; }
    public void setNewFilePath(String newFilePath) { this.newFilePath = newFilePath; }
    public String getNewFunctionName() { return newFunctionName; }
    public void setNewFunctionName(String newFunctionName) { this.newFunctionName = newFunctionName; }
    public String getMatchedFilePath() { return matchedFilePath; }
    public void setMatchedFilePath(String matchedFilePath) { this.matchedFilePath = matchedFilePath; }
    public String getMatchedFunctionName() { return matchedFunctionName; }
    public void setMatchedFunctionName(String matchedFunctionName) { this.matchedFunctionName = matchedFunctionName; }
    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
    public boolean isConfirmedDuplicate() { return confirmedDuplicate; }
    public void setConfirmedDuplicate(boolean confirmedDuplicate) { this.confirmedDuplicate = confirmedDuplicate; }
    public String getJudgmentReasoning() { return judgmentReasoning; }
    public void setJudgmentReasoning(String judgmentReasoning) { this.judgmentReasoning = judgmentReasoning; }
    public Instant getFlaggedAt() { return flaggedAt; }
    public void setFlaggedAt(Instant flaggedAt) { this.flaggedAt = flaggedAt; }
    public String getCommitSha() { return commitSha; }
    public void setCommitSha(String commitSha) { this.commitSha = commitSha; }
}
