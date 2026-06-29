# Expense Tracker — Claude Project Instructions

## Project Overview
A lightweight expense tracker with a Java REST backend and a vanilla HTML/CSS/JS frontend.
No frontend frameworks. No ORM. Keep it simple and readable.

---

## Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Backend   | Java 17+, Spring Boot 3.x           |
| DB Access | Spring JDBC (no JPA, no Hibernate)  |
| Database  | SQLite (`./data/expenses.db`)       |
| Frontend  | Vanilla HTML + CSS + JavaScript     |
| Build     | Maven (`pom.xml`)                   |

---

## Project Structure

```
expense-tracker/
├── CLAUDE.md
├── pom.xml
├── data/
│   └── expenses.db              ← SQLite database file (gitignored)
├── src/
│   ├── main/
│   │   ├── java/com/tracker/
│   │   │   ├── ExpenseTrackerApp.java
│   │   │   ├── controller/
│   │   │   │   └── ExpenseController.java
│   │   │   ├── service/
│   │   │   │   └── ExpenseService.java
│   │   │   ├── repository/
│   │   │   │   └── ExpenseRepository.java
│   │   │   └── model/
│   │   │       └── Expense.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/
│       └── java/com/tracker/
└── frontend/
    └── index.html               ← single file: HTML + CSS + JS
```

---

## Database

- SQLite file path: `./data/expenses.db`
- Schema is defined in `src/main/resources/schema.sql`
- Run schema.sql on app startup via `DataSourceInitializer` or `@PostConstruct`
- Use `org.xerial:sqlite-jdbc` driver
- Configure in `application.properties`:
  ```
  spring.datasource.url=jdbc:sqlite:./data/expenses.db
  spring.datasource.driver-class-name=org.sqlite.JDBC
  spring.sql.init.mode=always
  spring.sql.init.schema-locations=classpath:schema.sql
  ```

### Expense Table Schema
```sql
CREATE TABLE IF NOT EXISTS expenses (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    amount      REAL    NOT NULL CHECK(amount > 0),
    category    TEXT    NOT NULL,
    description TEXT,
    date        TEXT    NOT NULL   -- ISO format: YYYY-MM-DD
);
```

---

## Backend Rules

- **No JPA / No Hibernate** — use `JdbcTemplate` only
- **No Lombok** — write plain Java POJOs with explicit getters/setters
- Keep controllers thin: only request/response handling
- Business logic lives in the Service layer
- SQL lives in the Repository layer
- Validate all inputs in the Service layer; return `400 Bad Request` with a clear message on failure
- Return `404 Not Found` if a requested expense ID does not exist
- Use `ResponseEntity<?>` for all controller return types

### REST API Endpoints
| Method | Path                  | Description              |
|--------|-----------------------|--------------------------|
| GET    | `/api/expenses`       | List all expenses        |
| GET    | `/api/expenses/{id}`  | Get single expense by ID |
| POST   | `/api/expenses`       | Add a new expense        |
| DELETE | `/api/expenses/{id}`  | Delete an expense        |

### Input Validation Rules
- `amount`: required, must be > 0
- `category`: required, non-empty string
- `date`: required, must be a valid ISO date (YYYY-MM-DD)
- `description`: optional

---

## Frontend Rules

- **Single file**: all HTML, CSS, and JS in `frontend/index.html`
- **No frameworks** — no React, Vue, jQuery, or any external JS libraries
- Use `fetch()` for all API calls to the Spring Boot backend (default port: `8080`)
- UI must support: add expense, list all expenses, delete an expense
- Show a summary section: total spend grouped by category
- Display user-friendly error messages on failed API calls (don't just console.log)
- Keep the CSS clean and minimal — no inline styles

---

## What NOT to Do

- Do not use JPA, Hibernate, or any ORM
- Do not use Lombok
- Do not add any JS frameworks or CDN libraries to the frontend
- Do not hardcode the SQLite path — always use `application.properties`
- Do not swallow exceptions silently — always log and return a meaningful HTTP error
- Do not put SQL queries in the Controller or Service layer

---

## Running the App

```bash
# Build and run backend
mvn spring-boot:run

# Frontend: open directly in browser
open frontend/index.html
# or serve via VS Code Live Server extension
```

Backend runs on: `http://localhost:8080`

---

## Git

- Gitignore: `data/`, `target/`, `*.db`, `.idea/`, `*.iml`
- Commit messages: use imperative mood (`Add validation`, `Fix NPE in repository`)