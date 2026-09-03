package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;

@Component
public class SourceFileFilter {

    private static final long MAX_FILE_SIZE_BYTES = 512_000; // 500 KB limit to prevent OOM on giant files
    public static final int MIN_FUNCTION_LINES = 5; // Suppress trivial 1-4 line boilerplate

    private static final Set<String> IGNORED_DIRS = Set.of(
            "node_modules", "dist", "build", ".git", "target", ".next",
            "out", "coverage", "vendor", ".idea", ".vscode", "tmp", "temp",
            ".gradle", "bin", "__pycache__", "obj"
    );

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "java", "ts", "tsx", "js", "jsx", "mjs", "cjs"
    );

    /**
     * Determines if a directory path should be traversed or skipped entirely.
     */
    public boolean shouldTraverseDirectory(Path dir) {
        String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
        return !IGNORED_DIRS.contains(dirName.toLowerCase());
    }

    /**
     * Determines if a source file is eligible for AST parsing and semantic duplicate indexing.
     */
    public boolean isEligibleCodeFile(Path file, long fileSizeBytes) {
        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            return false;
        }

        String fileName = file.getFileName() != null ? file.getFileName().toString() : "";
        String lowerName = fileName.toLowerCase();

        // 1. Check extension
        int dotIndex = lowerName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == lowerName.length() - 1) {
            return false;
        }
        String ext = lowerName.substring(dotIndex + 1);
        if (!SUPPORTED_EXTENSIONS.contains(ext)) {
            return false;
        }

        // 2. Filter out minified, bundled, or declaration files
        if (lowerName.endsWith(".d.ts")
                || lowerName.endsWith(".min.js")
                || lowerName.endsWith(".bundle.js")
                || lowerName.endsWith(".chunk.js")
                || lowerName.endsWith(".map")) {
            return false;
        }

        // 3. Filter out unit and integration test files
        if (lowerName.contains(".test.")
                || lowerName.contains(".spec.")
                || lowerName.contains("_test.")
                || lowerName.endsWith("test.java")
                || lowerName.endsWith("tests.java")) {
            return false;
        }

        return true;
    }

    /**
     * Checks if an extracted function has sufficient logic depth to warrant AI vector indexing.
     */
    public boolean isMeaningfulFunction(int lineCount) {
        return lineCount >= MIN_FUNCTION_LINES;
    }
}
