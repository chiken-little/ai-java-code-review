package com.harsha.ai_code_review.service;

import java.util.List;

public class ParsedJavaFile {
    private final String className;
    private final List<ParsedMethod> methods;

    public ParsedJavaFile(String className, List<ParsedMethod> methods) {
        this.className = className;
        this.methods = methods;
    }

    public String getClassName() { return className; }
    public List<ParsedMethod> getMethods() { return methods; }

    public static class ParsedMethod {
        private final String name;
        private final String signature;
        private final String code;

        public ParsedMethod(String name, String signature, String code) {
            this.name = name;
            this.signature = signature;
            this.code = code;
        }

        public String getName() { return name; }
        public String getSignature() { return signature; }
        public String getCode() { return code; }
    }
}
