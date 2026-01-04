package com.harsha.ai_code_review.dto;

import java.util.List;

public class CodeReviewResult {

    private String className;
    private List<MethodReview> methods;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<MethodReview> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodReview> methods) {
        this.methods = methods;
    }

    // Nested DTO for methods
    public static class MethodReview {
        private String methodName;
        private List<String> reviewComments;
        private String suggestedMethodName;
        private String suggestedJavadoc;

        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }

        public List<String> getReviewComments() { return reviewComments; }
        public void setReviewComments(List<String> reviewComments) { this.reviewComments = reviewComments; }

        public String getSuggestedMethodName() { return suggestedMethodName; }
        public void setSuggestedMethodName(String suggestedMethodName) { this.suggestedMethodName = suggestedMethodName; }

        public String getSuggestedJavadoc() { return suggestedJavadoc; }
        public void setSuggestedJavadoc(String suggestedJavadoc) { this.suggestedJavadoc = suggestedJavadoc; }
    }
}
