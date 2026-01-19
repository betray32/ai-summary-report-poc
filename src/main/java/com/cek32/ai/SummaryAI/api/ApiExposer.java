package com.cek32.ai.SummaryAI.api;

import com.cek32.ai.SummaryAI.model.RequestSummaryAnalysis;
import com.cek32.ai.SummaryAI.model.ResponseSummaryAnalysis;
import com.cek32.ai.SummaryAI.service.GenerateSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiExposer {

    private final GenerateSummaryService generateSummaryService;

    @PostMapping(value = "/checkAndGenerateAnalysis", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ResponseSummaryAnalysis> checkAndGenerateAnalysis(@RequestBody(required = false) RequestSummaryAnalysis request) {

        if (request == null) {
            ResponseSummaryAnalysis error = new ResponseSummaryAnalysis();
            error.setFlightKey("INVALID-REQUEST");
            error.setGeneratedAt(Instant.now().toString());
            error.setSummary(new ResponseSummaryAnalysis.Summary(
                    "Request body is required",
                    ResponseSummaryAnalysis.Priority.CRITICAL
            ));
            error.setSignals(new ResponseSummaryAnalysis.Signals(0, 0, List.of()));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        ResponseSummaryAnalysis result = generateSummaryService.analyze(request);
        return ResponseEntity.ok(result);
    }
}
