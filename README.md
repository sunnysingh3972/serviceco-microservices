# ServiceCo — Service Provider Marketplace

ServiceCo is a Java and Spring Boot microservices project that connects customers with local service providers such as cooks, cleaners, and babysitters. It demonstrates REST API design, JWT authentication, role-based authorization, service discovery, inter-service communication, booking lifecycle management, concurrency control, and budget-aware provider recommendations.

> **Project status:** Core microservice functionality is implemented and the main flows have been manually exercised. Matching Service returns ranked recommendations, and downstream Provider Service unavailability is mapped to a custom HTTP `503 Service Unavailable` response through both the Matching endpoint and API Gateway. Broader automated testing, deployment automation, and production hardening remain future work.

## Contents

- [Features](#features)
- [Architecture](#architecture)
- [Services and ports](#services-and-ports)
- [Technology stack](#technology-stack)
- [Authentication and authorization](#authentication-and-authorization)
- [API Gateway](#api-gateway)
- [Provider Service](#provider-service)
- [Booking Service](#booking-service)
- [Matching Service](#matching-service)
- [Databases and migrations](#databases-and-migrations)
- [Run locally](#run-locally)
- [Build and verify](#build-and-verify)
- [Resilience and error handling](#resilience-and-error-handling)
- [Testing and roadmap](#testing-and-roadmap)
- [Repository structure](#repository-structure)

## Features

- **Authentication:** User registration and login, BCrypt password hashing, JWT issuance, and role-based authorization.
- **Provider management:** Provider profiles, skills, availability, filtering, search, and pagination.
- **Resource ownership:** Provider profile updates and booking actions are protected by authenticated-user ownership checks, not role checks alone.
- **Booking management:** Booking status transitions, customer/provider access rules, idempotency, overlapping-booking validation, and pessimistic locking.
- **Service discovery:** Eureka registration and discovery for inter-service calls.
- **API Gateway:** A single entry point routes Auth, Provider, Booking, and Matching requests.
- **Provider recommendations:** Matching filters candidates by location, skill, status, and optional budget, then scores and ranks results.
- **Consistent failure responses:** Matching Service maps downstream provider unavailability to an HTTP `503` response with a readable JSON message.

## Architecture

```text
                             Client / Postman
                                    |
                                    v
                           API Gateway :8084
                                    |
              +---------------------+---------------------+
              |                     |                     |
              v                     v                     v
        Auth Service          Provider Service       Booking Service
           :8083                  :8080                  :8082
                                    ^
                                    |
                          OpenFeign + Eureka
                                    |
                                    |
                            Matching Service
                                  :8085

                     Eureka Discovery Server :8761
```

Business services communicate through HTTP APIs. Each database-backed service owns its data and schema; the project does not use cross-service JPA relationships.

## Services and ports

| Service | Port | Responsibility |
|---|---:|---|
| API Gateway | `8084` | Routes client requests to downstream services |
| Auth Service | `8083` | Registration, login, user roles, and JWT issuance |
| Provider Service | `8080` | Provider profiles, skills, availability, and search |
| Booking Service | `8082` | Booking creation, status transitions, idempotency, and concurrency protection |
| Matching Service | `8085` | Candidate filtering, recommendation scoring, ranking, and pagination |
| Eureka Discovery Server | `8761` | Service registration and discovery |

## Technology stack

- Java 21 target (the local JDK may be newer)
- Spring Boot 4.1.1
- Spring Cloud 2025.1.3
- Spring Web MVC for business services
- Spring Cloud Gateway Server WebFlux for the API Gateway
- Spring Data JPA and Hibernate
- MySQL and Flyway database migrations
- Spring Security and JWT
- Netflix Eureka, OpenFeign, and Spring Cloud LoadBalancer
- Maven Wrapper
- JUnit and Mockito dependencies for testing
- Postman for manual API verification

## Authentication and authorization

Auth Service owns user identities, password hashes, and roles. The current roles are:

```text
CUSTOMER
PROVIDER
ADMIN
```

### Login flow

1. A user registers through `POST /api/auth/register`.
2. The password is hashed with BCrypt before storage.
3. Login verifies credentials using Spring Security.
4. Auth Service issues a signed JWT. The user ID is carried in the `sub` claim and the role is included as a claim.
5. Protected services validate the JWT and map the role claim to Spring Security authorities.

Send the token on protected requests using:

```http
Authorization: Bearer <JWT>
```

### Identity and ownership

Auth user IDs and Provider profile IDs represent different things. A provider profile stores its owning Auth user ID in `providers.user_id`. The owner ID is derived from the authenticated JWT rather than trusted from a client-provided request field.

Booking creation derives the customer ID from the JWT subject. Booking reads and lifecycle actions apply role and ownership rules; administrators have the broader access permitted by the current policy.

> **Security note:** Keep database credentials, JWT signing keys, and other secrets in local configuration or a proper secrets manager. Do not commit credentials or real environment secrets to source control.

## API Gateway

The reactive Gateway runs on port `8084`, uses Eureka and Spring Cloud LoadBalancer for service discovery, and routes requests by path. It forwards the request to the relevant service, where JWT validation and authorization are enforced.

| Incoming path | Destination |
|---|---|
| `/api/auth/**` | `lb://serviceco-auth-service` |
| `/api/providers/**` | `lb://serviceco-provider-service` |
| `/api/bookings/**` | `lb://serviceco-booking-service` |
| `/api/matching/**` | `lb://serviceco-matching-service` |

Gateway health endpoint:

```http
GET http://localhost:8084/actuator/health
```

Example provider list request through the Gateway:

```http
GET http://localhost:8084/api/providers?page=0&size=10
```

## Provider Service

Provider Service owns provider profiles and their skills. It supports CRUD operations, availability changes, skill management, filtered search, and pagination. Provider search returns a DTO projection rather than exposing full persistence entities.

### Main endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/providers` | Create a provider profile for the authenticated user |
| `GET` | `/api/providers/me` | Get the authenticated provider's own profile |
| `GET` | `/api/providers/{id}` | Get a provider by profile ID |
| `GET` | `/api/providers?page=0&size=10` | List providers with pagination |
| `GET` | `/api/providers/search?location=Noida&skill=COOKING&status=AVAILABLE&page=0&size=10` | Search by location, skill, status, and pagination |
| `PUT` | `/api/providers/{id}` | Update a provider profile, subject to ownership rules |
| `DELETE` | `/api/providers/{id}` | Delete a provider profile, subject to ownership rules |
| `PATCH` | `/api/providers/{id}/availability` | Update provider availability, subject to ownership rules |
| `POST` | `/api/providers/{providerId}/skills` | Add a skill, subject to ownership rules |
| `GET` | `/api/providers/{providerId}/skills` | List a provider's skills |
| `DELETE` | `/api/providers/{providerId}/skills/{skillId}` | Delete a skill, subject to ownership rules |

Provider statuses:

```text
AVAILABLE
BUSY
INACTIVE
```

Provider skills use a one-to-many relationship with the provider profile. Skills are lazily loaded; indexes and database constraints support common lookup patterns and data integrity.

## Booking Service

Booking Service owns booking records and their status transitions. It stores `customerId` and `providerId` as identifiers instead of using cross-service JPA relationships.

### Booking statuses

```text
PENDING
ACCEPTED
REJECTED
CANCELLED
COMPLETED
```

### Main endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/bookings` | Create a booking; requires an `Idempotency-Key` header |
| `GET` | `/api/bookings/{bookingId}` | Read a booking when caller authorization permits it |
| `GET` | `/api/bookings/my?page=0&size=10` | List the authenticated user's bookings; admin behavior follows the current access policy |
| `PATCH` | `/api/bookings/{bookingId}/accept` | Accept a pending booking; provider ownership is checked |
| `PATCH` | `/api/bookings/{bookingId}/reject` | Reject a pending booking; provider ownership is checked |
| `PATCH` | `/api/bookings/{bookingId}/cancel` | Cancel according to role, ownership, and status rules |
| `PATCH` | `/api/bookings/{bookingId}/complete` | Complete an accepted booking; provider ownership is checked |

### Example create-booking request

The client sends the provider, skill, date/time, and duration. The customer ID is derived from the authenticated JWT, not accepted from the request body.

```http
POST /api/bookings
Authorization: Bearer <JWT>
Idempotency-Key: <unique-key-for-this-request>
Content-Type: application/json
```

```json
{
  "providerId": 5,
  "serviceSkill": "COOKING",
  "bookingDate": "2027-01-15T10:00:00",
  "durationHours": 2
}
```

Retrying with the same authenticated customer and idempotency key returns the existing booking rather than creating a duplicate. A replay returns the booking's current state; it does not reset a cancelled or completed booking to `PENDING`.

### Validation and concurrency

- Provider availability, requested skill, and hourly rate are obtained through the Provider Service API using OpenFeign and Eureka.
- Booking start/end times are checked for overlap; adjacent bookings can be scheduled back-to-back.
- A provider lock row and pessimistic locking protect the booking persistence critical section.
- Booking status transitions are validated in the service layer.

## Matching Service

Matching Service runs on port `8085` and registers with Eureka. It calls Provider Service via OpenFeign and gets candidates from the provider-search API.

### Matching endpoint

Direct request:

```http
GET http://localhost:8085/api/matching/providers?location=Noida&skill=COOKING&status=AVAILABLE&maxHourlyRate=600&page=0&size=10
```

Through the Gateway:

```http
GET http://localhost:8084/api/matching/providers?location=Noida&skill=COOKING&status=AVAILABLE&maxHourlyRate=600&page=0&size=10
```

### Query parameters

| Parameter | Description |
|---|---|
| `location` | Required location filter |
| `skill` | Required service skill |
| `status` | Provider status; defaults to `AVAILABLE` |
| `maxHourlyRate` | Optional maximum hourly-rate budget |
| `page` | Zero-based page number; defaults to `0` |
| `size` | Page size; defaults to `10`, maximum `100` |

### Recommendation behaviour

- Retrieves provider-search pages through OpenFeign and Eureka discovery.
- Applies the optional maximum hourly-rate filter before ranking.
- Without a budget, the score is primarily based on provider rating.
- With a budget, it combines rating (70% weight) and relative budget savings (30% weight).
- Sorts by descending match score, then ascending hourly rate, then provider ID for deterministic ordering.
- Applies pagination after filtering and ranking so page contents follow recommendation order.
- Returns the provider details with a match score and a short explanation of the recommendation.

## Resilience and error handling

Matching Service maps Provider Service unavailability to a consistent HTTP `503 Service Unavailable` response. This failure response was manually checked through both the direct Matching endpoint and the API Gateway; the normal flow was also checked and returned `200 OK` when Provider Service was available.

A Resilience4j circuit-breaker dependency/configuration and a Feign fallback factory are present in the code. The fallback factory's activation itself was not independently verified, so the circuit-breaker failure path should not yet be described as fully production-validated.

Example downstream-unavailable response:

```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Provider Service is currently unavailable. Please try again later."
}
```

## Databases and migrations

| Service | Database/schema |
|---|---|
| Provider Service | `serviceco` |
| Auth Service | `serviceco_auth` |
| Booking Service | `serviceco_booking` |
| API Gateway | No service-owned database required for current functionality |
| Matching Service | No service-owned database required for current functionality |

Flyway manages schema changes for the database-backed services. Hibernate schema validation is configured with `ddl-auto=validate`.

## Run locally

### Prerequisites

- Java toolchain compatible with the project's Java 21 target
- Git
- MySQL
- Postman
- IDE such as VS Code or IntelliJ IDEA

### Local configuration

Each service needs its own local `application.properties` configuration. Configure the database connection, Eureka URL, and JWT signing configuration where required. Keep local properties and secrets out of source control. Property names and required values should be taken from the service's own configuration and code; do not copy production credentials into this file.

### Start order

Start the Discovery Server before services that register with Eureka:

1. Eureka Discovery Server — `8761`
2. Auth Service — `8083`
3. Provider Service — `8080`
4. Booking Service — `8082`
5. API Gateway — `8084`
6. Matching Service — `8085`

Open a terminal in each service directory and run the following command on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### Local checks

- Eureka dashboard: `http://localhost:8761`
- API Gateway health: `http://localhost:8084/actuator/health`
- Provider list through Gateway: `http://localhost:8084/api/providers?page=0&size=10`
- Matching recommendations through Gateway: `http://localhost:8084/api/matching/providers?location=Noida&skill=COOKING&status=AVAILABLE&page=0&size=10`

## Build and verify

Run the Maven Wrapper from each service directory.

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

Unix-like shells:

```bash
./mvnw clean package
```

Repeat for all six services. `clean package` compiles and packages the service and runs any tests configured in that module. A service being up in Eureka does not, by itself, prove a clean package build or comprehensive test coverage.

## Testing and roadmap

The current development flow used manual Postman checks for key API paths, including provider search/matching, Gateway routing, and the Matching Service's normal and downstream-unavailable responses. Broader automated unit and integration test coverage is still a future improvement.

- [x] Provider CRUD, skills, availability, search, filtering, and pagination
- [x] JWT-based provider profile ownership checks
- [x] Booking lifecycle and status-transition validation
- [x] Idempotency support for booking creation
- [x] Booking overlap checks and pessimistic locking
- [x] Paginated `GET /api/bookings/my`
- [x] Eureka service discovery and OpenFeign integration
- [x] Gateway routes for Auth, Provider, Booking, and Matching
- [x] Matching filters, optional budget limit, scoring, ranking, and pagination
- [x] Custom HTTP `503` error response for Provider Service unavailability
- [ ] Independently verify Resilience4j fallback-factory activation
- [ ] Expand automated unit and integration tests
- [ ] Add Docker and Docker Compose
- [ ] Add CI build workflow
- [ ] Improve production secrets/configuration strategy
- [ ] Expand monitoring, logging, and resilience policies

## Repository structure

```text
serviceco-microservices/
├── serviceco-auth-service/
├── serviceco-provider-service/
├── serviceco-booking-service/
├── serviceco-discovery-server/
├── serviceco-api-gateway/
├── serviceco-matching-service/
├── .github/                 # Add workflows here when CI is introduced
├── .gitignore
└── README.md
```

## Author

**Sunny Singh**  
Java | Spring Boot | Microservices | SQL | Backend Development
