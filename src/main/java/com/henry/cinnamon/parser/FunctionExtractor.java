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
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class FunctionExtractor {

    private final LanguageAdapterRegistry registry;
    private final IdentifierNormalizer normalizer;

    public FunctionExtractor(LanguageAdapterRegistry registry, IdentifierNormalizer normalizer) {
        this.registry = registry;
        this.normalizer = normalizer;
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
            walk(tree.getRootNode(), sourceCode, filePath, repository, lang, units);
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
                      LanguageAdapter lang, List<CodeUnit> units) {
        if (node == null || node.isNull()) {
            return;
        }

        if (lang.isFunctionNode(node)) {
            String normalizedText = normalizer.normalize(node, source, lang);

            CodeUnit unit = new CodeUnit();
            unit.setRepository(repository);
            unit.setFilePath(filePath);
            unit.setFunctionName(lang.extractFunctionName(node, source));
            unit.setNormalizedText(normalizedText);
            unit.setContentHash(sha256(normalizedText));
            unit.setLineCount(countLines(source, node));
            unit.setLastModified(Instant.now());

            units.add(unit);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            walk(node.getChild(i), source, filePath, repository, lang, units);
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
