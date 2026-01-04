package com.harsha.ai_code_review.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.harsha.ai_code_review.dto.CodeReviewResult;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JavadocInserterService {

    /**
     * Inserts or replaces Javadoc for methods based on CodeReviewResult.
     * Matches by method name (and if overloaded, best-effort by parameter count).
     */
    public String applyJavadocs(InputStream originalJava, CodeReviewResult reviewResult) throws Exception {
        CompilationUnit cu = StaticJavaParser.parse(originalJava);

        ClassOrInterfaceDeclaration clazz = cu
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new IllegalArgumentException("No class found in Java file"));

        Map<String, List<CodeReviewResult.MethodReview>> byName = new HashMap<>();
        for (CodeReviewResult.MethodReview mr : reviewResult.getMethods()) {
            byName.computeIfAbsent(mr.getMethodName(), k -> new ArrayList<>()).add(mr);
        }

        for (MethodDeclaration md : clazz.getMethods()) {
            String name = md.getNameAsString();
            List<CodeReviewResult.MethodReview> candidates = byName.getOrDefault(name, List.of());
            if (candidates.isEmpty()) continue;

            CodeReviewResult.MethodReview chosen = chooseBest(md, candidates);

            String raw = chosen.getSuggestedJavadoc();
            if (raw == null || raw.isBlank()) continue;

            String normalized = normalizeJavadoc(raw);
// If method does NOT declare throws, remove any @throws lines from Javadoc
            if (md.getThrownExceptions().isEmpty()) {
                normalized = normalized.replaceAll("(?m)^@throws\\s+.*\\R?", "").trim();
            }
            // Replace existing javadoc or add new
            md.setJavadocComment(normalized);
        }

        return cu.toString();
    }

    private CodeReviewResult.MethodReview chooseBest(MethodDeclaration md, List<CodeReviewResult.MethodReview> candidates) {
        if (candidates.size() == 1) return candidates.get(0);

        int paramCount = md.getParameters().size();

        for (CodeReviewResult.MethodReview mr : candidates) {
            String j = mr.getSuggestedJavadoc();
            if (j == null) continue;

            // Convert \\n to real newlines for counting
            String normalized = j.replace("\\n", "\n");

            long tagCount = Arrays.stream(normalized.split("\\R"))
                    .map(String::trim)
                    .filter(line -> line.startsWith("@param"))
                    .count();

            if (tagCount == paramCount) return mr;
        }

        return candidates.get(0);
    }
    /**
     * Converts plain text into a proper Javadoc body.
     *
     * Removes surrounding Javadoc comment markers if the AI accidentally includes them.
     */
    private String normalizeJavadoc(String input) {
        String s = input == null ? "" : input.trim();
        if (s.isBlank()) return s;

        // Convert literal \\n into real newlines
        s = s.replace("\\n", "\n");

        // Strip /** */ if model included them
        s = s.replaceAll("^/\\*\\*\\s*", "");
        s = s.replaceAll("\\s*\\*/$", "");

        // Remove leading '*' bullets if present
        s = s.replaceAll("(?m)^\\s*\\*\\s?", "");

        // Force common tags onto new lines (handles "text @param a ... @return ...")
        s = s.replace(" @param", "\n@param")
                .replace(" @return", "\n@return")
                .replace(" @throws", "\n@throws")
                .replace(" @exception", "\n@throws")
                .replace(" @see", "\n@see")
                .replace(" @deprecated", "\n@deprecated");

        // Split into lines for cleanup
        List<String> lines = new ArrayList<>();
        for (String line : s.split("\\R")) {
            String t = line.trim();

            // Drop meaningless tags
            if (t.matches("^@param\\s+none\\b.*")) continue;
            if (t.matches("^@return\\s+none\\b.*")) continue;
            if (t.matches("^@throws\\s+none\\b.*")) continue;

            // Also drop empty tag-only lines
            if (t.equals("@param") || t.equals("@return") || t.equals("@throws")) continue;

            lines.add(line);
        }

        // Re-join
        s = String.join("\n", lines).trim();

        // Ensure a blank line before first tag if there is description text
        s = s.replaceAll("\\R(@param|@return|@throws|@see|@deprecated)", "\n\n$1");
// Remove blank lines between consecutive tags
        s = s.replaceAll("(?m)^(@param.*)\\R\\s*\\R(?=@param|@return|@throws|@see|@deprecated)", "$1\n");

        return s.trim();
    }



}
