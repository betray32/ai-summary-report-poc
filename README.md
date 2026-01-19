# ✈️ Flight Trax AI Summary POC

A lightweight Proof of Concept built with **Java 17 + Spring Boot** that analyzes structured flight operational data and generates a **concise AI-powered summary** to improve situational awareness and reduce cognitive load for operators.

---

## 🧠 Concept

Operational dashboards display large volumes of flight data across multiple sections (status, routing, segments, aircraft, delays, gates). This service adds an **AI advisory layer** that:

* Accepts a structured operational request
* Builds a context-aware prompt
* Calls an AI provider (OpenAI)
* Returns a **2–4 line professional summary** plus deterministic operational signals

AI is used for **decision support**, not automation.

---

## 🏗️ Architecture

```
[Client / Postman]
      |
      v
[Spring REST API]
      |
      v
[GenerateSummaryService]
      |
      v
[OpenAiClient] → [OpenAI API]
```

Design goals:

* Provider isolation (easy swap to AWS Bedrock / Azure OpenAI)
* Deterministic backend signals
* Low-cost, usage-limited AI calls

---

## 🚀 Features

* Structured flight request model
* Context-rich AI prompt builder
* AI-generated operational summary
* Backend signals:

    * Departure delay
    * Arrival delay
    * Routing flags (IRROP / RCVR / XSTP)
* Priority classification (LOW → CRITICAL)
* Fallback logic when AI is unavailable

---

## 🛠️ Tech Stack

* Java 17
* Spring Boot (Web MVC)
* Lombok
* Gson
* OpenAI API (`/v1/responses`)
* Maven

---

## 🔐 Setup

### 1. API Key

Create a key at:

> [https://platform.openai.com/api-keys](https://platform.openai.com/api-keys)

### 2. Environment Variable

**Linux / Mac**

```bash
export OPENAI_API_KEY="sk-xxxx"
```

**Windows (PowerShell)**

```powershell
setx OPENAI_API_KEY "sk-xxxx"
```

### 3. Application Properties

```properties
openai.api.key=${OPENAI_API_KEY}
```

---

## ▶️ Run

```bash
mvn clean spring-boot:run
```

Service runs at:

```
http://localhost:8080
```

---

## 🌐 API

### POST

```
/api/checkAndGenerateAnalysis
```

**Headers**

```
Content-Type: application/json
```

---
## 📤 Sample Request

```json
{
  "searchContext": {
    "carrier": "UA",
    "flightNumber": "1003",
    "origin": "EWR",
    "date": "2026-01-14",
    "timeMode": "LOCAL",
    "searchType": "SEGMENT_DEP_DATE"
  },
  "operationalStatus": {
    "status": "DELAYED",
    "statusLabel": "DELAYED 259",
    "out": null,
    "off": null,
    "on": null,
    "in": null
  },
  "flightInfo": {
    "flightNumber": "1003",
    "originatingStation": "EWR",
    "destination": "CHS",
    "scheduledDeparture": "16:04",
    "estimatedDeparture": "16:04",
    "actualDeparture": null,
    "departureDelayMinutes": 259,
    "divisionDelayMinutes": 259,
    "lateTurnMinutes": 0,
    "minimumServiceTime": 0,
    "departureGate": null
  },
  "flightFeed": {
    "flightDate": "2026-01-14",
    "scheduledArrival": "18:11",
    "estimatedArrival": "18:11",
    "actualArrival": null,
    "arrivalDelayMinutes": 186,
    "actualCrewOutDeparture": null,
    "actualGate": "B1"
  },
  "segments": [
    {
      "carrier": "UA",
      "flightDate": "2026-01-14",
      "origin": "DEN",
      "departureTime": "09:36",
      "destination": "EWR",
      "arrivalTime": "15:00",
      "nose": "4655",
      "equipment": "20S",
      "departureGate": "B11",
      "arrivalGate": null,
      "irropCode": "XSTP",
      "status": "IN"
    },
    {
      "carrier": "UA",
      "flightDate": "2026-01-14",
      "origin": "EWR",
      "departureTime": "16:04",
      "destination": "CHS",
      "arrivalTime": "18:11",
      "nose": "4655",
      "equipment": "20S",
      "departureGate": null,
      "arrivalGate": "B1",
      "irropCode": "RCVR-XSTP",
      "status": "N/A"
    }
  ],
  "aircraftInfo": {
    "noseNumber": "4655",
    "registrationNumber": "N455UA",
    "equipment": "320",
    "subfleetCode": "20S",
    "totalSeats": 150,
    "seatConfiguration": "12-0-138",
    "totalBooked": 75,
    "onboardConfiguration": "12-0-63"
  },
  "aircraftRouting": [
    {
      "flightDate": "2026-01-14",
      "origin": "DEN",
      "departureTime": "09:36",
      "destination": "EWR",
      "arrivalTime": "15:00",
      "irrop": "XSTP",
      "status": "IN"
    },
    {
      "flightDate": "2026-01-14",
      "origin": "EWR",
      "departureTime": "16:04",
      "destination": "CHS",
      "arrivalTime": "18:11",
      "irrop": "RCVR-XSTP",
      "status": "N_A"
    }
  ]
}
```

---

## 📤 Sample Response

```json
{
  "flightKey": "UA-1003-2026-01-14-EWR",
  "generatedAt": "2026-01-18T22:33:15Z",
  "summary": {
    "text": "Flight 1003 from EWR to CHS is significantly delayed with routing recovery in progress. Continued monitoring is recommended.",
    "priority": "CRITICAL"
  },
  "signals": {
    "departureDelayMinutes": 259,
    "arrivalDelayMinutes": 186,
    "routingFlags": ["XSTP", "RCVR-XSTP"]
  }
}
```

---

## 💰 Cost Control

* Model: `gpt-4o-mini`
* Typical usage: ~1,000 tokens per request
* Recommended monthly hard limit: **$5 USD**

---

## 📜 Disclaimer

This project is a **technical and architectural proof of concept**. AI-generated summaries are advisory and must not be treated as authoritative operational decisions.

---

## 👤 Author

**Camilo E. Contreras (Betray32 - Cek32)**
