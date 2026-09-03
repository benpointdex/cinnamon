package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SourceFileFilter {

    public static final long MAX_FILE_SIZE_BYTES = 512_000; // 500 KB limit to prevent OOM on giant/generated files
    public static final int MIN_FUNCTION_LINES = 5;

    // Filter Level 1: Comprehensive Directory Exclusion
    private static final Set<String> IGNORED_DIRS = Set.of(
            "node_modules", "dist", "build", ".git", "target", ".next", ".nuxt", ".turbo",
            "out", "coverage", "vendor", ".idea", ".vscode", "tmp", "temp", ".gradle", "bin",
            "obj", "__pycache__", ".pnpm-store", "bower_components", "logs", ".cache", ".parcel-cache"
    );

    // Filter Level 2: Supported Code Extensions
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "java", "ts", "tsx", "js", "jsx", "mjs", "cjs"
    );

    // Filter Level 3 Regex Patterns for Trivial Boilerplate (Getters, Setters, Empty Bodies)
    // Matches: return this.foo; OR return foo; OR return foo();
    private static final Pattern TRIVIAL_GETTER_PATTERN = Pattern.compile(
            "^.*\\{\\s*return\\s+([a-zA-Z0-9_$.]+)(\\(\\))?\\s*;?\\s*\\}$", Pattern.DOTALL);

    // Matches: this.foo = val; OR foo = val;
    private static final Pattern TRIVIAL_SETTER_PATTERN = Pattern.compile(
            "^.*\\{\\s*([a-zA-Z0-9_$.]+)\\s*=\\s*([a-zA-Z0-9_$.]+)\\s*;?\\s*\\}$", Pattern.DOTALL);

    // Matches arrow getters: const getX = () => this.x; OR () => x
    private static final Pattern TRIVIAL_ARROW_GETTER = Pattern.compile(
            "^.*=>\\s*([a-zA-Z0-9_$.]+)(\\(\\))?\\s*;?$", Pattern.DOTALL);

    /**
     * Filter Level 1: Checks if a directory path should be traversed or skipped entirely.
     */
    public boolean shouldTraverseDirectory(Path dir) {
        if (dir == null || dir.getFileName() == null) {
            return false;
        }
        String dirName = dir.getFileName().toString().toLowerCase();
        return !IGNORED_DIRS.contains(dirName);
    }

    /**
     * Filter Level 2: Validates if a file is an eligible source code file and not a test, minified bundle, or declaration.
     */
    public boolean isEligibleCodeFile(Path file, long fileSizeBytes) {
        if (file == null || fileSizeBytes > MAX_FILE_SIZE_BYTES || fileSizeBytes <= 0) {
            return false;
        }

        String fileName = file.getFileName() != null ? file.getFileName().toString() : "";
        String lowerName = fileName.toLowerCase();

        // 1. Extension check
        int dotIndex = lowerName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == lowerName.length() - 1) {
            return false;
        }
        String ext = lowerName.substring(dotIndex + 1);
        if (!SUPPORTED_EXTENSIONS.contains(ext)) {
            return false;
        }

        // 2. Filter out declaration files, source maps, and minified bundles
        if (lowerName.endsWith(".d.ts")
                || lowerName.endsWith(".d.mts")
                || lowerName.endsWith(".d.cts")
                || lowerName.endsWith(".map")
                || lowerName.endsWith(".min.js")
                || lowerName.endsWith(".min.mjs")
                || lowerName.endsWith(".bundle.js")
                || lowerName.endsWith(".chunk.js")) {
            return false;
        }

        // 3. Filter out unit and integration tests
        if (lowerName.contains(".test.")
                || lowerName.contains(".spec.")
                || lowerName.contains("_test.")
                || lowerName.contains("-test.")
                || lowerName.endsWith("test.java")
                || lowerName.endsWith("tests.java")
                || lowerName.endsWith("testcase.java")
                || lowerName.endsWith("it.java")) {
            return false;
        }

        // 4. Filter out generated, config, and fixture mocks
        if (lowerName.contains(".generated.")
                || lowerName.contains(".g.ts")
                || lowerName.contains(".config.")
                || lowerName.contains(".setup.")
                || lowerName.contains(".mock.")
                || lowerName.contains(".stub.")) {
            return false;
        }

        return true;
    }

    /**
     * Filter Level 3: AST Logic Depth & Boilerplate Filter.
     * Guards against:
     * - Empty functions ({})
     * - Trivial 1-statement getters (return this.x;)
     * - Trivial 1-statement setters (this.x = x;)
     * - Single-line pass-throughs
     * - Allows compact 3-4 line functions if they contain >= 2 distinct logic statements
     */
    public boolean isMeaningfulFunction(int lineCount, String normalizedText) {
        if (lineCount < 2 || normalizedText == null || normalizedText.isBlank()) {
            return false;
        }

        String trimmed = normalizedText.replaceAll("/\\*.*?\\*/", "")
                                       .replaceAll("//.*", "")
                                       .trim();

        // Rejection 1: Empty body {} or { ; }
        if (trimmed.endsWith("{}") || trimmed.endsWith("{ }") || trimmed.endsWith("{\n}")) {
            return false;
        }

        // Rejection 2: Trivial Getters & Setters
        if (TRIVIAL_GETTER_PATTERN.matcher(trimmed).matches()
                || TRIVIAL_SETTER_PATTERN.matcher(trimmed).matches()
                || TRIVIAL_ARROW_GETTER.matcher(trimmed).matches()) {
            return false;
        }

        // Count logical statement delimiters (; or line breaks)
        long statementCount = trimmed.chars().filter(ch -> ch == ';').count();

        // Accept if line count >= 5 OR compact multi-statement logic (>= 2 statements)
        return lineCount >= MIN_FUNCTION_LINES || statementCount >= 2;
    }

    /**
     * Backward-compatible overload checking line count.
     */
    public boolean isMeaningfulFunction(int lineCount) {
        return lineCount >= MIN_FUNCTION_LINES;
    }

    /**
     * Monorepo & Directory Scoping Helper.
     * If sourceDirs is null or empty, automatically traverses all non-ignored project directories.
     */
    public boolean matchesSourceDirs(String relativePath, List<String> sourceDirs) {
        if (sourceDirs == null || sourceDirs.isEmpty()) {
            return true; // Auto-discovery mode: scan all source files across the monorepo
        }
        for (String dir : sourceDirs) {
            String cleanDir = dir.replace('\\', '/').trim();
            if (cleanDir.endsWith("/")) {
                cleanDir = cleanDir.substring(0, cleanDir.length() - 1);
            }
            if (relativePath.startsWith(cleanDir + "/") || relativePath.equalsIgnoreCase(cleanDir)) {
                return true;
            }
        }
        return false;
    }
}
