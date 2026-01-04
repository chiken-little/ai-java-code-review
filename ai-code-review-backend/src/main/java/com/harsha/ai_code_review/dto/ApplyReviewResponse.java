package com.harsha.ai_code_review.dto;

public class ApplyReviewResponse {
    private CodeReviewResult result;
    private String updatedSource;
    private String rawResponse;

    public ApplyReviewResponse() {}

    public ApplyReviewResponse(CodeReviewResult result, String updatedSource, String rawResponse) {
        this.result = result;
        this.updatedSource = updatedSource;
        this.rawResponse = rawResponse;
    }

    public CodeReviewResult getResult() { return result; }
    public void setResult(CodeReviewResult result) { this.result = result; }

    public String getUpdatedSource() { return updatedSource; }
    public void setUpdatedSource(String updatedSource) { this.updatedSource = updatedSource; }

    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
