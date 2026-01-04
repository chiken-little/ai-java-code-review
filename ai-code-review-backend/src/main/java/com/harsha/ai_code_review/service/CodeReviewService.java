package com.harsha.ai_code_review.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.ai_code_review.dto.CodeReviewResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodeReviewService {

    private final JavaParserService javaParserService;
    private final GroqClient groqClient;
    private final JavadocInserterService javadocInserterService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CodeReviewService(JavaParserService javaParserService,
                             GroqClient groqClient,
                             JavadocInserterService javadocInserterService) {
        this.javaParserService = javaParserService;
        this.groqClient = groqClient;
        this.javadocInserterService = javadocInserterService;
    }

    public CodeReviewResult reviewFile(MultipartFile file) throws Exception {
        ParsedJavaFile parsed = javaParserService.parseDetailed(file.getInputStream());

        CodeReviewResult result = new CodeReviewResult();
        result.setClassName(parsed.getClassName());

        List<CodeReviewResult.MethodReview> reviews = new ArrayList<>();

        for (ParsedJavaFile.ParsedMethod m : parsed.getMethods()) {
            String prompt = buildPrompt(parsed.getClassName(), m);

            // Groq returns assistant message content (string)
            String content = groqClient.chat(prompt);

            // Extract JSON object, then repair common invalid newline issues inside strings
            String json = extractJsonObject(content);
            json = repairInvalidJsonNewlines(json);

            CodeReviewResult.MethodReview review =
                    objectMapper.readValue(json, CodeReviewResult.MethodReview.class);

            // Ensure methodName is always set even if model forgets
            if (review.getMethodName() == null || review.getMethodName().isBlank()) {
                review.setMethodName(m.getName());
            }

            // Ensure suggestedMethodName is at least something sane
            if (review.getSuggestedMethodName() == null || review.getSuggestedMethodName().isBlank()) {
                review.setSuggestedMethodName(m.getName());
            }

            // Force main() to never be renamed
            if (m.getSignature() != null && m.getSignature().contains("static void main(")) {
                review.setSuggestedMethodName("main");
            }

            // Ensure reviewComments is not null
            if (review.getReviewComments() == null) {
                review.setReviewComments(List.of());
            }
            review.setSuggestedJavadoc(sanitizeForApi(review.getSuggestedJavadoc()));

            reviews.add(review);
        }

        result.setMethods(reviews);
        return result;
    }

    public ApplyReviewBundle reviewAndApply(MultipartFile file) throws Exception {
        CodeReviewResult review = reviewFile(file);

        // IMPORTANT: re-open the stream (MultipartFile streams are one-time)
        String updated = javadocInserterService.applyJavadocs(file.getInputStream(), review);

        return new ApplyReviewBundle(review, updated);
    }

    private String buildPrompt(String className, ParsedJavaFile.ParsedMethod m) {
        return """
You are a seniormost Java reviewer. Review ONE method and respond in STRICT JSON only.

Rules:
- Output MUST be valid JSON. No markdown. No extra text.
- JSON keys must be exactly:
  methodName (string),
  reviewComments (array of strings),
  suggestedMethodName (string),
  suggestedJavadoc (string)
- Do NOT output "@return none" or "@throws none" or "@param none".
- For void methods, omit @return entirely.
- Only include @throws if the method declares/throws an exception.
- suggestedJavadoc must be proper Javadoc WITHOUT surrounding /** */
- suggestedJavadoc MUST be a single JSON string value.
- Do NOT put real newline characters inside suggestedJavadoc.
- Use \\n (backslash+n) for line breaks inside suggestedJavadoc.
- Any @param/@return/@throws must be inside the suggestedJavadoc string using \\n for new lines.
- Put each tag on its own line (inside suggestedJavadoc using \\n):
  @param ...
  @return ...
  @throws ...
- Add a blank line before the first tag if tags exist (use \\n\\n before first tag).
- If signature contains "static void main(", suggestedMethodName MUST be "main".
- Conventional Java entrypoint signatures are:
  public static void main(String[] args)
  public static void main(String... args)
- If signature matches either one, DO NOT say it is unconventional.
- For main methods, do not criticize args being unused (it is normal).
- If signature matches public static void main(String[] args) or public static void main(String... args):
  - Do NOT claim it violates naming conventions.
  - Do NOT mention null/empty input validation (args is optional and may be unused).
  - Do NOT suggest @throws unless the signature declares throws.

Class: %s
Method signature: %s

Method code:
%s

Return JSON now.
""".formatted(className, m.getSignature(), m.getCode());
    }

    public static class ApplyReviewBundle {
        private final CodeReviewResult result;
        private final String updatedSource;

        public ApplyReviewBundle(CodeReviewResult result, String updatedSource) {
            this.result = result;
            this.updatedSource = updatedSource;
        }

        public CodeReviewResult getResult() { return result; }
        public String getUpdatedSource() { return updatedSource; }
    }

    private String extractJsonObject(String text) {
        if (text == null) return "{}";
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1).trim();
        }
        return text.trim();
    }

    /**
     * Repairs invalid JSON produced by LLMs where raw newline characters appear inside JSON strings.
     * Converts \n inside quoted strings into \\n so Jackson can parse it.
     */
    private String repairInvalidJsonNewlines(String json) {
        if (json == null) return null;

        StringBuilder out = new StringBuilder(json.length() + 32);
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                out.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                out.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                out.append(c);
                inString = !inString;
                continue;
            }

            if (inString) {
                if (c == '\n') {
                    out.append("\\n");
                    continue;
                }
                if (c == '\r') {
                    // drop CR
                    continue;
                }
            }

            out.append(c);
        }

        return out.toString();
    }

    private String sanitizeForApi(String javadoc) {
        if (javadoc == null) return null;

        String s = javadoc.trim();

        // Convert any real newlines to \\n so JSON stays safe if model slips
        s = s.replace("\r\n", "\n").replace("\r", "\n");

        // Remove common meaningless tags
        s = s.replaceAll("(?m)^@param\\s+none\\b.*\\R?", "");
        s = s.replaceAll("(?m)^@return\\s+none\\b.*\\R?", "");
        s = s.replaceAll("(?m)^@throws\\s+none\\b.*\\R?", "");

        return s.trim();
    }

}
