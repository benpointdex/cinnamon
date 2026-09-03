package com.henry.cinnamon.parser;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SourceFileFilterTest {

    private final SourceFileFilter filter = new SourceFileFilter();

    @Test
    void shouldIgnoreExcludedDirectories() {
        assertFalse(filter.shouldTraverseDirectory(Path.of("node_modules")));
        assertFalse(filter.shouldTraverseDirectory(Path.of("dist")));
        assertFalse(filter.shouldTraverseDirectory(Path.of("target")));
        assertFalse(filter.shouldTraverseDirectory(Path.of(".next")));
        assertFalse(filter.shouldTraverseDirectory(Path.of(".git")));

        assertTrue(filter.shouldTraverseDirectory(Path.of("src")));
        assertTrue(filter.shouldTraverseDirectory(Path.of("lib")));
        assertTrue(filter.shouldTraverseDirectory(Path.of("app")));
        assertTrue(filter.shouldTraverseDirectory(Path.of("components")));
    }

    @Test
    void shouldFilterNonEligibleCodeFiles() {
        // Ignored: tests, minified, declarations, oversized
        assertFalse(filter.isEligibleCodeFile(Path.of("utils.test.ts"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("service.spec.js"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("index.d.ts"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("bundle.min.js"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("MainTest.java"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("UserService.java"), 600_000)); // > 500KB

        // Accepted code files
        assertTrue(filter.isEligibleCodeFile(Path.of("UserService.java"), 15_000));
        assertTrue(filter.isEligibleCodeFile(Path.of("calculator.ts"), 8_000));
        assertTrue(filter.isEligibleCodeFile(Path.of("Header.tsx"), 12_000));
        assertTrue(filter.isEligibleCodeFile(Path.of("auth.js"), 5_000));
    }

    @Test
    void shouldSuppressTrivialBoilerplateFunctions() {
        assertFalse(filter.isMeaningfulFunction(1));
        assertFalse(filter.isMeaningfulFunction(4));
        assertTrue(filter.isMeaningfulFunction(5));
        assertTrue(filter.isMeaningfulFunction(20));
    }
}
