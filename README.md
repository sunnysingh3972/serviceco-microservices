# ServiceCo — Service Provider Marketplace

ServiceCo is a Java and Spring Boot microservices project for connecting customers with local service providers such as cooks, babysitters, and cleaners. It demonstrates REST API design, service discovery, inter-service communication, JWT-based security, booking lifecycle management, and provider matching foundations.

> **Project status:** Auth, Provider, Booking, Eureka Discovery Server, and API Gateway are implemented in the current development flow. Matching Service is registered with Eureka and its initial project/DTO foundation is in place; its Feign client and matching/ranking logic are still in progress.

---

## Architecture

```text
                         Client / Postman
                                |
                                v
                       API Gateway :8084
                                |
               +----------------+----------------+
               |                |                |
               v                v                v
         Auth Service     Provider Service   Booking Service
            :8083              :8080             :8082
               |                |                |
               v                v                v
         serviceco_auth      serviceco       serviceco_booking
               
        Matching Service :8085 (in progress)
                   |
                   | planned OpenFeign integration
                   v
             Provider Service

             Eureka :8761
      Service registration/discovery
```

Each business service owns its database/schema boundary. Services exchange data through HTTP APIs rather than cross-service JPA relationships.

## Services and ports

| Service | Port | Responsibility | Status |
|---|---:|---|---|
| API Gateway | `8084` | Common entry point; routes requests to Auth, Provider, and Booking | Implemented; routes verified |
| Auth Service | `8083` | Registration, login, user roles, JWT issuance | Implemented |
| Provider Service | `8080` | Provider profiles, skills, availability, search, profile ownership | Implemented |
| Booking Service | `8082` | Booking creation, lifecycle, idempotency, concurrency, booking ownership | Implemented |
| Matching Service | `8085` | Provider matching and ranking | Initial project, Eureka registration, and response DTO in place; matching logic in progress |
| Eureka Discovery Server | `8761` | Service registration and discovery | Implemented |

## Technology stack

- Java 21 language target (local JDK may be newer)
- Spring Boot 4.1.1
- Spring Cloud 2025.1.3
- Spring Web MVC for business services
- Spring Cloud Gateway Server WebFlux for the API Gateway
- Spring Data JPA and Hibernate
- MySQL and Flyway migrations
- Spring Security and JWT (JJWT in Auth Service; OAuth2 Resource Server in protected services)
- Netflix Eureka, OpenFeign, and Spring Cloud LoadBalancer
- Maven / Maven Wrapper
- JUnit 5 and Mockito (test coverage remains a continuing task)
- Postman, IntelliJ IDEA, and VS Code

---

## Authentication and security

Auth Service owns user identities, password hashes, and roles. Current roles are:

```text
CUSTOMER
PROVIDER
ADMIN
```

### Login flow

1. A user registers through `POST /api/auth/register`.
2. Passwords are hashed with BCrypt before being stored.
3. Login uses Spring Security's `AuthenticationManager` and `UserDetailsService` to verify credentials.
4. Auth Service issues a signed JWT containing the user ID as `sub` and the role as a claim.
5. Protected services validate the JWT locally and map the role claim to Spring Security authorities. They do not need to query the Auth database for every request.

Send a token on protected requests using:

```http
Authorization: Bearer <JWT>
```

### Identity and resource ownership

Auth user IDs and Provider profile IDs are different identifiers. The Provider profile stores the owning Auth user ID in `providers.user_id`; it is derived from the authenticated JWT, not trusted from the request body.

Ownership checks are implemented for provider profile operations and booking actions. Booking reads are restricted by customer/provider ownership, with an admin exemption according to the current access policy. These checks complement role-based authorization: a `PROVIDER` role alone does not grant access to every provider's records.

> Local configuration and signing keys must remain outside source control. Do not commit passwords, JWT secrets, or real environment credentials.

---

## API Gateway

The reactive Spring Cloud Gateway runs on port `8084`. It uses Eureka service discovery and LoadBalancer to route requests by path. It forwards the original request, including the `Authorization` header when supplied by the client; the downstream service continues to validate the JWT and enforce access rules.

| Incoming path | Destination service |
|---|---|
| `/api/auth/**` | `lb://serviceco-auth-service` |
| `/api/providers/**` | `lb://serviceco-provider-service` |
| `/api/bookings/**` | `lb://serviceco-booking-service` |

Health endpoint:

```http
GET http://localhost:8084/actuator/health
```

Example request through the Gateway:

```http
GET http://localhost:8084/api/providers?page=0&size=10
```

---

## Provider Service

Provider Service owns provider profiles and skills. It supports CRUD, availability updates, skill management, pagination, filtering, and search. Provider search uses a DTO projection to return selected fields rather than full entities.

### Main endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/providers` | Create a provider profile linked to the authenticated user |
| `GET` | `/api/providers/me` | Fetch the authenticated provider's own profile |
| `GET` | `/api/providers/{id}` | Fetch provider details by profile ID |
| `GET` | `/api/providers?page=0&size=10` | List providers with pagination |
| `GET` | `/api/providers/search?location=Noida&skill=COOKING&status=AVAILABLE&page=0&size=10` | Search by location, skill, status, and pagination |
| `PUT` | `/api/providers/{id}` | Update provider details; ownership enforced |
| `DELETE` | `/api/providers/{id}` | Delete provider profile; ownership enforced |
| `PATCH` | `/api/providers/{id}/availability` | Update availability; ownership enforced |
| `POST` | `/api/providers/{providerId}/skills` | Add a skill; ownership enforced |
| `GET` | `/api/providers/{providerId}/skills` | List provider skills |
| `DELETE` | `/api/providers/{providerId}/skills/{skillId}` | Delete a skill; ownership enforced |

Provider statuses:

```text
AVAILABLE
BUSY
INACTIVE
```

A provider has a one-to-many relationship with provider skills. Skills are lazily loaded, and database constraints/indexes protect skill uniqueness and improve search access.

---

## Booking Service

Booking Service owns booking records and status transitions. It stores `providerId` and `customerId` as identifiers rather than using cross-service JPA relationships.

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
| `GET` | `/api/bookings/{bookingId}` | Fetch a booking when the caller is authorized to view it |
| `GET` | `/api/bookings/my?page=0&size=10` | Fetch bookings for the authenticated customer/provider; admin can view all under current policy |
| `PATCH` | `/api/bookings/{bookingId}/accept` | Accept a pending booking; provider ownership enforced |
| `PATCH` | `/api/bookings/{bookingId}/reject` | Reject a pending booking; provider ownership enforced |
| `PATCH` | `/api/bookings/{bookingId}/cancel` | Cancel according to role, ownership, and status rules |
| `PATCH` | `/api/bookings/{bookingId}/complete` | Complete an accepted booking; provider ownership enforced |

### Customer identity

The booking request body provides the provider, skill, booking time, and duration. The authenticated customer ID is derived from the JWT subject on the server, rather than trusting a `customerId` supplied by the client.

Example request body:

```json
{
  "providerId": 5,
  "serviceSkill": "COOKING",
  "bookingDate": "2027-01-15T10:00:00",
  "durationHours": 2
}
```

Include the idempotency header:

```http
Idempotency-Key: <unique-key-for-this-request>
```

Retrying with the same authenticated customer and idempotency key returns the existing booking instead of creating a duplicate. A replay returns the booking's current status; it does not reset a cancelled/rejected/completed booking to `PENDING`.

### Booking validation and concurrency

- The Provider Service is called through OpenFeign and Eureka to validate provider availability, requested skill, and hourly rate.
- Booking start/end times are checked for overlap. Back-to-back bookings are allowed.
- A provider lock row and pessimistic locking protect the local booking persistence critical section.
- Booking status transitions are validated in the service layer.

---

## Matching Service

Matching Service is the current development focus. It runs on port `8085` and registers with Eureka. Its initial project includes Spring Web MVC, Validation, OpenFeign, Eureka Client, LoadBalancer, and a local `ProviderSearchResponse` DTO matching the provider search response contract.

**Still in progress:** implement the Feign client for Provider Service, expose a matching endpoint, and add skill/location/availability filters and ranking (including rating-based sorting). These matching features should not be considered complete yet.

---

## Databases and migrations

| Service | Database/schema |
|---|---|
| Provider Service | `serviceco` |
| Auth Service | `serviceco_auth` |
| Booking Service | `serviceco_booking` |
| API Gateway / Matching Service | No service-owned database currently required for the implemented functionality |

Flyway manages schema changes for the database-backed services. Hibernate schema validation is configured with `ddl-auto=validate`.

---

## Local setup and running services

### Prerequisites

- Java 21 compatible toolchain (the local JDK can be newer)
- Git
- MySQL
- IDE (IntelliJ IDEA or VS Code)
- Postman

### Local configuration

Each service's `application.properties` is local configuration and should not be committed. Configure the database connection, Eureka URL, and (where required) the shared JWT signing key locally. Do not paste secrets into this README or commit them to Git.

### Start order

Start the services in this order so service discovery is available first:

1. Eureka Discovery Server — `8761`
2. Auth Service — `8083`
3. Provider Service — `8080`
4. Booking Service — `8082`
5. API Gateway — `8084`
6. Matching Service — `8085`

Run each project from its own directory using the Maven Wrapper on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Compile a service with:

```powershell
.\mvnw.cmd clean compile
```

### Verify

- Eureka dashboard: `http://localhost:8761`
- API Gateway health: `http://localhost:8084/actuator/health`
- Provider list through Gateway: `http://localhost:8084/api/providers?page=0&size=10`

---

## Development roadmap

### Provider Service

- [x] Provider CRUD, skills, and availability APIs
- [x] Validation and centralized exception handling
- [x] Search filtering, pagination, and DTO projection
- [x] Flyway migrations and schema validation
- [x] JWT-based current-provider lookup and profile ownership checks

### Auth and security

- [x] Registration and BCrypt password hashing
- [x] `AuthenticationManager`-based login and JWT generation
- [x] JWT validation and role mapping in protected services
- [x] Customer identity derived from JWT for booking creation
- [x] Customer/provider ownership checks for booking reads/actions

### Infrastructure

- [x] Eureka Discovery Server
- [x] OpenFeign and LoadBalancer in service integration setup
- [x] Reactive API Gateway routes for Auth, Provider, and Booking
- [ ] More Gateway-level security and resilience configuration

### Booking

- [x] Booking lifecycle and status-transition checks
- [x] Provider availability and skill validation
- [x] Idempotency key support
- [x] Overlap detection and pessimistic locking
- [x] Paginated `GET /api/bookings/my`
- [ ] Broader automated unit/integration tests

### Matching Service

- [x] Initial service project and Eureka registration
- [x] Provider search response DTO foundation
- [ ] OpenFeign client to Provider Service
- [ ] Matching API endpoint
- [ ] Skill and location matching
- [ ] Availability-aware results
- [ ] Rating-based ranking and recommendations

### Quality and deployment

- [ ] Broader automated tests
- [ ] Docker and Docker Compose
- [ ] Production secrets/configuration strategy
- [ ] Monitoring, logging, and resilience policies

---

## Repository structure

```text
serviceco-microservices/
├── serviceco-auth-service/
├── serviceco-provider-service/
├── serviceco-booking-service/
├── serviceco-api-gateway/
├── serviceco-matching-service/
├── serviceco-discovery-server/
├── README.md
└── .gitignore
```

## Author

**Sunny Singh**  
Java | Spring Boot | Microservices | SQL | Backend Development
