package com.harsha.ai_code_review.service;

import org.springframework.stereotype.Service;

@Service
public class AiReviewService {

    private final GroqClient groqClient;

    public AiReviewService(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    public String review(String prompt) throws Exception {
        // Directly call Groq API
        return groqClient.chat(prompt);
    }
}
