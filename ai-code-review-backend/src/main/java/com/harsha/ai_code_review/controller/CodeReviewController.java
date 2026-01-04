package com.harsha.ai_code_review.controller;

import com.harsha.ai_code_review.dto.CodeReviewResult;
import com.harsha.ai_code_review.dto.ReviewResponse;
import com.harsha.ai_code_review.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    @PostMapping("/upload")
    public ResponseEntity<ReviewResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            CodeReviewResult result = codeReviewService.reviewFile(file);
            ReviewResponse response = new ReviewResponse(result, "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ReviewResponse response = new ReviewResponse();
            response.setRawResponse("Groq review failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    private String rootMessage(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : e.toString();
    }



    @PostMapping("/upload-apply")
    public ResponseEntity<com.harsha.ai_code_review.dto.ApplyReviewResponse> uploadAndApply(@RequestParam("file") MultipartFile file) {
        try {
            CodeReviewService.ApplyReviewBundle bundle = codeReviewService.reviewAndApply(file);
            return ResponseEntity.ok(new com.harsha.ai_code_review.dto.ApplyReviewResponse(
                    bundle.getResult(),
                    bundle.getUpdatedSource(),
                    "Success"
            ));
        } catch (Exception e) {
            com.harsha.ai_code_review.dto.ApplyReviewResponse response = new com.harsha.ai_code_review.dto.ApplyReviewResponse();
            response.setRawResponse("Groq review/apply failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping(value = "/upload-apply/download", produces = "application/octet-stream")
    public ResponseEntity<byte[]> uploadApplyAndDownload(@RequestParam("file") MultipartFile file) {
        try {
            CodeReviewService.ApplyReviewBundle bundle = codeReviewService.reviewAndApply(file);

            String originalName = file.getOriginalFilename();
            String downloadName = (originalName != null && !originalName.isBlank())
                    ? originalName.replaceAll("\\s+", "_")
                    : (bundle.getResult().getClassName() != null ? bundle.getResult().getClassName() + ".java" : "Updated.java");

            byte[] bytes = bundle.getUpdatedSource().getBytes(java.nio.charset.StandardCharsets.UTF_8);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", downloadName);
            headers.setContentLength(bytes.length);

            return new ResponseEntity<>(bytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            // Return readable error text as file (keeps frontend simple)
            String msg = "Groq review/apply failed: " + e.getMessage();
            return ResponseEntity.status(500).body(msg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }


}
