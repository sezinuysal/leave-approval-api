# Leave Approval API

A small REST API for **leave requests with a two-step approval workflow** (manager, then HR), built with Java and Spring Boot.

This is a personal practice project that I'm using to learn the Spring ecosystem. It is based on the kind of approval workflows I build at work with C#/.NET, rebuilt from scratch in Java. It is still in development.

## Features

- Create leave requests and follow them through a two-step approval workflow: **MANAGER → HR**
- A rejection at any step ends the workflow; finished requests cannot be decided again (HTTP 409)
- Every decision is stored with approver, comment and timestamp
- Filter requests by status
- Request validation and consistent error responses (RFC 7807 problem details)
- Interactive API documentation with **Swagger UI**
- In-memory **H2** database by default, **PostgreSQL** profile included

## Tech stack

Java 17, Spring Boot, Spring Web (MVC), Spring Data JPA / Hibernate, Bean Validation, H2, PostgreSQL, springdoc-openapi (Swagger), JUnit 5, MockMvc, Maven

## Getting started

Requirements: JDK 17 and Maven (or use the included `mvnw` wrapper).

```bash
./mvnw spring-boot:run
```

- API: http://localhost:8080/api/leave-requests
- Swagger UI: http://localhost:8080/swagger-ui.html

### Run with PostgreSQL

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Database user and password can be changed with the `DB_USER` and `DB_PASSWORD` environment variables.

### Run the tests

```bash
./mvnw test
```

The tests cover the workflow rules in the domain model (unit tests) and the HTTP API end to end with MockMvc (validation, full approval flow, rejection, 404 and 409 responses, status filter).

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/leave-requests` | Create a leave request |
| GET | `/api/leave-requests?status=PENDING` | List requests, optionally filtered by status |
| GET | `/api/leave-requests/{id}` | Get one request with its decisions |
| POST | `/api/leave-requests/{id}/decisions` | Approve or reject the current step |

### Example

```bash
# 1) create
curl -X POST localhost:8080/api/leave-requests -H "Content-Type: application/json" \
  -d '{"employeeName":"Ayse","startDate":"2026-03-02","endDate":"2026-03-06","reason":"Holiday"}'

# 2) manager approves, then 3) HR approves
curl -X POST localhost:8080/api/leave-requests/1/decisions -H "Content-Type: application/json" \
  -d '{"approver":"manager","approved":true,"comment":"ok"}'
curl -X POST localhost:8080/api/leave-requests/1/decisions -H "Content-Type: application/json" \
  -d '{"approver":"hr","approved":true}'
```

After both approvals the request status is `APPROVED`. If a step is rejected the status becomes `REJECTED`, and any further decision returns `409 Conflict`.

## Project structure

```
src/main/java/com/sezinsu/leaveapproval
  controller/   REST endpoints
  service/      business operations and transactions
  domain/       entities and the approval workflow rules
  dto/          request and response records
  repository/   Spring Data JPA repository
  exception/    custom exceptions and the global error handler
```

## Next steps

- Authentication and role-based access (who may approve which step)
- Pagination for the list endpoint
- Publish events when a request is approved or rejected (e.g. Kafka)
- Dockerfile and a CI pipeline
