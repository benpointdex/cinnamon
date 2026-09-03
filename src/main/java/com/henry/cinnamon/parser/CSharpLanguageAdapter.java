package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterCSharp;

import java.util.Set;

@Component
public class CSharpLanguageAdapter implements LanguageAdapter {

    private static final Set<String> EXTENSIONS = Set.of("cs");

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterCSharp();
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
        return "method_declaration".equals(type)
                || "local_function_statement".equals(type)
                || "lambda_expression".equals(type)
                || "constructor_declaration".equals(type);
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
        if ("parameter".equals(parentType) || "variable_declarator".equals(parentType)) {
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

        if ("invocation_expression".equals(parent.getType())) {
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

        if ("member_access_expression".equals(parent.getType())) {
            TSNode name = parent.getChildByFieldName("name");
            return name != null && !name.isNull() && name.equals(node);
        }
        return false;
    }
}
