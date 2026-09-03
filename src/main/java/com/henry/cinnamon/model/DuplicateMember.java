package com.henry.cinnamon.model;

public record DuplicateMember(
    String filePath,
    String functionName,
    int lineCount
) {}
