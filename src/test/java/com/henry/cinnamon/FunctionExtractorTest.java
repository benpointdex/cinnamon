package com.henry.cinnamon;

import com.henry.cinnamon.model.CodeUnit;
import com.henry.cinnamon.parser.*;
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
        PythonLanguageAdapter pyAdapter = new PythonLanguageAdapter();
        GoLanguageAdapter goAdapter = new GoLanguageAdapter();
        DartLanguageAdapter dartAdapter = new DartLanguageAdapter();
        CSharpLanguageAdapter csAdapter = new CSharpLanguageAdapter();
        KotlinLanguageAdapter ktAdapter = new KotlinLanguageAdapter();
        RustLanguageAdapter rsAdapter = new RustLanguageAdapter();
        PhpLanguageAdapter phpAdapter = new PhpLanguageAdapter();

        LanguageAdapterRegistry registry = new LanguageAdapterRegistry(List.of(
                javaAdapter, tsAdapter, pyAdapter, goAdapter, dartAdapter, csAdapter, ktAdapter, rsAdapter, phpAdapter
        ));
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

    @Test
    void shouldExtractPythonFunctions() {
        String pyCode = """
            def calculate_discount(price, discount_rate):
                # Apply percentage discount
                discount = price * discount_rate
                final_price = price - discount
                return final_price
            """;

        List<CodeUnit> units = functionExtractor.extractFunctions(pyCode, "pricing.py", "my-repo");
        assertEquals(1, units.size());
        assertEquals("calculate_discount", units.get(0).getFunctionName());
        assertNotNull(units.get(0).getNormalizedText());
    }

    @Test
    void shouldExtractGoFunctions() {
        String goCode = """
            package utils

            func ComputeTax(subtotal float64, taxRate float64) float64 {
                tax := subtotal * taxRate
                return subtotal + tax
            }
            """;

        List<CodeUnit> units = functionExtractor.extractFunctions(goCode, "tax.go", "my-repo");
        assertEquals(1, units.size());
        assertEquals("ComputeTax", units.get(0).getFunctionName());
        assertNotNull(units.get(0).getNormalizedText());
    }

    @Test
    void shouldExtractDartFunctions() {
        String dartCode = """
            double calculateTotalFee(double baseFee, double lateFee) {
              final double total = baseFee + lateFee;
              return total;
            }
            """;

        List<CodeUnit> units = functionExtractor.extractFunctions(dartCode, "fee_calculator.dart", "my-repo");
        assertEquals(1, units.size());
        assertEquals("calculateTotalFee", units.get(0).getFunctionName());
        assertNotNull(units.get(0).getNormalizedText());
    }

    @Test
    void shouldExtractCSharpFunctions() {
        String csCode = """
            namespace Services {
                public class PaymentService {
                    public decimal ProcessPayment(decimal amount, decimal fee) {
                        decimal total = amount + fee;
                        return total;
                    }
                }
            }
            """;

        List<CodeUnit> units = functionExtractor.extractFunctions(csCode, "PaymentService.cs", "my-repo");
        assertEquals(1, units.size());
        assertEquals("ProcessPayment", units.get(0).getFunctionName());
        assertNotNull(units.get(0).getNormalizedText());
    }

    @Test
    void shouldExtractKotlinFunctions() {
        String ktCode = """
            package com.example

            fun formatUserName(firstName: String, lastName: String): String {
                val fullName = "$firstName $lastName"
                return fullName.trim()
            }
            """;

        List<CodeUnit> units = functionExtractor.extractFunctions(ktCode, "UserUtils.kt", "my-repo");
        assertEquals(1, units.size());
        assertEquals("formatUserName", units.get(0).getFunctionName());
        assertNotNull(units.get(0).getNormalizedText());
    }

    @Test
    void shouldExtractRustFunctions() {
        String rsCode = """
            pub fn calculate_hash(data: &str, salt: &str) -> String {
                let combined = format!("{}{}", data, salt);
                combined
            }
            """;

        List<CodeUnit> units = functionExtractor.extractFunctions(rsCode, "crypto.rs", "my-repo");
        assertEquals(1, units.size());
        assertEquals("calculate_hash", units.get(0).getFunctionName());
        assertNotNull(units.get(0).getNormalizedText());
    }

    @Test
    void shouldExtractPhpFunctions() {
        String phpCode = """
            <?php
            function sanitizeInput($inputData, $maxLength) {
                $trimmed = trim($inputData);
                return substr($trimmed, 0, $maxLength);
            }
            """;

        List<CodeUnit> units = functionExtractor.extractFunctions(phpCode, "sanitizer.php", "my-repo");
        assertEquals(1, units.size());
        assertEquals("sanitizeInput", units.get(0).getFunctionName());
        assertNotNull(units.get(0).getNormalizedText());
    }
}
