package com.cek32.ai.SummaryAI.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseSummaryAnalysis {

    private String flightKey;
    private String generatedAt;
    private Summary summary;
    private Signals signals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String text;
        private Priority priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Signals {
        private int departureDelayMinutes;
        private int arrivalDelayMinutes;
        private List<String> routingFlags;
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
