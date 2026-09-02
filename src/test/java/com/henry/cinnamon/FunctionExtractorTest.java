package com.henry.cinnamon;

import com.henry.cinnamon.model.CodeUnit;
import com.henry.cinnamon.parser.FunctionExtractor;
import com.henry.cinnamon.parser.IdentifierNormalizer;
import com.henry.cinnamon.parser.JavaLanguageAdapter;
import com.henry.cinnamon.parser.LanguageAdapterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FunctionExtractorTest {

    private FunctionExtractor functionExtractor;

    @BeforeEach
    void setUp() {
        JavaLanguageAdapter javaAdapter = new JavaLanguageAdapter();
        LanguageAdapterRegistry registry = new LanguageAdapterRegistry(List.of(javaAdapter));
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        functionExtractor = new FunctionExtractor(registry, normalizer);
    }

    @Test
    void shouldProduceExactSameHashForRenamedVariablesAndComments() {
        String codeA = """
            public class MathUtil {
                // Computes the sum of two integers
                public int calculate(int a, int b) {
                    int total = a + b;
                    return total;
                }
            }
            """;

        String codeB = """
            public class MathService {
                /*
                 * Different comments and different variable names
                 */
                public int compute(int x, int y) {
                    int sum = x + y;
                    return sum;
                }
            }
            """;

        List<CodeUnit> unitsA = functionExtractor.extractFunctions(codeA, "MathUtil.java", "repo-1");
        List<CodeUnit> unitsB = functionExtractor.extractFunctions(codeB, "MathService.java", "repo-1");

        assertEquals(1, unitsA.size());
        assertEquals(1, unitsB.size());

        // The SHA-256 hashes must be 100% IDENTICAL!
        assertEquals(unitsA.get(0).getContentHash(), unitsB.get(0).getContentHash());
    }
}
