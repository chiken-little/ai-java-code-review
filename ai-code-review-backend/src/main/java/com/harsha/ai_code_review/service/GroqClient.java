package com.harsha.ai_code_review.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GroqClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.apiKey}")
    private String apiKey;

    @Value("${groq.baseUrl:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    public String chat(String prompt) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Groq API key is missing. Set groq.apiKey (or env var).");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyMap = Map.of(
                "model", model,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        String body = objectMapper.writeValueAsString(bodyMap);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions",
                    request,
                    String.class
            );

            String raw = response.getBody();
            if (raw == null || raw.isBlank()) return "";

            JsonNode root = objectMapper.readTree(raw);
            JsonNode contentNode = root.at("/choices/0/message/content");
            return contentNode.isMissingNode() ? "" : contentNode.asText();

        } catch (HttpStatusCodeException e) {
            // Includes response body (very useful)
            throw new RuntimeException("Groq HTTP " + e.getStatusCode() + " body=" + e.getResponseBodyAsString(), e);
        }
    }
}
