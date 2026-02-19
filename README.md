# Retailer Rewards Calculator

A Spring Boot REST API that calculates reward points earned by customers based on their purchase transactions.

## Tech Stack

| | |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.2 |
| Database | PostgreSQL (runtime) · H2 (tests) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Boilerplate | Lombok |

## Prerequisites

- Java 21
- Maven 3.x
- PostgreSQL running on `localhost:5432`

## Database Setup

```sql
CREATE DATABASE rewarddb;
```

The app connects using these defaults, all overridable via environment variables:

| Env Var | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/rewarddb` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `12341234` |

## Running the App

```bash
mvn spring-boot:run
```

Available at `http://localhost:8081`. On first run, Hibernate creates the schema and `data.sql` seeds sample data (3 customers, 14 transactions).

## Running Tests

```bash
mvn test
```

Tests use an in-memory H2 database — no PostgreSQL needed.

## API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/rewards` | Reward summary for all customers |
| `GET` | `/api/rewards/{customerId}` | Reward summary for a single customer |

**Sample response — `GET /api/rewards/1`:**
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

**Error responses:**

| Scenario | Status |
|---|---|
| Customer not found | `404 Not Found` |
| Invalid ID (e.g. `/api/rewards/abc`) | `400 Bad Request` |

## Project Structure

```
com.example.rewardcalculator
├── model/        Customer, Transaction entities
├── repository/   Spring Data JPA repositories
├── service/      Reward points business logic
├── controller/   REST endpoints
├── dto/          API response shapes
└── exception/    Error handling (404, 400, 500)
```
