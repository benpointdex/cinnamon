package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterRust;

import java.util.Set;

@Component
public class RustLanguageAdapter implements LanguageAdapter {

    private static final Set<String> EXTENSIONS = Set.of("rs");

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterRust();
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
        return "function_item".equals(type) || "closure_expression".equals(type);
    }

    @Override
    public String extractFunctionName(TSNode node, String sourceCode) {
        if (node == null || node.isNull()) return "anonymous";
        TSNode nameNode = node.getChildByFieldName("name");
        if (nameNode != null && !nameNode.isNull()) {
            return sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte()).trim();
        }
        return "anonymous";
    }

    @Override
    public boolean isLocalDeclaration(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        String parentType = parent.getType();
        if ("parameter".equals(parentType) || "let_declaration".equals(parentType)) {
            TSNode pattern = parent.getChildByFieldName("pattern");
            return pattern != null && !pattern.isNull() && pattern.equals(node);
        }
        return false;
    }

    @Override
    public boolean isMethodNameReference(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        if ("call_expression".equals(parent.getType())) {
            TSNode func = parent.getChildByFieldName("function");
            return func != null && !func.isNull() && func.equals(node);
        }
        return false;
    }

    @Override
    public boolean isFieldAccessReference(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        if ("field_expression".equals(parent.getType())) {
            TSNode field = parent.getChildByFieldName("field");
            return field != null && !field.isNull() && field.equals(node);
        }
        return false;
    }
}
