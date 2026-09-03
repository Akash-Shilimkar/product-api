# Product API

RESTful CRUD API around **Products** (and nested **Items**) built with Java 17 and Spring Boot.
## Tech Stack

| Concern | Choice |
|---|---|
| Language / Runtime | Java 17+  |
| Framework | Spring Boot 3.3 |
| Persistence | Spring Data JPA (Hibernate), MySQL |
| Security | Spring Security, JWT access tokens + rotating refresh tokens |
| Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, Spring Boot Test, H2 |
| Containerization | Docker, Docker Compose |

## Architecture

```
controller/   → REST endpoints, request/response mapping, HTTP status codes
service/      → business logic, transactions, orchestration 
repository/   → Spring Data JPA repositories
entity/       → JPA entities (Product, Item, User, RefreshToken)
dto/          → request/response payloads (never expose entities directly)
security/     → JWT util, auth filter, UserDetailsService
config/       → SecurityConfig, OpenApiConfig, AsyncConfig, DataSeeder
exception/    → custom exceptions + @RestControllerAdvice global handler
```

- **Layered architecture**: controllers depend on service interfaces, not implementations, so services are trivially mockable in unit tests.
- **DTOs at the boundary**: entities are never serialized directly, keeping the API contract stable and independent of the DB schema.
- **Global exception handling**: all errors  are mapped to a consistent `ErrorResponse` JSON shape via `GlobalExceptionHandler`.
- **Stateless auth**: JWT access tokens (15 min default) + rotating opaque refresh tokens (7 days default) stored server-side, so a refresh token can be invalidated/rotated on every use.
- **Async**: audit logging of product create/update/delete runs on a dedicated `@Async` executor so it never blocks the request thread (see `AsyncConfig` / `AuditLogService`).

## API Endpoints

| Method | Path | Auth |
|---|---|---|
| POST | `/api/v1/auth/register` | public |
| POST | `/api/v1/auth/login` | public |
| POST | `/api/v1/auth/refresh` | public |
| GET | `/api/v1/products` | ADMIN or USER |
| GET | `/api/v1/products/{id}` | ADMIN or USER |
| POST | `/api/v1/products` | ADMIN |
| PUT | `/api/v1/products/{id}` | ADMIN |
| DELETE | `/api/v1/products/{id}` | ADMIN |
| GET | `/api/v1/products/{id}/items` | ADMIN or USER |
| POST | `/api/v1/products/{id}/items` | ADMIN |

## Download project from GitHub

Option 1 — git clone (recommended, keeps version history and lets you pull future updates)

Install Git if you don't have it: https://git-scm.com/download/win — run the installer, defaults are fine
Verify it installed:
powershell
   git --version
On the GitHub repo page, click the `green Code button → copy the HTTPS URL` (looks like https://github.com/<username>/<repo-name>.git)
Clone it:
powershell
   cd $env:USERPROFILE\Downloads
   git clone https://github.com/<username>/<repo-name>.git
   cd <repo-name>

Replace <username>/<repo-name> with the actual repo path. 5. If the repo is private, Git will prompt for GitHub credentials — use a Personal Access Token as the password (GitHub no longer accepts your account password directly): `GitHub → Settings → Developer settings → Personal access tokens → Generate new token`, give it repo scope, copy it, paste it when Git asks for a password.

Option 2 — Download ZIP (no Git required)

On the GitHub repo page, click the `green Code button → Download ZIP`
It saves to your Downloads folder as <repo-name>-main.zip
Then extract it and import on **Eclipse IDE** as **Exiting Maven Project**.


## Running Locally

### Option A — Docker Compose (recommended)
```bash
docker compose up --build
```
This starts a MySQL 8.4 container and the API together. The API is available at `http://localhost:8080`.

### Option B — Eclipse IDE (step by step)

**Step 1 — Install the Lombok agent**
This project uses Lombok (`@Getter`, `@Builder`, `@RequiredArgsConstructor`, etc.). Eclipse can't understand it without a one-time setup:
1. Download `lombok.jar` from `https://projectlombok.org/download`
2. Run it: `java -jar lombok.jar`
3. In the installer window, point it at your Eclipse installation and click **Install**
4. Restart Eclipse

**Step 2 — Create the MySQL database**
Open a MySQL client (Workbench, DBeaver, or the `mysql` CLI) and run:
```sql
CREATE DATABASE productdb;
```

**Step 3 — Generate the project with Spring Initializr**

If you're setting this up from scratch rather than starting from the provided zip, generate the base project structure and dependencies first:

1. Go to `https://start.spring.io`
2. Set:
   - **Project:** Maven
   - **Language:** Java
   - **Spring Boot:** 3.3.x
   - **Group:** `com.zestindia`
   - **Artifact:** `product-api`
   - **Packaging:** Jar
   - **Java:** 17 (or 21, matching what you have installed)
3. Under **Dependencies**, click **Add Dependencies** and select:
   - Spring Web
   - Spring Data JPA
   - Spring Security
   - Validation
   - MySQL Driver
   - Lombok
4. Click **Generate** — downloads a `product-api.zip` project
5. Extract it to a folder of your choice

**Step 4 — Import the project**
1. `File → Import…`
2. Choose `Maven → Existing Maven Projects` → **Next**
3. **Browse** to the `product-api` folder (either the one from Step 3, now merged with the provided source, or the zip's folder directly) → select it
4. Confirm `pom.xml` is checked → **Finish**
5. Wait for Eclipse to download dependencies (status bar, bottom-right) — needs internet access to Maven Central

**Step 5 — Confirm no red errors**
In **Project Explorer**, the project folder should have no red error icon. If it does, right-click the project → `Maven → Update Project…` (or press `Alt+F5`), check **Force Update of Snapshots/Releases**, click **OK**.

**Step 6 — Set your DB credentials (only if they differ from root/root)**
`application.yml` already defaults to `jdbc:mysql://localhost:3306/productdb` with username/password `root`/`root`. If your MySQL uses different credentials:
- Open `src/main/resources/application.yml`
- Edit the `username`/`password` defaults directly, **or**
- Set them as environment variables instead: `Run → Run Configurations… → (select your run config) → Environment tab → New` → add `DB_USERNAME` and `DB_PASSWORD`

**Step 7 — Run the application**
Right-click `src/main/java/com/zestindia/productapi/ProductApiApplication.java` → `Run As → Java Application` (or `Spring Boot App` if you have the Spring Tools Suite plugin installed).

**Step 8 — Verify it started cleanly**
Check the **Console** tab at the bottom — look for a line like `Started ProductApiApplication in X seconds` with no red stack traces above it.

**Step 9 — Test it**
Open `http://localhost:8080/swagger-ui.html` in a browser, or run:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
```

**Step 10 — Run the tests (optional)**
Right-click the `src/test/java` folder → `Run As → JUnit Test`. These use an in-memory H2 database, so no MySQL connection is needed for this step.

### Option C — Local Maven (command line) + your own MySQL
```bash
export DB_URL="jdbc:mysql://localhost:3306/productdb?allowPublicKeyRetrieval=true&useSSL=false"
export DB_USERNAME=root
export DB_PASSWORD=root
mvn spring-boot:run
```

To use PostgreSQL instead, set `DB_URL=jdbc:postgresql://...` and `DB_DRIVER=org.postgresql.Driver` (the driver dependency is already in `pom.xml` for both databases).

### Test accounts

`DataSeeder` provisions two accounts on first-ever startup (skipped on subsequent restarts if they already exist):

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | ROLE_ADMIN |
| `user` | `User@123` | ROLE_USER |

You can also create your own accounts via `POST /api/v1/auth/register` (see below).

### Authenticating

**Register a new account** (one-time per account):
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@123","role":"USER"}'
```
`role` is optional and defaults to `USER`; pass `"ADMIN"` for an admin account. Returns `201 Created` with a token pair immediately, or `409 Conflict` if the username is already taken.

**Log in** (every time you need a new session — safe to call repeatedly, never errors on repeat use):
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

Use the returned `accessToken` as `Authorization: Bearer <token>` on subsequent requests. When it expires (15 min default), call `/api/v1/auth/refresh` with the `refreshToken` to get a new pair — the old refresh token is deleted server-side the moment you use it (rotation), so it only works once.

### API Docs
Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

```bash
mvn test
```
- `ProductServiceImplTest`, `ItemServiceImplTest` — unit tests with JUnit 5 + Mockito, no Spring context.
- `ProductControllerIntegrationTest` — full Spring Boot Test using an H2 in-memory database (`application-test.yml`), covering auth, the full CRUD lifecycle, and validation failures.

## Configuration Reference

All config is environment-variable driven (see `application.yml`):

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/productdb?allowPublicKeyRetrieval=true&useSSL=false` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `root` | DB credentials |
| `DDL_AUTO` | `update` | Hibernate schema strategy |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` (15 min) | Access token TTL |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 days) | Refresh token TTL |

## Notes / Assumptions

- HTTPS enforcement is left to the deployment layer (reverse proxy / load balancer / ingress), which is the standard place to terminate TLS for a containerized Spring Boot service — the app itself is protocol-agnostic.
- `RefreshTokenRepository.deleteByUser()` uses an explicit `@Modifying @Query` (rather than a derived delete method) so the old refresh token row is actually removed from the database before the new one is inserted — required because `user_id` on `refresh_token` has a unique constraint, and Hibernate's default flush ordering runs INSERTs before DELETEs otherwise.
- This build was assembled and reviewed without a live Maven Central connection in the authoring sandbox; run `mvn clean verify` before pushing to catch any dependency-resolution issues in your own environment.

