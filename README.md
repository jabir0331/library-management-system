# Library Management System (REST API)

A backend-only REST API for running a small library: managing members, books and their physical editions, and the full borrowing cycle (issue, return, overdue fines and lost books). Admins authenticate with JWT, and the borrowing rules (loan length, fines, borrow limit) are configurable at runtime.

Built with **Spring Boot 4.1**, **Java 21**, **Spring Security (JWT)**, **Spring Data JPA** and **PostgreSQL**. API docs are served through Swagger UI.

## Features

- **JWT authentication** – admin login and logout (logged-out tokens are revoked server-side), BCrypt-hashed passwords, stateless sessions.
- **Catalogue** – authors (soft delete via activate/deactivate), genres, books, and book editions (physical copies with ISBN, price and copy counts).
- **Members** – full CRUD, status management (`ACTIVE`, `SUSPENDED`, `INACTIVE`) and search.
- **Circulation** – issue a book, return it, or mark it lost. The admin performing each action is taken from the logged-in token, so it can't be spoofed in the request.
- **Business rules, enforced on issue/return**
  - Only active members can borrow, and only while copies are available.
  - A member can't exceed the maximum number of active loans.
  - Fines are calculated per overdue day, after an optional grace period.
  - Lost books carry a penalty of `price × multiplier + service charge` and reduce the edition's total copies.
- **System configuration** – loan days, fine per day, borrow limit, grace period and more are stored in the database, seeded from `config/seed-data/system-config.txt`, validated by data type, and editable by admins.
- **Pagination and sorting** – every list and search endpoint is paged (`page`, `size`, `sort`) and returns a consistent envelope.
- **Readable IDs** – `ADM000001`, `MEM000001`, `AUTH000001`, `GEN000001`, `BK000001`, `ED000001`, `BKI000001`.
- **Consistent error responses** – every error returns the same JSON shape with the correct HTTP status (400, 401, 403, 404, 405, 415, 500).
- **OpenAPI / Swagger UI** for all endpoints.

## Tech stack

| Area | Technology |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.1.0 (Web MVC, Data JPA, Validation, Security) |
| Auth | JWT (jjwt 0.12), BCrypt |
| Database | PostgreSQL |
| Mapping / boilerplate | ModelMapper, Lombok |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper included) |

## Architecture

```
controller  →  service (interface + impl)  →  repository  →  PostgreSQL
    │                  │
   DTOs          domain rules, exceptions
```

Code lives under `src/main/java/com/library/`:

| Package | Purpose |
|---|---|
| `controller` | REST endpoints with Swagger annotations |
| `service`, `service/impl` | Business logic |
| `repository` | Spring Data JPA repositories |
| `model` | JPA entities and enums |
| `dto` | Request, response and seed DTOs |
| `security` | JWT service, auth filter, security configuration |
| `exception` | Custom exceptions and the global error handler |
| `util` | Custom ID and reference-number generators |
| `config` | ModelMapper, Swagger and first-admin bootstrap |

## Getting started

### Prerequisites
- JDK 21
- PostgreSQL running locally (or reachable)

### 1. Create the database
```bash
createdb -U postgres library_db
```
Tables are created automatically on first start.

### 2. Configure the environment
Copy the example file and fill in real values:
```bash
cp .env.example .env
```

| Variable | Required | Description |
|---|---|---|
| `DB_URL` | no | JDBC URL (default `jdbc:postgresql://localhost:5432/library_db`) |
| `DB_USERNAME` | no | Database user (default `postgres`) |
| `DB_PASSWORD` | **yes** | Database password |
| `JWT_SECRET` | **yes** | Random string, at least 32 characters |
| `JWT_EXPIRATION_MS` | no | Token lifetime in ms (default `3600000`, 1 hour) |
| `BOOTSTRAP_ADMIN_USERNAME` / `_EMAIL` / `_PASSWORD` | first run | Creates the first admin when the admin table is empty |

`.env` is git-ignored. Real environment variables (IDE run configuration, CI, Docker) take precedence over it.

### 3. Run
```bash
./mvnw spring-boot:run
```
The API is at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui.html`.

> **Upgrading an existing database?** Password hashes need more room than the old column allowed. Run once:
> `ALTER TABLE admin ALTER COLUMN password TYPE VARCHAR(100);`
> Existing plain-text admin passwords are upgraded to BCrypt the first time that admin logs in.

## Quick tour

Log in, then call any endpoint with the token:

```bash
# 1. Log in
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<your bootstrap password>"}'
# → {"token":"eyJ...","tokenType":"Bearer","expiresInMs":3600000,"adminId":"ADM000001","username":"admin"}

# 2. Issue a book (the issuing admin comes from the token)
curl -s -X POST http://localhost:8080/api/book-issues/issue \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"memberId":"MEM000001","bookEditionId":"ED000001","issuedDate":"2026-07-20"}'

# 3. Return it
curl -s -X POST http://localhost:8080/api/book-issues/return \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"issueId":"BKI000001","returnedDate":"2026-07-22","returnCondition":"GOOD"}'

# 4. Log out (revokes the token)
curl -s -X POST http://localhost:8080/api/auth/logout -H "Authorization: Bearer <token>"
```

In Swagger UI, use **Authorize** and paste the token from the login response.

## Running the tests

```bash
./mvnw test
```

The suite needs no database or environment variables. It covers:

- **Circulation rules** – issue, return and lost flows: borrow limit, inactive members, unavailable copies, fines with and without a grace period, lost-book penalty and copy counts.
- **Authentication** – JWT creation and validation (tampered, expired and foreign tokens), BCrypt login, upgrade of legacy plain-text passwords, and logout revocation.
- **Security and error handling** – through the real security filter chain: protected endpoints, revoked tokens, the admin being taken from the token instead of the request body, and the 400/401/404/405/415 error responses.
- **ID and reference-number generation**, including uniqueness within the same second.

A full-context smoke test (`LibraryManagementApplicationTests`) needs a running PostgreSQL and is skipped unless `RUN_INTEGRATION_TESTS=true` is set.

## Pagination and sorting

List, search and filter endpoints (members, authors, genres, books, editions, issues, admins) accept:

| Parameter | Default | Notes |
|---|---|---|
| `page` | `0` | Zero-based page index |
| `size` | `20` | Capped at `100` |
| `sort` | by ID | `field,asc` or `field,desc`; repeat for multiple fields. Issues default to newest first |

```bash
curl -s "http://localhost:8080/api/members?page=1&size=10&sort=lastName,asc" -H "Authorization: Bearer <token>"
```

```json
{
  "content": [ { "memberId": "MEM000011", "...": "..." } ],
  "page": 1,
  "size": 10,
  "totalElements": 46,
  "totalPages": 5,
  "first": false,
  "last": false
}
```

An unknown `sort` field returns `400`. `GET /api/configs` is not paged (there are only a handful of keys).

## API overview

All endpoints except `POST /api/auth/login` and the Swagger pages require `Authorization: Bearer <token>`.

| Resource | Base path | Highlights |
|---|---|---|
| Auth | `/api/auth` | `POST /login`, `POST /logout` |
| Admins | `/api/admins` | CRUD, search, lookup by username/email, exists checks |
| Members | `/api/members` | CRUD, search, filter by status, `PATCH /{id}/status` |
| Authors | `/api/authors` | CRUD, search, active/inactive lists, activate/deactivate |
| Genres | `/api/genres` | CRUD, search, lookup by name |
| Books | `/api/books` | CRUD, search, by author/genre, available, toggle availability |
| Book editions | `/api/book-editions` | create/get/update/delete, by book, by ISBN, available, adjust copies |
| Book issues | `/api/book-issues` | `POST /issue`, `POST /return`, `PATCH /{id}/lost`, by member, active, overdue, by status, `GET /can-issue` |
| System config | `/api/configs` | list, get/update by key, add/delete keys, re-seed |

The complete, interactive reference (request/response schemas and examples) is in Swagger UI.

## Default business rules

Seeded from `src/main/resources/config/seed-data/system-config.txt` on first start and editable afterwards through `/api/configs`:

| Key | Default |
|---|---|
| `MAX_ISSUE_DAYS` | 14 |
| `FINE_PER_DAY` | 25.00 |
| `GRACE_PERIOD_DAYS` | 0 |
| `MAX_BOOKS_PER_MEMBER` | 3 |
| `LOST_BOOK_MULTIPLIER` | 1.5 |
| `SERVICE_CHARGE` | 250.00 |

`MAX_RENEWAL_COUNT`, `RENEWAL_DAYS` and `MEMBERSHIP_FEE` are also seeded but not used yet (see below).

## Known limitations / roadmap

- Test coverage focuses on the circulation rules, authentication and security; the other CRUD modules (members, authors, genres, books, editions, configs) have no tests yet.
- Single `ADMIN` role; members are records, not system users.
- Book renewals and fine-payment flow are not implemented (the related config keys are reserved).
- A book becomes `OVERDUE` when it is returned late; there is no scheduled job marking still-borrowed books overdue.
- IDs are generated by reading the last ID and incrementing it, which could collide under heavy concurrent writes.
- Schema is managed by Hibernate (`ddl-auto=update`) rather than migrations such as Flyway.
- No refresh tokens; tokens simply expire.
