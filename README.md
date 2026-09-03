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


## Database Schema

Matches section 5 of the assignment exactly (`product`, `item` tables), plus two auth-support tables (`app_user`, `refresh_token`) not specified in the brief but required to make JWT auth functional. Indexes are added on `product.product_name` and `item.product_id` per the "database indexing strategy" requirement.

## Running Locally

### Option A — Docker Compose (recommended)
```bash
docker compose up --build
```
This starts a MySQL 8.4 container and the API together. The API is available at `http://localhost:8080`.

### Option B — Local Maven / Eclipse + your own MySQL
Create the database first:
```sql
CREATE DATABASE productdb;
```
Then either set env vars before `mvn spring-boot:run`:
```bash
export DB_URL="jdbc:mysql://localhost:3306/productdb?allowPublicKeyRetrieval=true&useSSL=false"
export DB_USERNAME=root
export DB_PASSWORD=root
mvn spring-boot:run
```
or, in Eclipse, just run `ProductApiApplication.java` directly — `application.yml` already defaults to `jdbc:mysql://localhost:3306/productdb` with `root`/`root`, so no env vars are required unless your credentials differ.

To use PostgreSQL instead, set `DB_URL=jdbc:postgresql://...` and `DB_DRIVER=org.postgresql.Driver` (the driver dependency is already in `pom.xml` for both databases).

On startup, `DataSeeder` provisions two accounts for immediate testing (no registration endpoint was specified in the brief):

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | ROLE_ADMIN |
| `user` | `User@123` | ROLE_USER |

### Authenticating

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

Use the returned `accessToken` as `Authorization: Bearer <token>` on subsequent requests. When it expires, call `/api/v1/auth/refresh` with the `refreshToken` to get a new pair (the old refresh token is invalidated — rotation).

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
| `JWT_SECRET` | (dev default, **override in prod**) | HS256 signing key |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` (15 min) | Access token TTL |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 days) | Refresh token TTL |

## Notes / Assumptions

- The brief did not specify a `/register` endpoint, so accounts are seeded on boot (`DataSeeder`) — swap for a real registration/IdP flow in production.
- HTTPS enforcement is left to the deployment layer (reverse proxy / load balancer / ingress), which is the standard place to terminate TLS for a containerized Spring Boot service — the app itself is protocol-agnostic.
- This build was assembled and reviewed without a live Maven Central connection in the authoring sandbox; run `mvn clean verify` before pushing to catch any dependency-resolution issues in your own environment.
