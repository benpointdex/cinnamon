package com.henry.cinnamon.parser;

import org.treesitter.TSNode;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IdentifierNormalizer {

    /**
     * Normalizes a function's AST node into a comment-free, variable-renamed canonical string.
     */
    public String normalize(TSNode functionNode, String sourceCode, LanguageAdapter lang) {
        // Pass 1: Collect local variable & parameter names into a symbol table
        Map<String, String> localSymbolTable = new LinkedHashMap<>();
        collectLocalDeclarations(functionNode, sourceCode, lang, localSymbolTable);

        // Pass 2: Traverse and emit normalized tokens
        StringBuilder result = new StringBuilder();
        emitNormalized(functionNode, functionNode, sourceCode, lang, localSymbolTable, result);

        return result.toString().trim();
    }

    private void collectLocalDeclarations(TSNode node, String source, LanguageAdapter lang,
                                          Map<String, String> table) {
        if (lang.isLocalDeclaration(node)) {
            TSNode nameNode = node.getChildByFieldName("name");
            if (nameNode != null) {
                String varName = text(nameNode, source);
                // Assign incremental placeholder: VAR_1, VAR_2, ...
                table.putIfAbsent(varName, "VAR_" + (table.size() + 1));
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            collectLocalDeclarations(node.getChild(i), source, lang, table);
        }
    }

    private void emitNormalized(TSNode node, TSNode functionNode, String source, LanguageAdapter lang,
                                Map<String, String> table, StringBuilder out) {
        String nodeType = node.getType();

        // 1. Ignore all comment nodes (strips single-line, block, and doc comments)
        if ("comment".equals(nodeType) || "line_comment".equals(nodeType) || "block_comment".equals(nodeType)) {
            return;
        }

        // 2. Leaf node (token)
        if (node.getChildCount() == 0) {
            String tokenText = text(node, source);

            // If this token is the function's own name, normalize to "FUNC"
            TSNode funcNameNode = functionNode.getChildByFieldName("name");
            if (funcNameNode != null && node.equals(funcNameNode)) {
                out.append("FUNC").append(' ');
                return;
            }

            boolean isKnownLocal = "identifier".equals(nodeType) && table.containsKey(tokenText);
            boolean isSemanticReference = lang.isMethodNameReference(node) || lang.isFieldAccessReference(node);

            // If it's a local variable (and not a method call or field access), replace with placeholder
            if (isKnownLocal && !isSemanticReference) {
                out.append(table.get(tokenText)).append(' ');
            } else {
                out.append(tokenText).append(' ');
            }
            return;
        }

        // 3. Recursive traversal for non-leaf nodes
        for (int i = 0; i < node.getChildCount(); i++) {
            emitNormalized(node.getChild(i), functionNode, source, lang, table, out);
        }
    }

    private String text(TSNode node, String source) {
        return source.substring(node.getStartByte(), node.getEndByte());
    }
}
