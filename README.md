# 🏥 Patient Management System

### A Production-Style Microservices Backend for Patient Management

<p align="center">
  <b>Scalable • Secure • Event-Driven • Cloud-Ready</b>
</p>

<p align="center">

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge\&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%2F4.x-brightgreen?style=for-the-badge\&logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Gateway-blue?style=for-the-badge\&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-336791?style=for-the-badge\&logo=postgresql)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-black?style=for-the-badge\&logo=apachekafka)
![gRPC](https://img.shields.io/badge/gRPC-Inter--Service%20Communication-244c5a?style=for-the-badge\&logo=grpc)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge\&logo=docker)
![AWS](https://img.shields.io/badge/AWS-Cloud%20Infrastructure-FF9900?style=for-the-badge\&logo=amazonaws)
![JUnit](https://img.shields.io/badge/JUnit-Testing-25A162?style=for-the-badge\&logo=junit5)

</p>

---

## 📌 Overview

**Patient Management System** is a backend application designed using a **microservices architecture** to demonstrate how a real-world healthcare-oriented platform can be decomposed into independently deployable services.

Instead of implementing everything inside one monolithic Spring Boot application, the system separates responsibilities across dedicated services for:

* 🔐 Authentication & authorization
* 👨‍⚕️ Patient management
* 💳 Billing
* 📊 Analytics
* 🌐 API routing and security
* ☁️ Cloud infrastructure

The application combines **synchronous gRPC communication** with **asynchronous Kafka event streaming**, allowing services to communicate according to the nature of the operation.

The infrastructure layer is defined using **AWS CDK** and can be synthesized/deployed against a **LocalStack-based AWS environment** for local cloud development.

---

# 🎯 Key Objectives

The project was built to demonstrate practical backend engineering concepts including:

* Microservices architecture
* RESTful API development
* API Gateway pattern
* JWT-based authentication
* Service-to-service communication
* gRPC
* Protocol Buffers
* Event-driven architecture
* Apache Kafka
* Database-per-service architecture
* PostgreSQL
* Spring Data JPA
* DTO-based API design
* Validation and exception handling
* Docker containerization
* AWS infrastructure as code
* AWS ECS/Fargate architecture
* LocalStack-based cloud simulation
* Integration testing

---

# ✨ Features

## 👨‍⚕️ Patient Management

The Patient Service provides the core patient-management functionality.

### Supported operations

| Operation | Endpoint         | Description                |
| --------- | ---------------- | -------------------------- |
| `GET`     | `/patients`      | Retrieve all patients      |
| `POST`    | `/patients`      | Create a new patient       |
| `PUT`     | `/patients/{id}` | Update patient information |
| `DELETE`  | `/patients/{id}` | Delete a patient           |

### Patient information

Each patient contains:

* UUID
* Name
* Email
* Address
* Date of Birth
* Registration Date

The email field is unique, preventing duplicate patient registrations.

---

# 🔐 Authentication & Security

The system contains a dedicated **Authentication Service**.

Authentication is implemented using:

* Spring Security
* BCrypt password hashing
* JWT
* Role information inside JWT claims
* Token validation
* API Gateway authentication filtering

### Authentication flow

```text
Client
   │
   │ POST /auth/login
   ▼
API Gateway
   │
   ▼
Auth Service
   │
   ├── Validate credentials
   ├── Verify BCrypt password
   └── Generate JWT
          │
          ▼
       Client
          │
          │ Authorization: Bearer <JWT>
          ▼
     API Gateway
          │
          ├── Validate JWT
          │
          ▼
    Patient Service
```

Protected patient APIs require a valid JWT.

---

# 🌐 API Gateway

The project uses **Spring Cloud Gateway** as the single entry point for API consumers.

### Gateway

```text
Client
  │
  ▼
┌────────────────────────────┐
│       API Gateway          │
│         :4004              │
└──────────────┬─────────────┘
               │
       ┌───────┴────────┐
       │                │
       ▼                ▼
 Auth Service      Patient Service
    :4005              :4000
```

The gateway is responsible for:

* Request routing
* JWT validation
* Authentication enforcement
* Service abstraction
* API documentation routing

### Routes

| Gateway Route        | Destination                          |
| -------------------- | ------------------------------------ |
| `/auth/**`           | Authentication Service               |
| `/api/patients/**`   | Patient Service                      |
| `/api-docs/patients` | Patient OpenAPI documentation        |
| `/api-docs/auth`     | Authentication OpenAPI documentation |

---

# ⚡ Event-Driven Architecture

The system uses **Apache Kafka** for asynchronous communication.

When a patient is created:

```text
Client
  │
  ▼
API Gateway
  │
  ▼
Patient Service
  │
  ├───────────────► PostgreSQL
  │
  ├───────────────► Billing Service
  │                    │
  │                    └── gRPC
  │
  └───────────────► Kafka
                         │
                         ▼
                  Analytics Service
```

The Patient Service publishes a `PATIENT_CREATED` event to the Kafka topic:

```text
patient
```

The event is serialized using **Protocol Buffers**.

### Event structure

```protobuf
message PatientEvent {
  string patientId = 1;
  string name = 2;
  string email = 3;
  string event_type = 4;
}
```

The Analytics Service consumes these events independently.

This demonstrates an important microservices principle:

> The Patient Service does not need to synchronously call the Analytics Service.

Instead, Analytics reacts to events.

---

# 🔄 gRPC Communication

The Patient Service communicates synchronously with the Billing Service using **gRPC**.

### Patient creation flow

```text
Patient Service
      │
      │ CreateBillingAccount()
      │
      │ gRPC
      ▼
Billing Service
      │
      ▼
BillingResponse
      │
      ▼
Patient Service
```

The gRPC contract is defined using Protocol Buffers:

```protobuf
service BillingService {
  rpc CreateBillingAccount
      (BillingRequest)
      returns
      (BillingResponse);
}
```

### Billing request

```protobuf
message BillingRequest {
  string patientId = 1;
  string name = 2;
  string email = 3;
}
```

### Billing response

```protobuf
message BillingResponse {
  string accountId = 1;
  string status = 2;
}
```

---

# 📊 Analytics Service

The Analytics Service acts as a Kafka consumer.

It listens to:

```text
Topic: patient
Consumer Group: analytics-service
```

When a patient-created event is received, the service:

1. Reads the Kafka message.
2. Deserializes the Protocol Buffer payload.
3. Converts it into a `PatientEvent`.
4. Processes/logs the event.

This service is intentionally decoupled from the Patient Service, making it possible to extend analytics functionality without modifying the core patient-management workflow.

---

# 🏗️ System Architecture

```text
                           ┌──────────────────┐
                           │      Client      │
                           └────────┬─────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    API Gateway      │
                         │       :4004          │
                         │                     │
                         │  JWT Validation     │
                         │  Request Routing    │
                         └──────┬───────┬──────┘
                                │       │
                   /auth/**     │       │ /api/patients/**
                                │       │
                                ▼       ▼
                     ┌──────────────┐  ┌─────────────────┐
                     │ Auth Service │  │ Patient Service │
                     │    :4005     │  │      :4000      │
                     └──────┬───────┘  └───────┬─────────┘
                            │                   │
                            ▼                   │
                     ┌──────────────┐           │
                     │  PostgreSQL  │           │
                     │ Auth DB      │           │
                     └──────────────┘           │
                                                │
                            ┌───────────────────┼───────────────────┐
                            │                   │                   │
                            ▼                   ▼                   ▼
                     ┌──────────────┐    ┌──────────────┐   ┌───────────────┐
                     │   Patient    │    │    Billing   │   │     Kafka     │
                     │  PostgreSQL  │    │   Service    │   │    Cluster    │
                     └──────────────┘    │ :4001 / :9001│   └───────┬───────┘
                                         └───────▲──────┘           │
                                                 │                  │
                                                gRPC                │
                                                                    ▼
                                                          ┌──────────────────┐
                                                          │ Analytics Service │
                                                          │       :4002       │
                                                          └──────────────────┘
```

---

# 🧩 Microservices

| Service               |            Port | Responsibility                           | Communication       |
| --------------------- | --------------: | ---------------------------------------- | ------------------- |
| **API Gateway**       |          `4004` | Entry point, routing, JWT validation     | HTTP                |
| **Auth Service**      |          `4005` | Login, JWT generation & validation       | REST                |
| **Patient Service**   |          `4000` | Patient CRUD & orchestration             | REST + gRPC + Kafka |
| **Billing Service**   | `4001` / `9001` | Billing account creation                 | REST / gRPC         |
| **Analytics Service** |          `4002` | Event consumption & analytics processing | Kafka               |

---

# 🛠️ Technology Stack

## Backend

* **Java**
* **Spring Boot**
* **Spring Web**
* **Spring Data JPA**
* **Spring Validation**
* **Spring Security**
* **Spring Cloud Gateway**

## Security

* JWT
* JJWT
* BCrypt
* Spring Security

## Communication

* REST APIs
* gRPC
* Protocol Buffers

## Messaging

* Apache Kafka
* Spring Kafka

## Database

* PostgreSQL
* H2 for local/testing scenarios
* Hibernate / JPA

## DevOps & Cloud

* Docker
* AWS CDK
* AWS ECS
* AWS Fargate
* AWS RDS
* AWS MSK
* AWS CloudFormation
* LocalStack

## Testing

* JUnit 5
* Spring Boot Test
* Spring Security Test
* REST Assured

## API Documentation

* OpenAPI
* SpringDoc

---

# 📁 Project Structure

```text
patient-management/
│
├── api-gateway/
│   ├── src/main/java/
│   │   └── com/pm/apigateway/
│   │       ├── config/
│   │       ├── exception/
│   │       └── filter/
│   │
│   └── src/main/resources/
│       └── application.yml
│
├── auth-service/
│   ├── src/main/java/
│   │   └── com/pm/authservice/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       └── util/
│   │
│   └── src/main/resources/
│       ├── application.properties
│       └── data.sql
│
├── patientService/
│   ├── src/main/java/
│   │   └── com/pm/patientservice/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── exception/
│   │       ├── grpc/
│   │       ├── kafka/
│   │       ├── mapper/
│   │       ├── model/
│   │       ├── repository/
│   │       └── service/
│   │
│   ├── src/main/proto/
│   │   ├── billing_service.proto
│   │   └── patient_event.proto
│   │
│   └── src/main/resources/
│       ├── application.properties
│       └── data.sql
│
├── billing-service/
│   ├── src/main/java/
│   │   └── com/pm/billingservice/
│   │       └── grpc/
│   │
│   └── src/proto/
│       └── billing_service.proto
│
├── analytics-service/
│   ├── src/main/java/
│   │   └── com/pm/analyticsservice/
│   │       └── kafka/
│   │
│   └── src/main/proto/
│       └── patient_event.proto
│
├── infrastructure/
│   ├── src/main/java/
│   │   └── com/pm/stack/
│   │       └── LocalStack.java
│   │
│   ├── cdk.out/
│   └── localstack-deploy.sh
│
├── integration-tests/
│   └── src/test/java/
│       ├── AuthIntegrationTest.java
│       └── PatientIntegrationTest.java
│
├── api-requests/
│   ├── auth-service/
│   └── patient-service/
│
├── grpc-requests/
│   └── billing-service/
│
└── events.json
```

---

# 🔌 API Documentation

## Authentication

### Login

```http
POST /auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "testuser@test.com",
  "password": "password123"
}
```

Response:

```json
{
  "token": "<JWT_TOKEN>"
}
```

---

### Validate Token

```http
GET /auth/validate
Authorization: Bearer <JWT_TOKEN>
```

Returns:

```text
200 OK
```

for a valid token.

---

# 👨‍⚕️ Patient APIs

## Get Patients

```http
GET /api/patients
Authorization: Bearer <JWT_TOKEN>
```

---

## Create Patient

```http
POST /api/patients
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

Example:

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": "New Delhi",
  "dateOfBirth": "2000-01-15",
  "registeredDate": "2026-08-30"
}
```

During creation, the Patient Service:

```text
1. Validates request
       ↓
2. Checks duplicate email
       ↓
3. Saves patient
       ↓
4. Creates billing account through gRPC
       ↓
5. Publishes PATIENT_CREATED event
       ↓
6. Returns patient response
```

---

## Update Patient

```http
PUT /api/patients/{id}
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

---

## Delete Patient

```http
DELETE /api/patients/{id}
Authorization: Bearer <JWT_TOKEN>
```

Successful deletion returns:

```text
204 No Content
```

---

# 🗄️ Database Architecture

The project follows a **database-per-service approach**.

```text
Auth Service
     │
     ▼
Auth PostgreSQL DB


Patient Service
     │
     ▼
Patient PostgreSQL DB
```

This reduces coupling between services and allows each service to own its persistence model.

The infrastructure code provisions PostgreSQL databases through AWS RDS when database provisioning is enabled.

---

# 🔒 Validation & Exception Handling

The Patient Service implements validation and centralized exception handling.

Examples include:

* Invalid email validation
* Missing required fields
* Duplicate patient email
* Patient not found
* Invalid patient UUID
* Request validation errors

Custom exceptions include:

```text
EmailAlreadyExistsException
PatientNotFoundException
GlobalExceptionHandler
```

This keeps error handling separate from business logic and produces cleaner controller implementations.

---

# 📦 DTO Architecture

The project separates API contracts from persistence entities using DTOs.

```text
HTTP Request
     │
     ▼
PatientRequestDTO
     │
     ▼
Validation
     │
     ▼
Patient Entity
     │
     ▼
Repository
     │
     ▼
PatientResponseDTO
     │
     ▼
HTTP Response
```

This prevents exposing the JPA entity directly as the external API contract.

---

# ☁️ Cloud Infrastructure

The infrastructure module uses **AWS CDK with Java**.

The stack provisions an architecture containing:

### Networking

* VPC
* Multiple Availability Zones

### Compute

* ECS Cluster
* ECS Fargate Services
* Application Load Balancer

### Database

* Amazon RDS PostgreSQL

### Messaging

* Amazon MSK / Kafka

### Logging

* CloudWatch Log Groups

### Containerized Services

The infrastructure defines Fargate services for:

```text
Auth Service
Billing Service
Analytics Service
Patient Service
API Gateway
```

---

# 🐳 Docker

Each backend service contains its own Dockerfile.

This allows services to be independently containerized and deployed.

Example conceptual workflow:

```text
Source Code
    │
    ▼
Maven Build
    │
    ▼
Spring Boot JAR
    │
    ▼
Docker Image
    │
    ▼
Container
    │
    ▼
ECS / Fargate
```

---

# 🧪 Testing

The project contains both service-level tests and integration tests.

### Unit / Service Tests

Services contain Spring Boot test classes for validating application startup and service behavior.

### Integration Tests

The `integration-tests` module uses:

* JUnit 5
* REST Assured

Authentication scenarios include:

```text
Valid credentials
      ↓
200 OK
      ↓
JWT returned
```

Invalid credentials:

```text
Invalid credentials
      ↓
401 Unauthorized
```

Patient integration testing verifies authenticated access through the API Gateway.

---

# 🚀 Running the Project Locally

## 1. Prerequisites

Install:

* Java
* Maven
* Docker
* Docker Compose / Docker-compatible container runtime
* AWS CLI
* LocalStack

Recommended Java versions should match the individual modules:

```text
Patient Service       → Java 17
Auth Service          → Java 21
API Gateway           → Java 21
Billing Service       → Java 21
Analytics Service     → Java 21
Infrastructure        → Java 21
Integration Tests     → Java 21
```

---

# 2. Build the Services

Build each service using Maven.

### Patient Service

```bash
cd patientService
./mvnw clean package
```

Windows:

```powershell
mvnw.cmd clean package
```

### Auth Service

```bash
cd auth-service
./mvnw clean package
```

### Billing Service

```bash
cd billing-service
./mvnw clean package
```

### Analytics Service

```bash
cd analytics-service
./mvnw clean package
```

### API Gateway

```bash
cd api-gateway
./mvnw clean package
```

---

# 3. Run Supporting Infrastructure

The application expects infrastructure such as:

```text
PostgreSQL
Kafka
```

For cloud-style local development, the repository includes an AWS CDK/LocalStack infrastructure module.

The deployment helper is:

```bash
infrastructure/localstack-deploy.sh
```

The script deploys the generated CloudFormation template to LocalStack and exposes the API Gateway through:

```text
http://localhost:4004
```

When the LocalStack environment provides an ELB DNS name, the script prints that endpoint instead.

---

# 4. Start Services

The services can be run independently from their respective modules/IDE configurations.

Default ports:

```text
API Gateway       → 4004
Auth Service      → 4005
Patient Service   → 4000
Billing HTTP      → 4001
Billing gRPC      → 9001
Analytics         → 4002
```

---

# 🔑 Environment Configuration

### API Gateway

```yaml
auth:
  service:
    url: http://localhost:4005

patient:
  service:
    url: http://localhost:4000
```

### Patient Service

Kafka:

```properties
spring.kafka.bootstrap-servers=localhost:9092
```

Billing gRPC:

```properties
billing.service.address=localhost
billing.service.grpc.port=9001
```

### Analytics Service

```properties
spring.kafka.bootstrap-servers=localhost:9092
```

The Kafka listener can be controlled through:

```properties
KAFKA_LISTENER_ENABLED
```

---

# 🧪 Example Request Flow

### 1️⃣ Login

```text
POST /auth/login
```

```json
{
  "email": "testuser@test.com",
  "password": "password123"
}
```

↓

### 2️⃣ Receive JWT

```text
JWT Token
```

↓

### 3️⃣ Create Patient

```text
POST /api/patients
Authorization: Bearer <JWT>
```

↓

### 4️⃣ Gateway validates JWT

```text
API Gateway
      ↓
Auth Service
      ↓
Token Valid
```

↓

### 5️⃣ Patient Service saves patient

```text
Patient Service
      ↓
PostgreSQL
```

↓

### 6️⃣ Billing account created

```text
Patient Service
      │
      │ gRPC
      ▼
Billing Service
```

↓

### 7️⃣ Event published

```text
Patient Service
      │
      ▼
Kafka Topic: patient
```

↓

### 8️⃣ Analytics consumes event

```text
Kafka
  │
  ▼
Analytics Service
```

---

# 🧠 Engineering Concepts Demonstrated

This project goes beyond simple CRUD and demonstrates several production-oriented backend patterns.

### 1. Microservices Architecture

Business responsibilities are separated into independently deployable services.

### 2. API Gateway Pattern

Clients interact with a single gateway rather than directly accessing internal services.

### 3. Authentication Gateway Filter

JWT validation is performed before forwarding protected requests.

### 4. Synchronous Service Communication

gRPC is used when the Patient Service requires an immediate Billing Service response.

### 5. Asynchronous Event Communication

Kafka is used when downstream processing does not need to block the primary request.

### 6. Database-per-Service

Services maintain ownership over their persistence layer.

### 7. Contract-Based Communication

Protocol Buffers provide explicit contracts for gRPC and event messages.

### 8. Infrastructure as Code

AWS infrastructure is defined programmatically using AWS CDK.

### 9. Containerization

Services are packaged as independent Docker containers.

### 10. Integration Testing

REST Assured tests verify complete request flows through the API Gateway.

---

# 📈 Scalability Model

The architecture allows individual services to scale independently.

For example:

```text
                    API Gateway
                        │
             ┌──────────┼──────────┐
             │          │          │
             ▼          ▼          ▼
          Patient    Patient    Patient
          Service    Service    Service
             │          │          │
             └──────────┼──────────┘
                        │
                      Kafka
```

This makes it possible to scale a heavily used service without scaling the entire application.

---

# 🔮 Future Improvements

Potential production enhancements include:

* [ ] Add refresh-token support
* [ ] Introduce role-based authorization at the gateway
* [ ] Add distributed tracing with OpenTelemetry
* [ ] Add centralized configuration
* [ ] Add service discovery
* [ ] Add circuit breakers with Resilience4j
* [ ] Add retry and timeout policies for gRPC
* [ ] Add Kafka retry topics / dead-letter topics
* [ ] Add Kafka schema versioning
* [ ] Add Redis caching
* [ ] Add database migrations using Flyway
* [ ] Add centralized logging
* [ ] Add Prometheus + Grafana monitoring
* [ ] Add CI/CD pipeline
* [ ] Add Kubernetes deployment manifests
* [ ] Add automated Docker image publishing
* [ ] Add comprehensive API contract tests

---

# 💡 Why This Project Is Different

A typical beginner Patient Management System might look like:

```text
Frontend
   │
   ▼
Spring Boot
   │
   ▼
Database
```

This project instead demonstrates:

```text
                         ┌───────────────┐
                         │ API Gateway   │
                         └───────┬───────┘
                                 │
                 ┌───────────────┼──────────────┐
                 │               │              │
                 ▼               ▼              ▼
             Auth Service   Patient Service   ...
                                 │
                     ┌───────────┴───────────┐
                     │                       │
                     ▼                       ▼
                   gRPC                    Kafka
                     │                       │
                     ▼                       ▼
              Billing Service        Analytics Service
```

This architecture provides practical exposure to **distributed systems, service boundaries, synchronous/asynchronous communication, authentication, messaging, containerization, and cloud infrastructure**.

---

# 📚 API Request Examples

The repository contains ready-to-use HTTP request files under:

```text
api-requests/
```

### Authentication

```text
api-requests/auth-service/
├── login.http
└── validate.http
```

### Patient Service

```text
api-requests/patient-service/
├── create-patient.http
├── get-patients.http
├── update-patient.http
└── delete-patient.http
```

### Billing gRPC

```text
grpc-requests/billing-service/
└── create-billing-account.http
```

These can be used directly from IDE HTTP clients that support `.http` request files.

---

# 📖 API Documentation

The project integrates **SpringDoc OpenAPI**.

Gateway documentation routes are configured for:

```text
/api-docs/patients
/api-docs/auth
```

These routes forward to the respective services' OpenAPI specifications.

---

# ⚠️ Development Notes

This repository is primarily a learning/portfolio implementation of a production-style microservices architecture.

The infrastructure module is designed around LocalStack and AWS CDK. Some AWS resources—particularly services such as RDS/MSK—may depend on the capabilities/licensing of the LocalStack environment being used.

For production deployment, secrets such as JWT keys and database credentials should be supplied through a dedicated secret-management solution rather than committed configuration.

---

# 🗺️ Architecture at a Glance

```text
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT                              │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      API GATEWAY :4004                       │
│                                                             │
│             Routing + JWT Validation                        │
└───────────────┬─────────────────────────────┬───────────────┘
                │                             │
                ▼                             ▼
       ┌─────────────────┐          ┌─────────────────────┐
       │   AUTH SERVICE  │          │   PATIENT SERVICE   │
       │      :4005      │          │        :4000        │
       └────────┬────────┘          └───────┬───────┬─────┘
                │                           │       │
                ▼                           │       │
       ┌─────────────────┐                  │       │
       │   AUTH DB       │                  │       │
       │   PostgreSQL    │                  │       │
       └─────────────────┘                  │       │
                                            │       │
                           ┌────────────────┘       └─────────────┐
                           │                                      │
                           ▼                                      ▼
                   ┌───────────────┐                       ┌────────────┐
                   │ BILLING       │                       │   KAFKA    │
                   │ SERVICE       │                       │   TOPIC    │
                   │ :4001 / :9001 │                       │  patient   │
                   └───────────────┘                       └─────┬──────┘
                           ▲                                      │
                           │ gRPC                                 │
                           │                                      ▼
                           │                             ┌────────────────┐
                           │                             │   ANALYTICS    │
                           └─────────────────────────────│    SERVICE     │
                                                         │      :4002     │
                                                         └────────────────┘


                  ☁️ AWS / LOCALSTACK INFRASTRUCTURE

       VPC → ECS/Fargate → RDS PostgreSQL → MSK → ALB
                              │
                              └── CloudWatch Logs
```

---

# 👨‍💻 Author

**Kush Pathak**

GitHub: [kushpathakgit](https://github.com/kushpathakgit?utm_source=chatgpt.com)

Project Repository: [Patient Management System](https://github.com/kushpathakgit/Patient-Management-System?utm_source=chatgpt.com)

---

# ⭐ If You Found This Project Useful

If this project helped you understand microservices, Spring Boot, Kafka, gRPC, or cloud architecture, consider giving the repository a ⭐.

---

<p align="center">

### 🏥 Patient Management System

**Built to explore modern backend engineering and distributed systems.**

`Spring Boot` • `Microservices` • `Kafka` • `gRPC` • `JWT` • `Docker` • `AWS CDK`

</p>
