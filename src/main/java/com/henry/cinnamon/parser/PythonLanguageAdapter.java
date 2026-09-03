package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterPython;

import java.util.Set;

@Component
public class PythonLanguageAdapter implements LanguageAdapter {

    private static final Set<String> EXTENSIONS = Set.of("py", "pyi", "pyw");

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterPython();
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
        return "function_definition".equals(type) || "async_function_definition".equals(type);
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
        // Parameter in function definition
        if ("parameters".equals(parentType) || "lambda_parameters".equals(parentType)) {
            return true;
        }

        // Typed or default parameter
        if ("typed_parameter".equals(parentType) || "default_parameter".equals(parentType)) {
            return true;
        }

        // Variable assignment: x = 1
        if ("assignment".equals(parentType)) {
            TSNode left = parent.getChildByFieldName("left");
            return left != null && !left.isNull() && left.equals(node);
        }

        return false;
    }

    @Override
    public boolean isMethodNameReference(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        // Function call: func(...)
        if ("call".equals(parent.getType())) {
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

        // Attribute access: obj.attr
        if ("attribute".equals(parent.getType())) {
            TSNode attr = parent.getChildByFieldName("attribute");
            return attr != null && !attr.isNull() && attr.equals(node);
        }
        return false;
    }
}
