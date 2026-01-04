package com.harsha.ai_code_review.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class JavaParserService {

    public ParsedJavaFile parseDetailed(InputStream inputStream) throws Exception {
        CompilationUnit cu = StaticJavaParser.parse(inputStream);

        ClassOrInterfaceDeclaration clazz = cu
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new IllegalArgumentException("No class found in Java file"));

        String className = clazz.getNameAsString();
        List<ParsedJavaFile.ParsedMethod> methods = new ArrayList<>();

        for (MethodDeclaration m : clazz.getMethods()) {
            String methodName = m.getNameAsString();
            String signature = m.getDeclarationAsString(false, false, false); // e.g., public int sum(int a, int b)
            String code = m.toString(); // full method code
            methods.add(new ParsedJavaFile.ParsedMethod(methodName, signature, code));
        }

        return new ParsedJavaFile(className, methods);
    }
}
