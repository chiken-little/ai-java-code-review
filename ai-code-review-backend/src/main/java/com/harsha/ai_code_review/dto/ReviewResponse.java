package com.harsha.ai_code_review.dto;

public class ReviewResponse {

    private CodeReviewResult result;
    private String rawResponse;

    public ReviewResponse() {}

    public ReviewResponse(CodeReviewResult result, String rawResponse) {
        this.result = result;
        this.rawResponse = rawResponse;
    }

    public CodeReviewResult getResult() { return result; }
    public void setResult(CodeReviewResult result) { this.result = result; }

    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
