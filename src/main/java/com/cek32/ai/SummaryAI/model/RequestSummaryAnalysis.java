package com.cek32.ai.SummaryAI.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Single-file model for the provided request JSON.
 * Note: Dates/Times are kept as String to match the contract exactly (YYYY-MM-DD, HH:mm, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSummaryAnalysis {

    private SearchContext searchContext;
    private OperationalStatus operationalStatus;
    private FlightInfo flightInfo;
    private FlightFeed flightFeed;

    private List<Segment> segments;

    private AircraftInfo aircraftInfo;
    private List<AircraftRoutingItem> aircraftRouting;

    // -------------------------
    // Nested models
    // -------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchContext {
        private String carrier;
        private String flightNumber;
        private String origin;
        /**
         * YYYY-MM-DD
         */
        private String date;
        private TimeMode timeMode;
        private SearchType searchType;
    }

    public enum TimeMode {
        LOCAL, GMT
    }

    public enum SearchType {
        SEGMENT_DEP_DATE, FLIGHT_DATE
    }

    // -------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationalStatus {
        private OperationalStatusType status;
        private String statusLabel;

        /**
         * "string | null" in contract
         */
        private String out;
        private String off;
        private String on;
        private String in;
    }

    public enum OperationalStatusType {
        ON_TIME, DELAYED, CANCELLED, DIVERTED
    }

    // -------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightInfo {
        private String flightNumber;
        private String originatingStation;
        private String destination;

        /**
         * HH:mm
         */
        private String scheduledDeparture;
        private String estimatedDeparture;
        /**
         * HH:mm | null
         */
        private String actualDeparture;

        private Integer departureDelayMinutes;
        private Integer divisionDelayMinutes;
        private Integer lateTurnMinutes;
        private Integer minimumServiceTime;

        /**
         * string | null
         */
        private String departureGate;
    }

    // -------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightFeed {
        /**
         * YYYY-MM-DD
         */
        private String flightDate;

        /**
         * HH:mm
         */
        private String scheduledArrival;
        private String estimatedArrival;
        /**
         * HH:mm | null
         */
        private String actualArrival;

        private Integer arrivalDelayMinutes;

        /**
         * HH:mm | null
         */
        private String actualCrewOutDeparture;

        /**
         * string | null
         */
        private String actualGate;
    }

    // -------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Segment {
        private String carrier;

        /**
         * YYYY-MM-DD
         */
        private String flightDate;

        private String origin;
        /**
         * HH:mm
         */
        private String departureTime;

        private String destination;
        /**
         * HH:mm
         */
        private String arrivalTime;

        /**
         * string | null
         */
        private String nose;

        private String equipment;

        /**
         * string | null
         */
        private String departureGate;
        /**
         * string | null
         */
        private String arrivalGate;

        /**
         * string | null
         */
        private String irropCode;

        private String status;
    }

    // -------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AircraftInfo {
        /**
         * string | null
         */
        private String noseNumber;

        /**
         * string | null
         */
        private String registrationNumber;

        private String equipment;
        private String subfleetCode;

        private Integer totalSeats;
        private String seatConfiguration;

        private Integer totalBooked;
        private String onboardConfiguration;
    }

    // -------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AircraftRoutingItem {
        /**
         * YYYY-MM-DD
         */
        private String flightDate;

        private String origin;
        /**
         * HH:mm
         */
        private String departureTime;

        private String destination;
        /**
         * HH:mm
         */
        private String arrivalTime;

        /**
         * string | null
         */
        private String irrop;

        private AircraftRoutingStatus status;
    }

    public enum AircraftRoutingStatus {
        IN, OUT, CANCEL, N_A
    }
}
