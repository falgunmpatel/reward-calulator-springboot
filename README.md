# Retailer Rewards Calculator

A Spring Boot REST API that calculates reward points earned by customers based on their purchase transactions. Given a set of transactions over any time period, the API returns a per-month and total reward point breakdown for each customer.

---

## Tech Stack

| | |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.2 |
| Database | PostgreSQL (runtime) · H2 (tests) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Boilerplate | Lombok |

---

## How Reward Points Work

Points are calculated per transaction based on the dollar amount spent (cents are ignored):

| Spend Range | Points Earned |
|---|---|
| $0 – $50 | 0 points |
| $50.01 – $100 | 1 point per dollar over $50 |
| Over $100 | 50 points + 2 points per dollar over $100 |

**Examples:**

| Transaction | Calculation | Points |
|---|---|---|
| $120.00 | (120−100)×2 + 50 | 90 |
| $75.00 | (75−50)×1 | 25 |
| $200.00 | (200−100)×2 + 50 | 250 |
| $45.00 | — | 0 |

---

## Project Structure

```
com.example.rewardcalculator
├── model/
│   ├── Customer.java          — JPA entity (id, name, email)
│   └── Transaction.java       — JPA entity (id, customer, amount, transactionDate)
├── repository/
│   ├── CustomerRepository.java
│   └── TransactionRepository.java
├── service/
│   ├── RewardService.java     — interface
│   └── RewardServiceImpl.java — points calculation & monthly aggregation
├── controller/
│   └── RewardController.java  — GET /api/rewards, GET /api/rewards/{customerId}
├── dto/
│   ├── MonthlyRewardDTO.java
│   └── CustomerRewardSummaryDTO.java
└── exception/
    ├── CustomerNotFoundException.java
    └── GlobalExceptionHandler.java
```

---

## Prerequisites

- Java 21
- Maven 3.x
- PostgreSQL running on `localhost:5432`

---

## Database Setup

Create the database before running the app:

```sql
CREATE DATABASE rewarddb;
```

Connection defaults — all overridable via environment variables:

| Env Var | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/rewarddb` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `12341234` |

To override, set the env vars before running:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/rewarddb
export DB_USERNAME=myuser
export DB_PASSWORD=mypassword
```

---

## Running the App

```bash
mvn spring-boot:run
```

The app starts at **`http://localhost:8081`**.

On first run, Hibernate auto-creates the schema (`ddl-auto: update`) and `data.sql` seeds the database with 3 customers and 14 transactions across January–March 2024.

---

## Running Tests

```bash
mvn test
```

Tests run against an in-memory **H2 database** using the `test` Spring profile — no PostgreSQL installation required.

---

## API Reference

### Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/rewards` | Reward summary for all customers |
| `GET` | `/api/rewards/{customerId}` | Reward summary for a single customer |

---

### GET `/api/rewards`

Returns reward summaries for all customers.

```bash
curl http://localhost:8081/api/rewards
```

**Response `200 OK`:**
```json
[
  {
    "customerId": 1,
    "customerName": "Alice Johnson",
    "monthlyRewards": [
      { "year": 2024, "month": "JANUARY",  "points": 115 },
      { "year": 2024, "month": "FEBRUARY", "points": 250 },
      { "year": 2024, "month": "MARCH",    "points": 70  }
    ],
    "totalPoints": 435
  },
  {
    "customerId": 2,
    "customerName": "Bob Smith",
    "monthlyRewards": [
      { "year": 2024, "month": "JANUARY",  "points": 115 },
      { "year": 2024, "month": "FEBRUARY", "points": 49  },
      { "year": 2024, "month": "MARCH",    "points": 150 }
    ],
    "totalPoints": 314
  }
]
```

---

### GET `/api/rewards/{customerId}`

Returns the reward summary for a single customer.

```bash
curl http://localhost:8081/api/rewards/1
```

**Response `200 OK`:**
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

---

### Error Responses

All errors follow a consistent JSON structure:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id: 999"
}
```

| Scenario | Status |
|---|---|
| Customer ID does not exist | `404 Not Found` |
| Non-numeric ID (e.g. `/api/rewards/abc`) | `400 Bad Request` |
| Unexpected server error | `500 Internal Server Error` |

---

## Sample Data

The app seeds the following data on startup:

| Customer | Month | Transactions | Points |
|---|---|---|---|
| Alice Johnson | January 2024 | $120.00, $75.50 | 115 |
| Alice Johnson | February 2024 | $200.00, $45.00 | 250 |
| Alice Johnson | March 2024 | $110.00 | 70 |
| Bob Smith | January 2024 | $55.00, $130.00 | 115 |
| Bob Smith | February 2024 | $99.99, $40.00 | 49 |
| Bob Smith | March 2024 | $150.00 | 150 |
| Carol White | January 2024 | $300.00 | 550 |
| Carol White | February 2024 | $88.00, $50.00 | 38 |
| Carol White | March 2024 | $175.00 | 200 |
