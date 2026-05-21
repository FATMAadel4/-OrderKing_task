# Customer Manager – Spring Boot Backend

A RESTful API built with **Spring Boot 3** for managing customers.  
The backend handles all data access, business logic, and security.  
The desktop client (JavaFX) communicates with this API — it has no direct database connection.

---

## Screenshots (Desktop Client)

### Main Window – Customer List
![Main Table](screenshots/01_main_table.jpeg)
> All customers loaded via `GET /customers` — shown in the JavaFX desktop client.

---

### Add New Customer
![Add Dialog](screenshots/02_add_dialog.jpeg)
> The desktop form sends a `POST /customers` request to this backend.

---

### Client-Side Validation
![Validation](screenshots/03_validation.jpeg)
> Client validates before sending. Backend also validates with `@Valid` + `@NotBlank`, `@Email`.

---

### Edit Customer
![Edit Dialog](screenshots/04_edit_dialog.jpeg)
> The desktop form sends a `PUT /customers/{id}` request.  
> Backend checks for email conflict (409) before saving.

---

### Customer Added Successfully
![Customer Added](screenshots/05_customer_added.jpeg)
> `POST /customers` returns **201 Created** with the new customer object.

---

### Customer Updated Successfully
![Customer Updated](screenshots/06_customer_updated.jpeg)
> `PUT /customers/{id}` returns **200 OK** with the updated customer object.

---

### Delete Confirmation
![Delete Confirm](screenshots/07_delete_confirm.jpeg)
> `DELETE /customers/{id}` returns **204 No Content** after successful deletion.

---

## Project Structure

```
customer-management/
├── screenshots/
├── pom.xml
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── application.yaml                          ← DB + JWT config
    │   └── java/com/example/customer_management/
    │       ├── CustomerManagementApplication.java        ← Entry point
    │       ├── config/
    │       │   └── OpenApiConfig.java                    ← Swagger config
    │       ├── controller/
    │       │   ├── AuthController.java                   ← POST /auth/login
    │       │   └── CustomerController.java               ← REST endpoints
    │       ├── dto/
    │       │   └── CustomerRequest.java                  ← Request DTO
    │       ├── entity/
    │       │   └── Customer.java                         ← JPA Entity
    │       ├── exception/
    │       │   ├── ConflictException.java                ← 409
    │       │   ├── GlobalExceptionHandler.java           ← Error responses
    │       │   └── NotFoundException.java                ← 404
    │       ├── repository/
    │       │   └── CustomerRepository.java               ← Spring Data JPA
    │       ├── security/
    │       │   ├── JwtFilter.java                        ← JWT validation
    │       │   └── SecurityConfig.java                   ← Security rules
    │       └── service/
    │           └── CustomerService.java                  ← Business logic
    └── test/
```

---

## Prerequisites

| Tool        | Version     |
|-------------|-------------|
| Java        | 17 or newer |
| Maven       | 3.8+        |
| MySQL       | 8.0+        |

---

## Database Setup

### Step 1 – Create the database and table

Run the SQL script in MySQL Workbench or any MySQL client:

```sql
CREATE DATABASE IF NOT EXISTS customer_db;
USE customer_db;

CREATE TABLE IF NOT EXISTS customers (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    phone      VARCHAR(50)  NOT NULL,
    created_at DATETIME DEFAULT NOW()
);

-- Optional sample data
INSERT INTO customers (name, email, phone) VALUES
('John Doe',    'john@test.com',  '+201001234567'),
('Jane Smith',  'jane@test.com',  '+201007654321'),
('Ahmed Hassan','ahmed@test.com', '+201009876543');
```

### Step 2 – Configure the connection

Open `src/main/resources/application.yaml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/customer_db
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

jwt:
  secret: orderking-secret-key-2024-very-secure-string

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

> Change `username`, `password`, and `port` to match your MySQL setup.

---

## How to Run in IntelliJ IDEA

### Step 1 – Open the project
```
File → Open → select the customer-management folder
```

### Step 2 – Wait for Maven to download dependencies
IntelliJ will download all dependencies automatically.  
Wait until the bottom progress bar finishes.

### Step 3 – Run the application

**Option A – IntelliJ Run button:**
1. Open `CustomerManagementApplication.java`
2. Click the green ▶️ button next to `main()`
3. Or press `Shift + F10`

**Option B – Maven Terminal:**
```bash
mvn spring-boot:run
```

### Step 4 – Verify it's running
Open your browser and go to:
```
http://localhost:8080/swagger-ui.html
```
You should see the Swagger UI with all endpoints.

---

## Authentication (JWT)

The API is secured with **JWT Bearer Token**.

### Step 1 – Login to get a token

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

### Step 2 – Use the token in every request

```
GET http://localhost:8080/customers
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Step 3 – Unauthorized response (if token is missing)

```json
HTTP 401 Unauthorized
{
  "error": "Missing or invalid Authorization header"
}
```

---

## API Endpoints

| Method   | Endpoint                        | Description              | Status Code |
|----------|---------------------------------|--------------------------|-------------|
| `POST`   | `/auth/login`                   | Login and get JWT token  | 200         |
| `GET`    | `/customers`                    | Get all customers        | 200         |
| `GET`    | `/customers?keyword=john`       | Search customers         | 200         |
| `GET`    | `/customers?page=0&size=10`     | Paginated results        | 200         |
| `GET`    | `/customers/{id}`               | Get customer by ID       | 200 / 404   |
| `POST`   | `/customers`                    | Create new customer      | 201 / 400 / 409 |
| `PUT`    | `/customers/{id}`               | Update customer          | 200 / 404 / 409 |
| `DELETE` | `/customers/{id}`               | Delete customer          | 204 / 404   |

---

## Request & Response Examples

### POST /customers
**Request:**
```json
{
  "name": "Sama Ahmed",
  "email": "sama@test.com",
  "phone": "0199778965"
}
```
**Response (201 Created):**
```json
{
  "id": 5,
  "name": "Sama Ahmed",
  "email": "sama@test.com",
  "phone": "0199778965",
  "createdAt": "2026-05-22T00:11:00"
}
```

### GET /customers
**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 2,
      "name": "Jane Smith",
      "email": "jane@test.com",
      "phone": "+201007654321",
      "createdAt": null
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

## Error Handling

| Scenario                  | HTTP Status | Response Body |
|---------------------------|-------------|---------------|
| Customer not found        | `404`       | `{"error": "Customer not found with id: X"}` |
| Email already exists      | `409`       | `{"error": "Email already exists: x@test.com"}` |
| Validation failed         | `400`       | `{"name": "Name is required", "email": "Invalid email format"}` |
| Missing/invalid JWT token | `401`       | `{"error": "Missing or invalid Authorization header"}` |
| Server error              | `500`       | `{"error": "Something went wrong: ..."}` |

---

## Features

| Feature | Details |
|---------|---------|
| **CRUD** | Full Create, Read, Update, Delete |
| **Pagination** | `GET /customers?page=0&size=10` |
| **Search** | `GET /customers?keyword=john` — searches name, email, phone |
| **JWT Auth** | Bearer token required for all `/customers` endpoints |
| **Validation** | Server-side with `@Valid`, `@NotBlank`, `@Email`, `@Size` |
| **Conflict check** | Returns 409 if email already exists |
| **Swagger UI** | Available at `/swagger-ui.html` |
| **Proper HTTP codes** | 200, 201, 204, 400, 401, 404, 409, 500 |

---

## Architecture

```
JavaFX Desktop App
       │
       │  HTTP/JSON + Authorization: Bearer <token>
       ▼
┌─────────────────────────────────┐
│     Spring Boot REST API        │
│  ┌──────────────────────────┐   │
│  │  CustomerController      │   │  ← REST layer
│  └──────────┬───────────────┘   │
│             │                   │
│  ┌──────────▼───────────────┐   │
│  │  CustomerService         │   │  ← Business logic
│  └──────────┬───────────────┘   │
│             │                   │
│  ┌──────────▼───────────────┐   │
│  │  CustomerRepository      │   │  ← Spring Data JPA
│  └──────────┬───────────────┘   │
└────────────-│───────────────────┘
              │
              ▼
       MySQL Database
       (customer_db)
```
