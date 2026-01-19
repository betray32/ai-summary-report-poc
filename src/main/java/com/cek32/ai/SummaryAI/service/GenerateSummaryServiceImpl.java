package com.cek32.ai.SummaryAI.service;

import com.cek32.ai.SummaryAI.external.openai.OpenAiClient;
import com.cek32.ai.SummaryAI.model.RequestSummaryAnalysis;
import com.cek32.ai.SummaryAI.model.ResponseSummaryAnalysis;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GenerateSummaryServiceImpl implements GenerateSummaryService {

    private final OpenAiClient openAiClient;

    public GenerateSummaryServiceImpl(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    @Override
    public ResponseSummaryAnalysis analyze(RequestSummaryAnalysis request) {
        String flightKey = buildFlightKey(request);

        try {
            String text = openAiClient.generateSummary(request);

            int depDelay = request.getFlightInfo() != null && request.getFlightInfo().getDepartureDelayMinutes() != null
                    ? request.getFlightInfo().getDepartureDelayMinutes() : 0;
            int arrDelay = request.getFlightFeed() != null && request.getFlightFeed().getArrivalDelayMinutes() != null
                    ? request.getFlightFeed().getArrivalDelayMinutes() : 0;

            List<String> routingFlags = request.getAircraftRouting() == null ? List.of()
                    : request.getAircraftRouting().stream()
                    .map(r -> r.getIrrop())
                    .filter(f -> f != null && !f.isBlank())
                    .distinct()
                    .toList();

            ResponseSummaryAnalysis.Priority priority = computePriority(depDelay, arrDelay, routingFlags);

            ResponseSummaryAnalysis response = new ResponseSummaryAnalysis();
            response.setFlightKey(flightKey);
            response.setGeneratedAt(Instant.now().toString());
            response.setSummary(new ResponseSummaryAnalysis.Summary(text, priority));
            response.setSignals(new ResponseSummaryAnalysis.Signals(depDelay, arrDelay, routingFlags));

            return response;

        } catch (Exception e) {
            return buildFallbackResponse(flightKey);
        }
    }

    private ResponseSummaryAnalysis buildFallbackResponse(String flightKey) {
        ResponseSummaryAnalysis response = new ResponseSummaryAnalysis();
        response.setFlightKey(flightKey);
        response.setGeneratedAt(Instant.now().toString());
        response.setSummary(new ResponseSummaryAnalysis.Summary(
                "Unable to generate AI summary. Please review operational data manually.",
                ResponseSummaryAnalysis.Priority.MEDIUM
        ));
        response.setSignals(new ResponseSummaryAnalysis.Signals(0, 0, List.of()));
        return response;
    }

    private ResponseSummaryAnalysis.Priority computePriority(int dep, int arr, List<String> flags) {
        if (dep >= 240 || flags.stream().anyMatch(f -> f.contains("RCVR")))
            return ResponseSummaryAnalysis.Priority.CRITICAL;
        if (dep >= 120 || arr >= 120)
            return ResponseSummaryAnalysis.Priority.HIGH;
        if (dep > 0 || arr > 0)
            return ResponseSummaryAnalysis.Priority.MEDIUM;
        return ResponseSummaryAnalysis.Priority.LOW;
    }

    private String buildFlightKey(RequestSummaryAnalysis r) {
        if (r.getSearchContext() == null) return "UNKNOWN-FLIGHT";
        return String.format(
                "%s-%s-%s-%s",
                ns(r.getSearchContext().getCarrier()),
                ns(r.getSearchContext().getFlightNumber()),
                ns(r.getSearchContext().getDate()),
                ns(r.getSearchContext().getOrigin())
        );
    }

    private String ns(String v) {
        return v == null || v.isBlank() ? "N/A" : v;
    }
}
