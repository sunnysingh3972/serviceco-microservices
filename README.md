# ServiceCo — Service Provider Marketplace

ServiceCo is a **Java and Spring Boot based service-provider marketplace platform** that connects customers with local service providers such as cooks, babysitters, cleaners, and other professionals.

The platform is being developed using a **microservices architecture**, with a focus on clean REST APIs, scalable backend design, database optimization, JPA/Hibernate, service discovery, inter-service communication, security, and automated testing.

---

## Architecture

The final platform is designed around independently deployable microservices.

```text
                              Client / Postman
                                    |
                                    v
                             +--------------+
                             | API Gateway  |
                             +------+-------+
                                    |
             +----------------------+----------------------+
             |                      |                      |
             v                      v                      v
      +-------------+       +-------------+       +-------------+
      | Auth Service|       |  Provider   |       |  Booking    |
      |             |       |  Service    |       |  Service    |
      +-------------+       +------+------+       +------+------+
                                   |                      |
                                   |                      |
                                   v                      v
                            +--------------+       +--------------+
                            | Provider DB  |       | Booking DB   |
                            +--------------+       +--------------+
                                   |
                                   v
                            +--------------+
                            |  Matching    |
                            |   Service    |
                            +--------------+

                       +------------------------+
                       |    Eureka Server       |
                       |   Service Discovery    |
                       +------------------------+
```

Each business service is designed with its own database/schema boundary to maintain service independence.

---

# Microservices

| Service          | Responsibility                                      | Status            |
| ---------------- | --------------------------------------------------- | ----------------- |
| Provider Service | Provider profiles, skills, availability, and search | ✅ Implemented     |
| Discovery Server | Service registration and discovery using Eureka     | ✅ Implemented     |
| Booking Service  | Booking lifecycle and provider communication        | 🚧 In Development |
| Auth Service     | Registration, login, JWT authentication, and roles  | ⏳ Planned         |
| Matching Service | Provider matching and recommendations               | ⏳ Planned         |
| API Gateway      | Central routing and security entry point            | ⏳ Planned         |

---

# Technology Stack

## Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Maven
* Lombok

## Database

* MySQL

## Database Migration

* Flyway

## Security

* Spring Security
* JWT

## Microservices

* Spring Cloud Netflix Eureka
* Spring Cloud OpenFeign
* Spring Cloud LoadBalancer
* Spring Cloud Gateway

## API Documentation

* OpenAPI
* Swagger UI

## Testing

* JUnit 5
* Mockito

## Development Tools

* IntelliJ IDEA
* Postman
* Git
* GitHub

## Planned Deployment

* Docker
* Docker Compose

---

# Provider Service

The Provider Service is the primary business service currently implemented.

It manages:

* Provider profiles
* Provider skills
* Provider availability
* Provider search
* Pagination
* Location filtering
* Skill filtering
* Availability filtering

## Provider Service Package Structure

```text
serviceco-provider-service/
│
├── src/main/java/com/serviceco/serviceco_provider_service/
│
│   ├── controller/
│   │   └── ProviderController.java
│   │
│   ├── services/
│   │   ├── ProviderService.java
│   │   └── ProviderServiceImpl.java
│   │
│   ├── repository/
│   │   ├── ProviderRepository.java
│   │   └── ProviderSkillRepository.java
│   │
│   ├── model/
│   │   ├── Provider.java
│   │   ├── ProviderSkill.java
│   │   └── ProviderStatus.java
│   │
│   ├── model/dto/
│   │   ├── ProviderRequest.java
│   │   ├── ProviderResponse.java
│   │   ├── ProviderSearchResponse.java
│   │   ├── ProviderSummaryResponse.java
│   │   ├── SkillRequest.java
│   │   ├── SkillResponse.java
│   │   └── AvailabilityUpdateRequest.java
│   │
│   ├── mapper/
│   │   └── ProviderMapper.java
│   │
│   └── exception/
│       ├── ApiError.java
│       ├── GlobalExceptionHandler.java
│       ├── ProviderNotFoundException.java
│       └── DuplicateSkillException.java
│
└── src/main/resources/
    ├── application.properties
    └── db/migration/
        ├── V1__create_provider_tables.sql
        ├── V2__create_provider_skills_table.sql
        ├── V3__create_provider_indexes.sql
        └── V4__rename_phone_to_phone_number.sql
```

---

# Provider Domain Model

A provider can have multiple skills.

```text
Provider
   |
   +---- ProviderSkill
   |
   +---- ProviderSkill
   |
   +---- ProviderSkill
```

## Provider

```text
id
name
email
phoneNumber
experience
location
hourlyRate
rating
status
```

## Provider Status

```text
AVAILABLE
BUSY
INACTIVE
```

## Provider Skill

```text
id
providerId
skillName
```

The relationship is implemented using JPA:

```java
@OneToMany(
        mappedBy = "provider",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
)
@Builder.Default
private List<ProviderSkill> skills = new ArrayList<>();
```

The `ProviderSkill` entity owns the relationship through the `provider_id` foreign key.

---

# Implemented Provider APIs

## Create Provider

```http
POST /api/providers
```

Example request:

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "phoneNumber": "9876543210",
  "experience": 5,
  "location": "Noida",
  "hourlyRate": 500,
  "skills": [
    "COOKING",
    "BABYSITTING"
  ]
}
```

---

## Get Provider

```http
GET /api/providers/{id}
```

The provider detail API explicitly fetches skills when required.

---

## Get Providers

```http
GET /api/providers
```

Pagination example:

```http
GET /api/providers?page=0&size=10
```

The list API uses a summary DTO so that unnecessary skill collections are not loaded for every provider.

---

## Filter Providers by Location

```http
GET /api/providers?location=Noida&page=0&size=10
```

---

## Provider Search

Search providers using:

* Location
* Skill
* Availability
* Pagination

```http
GET /api/providers/search
```

Example:

```http
GET /api/providers/search?location=Noida&skill=BABYSITTING&status=AVAILABLE&page=0&size=10
```

The search uses a JPQL DTO projection joining:

```text
Provider
   |
   +---- ProviderSkill
```

Only the required fields are selected for the search response.

---

## Update Provider

```http
PUT /api/providers/{id}
```

---

## Delete Provider

```http
DELETE /api/providers/{id}
```

---

## Update Availability

```http
PATCH /api/providers/{id}/availability
```

Example:

```json
{
  "status": "BUSY"
}
```

---

# Skill APIs

## Add Skill

```http
POST /api/providers/{providerId}/skills
```

Example:

```json
{
  "skillName": "CLEANING"
}
```

Duplicate provider skills are prevented using validation/service checks and a database-level unique constraint.

## Get Skills

```http
GET /api/providers/{providerId}/skills
```

## Delete Skill

```http
DELETE /api/providers/{providerId}/skills/{skillId}
```

---

# Database Design

Current Provider Service database:

```text
serviceco_provider
```

Main tables:

```text
providers
provider_skills
```

Relationship:

```text
providers
    |
    | 1
    |
    | *
    v
provider_skills
```

Example:

```text
providers
----------------------------------------------------------------
id | name          | location | hourly_rate | status
1  | Rahul Sharma  | Noida    | 500          | AVAILABLE
2  | Amit Kumar    | Delhi    | 400          | BUSY
```

```text
provider_skills
--------------------------------------
id | provider_id | skill_name
1  | 1           | COOKING
2  | 1           | BABYSITTING
3  | 2           | BABYSITTING
```

---

# Hibernate and JPA Design

The Provider Service demonstrates several JPA/Hibernate optimization concepts.

## Lazy Loading

Provider skills are configured with:

```java
fetch = FetchType.LAZY
```

This prevents child collections from being loaded when they are not needed.

## Entity Graph

The provider detail query uses an EntityGraph to explicitly fetch skills for APIs that require the complete provider information.

## Cascade

```java
cascade = CascadeType.ALL
```

propagates relevant persistence operations between the provider and its associated skills.

## Orphan Removal

```java
orphanRemoval = true
```

allows orphaned skill records to be removed when they are no longer associated with the provider.

## DTO Projection

Provider search uses DTO projection instead of loading complete entities when only a subset of fields is required.

## Pagination

Spring Data `Page` and `Pageable` are used to avoid loading large result sets into memory.

## Database Indexes

Indexes have been added for frequently used skill-related queries:

```text
provider_skills.skill_name
provider_skills.provider_id
```

A unique constraint is also used to prevent duplicate skills for the same provider.

---

# Database Migration with Flyway

Database schema changes are managed using Flyway migrations.

Current migrations:

```text
V1__create_provider_tables.sql
V2__create_provider_skills_table.sql
V3__create_provider_indexes.sql
V4__rename_phone_to_phone_number.sql
```

Hibernate schema validation is used instead of automatic schema modification:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This keeps database structure controlled through versioned migration scripts.

---

# Error Handling

The Provider Service uses centralized exception handling through:

```java
@RestControllerAdvice
```

Example response:

```json
{
  "status": 404,
  "message": "Provider not found with id: 999",
  "timestamp": "2026-09-25T..."
}
```

Validation failures and duplicate skill errors are also handled centrally.

---

# API Documentation

OpenAPI/Swagger documentation is integrated into the Provider Service.

This makes it possible to view and test available REST endpoints through a browser-based API interface.

---

# Discovery Server

ServiceCo uses **Netflix Eureka** for service registration and discovery.

The Discovery Server runs on:

```text
http://localhost:8761
```

The current standalone Eureka configuration uses:

```properties
server.port=8761

eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

The Provider Service registers with Eureka using:

```properties
spring.application.name=serviceco-provider-service
```

The Eureka registry therefore identifies the Provider Service using:

```text
serviceco-provider-service
```

---

# Booking Service

The Booking Service is currently under development.

The first implementation focuses on demonstrating **service-to-service communication** between Booking Service and Provider Service.

## Current Booking Service Structure

```text
serviceco-booking-service/
│
├── src/main/java/com/serviceco/serviceco_booking_service/
│
│   ├── client/
│   │   └── ProviderClient.java
│   │
│   ├── controller/
│   │   └── BookingController.java
│   │
│   └── model/
│       └── dto/
│           └── ProviderResponse.java
│
└── src/main/resources/
    └── application.properties
```

## Current Booking Service Configuration

```properties
spring.application.name=serviceco-booking-service
server.port=8082

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

## OpenFeign Communication

Booking Service uses OpenFeign to communicate with Provider Service:

```java
@FeignClient(name = "serviceco-provider-service")
public interface ProviderClient {

    @GetMapping("/api/providers/{id}")
    ProviderResponse getProvider(
            @PathVariable("id") Long providerId
    );
}
```

The service lookup flow is:

```text
Booking Service
      |
      v
OpenFeign
      |
      v
Eureka Server
      |
      v
serviceco-provider-service
      |
      v
Provider Service : 8080
```

This removes the need for Booking Service to hardcode the Provider Service URL.

---

# Current Local Service Ports

| Service                 |    Port |
| ----------------------- | ------: |
| Eureka Discovery Server |    8761 |
| Provider Service        |    8080 |
| Booking Service         |    8082 |
| API Gateway             | Planned |
| Auth Service            | Planned |
| Matching Service        | Planned |

---

# Running the Project

## Prerequisites

Install:

* Java 21
* Maven
* MySQL
* IntelliJ IDEA or another Java IDE
* Postman

## Create Provider Database

```sql
CREATE DATABASE serviceco_provider;
```

## Local Provider Configuration

Keep sensitive configuration such as database credentials inside your local `application.properties`.

Example:

```properties
spring.application.name=serviceco-provider-service
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/serviceco_provider
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

spring.flyway.enabled=true

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

> `application.properties` is kept out of GitHub because it contains environment-specific configuration. A non-sensitive example configuration can be shared separately.

## Start Order

For local microservice development, start the services in this order:

```text
1. Discovery Server
2. Provider Service
3. Booking Service
```

The Provider and Booking services then register with Eureka.

---

# Development Roadmap

## Phase 1 — Provider Service

* [x] Provider CRUD
* [x] DTO layer
* [x] Validation
* [x] Mapper
* [x] Exception handling
* [x] Provider skills
* [x] Duplicate skill handling
* [x] Availability management
* [x] Pagination
* [x] Location filtering
* [x] Skill filtering
* [x] Availability filtering
* [x] Search DTO projection
* [x] Lazy loading
* [x] Entity Graph
* [x] Database indexes
* [x] Flyway migrations
* [x] OpenAPI / Swagger
* [x] Basic JUnit and Mockito tests
* [ ] Further query/performance optimization
* [ ] Expanded unit and integration test coverage

## Phase 2 — Discovery and Communication

* [x] Eureka Discovery Server
* [x] Provider Service registration
* [x] Booking Service registration
* [x] OpenFeign setup
* [x] Service discovery through Eureka
* [ ] Complete Booking-to-Provider flow
* [ ] API Gateway
* [ ] Centralized configuration

## Phase 3 — Authentication

* [ ] User registration
* [ ] Login
* [ ] Password hashing
* [ ] JWT authentication
* [ ] Role-based authorization

## Phase 4 — Booking

* [ ] Create booking
* [ ] Booking validation
* [ ] Accept booking
* [ ] Reject booking
* [ ] Cancel booking
* [ ] Complete booking
* [ ] Booking history
* [ ] Booking state management
* [ ] Booking database

## Phase 5 — Matching

* [ ] Skill-based matching
* [ ] Location-based matching
* [ ] Availability matching
* [ ] Rating-based sorting
* [ ] Provider recommendations

## Phase 6 — Quality and Deployment

* [ ] Unit testing
* [ ] Integration testing
* [ ] API integration testing
* [ ] Docker
* [ ] Docker Compose
* [ ] Production configuration
* [ ] Monitoring
* [ ] Logging

---

# Project Goals

ServiceCo is being developed to demonstrate practical backend engineering concepts including:

* REST API development
* Layered architecture
* Microservices architecture
* Spring Boot
* JPA/Hibernate
* Lazy loading
* Entity Graphs
* DTO projections
* Pagination
* Database indexing
* Flyway database migrations
* Centralized exception handling
* Service discovery with Eureka
* Inter-service communication with OpenFeign
* Load balancing
* JWT security
* Unit testing
* Integration testing
* Containerized deployment

---

# Repository Structure

The main repository contains all ServiceCo microservices:

```text
serviceco-microservices/
│
├── serviceco-provider-service/
├── serviceco-booking-service/
├── serviceco-auth-service/
├── serviceco-matching-service/
├── serviceco-api-gateway/
├── serviceco-discovery-server/
│
├── docker-compose.yml
├── README.md
└── .gitignore
```

> Microservices marked as planned will be added as development progresses.

---

# Author

**Sunny Singh**

Java | Spring Boot | Microservices | SQL | Backend Development
