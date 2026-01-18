package com.cek32.ai.SummaryAI.external.openai;

import com.cek32.ai.SummaryAI.model.RequestSummaryAnalysis;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Component
public class OpenAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public OpenAiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Sends structured flight context to OpenAI and returns a 2–4 line operational summary.
     */
    public String generateSummary(RequestSummaryAnalysis request) throws Exception {

        // 1) Build prompt from full operational context
        String prompt = buildPrompt(request);

        // 2) Build request body
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content",
                "You are an operational flight assistant. " +
                        "Generate a concise 2–4 line operational summary highlighting delays, routing anomalies, segment disruptions, and potential operational risks. " +
                        "Do not suggest automated actions. Use professional, neutral, and clear language."
        );

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);

        messages.add(systemMsg);
        messages.add(userMsg);

        body.add("messages", messages);
        body.addProperty("max_tokens", 140);
        body.addProperty("temperature", 0.2);

        String jsonBody = gson.toJson(body);

        // 3) Build HTTP request
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        // 4) Call OpenAI
        HttpResponse<String> response = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "OpenAI API error: " + response.statusCode() + " - " + response.body()
            );
        }

        // 5) Parse response
        return extractSummary(response.body());
    }

    // ------------------------
    // Prompt Builder
    // ------------------------

    private String buildPrompt(RequestSummaryAnalysis r) {
        StringBuilder sb = new StringBuilder(512);

        // Search Context
        if (r.getSearchContext() != null) {
            sb.append("Search Context:\n")
                    .append("- Carrier: ").append(nullSafe(r.getSearchContext().getCarrier())).append("\n")
                    .append("- Flight: ").append(nullSafe(r.getSearchContext().getFlightNumber())).append("\n")
                    .append("- Origin: ").append(nullSafe(r.getSearchContext().getOrigin())).append("\n")
                    .append("- Date: ").append(nullSafe(r.getSearchContext().getDate())).append("\n")
                    .append("- Time Mode: ").append(r.getSearchContext().getTimeMode()).append("\n")
                    .append("- Search Type: ").append(r.getSearchContext().getSearchType()).append("\n\n");
        }

        // Operational Status
        if (r.getOperationalStatus() != null) {
            sb.append("Operational Status:\n")
                    .append("- Status: ").append(r.getOperationalStatus().getStatus()).append("\n")
                    .append("- Label: ").append(nullSafe(r.getOperationalStatus().getStatusLabel())).append("\n")
                    .append("- OUT/OFF/ON/IN: ")
                    .append(nullSafe(r.getOperationalStatus().getOut())).append(" / ")
                    .append(nullSafe(r.getOperationalStatus().getOff())).append(" / ")
                    .append(nullSafe(r.getOperationalStatus().getOn())).append(" / ")
                    .append(nullSafe(r.getOperationalStatus().getIn())).append("\n\n");
        }

        // Flight Info
        if (r.getFlightInfo() != null) {
            sb.append("Flight Info:\n")
                    .append("- Route: ").append(nullSafe(r.getFlightInfo().getOriginatingStation()))
                    .append(" -> ").append(nullSafe(r.getFlightInfo().getDestination())).append("\n")
                    .append("- Scheduled Departure: ").append(nullSafe(r.getFlightInfo().getScheduledDeparture())).append("\n")
                    .append("- Estimated Departure: ").append(nullSafe(r.getFlightInfo().getEstimatedDeparture())).append("\n")
                    .append("- Actual Departure: ").append(nullSafe(r.getFlightInfo().getActualDeparture())).append("\n")
                    .append("- Departure Delay (min): ").append(nullSafe(r.getFlightInfo().getDepartureDelayMinutes())).append("\n")
                    .append("- Division Delay (min): ").append(nullSafe(r.getFlightInfo().getDivisionDelayMinutes())).append("\n")
                    .append("- Late Turn (min): ").append(nullSafe(r.getFlightInfo().getLateTurnMinutes())).append("\n")
                    .append("- Min Service Time (min): ").append(nullSafe(r.getFlightInfo().getMinimumServiceTime())).append("\n")
                    .append("- Gate: ").append(nullSafe(r.getFlightInfo().getDepartureGate())).append("\n\n");
        }

        // Flight Feed
        if (r.getFlightFeed() != null) {
            sb.append("Arrival Info:\n")
                    .append("- Scheduled Arrival: ").append(nullSafe(r.getFlightFeed().getScheduledArrival())).append("\n")
                    .append("- Estimated Arrival: ").append(nullSafe(r.getFlightFeed().getEstimatedArrival())).append("\n")
                    .append("- Actual Arrival: ").append(nullSafe(r.getFlightFeed().getActualArrival())).append("\n")
                    .append("- Arrival Delay (min): ").append(nullSafe(r.getFlightFeed().getArrivalDelayMinutes())).append("\n")
                    .append("- Actual Gate: ").append(nullSafe(r.getFlightFeed().getActualGate())).append("\n\n");
        }

        // Segments
        sb.append("Segments:\n");
        if (r.getSegments() != null && !r.getSegments().isEmpty()) {
            r.getSegments().forEach(seg -> {
                sb.append("- ")
                        .append(nullSafe(seg.getOrigin()))
                        .append(" -> ")
                        .append(nullSafe(seg.getDestination()))
                        .append(" | Dep: ").append(nullSafe(seg.getDepartureTime()))
                        .append(" | Arr: ").append(nullSafe(seg.getArrivalTime()))
                        .append(" | Status: ").append(nullSafe(seg.getStatus()))
                        .append(" | IRROP: ").append(nullSafe(seg.getIrropCode()))
                        .append("\n");
            });
        } else {
            sb.append("- None\n");
        }
        sb.append("\n");

        // Aircraft Info
        if (r.getAircraftInfo() != null) {
            sb.append("Aircraft:\n")
                    .append("- Nose: ").append(nullSafe(r.getAircraftInfo().getNoseNumber())).append("\n")
                    .append("- Registration: ").append(nullSafe(r.getAircraftInfo().getRegistrationNumber())).append("\n")
                    .append("- Equipment: ").append(nullSafe(r.getAircraftInfo().getEquipment())).append("\n")
                    .append("- Subfleet: ").append(nullSafe(r.getAircraftInfo().getSubfleetCode())).append("\n")
                    .append("- Seats/Booked: ")
                    .append(nullSafe(r.getAircraftInfo().getTotalBooked()))
                    .append("/")
                    .append(nullSafe(r.getAircraftInfo().getTotalSeats()))
                    .append("\n\n");
        }

        // Aircraft Routing
        sb.append("Aircraft Routing History:\n");
        if (r.getAircraftRouting() != null && !r.getAircraftRouting().isEmpty()) {
            r.getAircraftRouting().forEach(item -> {
                sb.append("- ")
                        .append(nullSafe(item.getFlightDate()))
                        .append(" ")
                        .append(nullSafe(item.getOrigin()))
                        .append(" -> ")
                        .append(nullSafe(item.getDestination()))
                        .append(" | Dep: ").append(nullSafe(item.getDepartureTime()))
                        .append(" | Arr: ").append(nullSafe(item.getArrivalTime()))
                        .append(" | Status: ").append(item.getStatus())
                        .append(" | IRROP: ").append(nullSafe(item.getIrrop()))
                        .append("\n");
            });
        } else {
            sb.append("- None\n");
        }

        sb.append("\nGenerate a concise operational summary (2–4 lines) based on the above context.");

        return sb.toString();
    }

    private String extractSummary(String jsonResponse) {
        JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);

        return root.getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString()
                .trim();
    }

    private String nullSafe(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}
