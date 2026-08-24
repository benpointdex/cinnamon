package com.henry.cinnamon.model;

public record CodeSnippetPair(
    String newCode,
    String newFilePath,
    String newFunctionName,
    String matchedCode,
    String matchedFilePath,
    String matchedFunctionName,
    String commitSha
) {}
