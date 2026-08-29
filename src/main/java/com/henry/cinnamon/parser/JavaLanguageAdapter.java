package com.henry.cinnamon.parser;


import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TreeSitterJava;

@Component
public class JavaLanguageAdapter implements LanguageAdapter{

    @Override
    public TSLanguage treeSitterLanguage() {
        return new TreeSitterJava();
    }

    @Override
    public boolean supports(String fileExtension) {
        return "java".equalsIgnoreCase(fileExtension);
    }

    @Override
    public boolean isFunctionNode(TSNode node) {
        String type = node.getType();
        return "method_declaration".equals(type)
                || "constructor_declaration".equals(type)
                || "compact_constructor_declaration".equals(type);
    }

    @Override
    public String extractFunctionName(TSNode node, String sourceCode) {
        TSNode nameNode = node.getChildByFieldName("name");
        if(nameNode!=null){
            return sourceCode.substring(nameNode.getStartByte(),nameNode.getEndByte());
        }
        return "anonymous";
    }

    @Override
    public boolean isLocalDeclaration(TSNode node) {
        String type = node.getType();

        return "variable_declarator".equals(type)
                || "formal_parameter".equals(type)
                || "catch_formal_parameter".equals(type)
                || "resource".equals(type);
    }

    @Override
    public boolean isMethodNameReference(TSNode node) {
        TSNode parent = node.getParent();
        return parent != null
                && "method_invocation".equals(parent.getType())
                && node.equals(parent.getChildByFieldName("name"));

    }

    @Override
    public boolean isFieldAccessReference(TSNode node) {
        TSNode parent = node.getParent();
        return parent != null
                && "field_access".equals(parent.getType())
                && node.equals(parent.getChildByFieldName("field"));
    }
}
