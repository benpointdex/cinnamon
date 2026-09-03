package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterPhp;

import java.util.Set;

@Component
public class PhpLanguageAdapter implements LanguageAdapter {

    private static final Set<String> EXTENSIONS = Set.of("php", "phtml", "php5", "php7", "php8");

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterPhp();
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
        return "function_definition".equals(type)
                || "method_declaration".equals(type)
                || "anonymous_function_creation_expression".equals(type)
                || "arrow_function".equals(type);
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
        if ("formal_parameter".equals(parentType)
                || "simple_parameter".equals(parentType)
                || "property_element".equals(parentType)) {
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

        if ("member_call_expression".equals(parent.getType()) || "function_call_expression".equals(parent.getType())) {
            TSNode name = parent.getChildByFieldName("name");
            return name != null && !name.isNull() && name.equals(node);
        }
        return false;
    }

    @Override
    public boolean isFieldAccessReference(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        if ("member_access_expression".equals(parent.getType())) {
            TSNode name = parent.getChildByFieldName("name");
            return name != null && !name.isNull() && name.equals(node);
        }
        return false;
    }
}
