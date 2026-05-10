# Enterprise Train Ticketing System

A full-stack Train Ticketing application built to handle complex routing algorithms, concurrent booking transactions, and real-time network administration. 

This project was developed to satisfy strict architectural requirements, including dynamic multi-leg route generation, overbooking prevention via transactional boundaries, and real-time administrative overrides using WebSockets.

---

## Architecture & Tech Stack

* **Backend Engine:** Java 17 LTS, Spring Boot 3.4.0
* **Persistence Layer:** Spring Data JPA (Hibernate), MySQL 8
* **Frontend Application:** React (Vite), Axios, Custom CSS (Flexbox/Grid)
* **Real-time Communication:** WebSockets (STOMP / SockJS)
* **Testing Suite:** JUnit 5, Mockito
* **Build Tool:** Gradle

---

## Core Features & Problem Coverage

### A. Transactional Booking Engine
* **Concurrent Integrity:** Utilizes Spring `@Transactional` boundaries and JPA locking mechanisms to evaluate seat availability and save records in a single atomic database operation, strictly preventing race conditions and overbooking.
* **Notifications:** Implements a `NotificationService` that dispatches simulated asynchronous email confirmations upon successful transactions.

### B. Dynamic Route Finding & Changeovers
* **Virtual Changeover Algorithm:** The system evaluates both direct station-to-station connections and 1-stop transfer itineraries. If a direct train isn't available, the algorithm dynamically stitches together overlapping train segments (verifying overlapping arrival/departure times) and generates a unified "Composite Ride" for the user on the fly.
* **Safe Fails:** If an origin and destination have no mathematical connection, the system safely returns an empty set, prompting the UI to display user-friendly warnings rather than failing.

### C. Operations & Admin Ledger
* **Fleet Management:** Administrators can dynamically add and remove physical Trains and abstract Routes through the REST API and React UI.
* **Global Audit:** Features a real-time ledger viewing all active tickets globally.
* **Real-time Delay Broadcasting:** When an admin reports a delay, the system mathematically shifts the departure/arrival times in the database, sends async apology emails to affected passengers, and pushes a real-time payload via WebSockets to visually update all connected web clients instantly without a page refresh.

---

## Setup & Installation

### 1. Database Configuration
1. Ensure MySQL is running locally on port `3306`.
2. Create a database named `tickets_db`.
3. Run the provided SQL seed script (e.g., `create_schema.sql`) in your MySQL client to populate the database with the testing scenario data (Stations, Trains, Initial Rides).
4. Verify your `application.properties` includes:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/tickets_db
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update

```

### 2. Run the Backend

Navigate to the root directory containing the `build.gradle` file:

```bash
./gradlew bootRun

```

*The Spring Boot server will initialize on `http://localhost:8080`.*

### 3. Run the Frontend

Navigate to the frontend directory:

```bash
cd frontend
npm install
npm run dev

```

*The React UI will initialize on `http://localhost:5173`.*

---

## API Documentation: Inputs & Outputs

The following examples demonstrate the JSON payloads for all supported functional requirements.

### 1. Route Finding (Direct Links & Changeovers)

* **Endpoint:** `GET /api/routes?departureId={id}&arrivalId={id}`
* **Scenario:** Searching from Timisoara (ID: 4) to Bucharest (ID: 3). No direct train exists, so the system generates a composite route via Sibiu.
* **Response (200 OK):**

```json
[
  {
    "id": 1002001,
    "train": {
      "id": 999,
      "name": "Transfer required at Sibiu",
      "totalCapacity": 0
    },
    "route": { "id": 999, "name": "Multi-Line Journey" },
    "segments": [
      {
        "id": 3,
        "fromStation": { "id": 4, "name": "Timisoara" },
        "toStation": { "id": 5, "name": "Sibiu" },
        "availableSeats": 50,
        "departureTime": "2026-06-01T06:00:00",
        "arrivalTime": "2026-06-01T09:45:00"
      },
      {
        "id": 4,
        "fromStation": { "id": 5, "name": "Sibiu" },
        "toStation": { "id": 3, "name": "Bucharest" },
        "availableSeats": 50,
        "departureTime": "2026-06-01T10:15:00",
        "arrivalTime": "2026-06-01T15:30:00"
      }
    ],
    "delayMinutes": 0
  }
]

```

### 2. Transactional Ticket Booking

* **Endpoint:** `POST /api/tickets/book`
* **Scenario:** User books 2 seats on the composite ride generated above.
* **Request Body:**

```json
{
  "userEmail": "passenger@siemens.com",
  "rideId": 1002001,
  "departureStationId": 4,
  "arrivalStationId": 3,
  "numberOfSeats": 2
}

```

* **Response (201 Created):**

```json
{
  "id": 105,
  "userEmail": "passenger@siemens.com",
  "rideId": 1002001,
  "departureStationId": 4,
  "arrivalStationId": 3,
  "numberOfSeats": 2,
  "status": "CONFIRMED"
}

```

> **Edge Case:** If `numberOfSeats` exceeds the current `availableSeats`, the transaction rolls back and the API returns `400 Bad Request` with the message: *"Booking failed: Overbooking prevented."*

### 3. Admin: View Booking Ledger

* **Endpoint:** `GET /api/admin/bookings`
* **Response (200 OK):**

```json
[
  {
    "id": 1,
    "userEmail": "passenger@siemens.com",
    "rideId": 1,
    "departureStationId": 1,
    "arrivalStationId": 3,
    "numberOfSeats": 1,
    "status": "CONFIRMED"
  }
]

```

### 4. Admin: Inject Network Delays (WebSockets & Email)

* **Endpoint:** `POST /api/admin/rides/{rideId}/delay?minutes={delayMinutes}`
* **Scenario:** Admin delays Ride #1 by 45 minutes.
* **Backend Processing:** 1. Mathematically shifts all segment departure/arrival times in MySQL.
2. Triggers `NotificationService` to email all passengers on Ride #1.
3. Pushes message to WebSocket `/topic/delays`.
* **WebSocket Outbound Payload (Received by React Clients):**

```json
{
  "rideId": 1,
  "delayMinutes": 45
}

```

* **REST Response (200 OK):** `"Delay reported and customers notified via async email."`

### 5. Admin: Fleet Management (Add Train)

* **Endpoint:** `POST /api/admin/trains`
* **Request Body:**

```json
{
  "name": "Siemens Vectron Neo",
  "totalCapacity": 180
}

```

* **Response (201 Created):**

```json
{
  "id": 10,
  "name": "Siemens Vectron Neo",
  "totalCapacity": 180
}

```

### 6. Admin: Fleet Management (Add Route)

* **Endpoint:** `POST /api/admin/routes`
* **Request Body:**

```json
{
  "name": "Balkan Express Line"
}

```

* **Response (201 Created):**

```json
{
  "id": 5,
  "name": "Balkan Express Line"
}

```

### 7. Admin: Fleet Management (Delete Train)

* **Endpoint:** `DELETE /api/admin/trains/{id}`
* **Response (204 No Content)**

> **Edge Case:** If the train is actively linked to existing historical rides or tickets, the database's Foreign Key constraints will prevent deletion and the API will return a `500 Internal Server Error` (caught and handled gracefully by the UI).

---

## Testing Strategy (JUnit 5 & Mockito)

The backend business logic is strictly tested using isolated unit tests to ensure speed and reliability without needing a live database connection.

* **Execution:** `./gradlew test`
* **Test Class:** `TicketingServiceImplTest.java`
* **Scenarios Proven Systematically:**
1. `bookTicket_Success_WhenDirectRouteHasEnoughSeats`: Verifies seat deduction math and mock email dispatch.
2. `bookTicket_ThrowsException_WhenOverbooked`: Proves exceptions are thrown and database saves are aborted when seats are insufficient.
3. `findRoutes_ReturnsCompositeRide_WhenChangeoverRequired`: Proves the custom algorithm successfully stitches segments together and generates a 1,000,000+ Virtual ID.
4. `bookTicket_ThrowsException_WhenUserNotFound`: Validates "Ghost User" rejections.
5. `findRoutes_ReturnsEmptyList_WhenNoRouteExists`: Proves algorithmic safety when given impossible geography.
6. `delayRide_Success_UpdatesTimesAndNotifiesUsers`: Validates that administrative actions successfully fan out to WebSockets and the Email notification queue.
