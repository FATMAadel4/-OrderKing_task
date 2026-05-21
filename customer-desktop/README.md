# Customer Manager – Desktop Application (JavaFX)

A JavaFX desktop client that consumes the **Customer Manager Spring Boot REST API**.  
The desktop app has **no direct database connection** – every data operation goes through the API.

---

## Screenshots

### Main Window – Customer List
![Main Table](screenshots/01_main_table.png)
> The main window loads all customers automatically on startup via `GET /customers`.  
> The table shows ID, Name, Email, Phone, and Created At columns.

---

### Add New Customer
![Add Dialog](screenshots/02_add_dialog.png)
> Click **"+ Add Customer"** to open the form dialog.  
> Fields: Full Name (required), Email (required), Phone (optional).

---

### Client-Side Validation
![Validation](screenshots/03_validation.png)
> The form validates input before sending to the API:
> - Name is required (max 100 characters)
> - Email is required and must be a valid format
> - Phone must match a valid pattern if provided

---

### Edit Customer
![Edit Dialog](screenshots/04_edit_dialog.png)
> Select a row and click **"Edit"** (or double-click the row) to open the edit form.  
> All existing data is pre-filled automatically.  
> Status bar shows **"✔ Customer updated."** on success.

---

### Customer Added Successfully
![Customer Added](screenshots/05_customer_added.png)
> After a successful `POST /customers`, the new customer appears in the table immediately  
> and the status bar shows **"✔ Customer 'name' added."**

---

### Customer Updated Successfully
![Customer Updated](screenshots/06_customer_updated.png)
> After a successful `PUT /customers/{id}`, the row is updated in place  
> and the status bar shows **"✔ Customer updated."**

---

### Delete Confirmation
![Delete Confirm](screenshots/07_delete_confirm.png)
> Click **"Delete"** to show a confirmation dialog before sending `DELETE /customers/{id}`.  
> The action cannot be undone.

---

## Project Structure

```
customer-desktop/
├── screenshots/                        ← Application screenshots
├── pom.xml
└── src/main/java/com/customermanager/
    ├── MainApp.java                    ← Application entry point
    ├── model/
    │   └── Customer.java               ← Data model (mirrors API JSON)
    ├── service/
    │   └── CustomerService.java        ← All HTTP calls (Java HttpClient + Gson)
    ├── ui/
    │   ├── MainController.java         ← Main window (table + toolbar)
    │   └── CustomerFormDialog.java     ← Add / Edit customer dialog
    └── util/
        ├── ApiConfig.java              ← Base URL & bearer token config
        ├── ApiResponse.java            ← Generic HTTP result wrapper
        └── PagedResponse.java          ← Paginated API response wrapper
```

---

## Prerequisites

| Tool  | Version    |
|-------|------------|
| Java  | 17 or newer |
| Maven | 3.8+       |

> The **Spring Boot backend must be running** before launching the desktop app.

---

## Configuration

Open `src/main/java/com/customermanager/util/ApiConfig.java` and adjust:

```java
public static final String BASE_URL     = "http://localhost:8080";  // backend URL
public static final String BEARER_TOKEN = "your-jwt-token-here";    // from /auth/login
```

---

## How to Run

### Option 1 – Maven (recommended)
```bash
cd customer-desktop
mvn javafx:run
```

### Option 2 – Executable JAR
```bash
mvn package
java -jar target/customer-desktop-1.0-SNAPSHOT.jar
```

### Option 3 – IntelliJ IDEA / NetBeans
1. Open the project (File → Open Project)
2. Run from Terminal: `mvn javafx:run`

> ⚠️ Do **not** use the IDE's Run button directly — use `mvn javafx:run` in the Terminal to avoid the *"JavaFX runtime components are missing"* error.

---

## Features

| Feature | Details |
|---------|---------|
| **List customers** | Table auto-loads on startup via `GET /customers` |
| **Add customer** | Click "+ Add Customer" → fill form → `POST /customers` |
| **Edit customer** | Select row → click "Edit" or double-click → `PUT /customers/{id}` |
| **Delete customer** | Select row → click "Delete" → confirmation → `DELETE /customers/{id}` |
| **Search** | Type in search box → `GET /customers?search=...` |
| **Loading spinner** | Shown during every API call |
| **Error handling** | Friendly error dialogs for network failures & API errors |
| **Client validation** | Name/email required, regex format checks before sending to API |
| **Bearer Token auth** | Configured in `ApiConfig.java` — sent in every request |
| **Pagination support** | Handles both plain list `[...]` and paginated `{content: [...]}` responses |

---

## How the Desktop App Communicates with the Backend

```
Desktop App (JavaFX)
       │
       │  HTTP/JSON  (Java HttpClient + Gson)
       │  Authorization: Bearer <token>
       ▼
Spring Boot REST API  (localhost:8080)
       │
       │  JPA / Hibernate
       ▼
   MySQL Database (port 3307)
```

Every button click triggers an async `Task` that calls `CustomerService`,  
which sends an `HttpRequest` and parses the JSON response with **Gson**.  
The UI thread is never blocked — a spinner is shown while the request is in flight.

---

## API Endpoints Used

| Method | Endpoint | Action |
|--------|----------|--------|
| `GET` | `/customers` | Load all customers |
| `POST` | `/customers` | Add new customer |
| `PUT` | `/customers/{id}` | Update existing customer |
| `DELETE` | `/customers/{id}` | Delete customer |
| `GET` | `/customers?search=...` | Search customers |
