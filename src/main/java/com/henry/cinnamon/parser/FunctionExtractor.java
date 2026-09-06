package com.henry.cinnamon.parser;

import com.henry.cinnamon.model.CodeUnit;
import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TSTree;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FunctionExtractor {

    private final LanguageAdapterRegistry registry;
    private final IdentifierNormalizer normalizer;
    private final MeterRegistry meterRegistry;

    @Autowired
    public FunctionExtractor(LanguageAdapterRegistry registry, IdentifierNormalizer normalizer, MeterRegistry meterRegistry) {
        this.registry = registry;
        this.normalizer = normalizer;
        this.meterRegistry = meterRegistry != null ? meterRegistry : new SimpleMeterRegistry();
    }

    public FunctionExtractor(LanguageAdapterRegistry registry, IdentifierNormalizer normalizer) {
        this(registry, normalizer, new SimpleMeterRegistry());
    }

    /**
     * Extracts all functions/methods from a source code file.
     * Gracefully returns an empty list if file extension is unsupported (e.g., .md, .json, .yml).
     */
    public List<CodeUnit> extractFunctions(String sourceCode, String filePath, String repository) {
        if (sourceCode == null || sourceCode.isBlank() || filePath == null) {
            return List.of();
        }

        Optional<LanguageAdapter> langOpt = registry.forFile(filePath);
        if (langOpt.isEmpty()) {
            return List.of(); // Safely skip unsupported file types without throwing exceptions
        }

        LanguageAdapter lang = langOpt.get();

        try {
            TSParser parser = new TSParser();
            parser.setLanguage(lang.treeSitterLanguage());
            TSTree tree = parser.parseString(null, sourceCode);
            if (tree == null || tree.getRootNode() == null) {
                return List.of();
            }

            List<CodeUnit> units = new ArrayList<>();
            Set<String> usedNames = new HashSet<>();
            walk(tree.getRootNode(), sourceCode, filePath, repository, lang, units, usedNames);
            if (!units.isEmpty()) {
                String langName = lang.getClass().getSimpleName().replace("LanguageAdapter", "").toLowerCase();
                meterRegistry.counter("cinnamon.parser.functions.extracted", "language", langName).increment(units.size());
            }
            return units;
        } catch (Exception e) {
            // Guard against any malformed AST syntax error
            return List.of();
        }
    }

    /**
     * Extracts a single function probe from a standalone code snippet.
     * Returns Optional.empty() if no function syntax is detected.
     */
    public Optional<CodeUnit> extractSingle(String sourceCode, String filePath, String repository) {
        List<CodeUnit> units = extractFunctions(sourceCode, filePath != null ? filePath : "snippet.java", repository);
        if (units.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(units.get(0));
    }

    private void walk(TSNode node, String source, String filePath, String repository,
                      LanguageAdapter lang, List<CodeUnit> units, Set<String> usedNames) {
        if (node == null || node.isNull()) {
            return;
        }

        if (lang.isFunctionNode(node)) {
            String normalizedText = normalizer.normalize(node, source, lang);
            String rawName = lang.extractFunctionName(node, source);
            int startLine = getStartLine(source, node);

            String finalName;
            if (rawName == null || rawName.isBlank() || "anonymous".equalsIgnoreCase(rawName.trim())) {
                finalName = "anonymous$L" + startLine;
            } else if (usedNames.contains(rawName)) {
                finalName = rawName + "$L" + startLine;
            } else {
                finalName = rawName;
            }
            usedNames.add(finalName);

            CodeUnit unit = new CodeUnit();
            unit.setRepository(repository);
            unit.setFilePath(filePath);
            unit.setFunctionName(finalName);
            unit.setNormalizedText(normalizedText);
            unit.setContentHash(sha256(normalizedText));
            unit.setLineCount(countLines(source, node));
            unit.setLastModified(Instant.now());

            units.add(unit);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            walk(node.getChild(i), source, filePath, repository, lang, units, usedNames);
        }
    }

    private int getStartLine(String source, TSNode node) {
        try {
            int startByte = Math.min(Math.max(0, node.getStartByte()), source.length());
            return (int) source.substring(0, startByte).lines().count() + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private int countLines(String source, TSNode node) {
        try {
            String snippet = source.substring(node.getStartByte(), node.getEndByte());
            return (int) snippet.lines().count();
        } catch (Exception e) {
            return 1;
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
