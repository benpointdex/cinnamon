package com.henry.cinnamon.model;

public record TenantCreatedResponse(
    String tenantId,
    String rawApiKey,
    String message
) {}
