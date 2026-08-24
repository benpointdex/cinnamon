package com.henry.cinnamon.parser;


import org.treesitter.TSLanguage;
import org.treesitter.TSNode;

public interface LanguageAdapter {

    // 1. Returns the native Tree-sitter grammar (e.g., Java grammar)
    TSLanguage treeSitterLanguage();

    // 2. Returns true if this file extension is supported (e.g., "java")
    boolean supports(String fileExtension);

    // 3. Is this node a function/method?
    boolean isFunctionNode(TSNode node);

    // 4. How to read the function name from the node
    String extractFunctionName(TSNode node, String sourceCode);

    // 5. Is this node declaring a variable or parameter? (e.g., int x = 5)
    boolean isLocalDeclaration(TSNode node);

    // 6. Is this identifier a method call? (e.g., the 'size' in list.size())
    boolean isMethodNameReference(TSNode node);

    // 7. Is this identifier a field? (e.g., the 'age' in user.age)
    boolean isFieldAccessReference(TSNode node);
}

