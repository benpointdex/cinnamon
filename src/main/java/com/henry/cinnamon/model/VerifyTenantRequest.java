package com.henry.cinnamon.model;

public record VerifyTenantRequest(
    String tenantId,
    String email,
    String code
) {}
