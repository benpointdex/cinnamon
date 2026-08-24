package com.henry.cinnamon.model;

import java.util.UUID;

public record IngestJobHandle(
    UUID jobId,
    String status,
    int totalFiles,
    String message
) {}
