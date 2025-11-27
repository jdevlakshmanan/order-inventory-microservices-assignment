# Order Inventory Microservices Assignment

This repository contains two Spring Boot microservices: Inventory Service and Order Service.

## Modules
- `inventory-service`: maintains inventory batches and provides endpoints to fetch batches sorted by expiry and update inventory.
- `order-service`: accepts orders and communicates with Inventory Service to reserve/update stock.

## Strategy documentation

Inventory Service supports multiple allocation strategies via the `InventoryStrategy` factory. Two strategies are included:

- `simple` (SimpleInventoryStrategy)
    - Adds new stock as a batch (expiry = 1 year).
    - Consumes stock from batches ordered by earliest `expiryDate` (minimize spoilage).

- `batch` (BatchAllocationStrategy)
    - Adds new stock as a batch (expiry = 3 months).
    - Consumes stock from the largest batches first (quantity-descending) to reduce fragmentation.

You can select a strategy by passing the `strategy` field in the `POST /inventory/update` request body. If omitted, the factory defaults to the first available strategy.

## Technology
- Java 21
- Spring Boot 3.5.8
- Spring Data JPA with H2 in-memory databases
- WebClient for inter-service communication (Order -> Inventory)
- Lombok
- springdoc OpenAPI (Swagger)
- JUnit 5 and Mockito for testing
- Actuator for monitoring health and metrics
- Gradle for build automation

## Run locally
1. Build the project:

```powershell
.\gradlew clean build
```

2. Run services (in separate shells):

```powershell
.\gradlew :inventory-service:bootRun
.\gradlew :order-service:bootRun
```

Inventory will run on port 8081 and Order on 8080 by the provided module properties. The `OrderService` points to the Inventory base URL via `inventory.service.base-url` property.

## APIs
- Inventory Service (port 8081):
  - GET `/inventory/{productId}` - returns list of batches sorted by expiry
  - POST `/inventory/update` - updates inventory (request JSON):
    ```
    { 
        "sku": "id", 
        "delta": -2, 
        "strategy": "simple" 
    }
    ```
- Order Service (port 8080):
  - POST `/order` - places an order (request JSON): 
    ```
    { 
        "customerId": "c1", 
        "items": [
            { "sku": "x", "quantity": 2 }
        ]
    }
    ```

## Swagger UIs
- Inventory Service: http://localhost:8081/swagger-ui.html
- Order Service: http://localhost:8080/swagger-ui.html

## Tests

- Make sure to build and bootRun services before running tests, as some integration tests depend on running services.
```
.\gradlew clean build
.\gradlew :inventory-service:bootRun
.\gradlew :order-service:bootRun
```

- Run all tests:
```
.\gradlew clean test
```

- Run a single module tests:
```
.\gradlew :inventory-service:test
.\gradlew :order-service:test
```

## In-memory DB Consoles
- Inventory Service H2 Console: http://localhost:8081/h2-console
```
username: root
password: root
```
- Order Service H2 Console: http://localhost:8080/h2-console
```
username: root
password: root
```

## Actuator Endpoints
- Inventory Service Actuator: http://localhost:8081/actuator
- Order Service Actuator: http://localhost:8080/actuator

