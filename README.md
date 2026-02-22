# Retailer Rewards Calculator

A Spring Boot REST API that calculates reward points earned by customers based on their purchase transactions. Returns a per-month breakdown and running total per customer, with optional date-range filtering and pagination.

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.2 |
| Database | PostgreSQL (runtime) · H2 (tests) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| API Docs | Springdoc OpenAPI 2.8.4 |

---

## How Points Are Calculated

Cents are truncated before calculating — `$120.99` counts as `$120`.

| Spend | Points |
|---|---|
| $0 – $50 | 0 |
| $50.01 – $100 | 1 pt per dollar over $50 |
| Over $100 | 50 pts + 2 pts per dollar over $100 |

```
$120  →  (120 - 100) × 2  +  50  =  90 pts
$75   →  (75  -  50) × 1          =  25 pts
$45   →  0 pts
```

---

## Project Structure

```
com.example.rewardcalculator
├── config/         — OpenAPI/Swagger setup
├── controller/     — REST endpoints
├── service/        — Points calculation, aggregation, pagination
├── repository/     — Spring Data JPA (includes date-range derived queries)
├── model/          — Customer, Transaction entities
├── dto/            — Java Records for all API responses
└── exception/      — Custom exceptions + GlobalExceptionHandler
```

---

## Prerequisites

- Java 21
- Maven 3.x
- PostgreSQL running on `localhost:5432`

---

## Database Setup

```sql
CREATE DATABASE rewarddb;
```

Connection values can be overridden via environment variables:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/rewarddb` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `12341234` |

---

## Running the App

```bash
./mvnw spring-boot:run
```

Starts on `http://localhost:8081`. Hibernate creates the schema on first run; `data.sql` seeds 3 customers and 14 transactions across Jan–Mar 2024.

---

## Swagger UI

`http://localhost:8081/swagger-ui.html`

OpenAPI spec (JSON): `http://localhost:8081/v3/api-docs`

---

## Running Tests

```bash
./mvnw test
```

29 tests — 17 unit (Mockito, no Spring context) + 12 integration (MockMvc + H2). PostgreSQL not required for tests.

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/rewards` | Paginated summaries for all customers |
| `GET` | `/api/rewards/{customerId}` | Summary for one customer |

### `GET /api/rewards`

| Param | Default | Notes |
|---|---|---|
| `page` | `0` | Zero-based |
| `size` | `10` | Min 1 |
| `from` | — | ISO date, inclusive |
| `to` | — | ISO date, inclusive |

### `GET /api/rewards/{customerId}`

| Param | Notes |
|---|---|
| `from` | Optional, ISO date |
| `to` | Optional, ISO date |

### Example Requests

```bash
curl "http://localhost:8081/api/rewards"
curl "http://localhost:8081/api/rewards?page=0&size=5&from=2024-01-01&to=2024-03-31"
curl "http://localhost:8081/api/rewards/1"
curl "http://localhost:8081/api/rewards/1?from=2024-01-01&to=2024-01-31"
```

### Response — `GET /api/rewards`

```json
{
  "content": [
    {
      "customerId": 1,
      "customerName": "Alice Johnson",
      "monthlyRewards": [
        { "year": 2024, "month": "JANUARY",  "points": 115 },
        { "year": 2024, "month": "FEBRUARY", "points": 250 },
        { "year": 2024, "month": "MARCH",    "points": 70  }
      ],
      "totalPoints": 435
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 3,
  "totalPages": 1,
  "last": true
}
```

### Response — `GET /api/rewards/{customerId}`

```json
{
  "customerId": 1,
  "customerName": "Alice Johnson",
  "monthlyRewards": [
    { "year": 2024, "month": "JANUARY",  "points": 115 },
    { "year": 2024, "month": "FEBRUARY", "points": 250 },
    { "year": 2024, "month": "MARCH",    "points": 70  }
  ],
  "totalPoints": 435
}
```

### Error Responses

```json
{ "status": 404, "error": "Not Found",   "message": "Customer not found with id: 999" }
{ "status": 400, "error": "Bad Request", "message": "'from' date must not be after 'to' date" }
```

| Scenario | Status |
|---|---|
| Customer not found | `404` |
| Non-numeric ID | `400` |
| Invalid date format | `400` |
| `from` after `to` | `400` |
| Page number < 0 or size < 1 | `400` |
| Unexpected error | `500` |

---

## Seed Data

| Customer | Jan 2024 | Feb 2024 | Mar 2024 | Total |
|---|---|---|---|---|
| Alice Johnson | 115 | 250 | 70 | **435** |
| Bob Smith | 115 | 49 | 150 | **314** |
| Carol White | 450 | 38 | 200 | **688** |

---

## Design Notes

**Constructor injection** — all dependencies are `final`. No `@Autowired` needed since Spring 4.3 auto-wires a single constructor. Tests instantiate the service directly: `new RewardServiceImpl(mockRepo, mockTxRepo)`.

**Java Records for DTOs** — `MonthlyRewardDTO`, `CustomerRewardSummaryDTO`, `PagedRewardSummaryDTO`, and `ErrorResponseDTO` are all records. Immutable by default, no boilerplate.

**Date filtering** — both `from` and `to` are optional. Missing bounds default to `LocalDate.MIN` / `LocalDate.MAX`. Passing `from` after `to` returns a `400` immediately rather than an empty result.

**Cents truncated** — `BigDecimal.longValue()` drops the fractional part before points are calculated, per the spec.

**Monthly sort** — `TreeMap<YearMonth, Long>` keeps months in chronological order automatically since `YearMonth` is `Comparable`.
