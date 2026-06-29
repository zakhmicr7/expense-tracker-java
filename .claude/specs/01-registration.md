# Spec: Registration

## Overview
This step adds user registration and login to the expense tracker so the app can support
multiple users, each with their own private set of expenses. Users sign up with a username
and password; subsequent sessions are maintained via a server-side cookie (JSESSIONID) using
Spring Security's built-in HTTP session support. All expense endpoints become protected and
return only the authenticated user's data. This is the foundation every subsequent feature
depends on.

## Depends on
None — this is the first feature step.

## API changes
- `POST /api/auth/register` — Register a new user — body: `{ "username": string, "password": string }`
- `POST /api/auth/login` — Authenticate and start a session — body: `{ "username": string, "password": string }`
- `POST /api/auth/logout` — Invalidate the current session — no body
- `GET  /api/auth/me` — Return the currently authenticated username — no body

All existing `/api/expenses` endpoints remain at the same paths but are now protected;
unauthenticated requests receive `401 Unauthorized`. Expense data is scoped to the
authenticated user (filtered by `user_id`).

## Database changes

Add a `users` table and a `user_id` foreign key on `expenses`.

In `src/main/resources/schema.sql`, append:

```sql
CREATE TABLE IF NOT EXISTS users (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT    NOT NULL UNIQUE,
    password TEXT    NOT NULL   -- BCrypt hash
);

-- Add user ownership to expenses (default 0 = legacy/unowned, safe for existing rows)
ALTER TABLE expenses ADD COLUMN user_id INTEGER NOT NULL DEFAULT 0
    REFERENCES users(id);
```

> Note: SQLite does not support transactional DDL for `ALTER TABLE`. The `IF NOT EXISTS`
> guard on `CREATE TABLE` makes the schema idempotent. The `ALTER TABLE` will fail silently
> on re-run if the column already exists — add a guard in the `DataSourceInitializer` or
> wrap in a try/catch in a `@PostConstruct` migration method.

## Backend files to create

| File | Purpose |
|------|---------|
| `src/main/java/com/expensetracker/model/User.java` | Plain POJO: `id`, `username`, `password` with getters/setters |
| `src/main/java/com/expensetracker/repository/UserRepository.java` | JdbcTemplate queries: `findByUsername`, `save` |
| `src/main/java/com/expensetracker/service/UserService.java` | Registration logic: validate input, hash password, persist |
| `src/main/java/com/expensetracker/service/ExpenseService.java` | Thin delegation layer wrapping `ExpenseRepository`; all expense business logic moves here (extracted from controller) |
| `src/main/java/com/expensetracker/controller/AuthController.java` | Handles `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/auth/me` |
| `src/main/java/com/expensetracker/security/SecurityConfig.java` | Spring Security config: permit `/api/auth/**`, protect everything else, disable CSRF for REST |
| `src/main/java/com/expensetracker/security/JdbcUserDetailsService.java` | Implements `UserDetailsService`; loads user from DB via `UserRepository` for Spring Security |

## Backend files to modify

| File | Change |
|------|--------|
| `src/main/java/com/expensetracker/controller/ExpenseController.java` | Inject `ExpenseService` instead of `ExpenseRepository` directly; add `Principal` parameter to GET/POST/DELETE so queries are scoped to the authenticated user's `user_id`; add `ExpenseService` delegation |
| `src/main/java/com/expensetracker/repository/ExpenseRepository.java` | Add `findAllByUserId(long userId)`, `saveForUser(Expense e, long userId)`, `deleteByIdAndUserId(long id, long userId)` to scope all queries by `user_id` |
| `src/main/resources/schema.sql` | Append `users` table and `ALTER TABLE expenses ADD COLUMN user_id` as described above |
| `src/main/resources/application.properties` | Add `spring.datasource.url=jdbc:sqlite:./data/expenses.db` (move db into `data/` per CLAUDE.md); add `server.port=8080` |

## Frontend changes

Changes in `src/main/resources/static/index.html`:

**New UI sections:**
- Auth screen (shown when not logged in): two tab-style panels — "Login" and "Register" — each with a username field, password field, and submit button. Shown instead of the main app.
- Logged-in header: show current username and a "Log out" button in the `<header>`.

**New `fetch()` calls:**
- `POST /api/auth/register` — on register form submit; on success switch to login tab with a success message
- `POST /api/auth/login` — on login form submit; on success hide auth screen and show main app
- `POST /api/auth/logout` — on logout button click; on success show auth screen again
- `GET /api/auth/me` — called on page load to check if a session already exists; if 200 show main app, if 401 show auth screen

**New JS functions:**
- `checkSession()` — calls `/api/auth/me` on load; branches to `showApp()` or `showAuth()`
- `showAuth()` / `showApp()` — toggle visibility between auth screen and main content
- `handleLogin(e)` — submits login form, calls `showApp()` on success
- `handleRegister(e)` — submits register form, shows success/error message
- `handleLogout()` — calls logout endpoint, calls `showAuth()` on success

**CSS additions:**
- `.auth-screen` — centered card, max-width 400px, shown full-page before login
- `.auth-tabs` / `.auth-tab` / `.auth-tab.active` — tab switcher styling
- `.auth-form` — stacked form inside auth screen
- `.auth-error` / `.auth-success` — inline message styling

## New Maven dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

No version needed — inherited from Spring Boot parent POM.

## Rules for implementation

- No JPA, Hibernate, or any ORM — use `JdbcTemplate` only
- No Lombok — plain Java POJOs with explicit getters/setters
- SQL lives only in the Repository layer (`UserRepository`, `ExpenseRepository`)
- All input validation in the Service layer; return `400` with a clear message on failure
- Return `404` if a resource ID does not exist
- Use `ResponseEntity<?>` for all controller return types
- No JS frameworks or CDN libraries in the frontend
- No inline styles in HTML — use CSS classes only
- Passwords must be hashed with `BCryptPasswordEncoder` — never store plaintext
- CSRF protection must be disabled in `SecurityConfig` (`http.csrf(csrf -> csrf.disable())`) — the app uses session cookies but does not serve server-rendered forms
- `SecurityConfig` must use `HttpSecurity` lambda DSL (Spring Security 6 style), not deprecated chained methods
- All expense queries must include a `user_id` filter — never return another user's expenses
- The `ALTER TABLE expenses ADD COLUMN user_id` migration must be wrapped in a `try/catch` to survive re-runs on an existing database (SQLite throws an error if the column already exists)
- Move the SQLite database file path to `./data/expenses.db` and ensure `data/` is in `.gitignore`

## Definition of done

- [ ] `POST /api/auth/register` with `{"username":"alice","password":"pass123"}` returns `201 Created`
- [ ] `POST /api/auth/register` with a duplicate username returns `409 Conflict` with a message
- [ ] `POST /api/auth/register` with a blank username or password returns `400 Bad Request`
- [ ] `POST /api/auth/login` with correct credentials returns `200 OK` and sets a `JSESSIONID` cookie
- [ ] `POST /api/auth/login` with wrong password returns `401 Unauthorized`
- [ ] `GET /api/expenses` without a session cookie returns `401 Unauthorized`
- [ ] `GET /api/expenses` with a valid session cookie returns only the authenticated user's expenses
- [ ] Two different users adding expenses see only their own data in the list
- [ ] `POST /api/auth/logout` invalidates the session; subsequent `GET /api/expenses` returns `401`
- [ ] `GET /api/auth/me` returns `{"username":"alice"}` when logged in
- [ ] Opening the page in a browser shows the auth screen (login/register tabs)
- [ ] After login the main expense UI is visible with the username shown in the header
- [ ] Registering and logging in via the browser UI completes without page reload
- [ ] Clicking "Log out" returns to the auth screen
- [ ] App still runs with `mvn spring-boot:run` and `schema.sql` initialises without errors
