package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterGo;

import java.util.Set;

@Component
public class GoLanguageAdapter implements LanguageAdapter {

    private static final Set<String> EXTENSIONS = Set.of("go");

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterGo();
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
        return "function_declaration".equals(type) || "method_declaration".equals(type) || "func_literal".equals(type);
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
        // Function parameter: x int
        if ("parameter_declaration".equals(parentType)) {
            TSNode name = parent.getChildByFieldName("name");
            return name != null && !name.isNull() && name.equals(node);
        }

        // Short variable declaration: x := 10
        if ("short_var_declaration".equals(parentType)) {
            TSNode left = parent.getChildByFieldName("left");
            return left != null && !left.isNull() && left.equals(node);
        }

        // Var declaration: var x int
        if ("var_spec".equals(parentType)) {
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

        // Call expression: foo()
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

        // Selector expression: user.Name
        if ("selector_expression".equals(parent.getType())) {
            TSNode field = parent.getChildByFieldName("field");
            return field != null && !field.isNull() && field.equals(node);
        }
        return false;
    }
}
