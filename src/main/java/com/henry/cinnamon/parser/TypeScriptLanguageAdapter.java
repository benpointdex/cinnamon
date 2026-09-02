package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterTypescript;

import java.util.Set;

@Component
public class TypeScriptLanguageAdapter implements LanguageAdapter {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "ts", "tsx", "js", "jsx", "mjs", "cjs"
    );

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterTypescript();
    }

    @Override
    public boolean supports(String fileExtension) {
        if (fileExtension == null) {
            return false;
        }
        return SUPPORTED_EXTENSIONS.contains(fileExtension.toLowerCase());
    }

    @Override
    public boolean isFunctionNode(TSNode node) {
        if (node == null || node.isNull()) {
            return false;
        }
        String type = node.getType();
        return "function_declaration".equals(type)
                || "method_definition".equals(type)
                || "arrow_function".equals(type)
                || "function_expression".equals(type)
                || "generator_function_declaration".equals(type);
    }

    @Override
    public String extractFunctionName(TSNode node, String sourceCode) {
        if (node == null || node.isNull()) {
            return "anonymous";
        }

        // Direct named function: function myFunc() {}
        TSNode nameNode = node.getChildByFieldName("name");
        if (nameNode != null && !nameNode.isNull()) {
            return sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte());
        }

        // Assigned to variable: const myFunc = () => {} or const myFunc = function() {}
        TSNode parent = node.getParent();
        if (parent != null && !parent.isNull() && "variable_declarator".equals(parent.getType())) {
            TSNode varNameNode = parent.getChildByFieldName("name");
            if (varNameNode != null && !varNameNode.isNull()) {
                return sourceCode.substring(varNameNode.getStartByte(), varNameNode.getEndByte());
            }
        }

        // Object property: { myFunc: () => {} }
        if (parent != null && !parent.isNull() && "pair".equals(parent.getType())) {
            TSNode keyNode = parent.getChildByFieldName("key");
            if (keyNode != null && !keyNode.isNull()) {
                return sourceCode.substring(keyNode.getStartByte(), keyNode.getEndByte());
            }
        }

        return "anonymous";
    }

    @Override
    public boolean isLocalDeclaration(TSNode node) {
        if (node == null || node.isNull()) {
            return false;
        }
        String type = node.getType();
        return "variable_declarator".equals(type)
                || "required_parameter".equals(type)
                || "optional_parameter".equals(type)
                || "rest_pattern".equals(type)
                || "catch_clause".equals(type);
    }

    @Override
    public boolean isMethodNameReference(TSNode node) {
        if (node == null || node.isNull()) {
            return false;
        }
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) {
            return false;
        }
        // Member invocation: obj.doSomething() -> 'doSomething' is member property
        return "member_expression".equals(parent.getType())
                && node.equals(parent.getChildByFieldName("property"));
    }

    @Override
    public boolean isFieldAccessReference(TSNode node) {
        if (node == null || node.isNull()) {
            return false;
        }
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) {
            return false;
        }
        return "member_expression".equals(parent.getType())
                && node.equals(parent.getChildByFieldName("property"));
    }
}
