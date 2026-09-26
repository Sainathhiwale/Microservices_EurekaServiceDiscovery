# Spring Boot Microservices – Eureka Service Discovery + API Gateway

A simple Spring Boot microservices project demonstrating:

* **Netflix Eureka Service Discovery**
* **Spring Cloud API Gateway**
* **Service Registration**
* **Service-to-Service Communication**
* **Load-balanced routing using Eureka**

This project contains five services:

* **DiscoveryServer** – Eureka Service Registry
* **GatewayService** – API Gateway
* **UserService** – Register users
* **ProductCatService** – Manage products, quantity and price
* **OrderService** – Check product quantity through `ProductCatService`

---

# Architecture

```text
                              Client
                                |
                                | HTTP Request
                                ▼
                    ┌─────────────────────────┐
                    │     GatewayService      │
                    │       API Gateway       │
                    │         :8080           │
                    └────────────┬────────────┘
                                 |
                                 | Service Discovery
                                 ▼
                    ┌─────────────────────────┐
                    │     DiscoveryServer     │
                    │      Eureka Server       │
                    │         :8761            │
                    └────────────┬────────────┘
                                 |
                    Service Registration
                                 |
              ┌──────────────────┼──────────────────┐
              │                  │                  │
              ▼                  ▼                  ▼
       ┌──────────────┐  ┌────────────────┐  ┌──────────────┐
       │ UserService  │  │ProductCatService│ │ OrderService │
       │    :8081     │  │     :8082       │ │    :8083     │
       │              │  │                 │ │              │
       │ Register     │  │ Product Name    │ │ Check Product│
       │ User         │  │ Quantity        │ │ Quantity     │
       │              │  │ Price           │ │              │
       └──────────────┘  └─────────────────┘ └──────┬───────┘
                                                    │
                                                    │ Service Discovery
                                                    ▼
                                           ProductCatService
```

---

# Request Flow

A client does not directly call individual microservices.

Instead:

```text
Client
  |
  | GET /products/101
  ▼
GatewayService :8080
  |
  | Find PRODUCT-CAT-SERVICE
  ▼
Eureka :8761
  |
  | Return available instance
  ▼
ProductCatService :8082
  |
  | Return Product
  ▼
Gateway
  |
  ▼
Client
```

The Gateway acts as the **single entry point** for external clients.

---

# Services

## 1. DiscoveryServer

**Responsibility:** Eureka Service Registry.

The DiscoveryServer maintains the registry of all available microservices.

### Port

```text
8761
```

### Eureka Dashboard

```text
http://localhost:8761
```

### Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

### Main Class

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

### application.yml

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

# 2. GatewayService

**Responsibility:** API Gateway and single entry point for client requests.

The Gateway receives requests from clients and forwards them to the appropriate microservice.

### Port

```text
8080
```

### Dependency

Because this project uses the WebFlux Gateway:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
```

Eureka Client:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### application.yml

```yaml
spring:
  application:
    name: gateway-service

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

# Gateway Routing

Gateway uses Eureka service names instead of hardcoded host/port values.

Example:

```text
lb://USER-SERVICE
lb://PRODUCT-CAT-SERVICE
lb://ORDER-SERVICE
```

`lb://` means that the request should be load-balanced to an available service instance.

### GatewayConfig.java

```java
package com.example.gatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(
            RouteLocatorBuilder builder) {

        return builder.routes()

                .route("user-service", r ->
                        r.path("/users/**")
                                .uri("lb://USER-SERVICE"))

                .route("product-service", r ->
                        r.path("/products/**")
                                .uri("lb://PRODUCT-CAT-SERVICE"))

                .route("order-service", r ->
                        r.path("/orders/**")
                                .uri("lb://ORDER-SERVICE"))

                .build();
    }
}
```

---

# Gateway Routing Table

| Client URL     | Gateway | Eureka Service        |
| -------------- | ------- | --------------------- |
| `/users/**`    | `:8080` | `USER-SERVICE`        |
| `/products/**` | `:8080` | `PRODUCT-CAT-SERVICE` |
| `/orders/**`   | `:8080` | `ORDER-SERVICE`       |

For example:

```text
GET http://localhost:8080/products/101
```

Gateway receives:

```text
/products/101
```

Then routes to:

```text
lb://PRODUCT-CAT-SERVICE/products/101
```

Eureka finds an available `PRODUCT-CAT-SERVICE` instance.

---

# Gateway Request Flow

```text
                    Client
                      |
                      | GET /products/101
                      ▼
              ┌─────────────────┐
              │ GatewayService  │
              │     :8080       │
              └────────┬────────┘
                       |
                       | lb://PRODUCT-CAT-SERVICE
                       ▼
              ┌─────────────────┐
              │ Eureka Server   │
              │     :8761       │
              └────────┬────────┘
                       |
                       | Find instance
                       ▼
              ┌─────────────────┐
              │ProductCatService│
              │     :8082       │
              └────────┬────────┘
                       |
                       ▼
                  Product Data
                       |
                       ▼
                    Client
```

---

# Why Use Gateway?

Without Gateway:

```text
Client
  |
  ├──→ UserService :8081
  |
  ├──→ ProductCatService :8082
  |
  └──→ OrderService :8083
```

The client needs to know every microservice.

With Gateway:

```text
Client
  |
  ▼
Gateway :8080
  |
  ├──→ UserService
  |
  ├──→ ProductCatService
  |
  └──→ OrderService
```

The client only needs to know:

```text
http://localhost:8080
```

This also provides a central location for concerns such as:

* Authentication
* Authorization
* Logging
* Rate limiting
* Request filtering
* Routing
* Monitoring
* CORS configuration

---

# 3. UserService

**Responsibility:** Register users.

### Port

```text
8081
```

### Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### application.yml

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

### Example API

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

Through Gateway:

```text
POST http://localhost:8080/users
```

---

# 4. ProductCatService

**Responsibility:** Add and manage products.

Product information includes:

```text
Product ID
Product Name
Quantity
Price
```

### Port

```text
8082
```

### Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### application.yml

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

### Example Product

```json
{
  "id": 101,
  "productName": "Laptop",
  "quantity": 10,
  "price": 75000
}
```

### APIs

Add product:

```text
POST /products
```

Check product:

```text
GET /products/101
```

Through Gateway:

```text
POST http://localhost:8080/products

GET http://localhost:8080/products/101
```

---

# 5. OrderService

**Responsibility:** Check product quantity before creating an order.

### Port

```text
8083
```

### Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### application.yml

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

Example API:

```text
GET /orders/check/{productId}
```

Through Gateway:

```text
GET http://localhost:8080/orders/check/101
```

---

# OrderService → ProductCatService

OrderService needs to check whether a product is available.

Instead of hardcoding:

```text
http://localhost:8082/products/101
```

OrderService discovers `ProductCatService` through Eureka.

```text
                    ┌───────────────────┐
                    │  DiscoveryServer  │
                    │   Eureka :8761    │
                    └─────────▲─────────┘
                              │
                         Discovery
                              │
                              │
┌────────────────┐            │
│  OrderService  │────────────┘
│     :8083      │
└───────┬────────┘
        │
        │ Find product-cat-service
        ▼
┌──────────────────────┐
│  ProductCatService   │
│       :8082          │
└──────────┬───────────┘
           │
           ▼
     Product Quantity
```

---

# Complete Architecture

The complete application now has two different uses of Eureka.

### 1. Gateway → Microservices

```text
Client
  |
  ▼
Gateway
  |
  ▼
Eureka
  |
  ▼
Target Microservice
```

### 2. Microservice → Microservice

```text
OrderService
  |
  ▼
Eureka
  |
  ▼
ProductCatService
```

Therefore:

```text
                         Client
                           |
                           ▼
                  ┌─────────────────┐
                  │ GatewayService  │
                  │     :8080       │
                  └────────┬────────┘
                           |
                           ▼
                  ┌─────────────────┐
                  │ DiscoveryServer │
                  │    Eureka       │
                  │     :8761       │
                  └────────┬────────┘
                           |
             ┌─────────────┼─────────────┐
             │             │             │
             ▼             ▼             ▼
       UserService   ProductCatService  OrderService
          :8081           :8082            :8083
                              ▲              |
                              |              |
                              └──────────────┘
                               Service Discovery
```

---

# Complete Request Flow

## Request 1 – Register User

Client calls:

```text
POST http://localhost:8080/users
```

Flow:

```text
Client
  ↓
Gateway :8080
  ↓
Eureka :8761
  ↓
UserService :8081
  ↓
Response
  ↓
Client
```

---

## Request 2 – Add Product

Client calls:

```text
POST http://localhost:8080/products
```

Flow:

```text
Client
  ↓
Gateway :8080
  ↓
Eureka :8761
  ↓
ProductCatService :8082
  ↓
Response
  ↓
Client
```

---

## Request 3 – Check Product

Client calls:

```text
GET http://localhost:8080/orders/check/101
```

Flow:

```text
Client
  ↓
Gateway :8080
  ↓
Eureka
  ↓
OrderService :8083
  ↓
Eureka
  ↓
ProductCatService :8082
  ↓
Product Quantity
  ↓
OrderService
  ↓
Gateway
  ↓
Client
```

---

# Service Registration

When each application starts, it registers itself with Eureka.

```text
UserService
     |
     └──────┐
            ▼
ProductService ────→ Eureka :8761
            ▲
OrderService ──────┘
            ▲
GatewayService ────┘
```

Eureka maintains the service registry:

```text
USER-SERVICE
PRODUCT-CAT-SERVICE
ORDER-SERVICE
GATEWAY-SERVICE
```

---

# Eureka Dashboard

After all services are started, open:

```text
http://localhost:8761
```

You should see registered applications such as:

```text
Instances currently registered with Eureka

Application

GATEWAY-SERVICE
ORDER-SERVICE
PRODUCT-CAT-SERVICE
USER-SERVICE
```

The exact status/details depend on the running instances.

---

# Project Structure

```text
eureka-microservices/
│
├── DiscoveryServer/
│   ├── src/
│   └── pom.xml
│
├── GatewayService/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── .../
│   │       │       └── GatewayConfig.java
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
│
├── UserService/
│   ├── src/
│   └── pom.xml
│
├── ProductCatService/
│   ├── src/
│   └── pom.xml
│
└── OrderService/
    ├── src/
    └── pom.xml
```

---

# Dependencies Summary

| Service           | Dependency                                    | Port |
| ----------------- | --------------------------------------------- | ---: |
| DiscoveryServer   | `spring-cloud-starter-netflix-eureka-server`  | 8761 |
| GatewayService    | `spring-cloud-starter-gateway-server-webflux` | 8080 |
| GatewayService    | `spring-cloud-starter-netflix-eureka-client`  | 8080 |
| UserService       | `spring-cloud-starter-netflix-eureka-client`  | 8081 |
| ProductCatService | `spring-cloud-starter-netflix-eureka-client`  | 8082 |
| OrderService      | `spring-cloud-starter-netflix-eureka-client`  | 8083 |

---

# How to Run

Start the applications in this order.

## 1. DiscoveryServer

```bash
cd DiscoveryServer
mvn spring-boot:run
```

Open:

```text
http://localhost:8761
```

---

## 2. UserService

```bash
cd UserService
mvn spring-boot:run
```

---

## 3. ProductCatService

```bash
cd ProductCatService
mvn spring-boot:run
```

---

## 4. OrderService

```bash
cd OrderService
mvn spring-boot:run
```

---

## 5. GatewayService

```bash
cd GatewayService
mvn spring-boot:run
```

Gateway will be available at:

```text
http://localhost:8080
```

---

# Test Flow

## Register User

```text
POST http://localhost:8080/users
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
POST http://localhost:8080/products
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

## Check Product

```text
GET http://localhost:8080/products/101
```

---

## Check Product Through OrderService

```text
GET http://localhost:8080/orders/check/101
```

---

# Gateway vs Eureka

These two components have different responsibilities.

### Eureka

Eureka answers:

> **"Where is this service running?"**

Example:

```text
PRODUCT-CAT-SERVICE
        ↓
localhost:8082
```

### Gateway

Gateway answers:

> **"Where should this client request go?"**

Example:

```text
/products/**
       ↓
PRODUCT-CAT-SERVICE
```

Together:

```text
Client
  |
  ▼
Gateway
  |
  | Which service?
  ▼
Eureka
  |
  | Where is it?
  ▼
ProductCatService
```

---

# Key Concepts

The main purpose of this project is to understand:

```text
Service Registration
        ↓
Service Discovery
        ↓
API Gateway
        ↓
Service-to-Service Communication
        ↓
Load-Balanced Routing
```

### Without Gateway

```text
Client
  |
  ├──→ UserService :8081
  |
  ├──→ ProductCatService :8082
  |
  └──→ OrderService :8083
```

### With Gateway + Eureka

```text
Client
  |
  ▼
Gateway :8080
  |
  ▼
Eureka :8761
  |
  ├──→ UserService
  |
  ├──→ ProductCatService
  |
  └──→ OrderService
```

---

# Technologies

* Java 17
* Spring Boot
* Spring Cloud
* Spring Cloud Gateway WebFlux
* Spring Cloud Netflix Eureka
* Maven
* REST API
* Microservices
* Service Discovery
* API Gateway
* Load-balanced Routing

---

# Future Enhancements

The next evolution of this project can be:

```text
                         Client
                           |
                           ▼
                    API Gateway
                           |
                           ▼
                    JWT Authentication
                           |
                           ▼
                     Eureka Server
                           |
            ┌──────────────┼──────────────┐
            ▼              ▼              ▼
       UserService   ProductService   OrderService
            │              │              │
            └──────────────┼──────────────┘
                           ▼
                         Kafka
                           |
                           ▼
                   Event-Driven System
                           |
                           ▼
                        Database
                           |
                           ▼
                         Docker
                           |
                           ▼
                      Kubernetes
```

Potential future additions:

* JWT Authentication
* Spring Security
* Kafka
* Database per service
* Docker containers
* Docker Compose
* Centralized configuration
* Circuit Breaker / Resilience4j
* Distributed tracing
* Prometheus + Grafana
* Kubernetes
* CI/CD

```

This version makes the **Gateway → Eureka → Microservice** flow explicit while preserving your original **OrderService → Eureka → ProductCatService** service-to-service flow.
```
