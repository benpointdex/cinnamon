package com.henry.cinnamon.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tenants", indexes = {
    @Index(name = "idx_tenants_api_key_hash", columnList = "apiKeyHash", unique = true)
})
public class Tenant {

    @Id
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    // SHA-256 hex string for fast O(1) indexed lookups
    @Column(nullable = false, unique = true)
    private String apiKeyHash;

    private boolean emailVerified = false;
    private String verificationCode;
    private Instant verificationCodeExpiresAt;
    private int dailyRequestLimit = 50;

    private Instant createdAt = Instant.now();

    public Tenant() {}

    // Getters and Setters
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getApiKeyHash() { return apiKeyHash; }
    public void setApiKeyHash(String apiKeyHash) { this.apiKeyHash = apiKeyHash; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public Instant getVerificationCodeExpiresAt() { return verificationCodeExpiresAt; }
    public void setVerificationCodeExpiresAt(Instant verificationCodeExpiresAt) { this.verificationCodeExpiresAt = verificationCodeExpiresAt; }
    public int getDailyRequestLimit() { return dailyRequestLimit; }
    public void setDailyRequestLimit(int dailyRequestLimit) { this.dailyRequestLimit = dailyRequestLimit; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
