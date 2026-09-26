# Spring Boot Microservices – Eureka, Config Server, API Gateway & Distributed Tracing

A Spring Boot microservices project demonstrating a practical **microservices architecture** using:

* Spring Boot
* Spring Cloud
* Netflix Eureka Service Discovery
* Spring Cloud Config Server
* Spring Cloud Gateway
* REST APIs
* Service-to-service communication
* Micrometer/Brave distributed tracing
* Zipkin
* Maven

The project is designed as a learning and interview-ready microservices architecture, starting with basic Eureka service discovery and extending toward centralized configuration, API Gateway, and distributed request tracing.

---

# Architecture

```text
                         ┌─────────────────────────────┐
                         │          Client              │
                         │     Postman / Browser        │
                         └──────────────┬──────────────┘
                                        │
                                        │ HTTP Request
                                        ▼
                         ┌─────────────────────────────┐
                         │       GatewayService        │
                         │      Spring Cloud Gateway   │
                         │           :7979             │
                         └──────────────┬──────────────┘
                                        │
                                        │ Service Discovery
                                        ▼
                         ┌─────────────────────────────┐
                         │      DiscoveryServer        │
                         │        Eureka Server        │
                         │           :8761             │
                         └──────────────┬──────────────┘
                                        │
                   ┌────────────────────┼────────────────────┐
                   │                    │                    │
                   ▼                    ▼                    ▼
          ┌────────────────┐   ┌────────────────────┐   ┌────────────────┐
          │  UserService   │   │ ProductCatService  │   │  OrderService  │
          │     :8081      │   │       :8082        │   │     :8083      │
          │                │   │                    │   │                │
          │ User APIs      │   │ Product APIs       │   │ Order APIs     │
          └────────────────┘   └─────────┬──────────┘   └───────┬────────┘
                                         │                      │
                                         │ Product information  │
                                         ◄──────────────────────┘


                 Centralized Configuration
                              │
                              ▼
                  ┌──────────────────────┐
                  │    ConfigServer      │
                  │ Spring Cloud Config  │
                  │       :8888          │
                  └──────────┬───────────┘
                             │
                             ▼
                    Git Configuration
                       Repository


                 Distributed Tracing
                              │
                              ▼
                       ┌────────────┐
                       │   Zipkin   │
                       │   :9411    │
                       └────────────┘
```

---

# Services

| Service           | Responsibility                        | Port |
| ----------------- | ------------------------------------- | ---: |
| ConfigServer      | Centralized application configuration | 8888 |
| DiscoveryServer   | Eureka service registry               | 8761 |
| GatewayService    | API Gateway and routing               | 7979 |
| UserService       | User management                       | 8081 |
| ProductCatService | Product catalog and inventory         | 8082 |
| OrderService      | Order/product validation              | 8083 |
| Zipkin            | Distributed tracing UI/server         | 9411 |

---

# Project Structure

```text
Microservices_EurekaServiceDiscovery/
│
├── config-server/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── discoveryserver/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── gatewayservice/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── orderservice/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── productcatlog/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── userservice/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
└── README.md
```

---

# 1. ConfigServer

## Responsibility

The ConfigServer provides **centralized configuration management** for the microservices.

Instead of maintaining application configuration independently inside every service, configuration can be maintained in a dedicated Git repository.

```text
                 ┌─────────────────────┐
                 │   Git Config Repo    │
                 │ application.yml      │
                 │ user-service.yml    │
                 │ order-service.yml   │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    ConfigServer     │
                 │        :8888        │
                 └──────────┬──────────┘
                            │
            ┌───────────────┼────────────────┐
            │               │                │
            ▼               ▼                ▼
       UserService     OrderService    ProductService
```

## Port

```text
8888
```

## Main Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

## Main Class

```java
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                ConfigServerApplication.class,
                args
        );
    }
}
```

## Configuration

A typical ConfigServer configuration uses a Git repository as its configuration backend.

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server

  cloud:
    config:
      server:
        git:
          uri: https://github.com/<username>/<config-repository>.git
```

### Current Status

The ConfigServer portion is included in this project, but the **Git-backed configuration retrieval is currently under troubleshooting**.

The current issue is related to Git authentication/JGit credentials when ConfigServer attempts to clone or fetch the configuration repository.

Example error:

```text
org.eclipse.jgit.api.errors.TransportException:
https://github.com/...
Authentication is required but no CredentialsProvider has been registered
```

Therefore, the ConfigServer architecture is documented here, but centralized Git configuration should be considered **work in progress** until authentication and configuration loading are verified.

---

# 2. DiscoveryServer

## Responsibility

DiscoveryServer is the **Netflix Eureka Server**.

It maintains the registry of running microservices.

```text
UserService
     │
     │ Register
     ▼
┌────────────────────┐
│  DiscoveryServer   │
│   Eureka :8761     │
└────────────────────┘
     ▲
     │ Register
     │
OrderService
```

## Port

```text
8761
```

## Eureka Dashboard

```text
http://localhost:8761
```

## Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

## Main Class

```java
package com.example.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                DiscoveryServerApplication.class,
                args
        );
    }
}
```

## application.yml

```yaml
spring:
  application:
    name: discovery-server

server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

---

# 3. UserService

## Responsibility

UserService manages user-related operations.

## Port

```text
8081
```

## Service Name

```text
user-service
```

## Eureka Configuration

```yaml
spring:
  application:
    name: user-service

server:
  port: 8081

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Example API

```text
POST /users
```

Example request:

```json
{
  "name": "Sainath",
  "email": "sainath@example.com"
}
```

Example response:

```json
{
  "message": "User registered successfully"
}
```

---

# 4. ProductCatService

## Responsibility

ProductCatService manages product information and inventory.

A product contains:

```text
Product ID
Product Name
Quantity
Price
```

## Port

```text
8082
```

## Service Name

```text
product-cat-service
```

## Eureka Configuration

```yaml
spring:
  application:
    name: product-cat-service

server:
  port: 8082

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Example Product

```json
{
  "id": 101,
  "productName": "Laptop",
  "quantity": 10,
  "price": 75000
}
```

## APIs

Add product:

```text
POST /products
```

Example:

```json
{
  "productName": "Laptop",
  "quantity": 10,
  "price": 75000
}
```

Get product:

```text
GET /products/101
```

Example response:

```json
{
  "id": 101,
  "productName": "Laptop",
  "quantity": 10,
  "price": 75000
}
```

---

# 5. OrderService

## Responsibility

OrderService handles order-related operations and checks product availability through ProductCatService.

## Port

```text
8083
```

## Service Name

```text
order-service
```

## Eureka Configuration

```yaml
spring:
  application:
    name: order-service

server:
  port: 8083

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

# OrderService → ProductCatService

One of the important concepts demonstrated by this project is **service-to-service communication through service discovery**.

Instead of permanently hardcoding:

```text
http://localhost:8082/products/101
```

OrderService can discover ProductCatService through Eureka.

```text
                 ┌─────────────────────┐
                 │   DiscoveryServer   │
                 │     Eureka :8761     │
                 └──────────▲──────────┘
                            │
                       Discovery
                            │
                            │
                 ┌──────────┴──────────┐
                 │    OrderService     │
                 │        :8083        │
                 └──────────┬──────────┘
                            │
                            │ Find
                            │ product-cat-service
                            ▼
                 ┌─────────────────────┐
                 │ ProductCatService   │
                 │        :8082        │
                 └─────────────────────┘
```

Example using `DiscoveryClient`:

```java
@RestController
public class OrderController {

    private final DiscoveryClient discoveryClient;

    public OrderController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/orders/check/{productId}")
    public String checkProduct(
            @PathVariable Long productId) {

        var instances =
                discoveryClient
                        .getInstances("product-cat-service");

        if (instances.isEmpty()) {
            return "ProductCatService is unavailable";
        }

        var instance = instances.get(0);

        return "ProductCatService found at: "
                + instance.getUri();
    }
}
```

The key operation is:

```java
discoveryClient.getInstances("product-cat-service");
```

This allows OrderService to discover the running ProductCatService instance dynamically.

---

# 6. GatewayService

## Responsibility

GatewayService acts as the **single entry point** for client requests.

Instead of clients directly accessing:

```text
UserService        :8081
ProductCatService  :8082
OrderService       :8083
```

the client can communicate through:

```text
GatewayService :7979
```

Architecture:

```text
                    Client
                      │
                      ▼
             ┌─────────────────┐
             │ GatewayService  │
             │      :7979      │
             └────────┬────────┘
                      │
              ┌───────┼────────┐
              │       │        │
              ▼       ▼        ▼
           User     Product   Order
          Service   Service   Service
           :8081     :8082     :8083
```

## Port

```text
7979
```

## Gateway Routing

The intended architecture is to use Eureka service discovery with Spring Cloud Gateway.

For example:

```text
/api/v1/users/**
        ↓
USER-SERVICE

/api/v1/products/**
        ↓
PRODUCT-CAT-SERVICE

/api/v1/orders/**
        ↓
ORDER-SERVICE
```

A typical route can use a load-balanced service URI:

```yaml
spring:
  cloud:
    gateway:
      routes:

        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/v1/users/**

        - id: product-service
          uri: lb://PRODUCT-CAT-SERVICE
          predicates:
            - Path=/api/v1/products/**

        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/v1/orders/**
```

`lb://` allows the Gateway to use service discovery instead of directly specifying a host and port.

---

# Distributed Tracing with Zipkin

The project also explores **distributed tracing**.

The objective is to trace a request across multiple microservices.

For example:

```text
Client
  │
  ▼
GatewayService
  │
  ▼
OrderService
  │
  ▼
ProductCatService
```

A distributed tracing system allows the request to be correlated across these services.

```text
                 Trace
                  │
                  ▼
       ┌────────────────────┐
       │ GatewayService     │
       │      Span 1        │
       └─────────┬──────────┘
                 │
                 ▼
       ┌────────────────────┐
       │ OrderService       │
       │      Span 2        │
       └─────────┬──────────┘
                 │
                 ▼
       ┌────────────────────┐
       │ ProductCatService  │
       │      Span 3        │
       └────────────────────┘
```

## Zipkin

Default Zipkin UI:

```text
http://localhost:9411
```

Tracing endpoint previously configured:

```text
http://localhost:9411/api/v2/spans
```

The project uses Micrometer/Brave-related dependencies for tracing and exporting spans.

---

# Current Zipkin / Gateway Status

The GatewayService and distributed tracing configuration are included as part of the architecture.

However, **Gateway routing and Zipkin trace logging are currently under troubleshooting**.

The intended flow is:

```text
Client
  │
  ▼
GatewayService
  │
  ▼
Eureka
  │
  ▼
OrderService
  │
  ▼
ProductCatService
  │
  ▼
Zipkin
```

The project should therefore currently be considered:

```text
Eureka Service Discovery       → Implemented
UserService                   → Implemented
ProductCatService             → Implemented
OrderService                  → Implemented
ConfigServer                  → Under troubleshooting
GatewayService                → Under troubleshooting
Zipkin Distributed Tracing    → Under troubleshooting
```

This README intentionally documents the target architecture without claiming that the currently troubleshooting components are fully operational.

---

# Complete Request Flow

## Direct Service Discovery Flow

```text
Client
  │
  ▼
OrderService :8083
  │
  │ Discover product-cat-service
  ▼
Eureka :8761
  │
  │ Return service instance
  ▼
ProductCatService :8082
  │
  │ Return product
  ▼
OrderService
  │
  ▼
Client
```

---

# Gateway-Based Request Flow

The target architecture is:

```text
Client
  │
  │ HTTP
  ▼
GatewayService :7979
  │
  │ Service Discovery
  ▼
Eureka :8761
  │
  ├───────────────┐
  │               │
  ▼               ▼
UserService    OrderService
 :8081            :8083
                    │
                    │ Discover
                    ▼
              ProductCatService
                    :8082
```

---

# Configuration Flow

The intended centralized configuration architecture is:

```text
                         Git Repository
                              │
                              │ Configuration
                              ▼
                       ConfigServer :8888
                              │
              ┌───────────────┼────────────────┐
              │               │                │
              ▼               ▼                ▼
        UserService     OrderService    ProductCatService
```

For example:

```text
user-service.yml
order-service.yml
product-cat-service.yml
gatewayservice.yml
```

can be maintained centrally.

---

# Eureka Service Registration

When the services start, they register themselves with Eureka.

```text
                  Eureka Server
                     :8761
                       │
          ┌────────────┼────────────┐
          │            │            │
          ▼            ▼            ▼
     USER-SERVICE  PRODUCT-CAT   ORDER-SERVICE
        :8081        :8082          :8083
```

The Eureka dashboard can be opened at:

```text
http://localhost:8761
```

Expected registered applications:

```text
ORDER-SERVICE
PRODUCT-CAT-SERVICE
USER-SERVICE
```

GatewayService may also appear as a registered application if Eureka client registration is enabled.

---

# Testing

## Register User

```text
POST http://localhost:8081/users
```

Example:

```json
{
  "name": "Sainath",
  "email": "sainath@example.com"
}
```

---

## Add Product

```text
POST http://localhost:8082/products
```

Example:

```json
{
  "productName": "Laptop",
  "quantity": 10,
  "price": 75000
}
```

---

## Get Product

```text
GET http://localhost:8082/products/101
```

---

## Check Product Through OrderService

```text
GET http://localhost:8083/orders/check/101
```

---

# Gateway APIs

Once Gateway routing is fully configured, the intended APIs are:

```text
http://localhost:7979/api/v1/users/...
```

```text
http://localhost:7979/api/v1/products/...
```

```text
http://localhost:7979/api/v1/orders/...
```

The Gateway then routes requests to the appropriate Eureka-registered service.

---

# Startup Order

For local development, start the infrastructure and services in the following order.

## 1. DiscoveryServer

```bash
cd discoveryserver
mvn spring-boot:run
```

Verify:

```text
http://localhost:8761
```

---

## 2. ConfigServer

```bash
cd config-server
mvn spring-boot:run
```

Port:

```text
8888
```

> Currently under troubleshooting because Git-backed configuration retrieval requires authentication/configuration to be resolved.

---

## 3. UserService

```bash
cd userservice
mvn spring-boot:run
```

Port:

```text
8081
```

---

## 4. ProductCatService

```bash
cd productcatlog
mvn spring-boot:run
```

Port:

```text
8082
```

---

## 5. OrderService

```bash
cd orderservice
mvn spring-boot:run
```

Port:

```text
8083
```

---

## 6. GatewayService

```bash
cd gatewayservice
mvn spring-boot:run
```

Port:

```text
7979
```

> Gateway routing and tracing are currently under troubleshooting.

---

## 7. Zipkin

If running Zipkin locally, the default UI is:

```text
http://localhost:9411
```

---

# Dependencies

## DiscoveryServer

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

## Eureka Client Services

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## ConfigServer

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

## Gateway

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

The exact dependency versions should be controlled through the Spring Boot and Spring Cloud versions defined in each project's `pom.xml`.

---

# Technologies

```text
Java
Spring Boot
Spring Cloud
Spring Cloud Netflix Eureka
Spring Cloud Config
Spring Cloud Gateway
REST APIs
Maven
Micrometer
Brave
Zipkin
Git
GitHub
```

---

# Microservices Concepts Demonstrated

This project demonstrates the following microservices concepts:

### 1. Service Registration

```text
Service
   ↓
Eureka
```

### 2. Service Discovery

```text
OrderService
   ↓
Eureka
   ↓
ProductCatService
```

### 3. Centralized Configuration

```text
Git
 ↓
ConfigServer
 ↓
Microservices
```

### 4. API Gateway

```text
Client
 ↓
Gateway
 ↓
Microservices
```

### 5. Distributed Tracing

```text
Gateway
 ↓
OrderService
 ↓
ProductCatService
 ↓
Zipkin
```

### 6. Load-Balanced Service Routing

```text
Gateway
   ↓
lb://USER-SERVICE
   ↓
Eureka
   ↓
Available UserService instance
```


# Learning Flow

The project is structured to learn microservices incrementally:

```text
1. Spring Boot REST
        ↓
2. Multiple Microservices
        ↓
3. Eureka Service Discovery
        ↓
4. Service-to-Service Communication
        ↓
5. Spring Cloud Config
        ↓
6. API Gateway
        ↓
7. Distributed Tracing
        ↓
8. Resilience4j
        ↓
9. Kafka
        ↓
10. Docker
        ↓
11. Kubernetes
        ↓
12. Cloud Deployment
```

---

# Project Goal

The primary goal of this repository is to demonstrate how multiple Spring Boot applications can work together as a **microservices-based system**.

The core architecture is:

```text
                   Client
                      │
                      ▼
                 API Gateway
                      │
                      ▼
               Service Discovery
                   Eureka
                      │
       ┌──────────────┼──────────────┐
       ▼              ▼              ▼
   UserService   ProductService  OrderService
       │              ▲              │
       │              │              │
       └──────────────┴──────────────┘
                      │
                Service-to-Service
                  Communication

                      +
               ConfigServer
                      │
                      ▼
                Git Repository

                      +

                   Zipkin
                      │
                      ▼
             Distributed Tracing
```

This repository represents an incremental microservices implementation. **Eureka-based service discovery is the core working foundation, while centralized configuration, Gateway routing, and distributed tracing are being integrated and troubleshot.**
