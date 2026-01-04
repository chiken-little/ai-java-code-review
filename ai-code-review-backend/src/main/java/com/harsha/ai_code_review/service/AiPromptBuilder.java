package com.harsha.ai_code_review.service;

import com.harsha.ai_code_review.dto.CodeReviewResult;
import org.springframework.stereotype.Service;

@Service
public class AiPromptBuilder {

    public String buildPrompt(CodeReviewResult result) {
        // Build a prompt from parsed code
        StringBuilder sb = new StringBuilder();
        sb.append("Review the following Java class:\n");
        sb.append("Class Name: ").append(result.getClassName()).append("\n");

        result.getMethods().forEach(m -> {
            sb.append("Method: ").append(m.getMethodName()).append("\n");
        });

        return sb.toString();
    }
}
