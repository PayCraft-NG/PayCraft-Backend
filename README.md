# PayCraft Backend

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

REST API for **PayCraft**, a payroll platform for employers: signup and onboarding, company and employee records, payroll runs, payouts, and virtual-account funding through **KoraPay**. This service exposes JSON APIs consumed by the PayCraft web app and integrates with KoraPay webhooks and optional USSD callbacks.

**Architecture:** see [ARCHITECTURE.md](ARCHITECTURE.md) for layers, security, data, and integrations.

---

## Table of contents

- [Architecture (detailed)](ARCHITECTURE.md)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Local setup](#local-setup)
- [Database migrations (Flyway)](#database-migrations-flyway)
- [Run the application](#run-the-application)
- [API documentation](#api-documentation)
- [Security model](#security-model)
- [HTTP API overview](#http-api-overview)
- [Project layout](#project-layout)
- [Docker](#docker)
- [Background jobs](#background-jobs)
- [Contributing](#contributing)
- [Related projects](#related-projects)

---

## Features

| Area | What the API supports |
|------|------------------------|
| **Authentication** | Employer login, JWT access tokens, refresh token flow (`/api/v1/auth`). |
| **Employers** | Registration, profile read/update/delete, password change (`/api/v1/employer`). |
| **Companies** | Create (during onboarding), list, current company details, update, delete (`/api/v1/company`). |
| **Employees** | CRUD for employees linked to the employer/company context (`/api/v1/employee`). |
| **Payroll** | Create payrolls, add/remove employees, update/delete, list, run payroll (`/api/v1/payroll`). |
| **Payments** | Single-employee payout, bulk payout by payroll id, bank list, payment verification (`api/v1/account/...`). |
| **Virtual account & funding** | VBA details, Kora transactions, paginated payments, bank transfer instructions, card funding, transfer verification, saved cards (`api/v1/account/...`). |
| **Webhooks** | KoraPay server-to-server events with signature verification (`POST /webhook`). |
| **USSD** | Telecom-style callback for basic flows (`/api/v1/ussd`); intentionally omitted from Swagger. |

Cross-cutting behavior includes **JPA auditing**, **global exception handling** with a consistent error JSON shape, **CORS** for configured front-end origins, and **Spring Boot Actuator** for operational endpoints.

---

## Tech stack

| Layer | Technologies |
|-------|----------------|
| **Runtime** | Java 17 |
| **Framework** | Spring Boot 3.3.3 (Web, Data JPA, Security, Validation, Mail, Actuator) |
| **Security** | Spring Security 6, stateless sessions, custom JWT filter, **JJWT** 0.12.x |
| **Persistence** | Spring Data JPA, Hibernate; **Flyway** (MySQL migrations); **MySQL** (dev/prod); **H2** for tests |
| **Documentation** | **springdoc-openapi** 2.6 (`/swagger-ui`, `/v3/api-docs`) |
| **Templating** | Thymeleaf (e.g. email or server-rendered content where used) |
| **Build** | Maven |

External integrations: **KoraPay** (payments, virtual accounts, webhooks), **Resend** (transactional email API).

---

## Prerequisites

- [JDK 17](https://openjdk.org/) (Temurin or another distribution)
- [Maven 3.8+](https://maven.apache.org/)
- [MySQL 8](https://dev.mysql.com/doc/) for the default **dev** profile
- Optional: [Docker](https://www.docker.com/) for container images

---

## Configuration

Spring loads `application.yml` and activates the profile from `spring.profiles.active` (default in repo: **`dev`**).

### Profiles

| Profile | Purpose | Server port (as configured) | Database |
|---------|---------|-----------------------------|----------|
| `dev` | Local development | **6020** (`application-dev.yml`) | MySQL on `localhost` |
| `prod` | Production | **6090** (`application-prod.yml`) | MySQL (host/credentials from env) |

There is no committed **`qa`** profile file in this repository; `application-qa.yml` is listed in `.gitignore` for local experiments (for example H2-only runs). You can add your own `application-qa.yml` if needed.

### Environment variables

Create a **`.env` file in the project root** for tools that load it, and export the same variables in your shell (or configure your IDE) before running. Spring reads **OS environment variables**; variable names must match those referenced in the YAML.

**Required for typical `dev` runs**

| Variable | Used for |
|----------|----------|
| `DATABASE_NAME` | MySQL database name (default in dev: `paycraft` if unset in JDBC URL) |
| `DATABASE_USERNAME` | MySQL user |
| `DATABASE_PASSWORD` | MySQL password (dev) |
| `SECRET_STRING` | JWT HMAC secret as **Base64** (see `JWTService`); must decode to sufficient key material |
| `KORA_SECRET_KEY` | KoraPay secret key |
| `KORA_PUBLIC_KEY` | KoraPay public key |
| `ENCRYPTION_KEY` | Application encryption material (see KoraPay / sensitive field usage) |

**Production (`prod`)**

| Variable | Notes |
|----------|--------|
| `DATABASE_PROD_PASSWORD` | Production DB password (`application-prod.yml`) |
| Same Kora/JWT/encryption vars as above | |

**Email (Resend)**

| Variable | Purpose |
|----------|---------|
| `RESEND_API_KEY` | Resend API key used by `EmailServiceImpl` |
| `EMAIL_SENDER` | Verified Resend sender address (`resend.from-email`) |

**Optional / defaults**

| Variable | Purpose |
|----------|---------|
| `FRONTEND_URL` | CORS and links (default in base config: `http://localhost:5173`) |
| `WEBHOOK_URL` | Webhook base URL (dev has a default placeholder in `application-dev.yml`) |
| `WEBHOOK_DEV_URL` | Dev webhook URL override |

Never commit real secrets. Keep `.env` local (it is gitignored).

---

## Local setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/PayCraft-NG/PayCraft-Backend.git
   cd PayCraft-Backend
   ```

2. **Create the MySQL database** (name should match `DATABASE_NAME`, e.g. `paycraft`):

   ```sql
   CREATE DATABASE paycraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'paycraft'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON paycraft.* TO 'paycraft'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Export environment variables** (example; adjust values):

   ```bash
   export DATABASE_NAME=paycraft
   export DATABASE_USERNAME=paycraft
   export DATABASE_PASSWORD=your_password
   # SECRET_STRING must be Base64 (e.g. openssl rand -base64 32)
   export SECRET_STRING="$(openssl rand -base64 32)"
   export KORA_SECRET_KEY=your_kora_secret
   export KORA_PUBLIC_KEY=your_kora_public
   export ENCRYPTION_KEY=your_32_char_compatible_key_material
   export RESEND_API_KEY=re_xxxxxxxxxxxxxxxxx
   export EMAIL_SENDER=onboarding@resend.dev
   export FRONTEND_URL=http://localhost:5173
   ```

4. **First build**

   ```bash
   mvn clean verify
   ```

---

## Database migrations (Flyway)

- **Scripts**: [`src/main/resources/db/migration/`](src/main/resources/db/migration/) — MySQL `V1__initial_schema.sql`. **PostgreSQL** (Docker Compose): [`db/migration/postgresql/V1__initial_schema.sql`](src/main/resources/db/migration/postgresql/V1__initial_schema.sql), applied when profile **`docker`** is active (see [`application-docker.yml`](src/main/resources/application-docker.yml)).
- **Runtime**: Flyway runs on startup before JPA; Hibernate **`ddl-auto` is `validate`** so the live schema must match entities and applied migrations.
- **New database**: empty schema → Flyway applies `V1` and creates all tables.
- **Existing database** (already created with older Hibernate `ddl-auto: update`): `spring.flyway.baseline-on-migrate: true` and `baseline-version: 1` record the current state as version **1** without re-running `V1`, so you avoid “table already exists” errors. Add **`V2__...sql`** (and later) for real changes going forward.
- **Tests**: profile `test` disables Flyway and uses H2 with `ddl-auto: create-drop` (see [`application-test.yml`](src/test/resources/application-test.yml)).

To generate a new Base64 JWT secret locally:

```bash
openssl rand -base64 32
```

---

## Run the application

```bash
# Default profile is dev (see application.yml)
mvn spring-boot:run
```

Other useful forms:

```bash
# Explicit profile
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# After mvn package
java -jar target/paycraft-0.0.1-SNAPSHOT.jar
```

- **Dev**: API base URL typically `http://localhost:6020`
- **Prod** (when using `prod` profile): default port **6090** per `application-prod.yml`

---

## API documentation

When the app is running:

| Resource | URL (dev, port 6020) |
|----------|----------------------|
| Swagger UI | [http://localhost:6020/swagger-ui/index.html](http://localhost:6020/swagger-ui/index.html) |
| OpenAPI JSON | [http://localhost:6020/v3/api-docs](http://localhost:6020/v3/api-docs) |

Use **Authorize** in Swagger with a Bearer token from `POST /api/v1/auth/login`.

**Postman:** import [`postman/PayCraft-Backend.postman_collection.json`](postman/PayCraft-Backend.postman_collection.json) (collection variables `baseUrl`, `employerId`, `employeeId`, `payrollId`; login **Tests** script stores `accessToken` / `refreshToken`).

---

## Security model

- **JWT** in the `Authorization: Bearer <token>` header for protected routes.
- **Public** (no JWT) routes include, among others:
  - `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh-token`
  - `POST /api/v1/employer/create`
  - `POST /api/v1/company/create` (with `employerId` query param)
  - `POST /webhook` (KoraPay; validates `X-Korapay-Signature`)
  - `api/v1/ussd` (GET/POST)
  - Swagger and OpenAPI endpoints under `/swagger-ui/**` and `/v3/api-docs/**`
- All other API paths require authentication.

Exact rules live in [`SecurityConfig.java`](src/main/java/com/aalto/paycraft/config/SecurityConfig.java).

---

## HTTP API overview

Base paths are shown as implemented in controllers (some use a leading `/`, some rely on Spring’s path normalization).

| Area | Base path | Notes |
|------|-----------|--------|
| Auth | `/api/v1/auth` | Login, refresh |
| Employer | `/api/v1/employer` | Signup + authenticated profile |
| Company | `/api/v1/company` | Companies for the logged-in employer |
| Employee | `api/v1/employee` | Employee CRUD |
| Payroll | `api/v1/payroll` | Payroll lifecycle and run |
| Account / payments / VBA | `api/v1/account` and `api/v1/account/` | Payouts, banks, verify, VBA, transfers, cards |
| Webhook | `/webhook` | KoraPay callback |
| USSD | `/api/v1/ussd` | External USSD gateway callback |

---

## Project layout

```text
src/main/java/com/aalto/paycraft/
├── api/            # USSD entrypoints
├── audit/          # JPA auditing
├── config/         # Security, OpenAPI, beans
├── constants/
├── controller/     # REST controllers
├── dto/
├── entity/
├── exception/
├── mapper/
├── repository/
└── service/        # Interfaces + implementations

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── db/migration/   # Flyway versioned migrations
├── schema/         # Legacy sample SQL (superseded by Flyway)
└── templates/      # Thymeleaf templates
```

---

## Docker

### Docker Compose (PostgreSQL + API)

[`docker-compose.yml`](docker-compose.yml) starts **PostgreSQL 16** and the app with `SPRING_PROFILES_ACTIVE=docker`. The app uses JDBC to the `postgres` service, runs Flyway from `classpath:db/migration/postgresql`, and listens on **6020**.

```bash
docker compose up --build
```

API: [http://localhost:6020](http://localhost:6020). Postgres is published on **5432** by default (override with `POSTGRES_PORT`).

Set real secrets via a `.env` file or your shell (at minimum **`SECRET_STRING`** as Base64, and Kora/email variables for full behavior). The compose file includes **development-only placeholders** so the stack can start without a `.env`.

### Image build (multi-stage Dockerfile)

The [`Dockerfile`](Dockerfile) builds the JAR with **Maven** inside the image, then runs a slim **JRE 17** image (no local `mvn package` required):

```bash
docker build -t paycraft-backend:local .
```

To run that image against your own PostgreSQL (same env vars as in compose, plus `SPRING_PROFILES_ACTIVE=docker` and a reachable `POSTGRES_HOST`):

```bash
docker run --rm -p 6020:6020 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e POSTGRES_HOST=host.docker.internal \
  -e DATABASE_USERNAME=paycraft \
  -e DATABASE_PASSWORD=paycraft \
  -e SECRET_STRING="$(openssl rand -base64 32)" \
  paycraft-backend:local
```

[`build_script.sh`](build_script.sh) is an older flow that assumes a **pre-built JAR** in `target/`; prefer `docker compose build` or `docker build` above unless you still push to a registry from that script.

---

## Background jobs

Automatic payrolls are driven by **`PayrollJobService`**: on startup it loads payrolls marked automatic from the database and schedules them with Spring’s **`TaskScheduler`** using each payroll’s **cron expression**. Creating or updating an automatic payroll can register or refresh that schedule.

`application.yml` contains a `payroll.job.fixedRate` property; it is **not referenced** by the current Java scheduling code—treat it as unused or reserved unless you wire it in later.

---

## Contributing

1. Fork the repository and create a branch for your change.
2. Run `mvn verify` before opening a pull request.
3. Keep API changes documented via OpenAPI annotations and Swagger where appropriate.
4. Open a pull request with a clear description of behavior and any new configuration.

---

## Related projects

- [PayCraft Frontend](https://github.com/PayCraft-NG/PayCraft-Frontend) — web client for this API.

---

## License

This project is licensed under the terms in [LICENSE](LICENSE).

---

*PayCraft — payroll flows for employers, with KoraPay-backed funding and payouts.*
