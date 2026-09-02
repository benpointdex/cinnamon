package com.henry.cinnamon.services;


import com.henry.cinnamon.model.CreateTenantRequest;
import com.henry.cinnamon.model.Tenant;
import com.henry.cinnamon.model.TenantCreatedResponse;
import com.henry.cinnamon.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EmailService emailService;


    public TenantCreatedResponse  createTenant (CreateTenantRequest req){

        if(req.email()==null || req.email().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST , "Email is required");

        }

        String rawApiKey = "dc" + UUID.randomUUID().toString().replace("-","");
        String apiKeyHash = sha256(rawApiKey);
        String verificationCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID().toString());
        tenant.setName(req.name() != null && !req.name().isBlank() ? req.name() : "Developer");
        tenant.setEmail(req.email());
        tenant.setApiKeyHash(apiKeyHash);
        tenant.setVerificationCode(verificationCode);
        tenant.setVerificationCodeExpiresAt(Instant.now().plus(Duration.ofHours(24)));
        tenant.setDailyRequestLimit(50);
        tenantRepository.save(tenant);
        log.info("Verification OTP for {}: {}", req.email(), verificationCode);
        emailService.sendVerificationCode(req.email(), verificationCode);
        return new TenantCreatedResponse(
                tenant.getTenantId(),
                rawApiKey,
                "Save your API key securely. It will not be shown again."
        );




    }


    /**
     * Verifies the 6-digit OTP code and upgrades the daily request limit.
     */
    public void verifyTenant(String tenantId, String code) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
        if (tenant.getVerificationCodeExpiresAt() != null
                && Instant.now().isAfter(tenant.getVerificationCodeExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code has expired");
        }
        if (!code.equals(tenant.getVerificationCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification code");
        }
        tenant.setEmailVerified(true);
        tenant.setDailyRequestLimit(1000);
        tenantRepository.save(tenant);
    }


    /**
     * Resolves a tenant by calculating the SHA-256 hash of the raw API key.
     */

    public Optional<Tenant> resolveByApiKey(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            return Optional.empty();
        }
        String hash = sha256(rawApiKey.trim());
        return tenantRepository.findByApiKeyHash(hash);
    }
    public Optional<Tenant> getTenantById(String tenantId) {
        return tenantRepository.findById(tenantId);
    }

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
