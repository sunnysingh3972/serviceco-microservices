# ServiceCo — Service Provider Marketplace

ServiceCo is a **Java and Spring Boot based service-provider marketplace platform** that connects customers with service providers such as cooks, babysitters, cleaners, and other local professionals.

The project is being developed using a **microservices architecture**, with a focus on clean REST APIs, scalable backend design, database optimization, Hibernate/JPA, security, and automated testing.

---

## Architecture

The final platform is planned around independently deployable microservices.

```text
                         Client / Postman
                                |
                                v
                         +---------------+
                         |  API Gateway  |
                         +-------+-------+
                                 |
             +-------------------+-------------------+
             |                   |                   |
             v                   v                   v
      +-------------+    +-------------+    +-------------+
      | Auth Service|    |  Provider   |    |   Booking   |
      |             |    |  Service    |    |   Service   |
      +-------------+    +-------------+    +-------------+
                               |
                               v
                       +---------------+
                       |   Matching    |
                       |    Service    |
                       +---------------+

                     +-------------------+
                     |  Eureka Server    |
                     | Service Discovery |
                     +-------------------+
```

Each business service will have its own database/schema boundary to maintain service independence.

---

## Microservices

| Service          | Responsibility                                  | Status           |
| ---------------- | ----------------------------------------------- | ---------------- |
| Provider Service | Provider profiles, skills, availability, search | ✅ In Development |
| Auth Service     | Registration, login, JWT authentication, roles  | ⏳ Planned        |
| Booking Service  | Booking lifecycle and booking history           | ⏳ Planned        |
| Matching Service | Provider matching and recommendations           | ⏳ Planned        |
| API Gateway      | Central routing and security entry point        | ⏳ Planned        |
| Discovery Server | Service registration and discovery using Eureka | ⏳ Planned        |

---

## Technology Stack

### Backend

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Maven

### Database

* MySQL

### Security

* Spring Security
* JWT

### Microservices

* Spring Cloud Gateway
* Netflix Eureka
* OpenFeign

### Testing

* JUnit 5
* Mockito

### Development Tools

* IntelliJ IDEA
* Postman
* Git / GitHub

### Planned Deployment

* Docker
* Docker Compose

---

# Provider Service

The Provider Service is the first microservice being implemented.

It manages provider profiles, skills, availability, and provider search.

## Current Package Structure

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
│       └── ProviderNotFoundException.java
│
└── src/main/resources/
    └── application.properties
```

---

## Provider Domain Model

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

### Provider

```text
id
name
email
phone
experience
location
hourlyRate
rating
status
```

### Provider Status

```text
AVAILABLE
BUSY
INACTIVE
```

### Provider Skill

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
private List<ProviderSkill> skills;
```

---

# Implemented APIs

## Provider APIs

### Create Provider

```http
POST /api/providers
```

Example request:

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "phone": "9876543210",
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

### Get Provider

```http
GET /api/providers/{id}
```

---

### Get Providers

```http
GET /api/providers
```

Pagination example:

```http
GET /api/providers?page=0&size=10
```

---

### Filter by Location

```http
GET /api/providers?location=Noida&page=0&size=10
```

---

## Provider Search

Search providers using location, skill, availability, and pagination.

```http
GET /api/providers/search
```

Example:

```http
GET /api/providers/search?location=Noida&skill=BABYSITTING&status=AVAILABLE&page=0&size=10
```

The search is implemented using a JPQL query joining:

```text
Provider
   ↓
ProviderSkill
```

The query uses a DTO projection so that only required fields are returned for the search response.

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

## Skill APIs

### Add Skill

```http
POST /api/providers/{providerId}/skills
```

Example:

```json
{
  "skillName": "CLEANING"
}
```

### Get Skills

```http
GET /api/providers/{providerId}/skills
```

### Delete Skill

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
-------------------------------------------------
id | name          | location | status
1  | Rahul Sharma  | Noida    | AVAILABLE
2  | Amit Kumar    | Delhi    | BUSY
```

```text
provider_skills
--------------------------------
id | provider_id | skill_name
1  | 1           | COOKING
2  | 1           | BABYSITTING
3  | 2           | BABYSITTING
```

---

# Hibernate and JPA Design

The Provider Service uses:

### Lazy Loading

Provider skills are configured with:

```java
fetch = FetchType.LAZY
```

This prevents child collections from being loaded unnecessarily.

### Cascade

```java
cascade = CascadeType.ALL
```

is used to propagate entity operations from Provider to its associated skills.

### Orphan Removal

```java
orphanRemoval = true
```

allows child skill records to be removed when they are no longer associated with the provider.

### DTO Projection

Provider search uses a DTO projection instead of loading complete entities when only a subset of fields is required.

### Pagination

Spring Data `Page` and `Pageable` are used to avoid loading all provider records at once.

### Database Indexes

Indexes are defined for frequently searched fields such as:

```text
provider_skills.skill_name
provider_skills.provider_id
```

Additional indexes will be evaluated as query patterns evolve.

---

# Error Handling

The Provider Service uses centralized exception handling through:

```java
@RestControllerAdvice
```

Example error response:

```json
{
  "status": 404,
  "message": "Provider not found with id: 999",
  "timestamp": "2026-09-25T..."
}
```

Validation errors are also handled centrally.

---

# Running the Provider Service

## Prerequisites

Install:

* Java 17
* Maven
* MySQL
* IntelliJ IDEA or another Java IDE
* Postman

## Create Database

```sql
CREATE DATABASE serviceco_provider;
```

## Configure Application

Create your local `application.properties` with your database configuration.

Example:

```properties
spring.application.name=serviceco-provider-service
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/serviceco_provider
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

> `application.properties` is kept out of GitHub in this project. Use an example configuration file for sharing non-sensitive configuration.

## Run

```bash
mvn spring-boot:run
```

The Provider Service runs on:

```text
http://localhost:8081
```

---

# Development Roadmap

## Phase 1 — Provider Service

* [x] Provider CRUD
* [x] DTO layer
* [x] Validation
* [x] Mapper
* [x] Exception handling
* [x] Provider skills
* [x] Availability management
* [x] Pagination
* [x] Location filtering
* [x] Skill filtering
* [x] Availability filtering
* [x] Search DTO projection
* [ ] Final Hibernate query optimization
* [ ] Comprehensive JUnit and Mockito tests
* [ ] Database migration with Flyway
* [ ] API documentation

## Phase 2 — Authentication

* [ ] User registration
* [ ] Login
* [ ] Password hashing
* [ ] JWT authentication
* [ ] Role-based authorization

## Phase 3 — Microservices Infrastructure

* [ ] Eureka Discovery Server
* [ ] API Gateway
* [ ] OpenFeign communication
* [ ] Centralized configuration

## Phase 4 — Booking

* [ ] Create booking
* [ ] Accept/reject booking
* [ ] Cancel booking
* [ ] Complete booking
* [ ] Booking history
* [ ] Booking state management

## Phase 5 — Matching

* [ ] Skill-based matching
* [ ] Location-based matching
* [ ] Availability matching
* [ ] Rating-based sorting
* [ ] Provider recommendations

## Phase 6 — Quality and Deployment

* [ ] Unit testing
* [ ] Integration testing
* [ ] Docker
* [ ] Docker Compose
* [ ] Production configuration
* [ ] Monitoring and logging

---

# Project Goals

The project is designed to demonstrate practical backend engineering concepts including:

* REST API development
* Clean layered architecture
* Microservices architecture
* Spring Boot
* JPA/Hibernate
* Database query optimization
* Lazy loading
* Pagination
* DTO projections
* Exception handling
* JWT security
* Inter-service communication
* Unit and integration testing
* Containerized deployment

---

## Repository Structure

The main repository contains all ServiceCo microservices:

```text
serviceco/
│
├── serviceco-provider-service/
├── serviceco-auth-service/
├── serviceco-booking-service/
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

## Author

**Sunny Singh**

Java | Spring Boot | Microservices | SQL | Backend Development
