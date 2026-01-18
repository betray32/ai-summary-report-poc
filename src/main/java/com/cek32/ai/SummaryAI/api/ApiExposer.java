package com.cek32.ai.SummaryAI.api;

import com.cek32.ai.SummaryAI.model.RequestSummaryAnalysis;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiExposer {

    @PostMapping(value = "/checkAnGenerateAnalysis", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> checkAnGenerateAnalysis(@RequestBody RequestSummaryAnalysis request) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Request body is required"));
        }

        // TODO: replace placeholder logic with real validation/analysis generation
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Analysis received and generated (placeholder)"
        ));
    }
}