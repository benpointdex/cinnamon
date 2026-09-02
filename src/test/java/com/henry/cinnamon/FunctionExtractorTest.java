package com.henry.cinnamon;

import com.henry.cinnamon.model.CodeUnit;
import com.henry.cinnamon.parser.FunctionExtractor;
import com.henry.cinnamon.parser.IdentifierNormalizer;
import com.henry.cinnamon.parser.JavaLanguageAdapter;
import com.henry.cinnamon.parser.LanguageAdapterRegistry;
import com.henry.cinnamon.parser.TypeScriptLanguageAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FunctionExtractorTest {

    private FunctionExtractor functionExtractor;

    @BeforeEach
    void setUp() {
        JavaLanguageAdapter javaAdapter = new JavaLanguageAdapter();
        TypeScriptLanguageAdapter tsAdapter = new TypeScriptLanguageAdapter();
        LanguageAdapterRegistry registry = new LanguageAdapterRegistry(List.of(javaAdapter, tsAdapter));
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

    @Test
    void shouldExtractAndNormalizeTypeScriptFunctions() {
        String tsCodeA = """
            // Add two numbers
            export function add(firstNumber: number, secondNumber: number): number {
                const totalSum = firstNumber + secondNumber;
                return totalSum;
            }
            """;

        String tsCodeB = """
            /* Different comment */
            export const sum = (x: number, y: number): number => {
                const result = x + y;
                return result;
            };
            """;

        List<CodeUnit> unitsA = functionExtractor.extractFunctions(tsCodeA, "calculator.ts", "my-repo");
        List<CodeUnit> unitsB = functionExtractor.extractFunctions(tsCodeB, "math.ts", "my-repo");

        assertEquals(1, unitsA.size());
        assertEquals(1, unitsB.size());
        assertEquals("add", unitsA.get(0).getFunctionName());
        assertEquals("sum", unitsB.get(0).getFunctionName());
    }
}
