package com.henry.cinnamon.parser;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

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
        assertFalse(filter.shouldTraverseDirectory(Path.of(".turbo")));
        assertFalse(filter.shouldTraverseDirectory(Path.of("coverage")));

        assertTrue(filter.shouldTraverseDirectory(Path.of("src")));
        assertTrue(filter.shouldTraverseDirectory(Path.of("lib")));
        assertTrue(filter.shouldTraverseDirectory(Path.of("app")));
        assertTrue(filter.shouldTraverseDirectory(Path.of("components")));
        assertTrue(filter.shouldTraverseDirectory(Path.of("packages")));
    }

    @Test
    void shouldFilterNonEligibleCodeFiles() {
        // Ignored: tests, minified, declarations, oversized, configs
        assertFalse(filter.isEligibleCodeFile(Path.of("utils.test.ts"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("service.spec.js"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("index.d.ts"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("bundle.min.js"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("MainTest.java"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("vite.config.ts"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("types.generated.ts"), 1000));
        assertFalse(filter.isEligibleCodeFile(Path.of("UserService.java"), 600_000)); // > 500KB

        // Accepted code files across existing languages
        assertTrue(filter.isEligibleCodeFile(Path.of("UserService.java"), 15_000));
        assertTrue(filter.isEligibleCodeFile(Path.of("calculator.ts"), 8_000));
        assertTrue(filter.isEligibleCodeFile(Path.of("Header.tsx"), 12_000));
        assertTrue(filter.isEligibleCodeFile(Path.of("auth.js"), 5_000));
    }

    @Test
    void shouldSupportAllNineLanguagesAndFilterTheirTests() {
        // Python
        assertTrue(filter.isEligibleCodeFile(Path.of("main.py"), 2000));
        assertFalse(filter.isEligibleCodeFile(Path.of("test_auth.py"), 2000));
        assertFalse(filter.isEligibleCodeFile(Path.of("user_test.py"), 2000));

        // Go
        assertTrue(filter.isEligibleCodeFile(Path.of("server.go"), 3000));
        assertFalse(filter.isEligibleCodeFile(Path.of("server_test.go"), 3000));

        // Dart (Flutter)
        assertTrue(filter.isEligibleCodeFile(Path.of("fees_screen.dart"), 4000));
        assertFalse(filter.isEligibleCodeFile(Path.of("fees_screen_test.dart"), 4000));
        assertFalse(filter.isEligibleCodeFile(Path.of("user.g.dart"), 4000)); // generated
        assertFalse(filter.isEligibleCodeFile(Path.of("state.freezed.dart"), 4000)); // generated

        // C#
        assertTrue(filter.isEligibleCodeFile(Path.of("PaymentService.cs"), 5000));
        assertFalse(filter.isEligibleCodeFile(Path.of("PaymentServiceTest.cs"), 5000));

        // Kotlin
        assertTrue(filter.isEligibleCodeFile(Path.of("UserViewModel.kt"), 3500));
        assertFalse(filter.isEligibleCodeFile(Path.of("UserViewModelTest.kt"), 3500));

        // Rust
        assertTrue(filter.isEligibleCodeFile(Path.of("lib.rs"), 4500));
        assertFalse(filter.isEligibleCodeFile(Path.of("lib_test.rs"), 4500));

        // PHP
        assertTrue(filter.isEligibleCodeFile(Path.of("UserController.php"), 6000));
        assertFalse(filter.isEligibleCodeFile(Path.of("UserControllerTest.php"), 6000));
    }

    @Test
    void shouldFilterBoilerplateAndPreserveRealLogic() {
        // 1. Empty body -> Rejected
        assertFalse(filter.isMeaningfulFunction(2, "public void doNothing() {}"));
        assertFalse(filter.isMeaningfulFunction(3, "const noop = () => {\n}"));

        // 2. Trivial Getter -> Rejected (even if formatted on multiple lines)
        assertFalse(filter.isMeaningfulFunction(3, "public String getName() { return this.name; }"));
        assertFalse(filter.isMeaningfulFunction(5, "getName() {\n  // returns name\n  return this.name;\n}"));
        assertFalse(filter.isMeaningfulFunction(2, "const getX = () => this.x;"));

        // 3. Trivial Setter -> Rejected
        assertFalse(filter.isMeaningfulFunction(3, "public void setName(String val) { this.name = val; }"));

        // 4. Compact 3-4 line function with >= 2 logic statements -> ACCEPTED!
        String compactLogic = "function add(a, b) {\n const sum = a + b;\n return sum * 2;\n}";
        assertTrue(filter.isMeaningfulFunction(4, compactLogic));

        // 5. Standard 6-line business function -> ACCEPTED!
        String businessLogic = """
            public double computeInterest(double principal, double rate, int years) {
                double base = 1.0 + rate;
                double multiplier = Math.pow(base, years);
                return principal * multiplier - principal;
            }
            """;
        assertTrue(filter.isMeaningfulFunction(6, businessLogic));
    }

    @Test
    void shouldHandleMonorepoSourceDirs() {
        // Auto-discovery mode (sourceDirs is empty or null) -> matches everything
        assertTrue(filter.matchesSourceDirs("packages/web/src/App.tsx", null));
        assertTrue(filter.matchesSourceDirs("apps/api/src/index.ts", List.of()));

        // Scoped mode
        List<String> scoped = List.of("src", "packages/web");
        assertTrue(filter.matchesSourceDirs("src/routes/user.js", scoped));
        assertTrue(filter.matchesSourceDirs("packages/web/Button.tsx", scoped));
        assertFalse(filter.matchesSourceDirs("docs/readme.md", scoped));
    }
}
