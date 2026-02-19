# Retailer Rewards Calculator

Spring Boot REST API that calculates reward points earned by customers based on their purchase transactions. It returns a per-month breakdown and a running total per customer. Supports optional date-range filtering and pagination.

## Tech Stack

- Java 21
- Spring Boot 4.0.2
- Spring Data JPA / Hibernate
- PostgreSQL (runtime), H2 (tests)
- Maven
- Springdoc OpenAPI 2.8.4

## How Points Are Calculated

Cents are truncated before calculating — $120.99 counts as $120.

- $0 to $50: 0 points
- $50.01 to $100: 1 point per dollar over $50
- Over $100: 50 points for the $50–$100 tier, plus 2 points per dollar over $100

Examples:
- $120 purchase = (20 x 2) + 50 = 90 points
- $75 purchase = 25 points
- $45 purchase = 0 points

## Project Structure

```
src/main/java/com/charter/rewardcalculator/
    config/          - OpenAPI/Swagger configuration
    controller/      - REST endpoints
    dto/             - Request/response data transfer objects
    exception/       - Custom exceptions and global exception handler
    model/           - JPA entities (Customer, Transaction)
    repository/      - Spring Data JPA repositories
    service/         - Business logic (interface + implementation)
    RewardCalculatorApplication.java

src/main/resources/
    application.yaml - App configuration
    data.sql         - Seed data (3 customers, 14 transactions)

src/test/java/com/charter/rewardcalculator/
    controller/      - RewardControllerTest (@WebMvcTest + Mockito)
    service/         - RewardServiceImplTest (pure Mockito unit tests)
    RewardCalculatorApplicationTest.java  - Smoke test
    RewardIntegrationTests.java           - @SpringBootTest + H2
```

## Prerequisites

- Java 21
- Maven 3.x
- PostgreSQL running on localhost:5432

## Database Setup

Create the database before running the app:

```sql
CREATE DATABASE rewarddb;
```

The schema is created automatically by Hibernate on first run. The `data.sql` file is executed automatically by Spring Boot on startup to seed the database.

Default connection settings:

- URL: `jdbc:postgresql://localhost:5432/rewarddb`
- Username: `postgres`
- Password: `12341234`

These can be overridden with environment variables `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

To reset and re-seed the database:

```sql
TRUNCATE TABLE transaction, customer RESTART IDENTITY CASCADE;
```

Then restart the app or run `data.sql` manually:

```bash
psql -U postgres -d rewarddb -f src/main/resources/data.sql
```

## Running the App

```bash
./mvnw spring-boot:run
```

The app starts on http://localhost:8081.

Swagger UI is available at http://localhost:8081/swagger-ui.html

OpenAPI JSON spec is available at http://localhost:8081/v3/api-docs

## Running Tests

```bash
./mvnw test
```

PostgreSQL is not required for tests. All tests use an H2 in-memory database configured via `application-test.yaml`.

Test classes:

- `RewardControllerTest` — controller slice test using @WebMvcTest and Mockito. Validates request handling, response serialization, constraint violations, and exception mapping.
- `RewardIntegrationTests` — full stack integration test using @SpringBootTest and H2. Validates end-to-end behaviour from HTTP request to database.
- `RewardServiceImplTest` — pure unit test using Mockito. Covers points calculation, date filtering, monthly aggregation, and pagination.
- `RewardCalculatorApplicationTest` — smoke test that verifies the Spring context loads successfully.

## API

### GET /api/rewards

Returns paginated reward summaries for all customers.

Query parameters:
- `page` — zero-based page number, default 0, must be >= 0
- `size` — page size, default 10, must be >= 1
- `from` — optional start date filter, inclusive, ISO-8601 format (e.g. 2024-01-01)
- `to` — optional end date filter, inclusive, ISO-8601 format (e.g. 2024-03-31)

Example request:
```
GET /api/rewards?page=0&size=10&from=2024-01-01&to=2024-03-31
```

Example response:
```json
{
  "content": [
    {
      "customerId": 1,
      "customerName": "Alice Johnson",
      "monthlyRewards": [
        { "year": 2024, "month": "JANUARY", "points": 115 },
        { "year": 2024, "month": "FEBRUARY", "points": 250 },
        { "year": 2024, "month": "MARCH", "points": 70 }
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

### GET /api/rewards/{customerId}

Returns the reward summary for a single customer.

Path variable:
- `customerId` — must be >= 1

Query parameters:
- `from` — optional start date filter, inclusive, ISO-8601 format
- `to` — optional end date filter, inclusive, ISO-8601 format

Example request:
```
GET /api/rewards/1?from=2024-01-01&to=2024-03-31
```

Example response:
```json
{
  "customerId": 1,
  "customerName": "Alice Johnson",
  "monthlyRewards": [
    { "year": 2024, "month": "JANUARY", "points": 115 },
    { "year": 2024, "month": "FEBRUARY", "points": 250 },
    { "year": 2024, "month": "MARCH", "points": 70 }
  ],
  "totalPoints": 435
}
```

### Error Responses

All errors return a consistent JSON body:

```json
{ "status": 404, "error": "Not Found", "message": "Customer not found with id: 999" }
{ "status": 400, "error": "Bad Request", "message": "'from' date (2024-03-01) must not be after 'to' date (2024-01-01)" }
```

Error scenarios:
- 404 — customer not found
- 400 — non-numeric or negative customer ID
- 400 — invalid date format
- 400 — `from` date is after `to` date
- 400 — page number < 0 or size < 1
- 500 — unexpected server error

## Seed Data

The following data is loaded on startup via `data.sql`:

| Customer          | Jan 2024 | Feb 2024 | Mar 2024 | Total |
|-------------------|----------|----------|----------|-------|
| Alice Johnson (1) | 115      | 250      | 70       | 435   |
| Bob Smith (2)     | 115      | 49       | 150      | 314   |
| Carol White (3)   | 450      | 38       | 200      | 688   |

## Code Documentation

All classes and methods have Javadoc comments. Field-level comments are omitted where the field name is self-explanatory. Overridden methods in `RewardServiceImpl` use `{@inheritDoc}` to reference the interface docs.
