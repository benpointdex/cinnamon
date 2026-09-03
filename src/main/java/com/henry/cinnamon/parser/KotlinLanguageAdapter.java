package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterKotlin;

import java.util.Set;

@Component
public class KotlinLanguageAdapter implements LanguageAdapter {

    private static final Set<String> EXTENSIONS = Set.of("kt", "kts");

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterKotlin();
    }

    @Override
    public boolean supports(String fileExtension) {
        if (fileExtension == null) return false;
        return EXTENSIONS.contains(fileExtension.toLowerCase());
    }

    @Override
    public boolean isFunctionNode(TSNode node) {
        if (node == null || node.isNull()) return false;
        String type = node.getType();
        return "function_declaration".equals(type)
                || "anonymous_function".equals(type)
                || "lambda_literal".equals(type);
    }

    @Override
    public String extractFunctionName(TSNode node, String sourceCode) {
        if (node == null || node.isNull()) return "anonymous";
        TSNode nameNode = node.getChildByFieldName("name");
        if (nameNode != null && !nameNode.isNull()) {
            return sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte()).trim();
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            TSNode child = node.getChild(i);
            if (child != null && !child.isNull() && "simple_identifier".equals(child.getType())) {
                return sourceCode.substring(child.getStartByte(), child.getEndByte()).trim();
            }
        }
        return "anonymous";
    }

    @Override
    public boolean isLocalDeclaration(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        String parentType = parent.getType();
        if ("parameter".equals(parentType)
                || "variable_declaration".equals(parentType)
                || "property_declaration".equals(parentType)) {
            TSNode name = parent.getChildByFieldName("name");
            return name != null && !name.isNull() && name.equals(node);
        }
        return false;
    }

    @Override
    public boolean isMethodNameReference(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        if ("call_expression".equals(parent.getType())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isFieldAccessReference(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        if ("navigation_expression".equals(parent.getType())) {
            return true;
        }
        return false;
    }
}
