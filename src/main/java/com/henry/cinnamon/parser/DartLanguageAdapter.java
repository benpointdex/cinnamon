package com.henry.cinnamon.parser;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterDart;

import java.util.Set;

@Component
public class DartLanguageAdapter implements LanguageAdapter {

    private static final Set<String> EXTENSIONS = Set.of("dart");

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterDart();
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
        // In Dart grammar, a method/function body is paired with a preceding signature
        if ("function_body".equals(type)) {
            TSNode prev = node.getPrevNamedSibling();
            return prev != null && !prev.isNull()
                    && ("function_signature".equals(prev.getType()) || "method_signature".equals(prev.getType()));
        }
        return "function_expression".equals(type);
    }

    @Override
    public String extractFunctionName(TSNode node, String sourceCode) {
        if (node == null || node.isNull()) return "anonymous";
        TSNode sig = node.getPrevNamedSibling();
        if (sig != null && !sig.isNull()) {
            TSNode nameNode = findIdentifierWithName(sig);
            if (nameNode != null && !nameNode.isNull()) {
                return sourceCode.substring(nameNode.getStartByte(), nameNode.getEndByte()).trim();
            }
        }
        return "anonymous";
    }

    private TSNode findIdentifierWithName(TSNode node) {
        if (node == null || node.isNull()) return null;
        TSNode byField = node.getChildByFieldName("name");
        if (byField != null && !byField.isNull()) {
            return byField;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            TSNode ch = node.getChild(i);
            TSNode res = findIdentifierWithName(ch);
            if (res != null) return res;
        }
        return null;
    }

    @Override
    public boolean isLocalDeclaration(TSNode node) {
        if (node == null || node.isNull()) return false;
        TSNode parent = node.getParent();
        if (parent == null || parent.isNull()) return false;

        String parentType = parent.getType();
        if ("formal_parameter".equals(parentType)
                || "simple_formal_parameter".equals(parentType)
                || "declared_identifier".equals(parentType)
                || "variable_declaration".equals(parentType)) {
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

        if ("method_invocation".equals(parent.getType())) {
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

        if ("property_access".equals(parent.getType())) {
            TSNode prop = parent.getChildByFieldName("property");
            return prop != null && !prop.isNull() && prop.equals(node);
        }
        return false;
    }
}
