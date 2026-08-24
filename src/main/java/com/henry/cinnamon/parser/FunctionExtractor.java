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
     */
    public List<CodeUnit> extractFunctions(String sourceCode, String filePath, String repository) {
        LanguageAdapter lang = registry.forFile(filePath)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file type for path: " + filePath));

        TSParser parser = new TSParser();
        parser.setLanguage(lang.treeSitterLanguage());
        TSTree tree = parser.parseString(null, sourceCode);

        List<CodeUnit> units = new ArrayList<>();
        walk(tree.getRootNode(), sourceCode, filePath, repository, lang, units);
        return units;
    }

    /**
     * Extracts a single function probe from a standalone code snippet.
     */
    public CodeUnit extractSingle(String sourceCode, String filePath, String repository) {
        List<CodeUnit> units = extractFunctions(sourceCode, filePath, repository);
        if (units.isEmpty()) {
            throw new IllegalArgumentException("No function/method found in the provided snippet for: " + filePath);
        }
        return units.get(0);
    }

    private void walk(TSNode node, String source, String filePath, String repository,
                      LanguageAdapter lang, List<CodeUnit> units) {
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
        String snippet = source.substring(node.getStartByte(), node.getEndByte());
        return (int) snippet.lines().count();
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
