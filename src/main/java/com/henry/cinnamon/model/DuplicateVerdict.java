package com.henry.cinnamon.model;

public record DuplicateVerdict(
    boolean duplicate,
    double confidence,
    String reasoning
) {}
