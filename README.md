# 🛡️ Content Sanitizer API

A robust RESTful microservice built as part of a Flash technical assessment. The service masks sensitive words in client messages and provides internal CRUD management for the sensitive words list.

---

## 🛠️ Technology Stack

| Component | Version / Detail | Purpose |
|---|---|---|
| Backend Framework | Spring Boot 3.x | Rapid application development |
| Language | Java 17 | Core business logic |
| Build Tool | Maven | Project lifecycle management |
| Database | Microsoft SQL Server 2022 | Persistent storage for sensitive words |
| API Documentation | Swagger UI | Interactive API exploration |
| Containerization | Docker & Docker Compose | Consistent dev/prod environments |

---

## 🎯 Key Features

- **Message Sanitization** — Replaces sensitive words with asterisks, preserving the original word length.
- **RESTful CRUD Operations** — Full lifecycle management of the sensitive words list.
- **Data Validation** — Unique words enforced (case-insensitive); required fields validated via DTOs.
- **Case-Insensitive Matching** — Sanitization works regardless of input casing.
- **Thread-Safe In-Memory Cache** — `AtomicReference`-backed pattern cache for low-latency sanitization, with a `/refresh` endpoint for live reloading without restart.

---

## 🚀 Getting Started

This service requires a running SQL Server instance. The recommended approach for both
local development and production is Docker Compose, which manages the database and
application containers together.

---

### Docker Compose (Recommended)

**1. Create your environment file from the example:**
```bash
cp .env.example .env
```

Edit `.env` and set a secure password for `DB_PASS`.

**2. Start the containers:**
```bash
docker-compose up -d
```

**3. First run only — create the database:**

SQL Server does not create the application database automatically. Once the `sqlserver`
container is healthy, run:
```bash
docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'your_password' \
  -Q 'CREATE DATABASE sensitivewords' \
  -C
```

Replace `your_password` with the value you set in `.env`.

**4. Restart the app so it can connect:**
```bash
docker-compose restart sanitizer-api
```

The API is now available at `http://localhost:8080/swagger-ui.html`.

**To stop the containers:**
```bash
docker-compose down
```

---

### Local Build & Run (Development)

> **Note:** This approach is for local development only. The Docker Compose approach
> is recommended for production and for first-time setup.

**Prerequisite:** SQL Server must be running and the `sensitivewords` database must
exist before starting the app. The easiest way is to start just the database container:
```bash
docker-compose up -d sqlserver
```

Wait for it to become healthy, then create the database if it does not exist yet:
```bash
docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'your_password' \
  -Q 'CREATE DATABASE sensitivewords' \
  -C
```

**Clone and build:**
```bash
git clone https://github.com/MrJusticeShai/content-sanitizer-api.git
cd content-sanitizer-api

mvn clean package -DskipTests
```

**Run the app:**
```bash
java -jar target/content-sanitizer-api-0.0.1-SNAPSHOT.jar
```

---

### Common Errors

**`Cannot open database "sensitivewords" requested by the login`**

The database has not been created yet. Run the `CREATE DATABASE` command above and
restart the app.

**`Connection refused` on port 1433**

SQL Server is not running. Start the database container first:
```bash
docker-compose up -d sqlserver
```

**`Connection reset` / SSL certificate error**

The JDBC driver is attempting an encrypted connection and rejecting the self-signed
certificate. Ensure `encrypt=false` is included in the datasource URL as shown above.

---

### Development vs Production

| | Local Development | Production |
|---|---|---|
| Database | SQL Server in Docker on `localhost` | Managed SQL Server (e.g. Azure SQL, AWS RDS) |
| Credentials | `.env` file | Environment variables injected by the platform (e.g. Azure App Service, Kubernetes secrets) |
| Database creation | Manual `CREATE DATABASE` via `sqlcmd` | Provisioned by infrastructure or migration tooling (e.g. Flyway) |
| SSL encryption | `encrypt=false` for self-signed cert | `encrypt=true` with a valid certificate |
| App startup | `java -jar` or `docker-compose up` | Container orchestration (e.g. Kubernetes, Azure Container Apps) |

---
## 💻 API Exploration

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

All endpoints are documented with request/response schemas and example payloads.

### SQL Server Connection

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:sqlserver://localhost:1433;databaseName=sensitivewords` |
| Username | `sa` |
| Password | *(from `.env`)* |

Data persists in a named Docker volume — SQL Server is **not** ephemeral like H2.

---

## 📦 API Endpoints

| Endpoint                                      | Method | Purpose | HTTP Statuses |
|-----------------------------------------------|---|---|---|
| `/api/internal/sensitive-words`               | `GET` | List all sensitive words | `200` |
| `/api/internal/sensitive-words`               | `POST` | Add a new sensitive word | `201`, `400`, `409` |
| `/api/internal/sensitive-words`               | `PUT` | Update an existing word | `200`, `400`, `404`, `409` |
| `/api/internal/sensitive=words/{word}`        | `DELETE` | Delete a sensitive word | `204`, `404` |
| `/api/sanitize`                               | `POST` | Sanitize a client message | `200`, `400` |
| `/api/internal/sensitive-words/cache/refresh` | `POST` | Reload sanitization cache live | `200` |

### Example Payloads

**Add Sensitive Word:**
```json
{ "word": "DROP" }
```

**Sanitize Message:**
```json
{ "message": "Please DROP the table" }
```

**Response:**
```json
{ "sanitizedMessage": "Please **** the table" }
```

---

## ⚡ cURL Quick Reference

```bash
# Add a word
curl -X POST http://localhost:8080/api/internal/sensitive-words \
  -H "Content-Type: application/json" \
  -d '{"word":"DROP"}'

# List all words
curl http://localhost:8080/api/internal/sensitive-words

# Update a word
curl -X PUT http://localhost:8080/api/internal/sensitive-words \
  -H "Content-Type: application/json" \
  -d '{"currentWord":"DELETE",
       "newWord": "DELETION"
      }'

# Delete a word
curl -X DELETE http://localhost:8080/api/internal/sensitive-words/CHECK

# Sanitize a message
curl -X POST http://localhost:8080/api/sanitize \
  -H "Content-Type: application/json" \
  -d '{"message":"Please DELETE all data"}'

# Refresh the in-memory cache
curl -X POST http://localhost:8080/api/words/cache/refresh
```

---

## 🔐 Credentials Management

- All sensitive values (DB credentials) are stored in `.env` and injected via Spring Boot property placeholders.
- **Never commit `.env` to source control.** Use `.env.example` as a committed reference template.

`.env.example`:
```env
DB_PASS=your_secure_password_here
```

---

## 🐳 Docker Configuration

**`Dockerfile`:**
```dockerfile
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/content-sanitizer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**`docker-compose.yml`:**
```yaml
services:
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: sqlserver
    ports:
      - "1433:1433"
    environment:
      ACCEPT_EULA: "Y"
      SA_PASSWORD: "${DB_PASS}"
    networks:
      - app-network
    healthcheck:
      test: ["CMD-SHELL", "/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P ${DB_PASS} -Q 'SELECT 1' -No"]
      interval: 15s
      timeout: 10s
      retries: 10
      start_period: 30s

  sanitizer-api:
    build: .
    container_name: sanitizer-api
    ports:
      - "8080:8080"
    env_file:
      - .env
    environment:
      DB_HOST: sqlserver
      DB_PORT: 1433
      DB_NAME: sensitivewords
      DB_USER: sa
      DB_PASS: "${DB_PASS}"
    depends_on:
      sqlserver:
        condition: service_healthy
    networks:
      - app-network
    restart: on-failure

networks:
  app-network:
    driver: bridge
```

> **Note:** The `healthcheck` uses the actual `sqlcmd` tool rather than `curl`, which is not available in the SQL Server image. The `depends_on` condition is upgraded to `service_healthy` so the app waits for SQL Server to be fully ready before starting.

---

## ⚡ Performance & Enhancements

The following checklist outlines production-grade improvements beyond the base implementation. Items marked ✅ are already implemented; items marked 🔲 are recommended next steps.

### Caching

- [x] **Thread-safe in-memory pattern cache** — `AtomicReference<List<Pattern>>` ensures atomic swaps with no locking overhead on reads.
- [x] **Compiled regex patterns** — `Pattern` objects are pre-compiled at load time, not per request.
- [x] **Live cache refresh endpoint** — `POST /api/internal/sensitive-words/cache/refresh` reloads patterns without a service restart.
- [x] **Resilient refresh** — If the DB call fails during refresh, the existing cache is retained rather than replaced with an empty list.
- [ ] **Distributed cache (Redis)** — Replace the local `AtomicReference` cache with a Redis-backed store so that all horizontal replicas share one consistent word list.

### Sanitization Performance

- [x] **Single-pass string replacement** — Each pattern iterates through the message once; no redundant scanning.
- [ ] **Aho-Corasick multi-pattern matching** — For large word lists (1 000+), replace sequential `Pattern` iteration with an [Aho-Corasick](https://github.com/robert-bor/aho-corasick) automaton. This reduces time complexity from O(n × m) to O(n + m + z), where z is the number of matches.
- [ ] **Parallel sanitization** — For very long messages, split into chunks and apply patterns in parallel using `ForkJoinPool` or a structured `CompletableFuture` pipeline.
- [ ] **Lookahead/lookbehind boundary matching** — The current implementation uses `\b` word boundaries
    which break down for sensitive words ending in non-word characters (e.g. `c++`, `f#`, `100%`).
    These words are protected from `PatternSyntaxException` via `Pattern.quote()` but will not be
    masked in practice.

### Database & Persistence

- [x] **Indexed `word` column** — Ensure a unique, case-insensitive index on the `word` column (`CREATE UNIQUE INDEX ... ON sensitive_words (LOWER(word))`), so duplicate checks are handled at the DB level, not just application level.
- [ ] **Bulk word import endpoint** — Add `POST /api/internal/sensitive-words/bulk` accepting a JSON array of words, processed in a single transaction with `saveAll()`, to avoid N+1 insert overhead.
- [ ] **Soft deletes** — Add an `active` boolean flag instead of hard-deleting rows, allowing audit history and easier rollback via an `/api/internal/sensitive-words/{word}/restore` endpoint.

### API & Observability
- [ ] **Rate limiting** — Apply `Bucket4j` or Spring Cloud Gateway rate limiting on `/api/sanitize` to prevent abuse and protect throughput SLAs.
- [ ] **Actuator + Micrometer metrics** — Expose `sanitize.requests.count`, `sanitize.duration`, and `cache.size` as Micrometer gauges/counters, scraped by Prometheus and visualised in Grafana.

### Resilience & Security

- [ ] **Input length guard** — Reject messages exceeding a configurable maximum length (e.g. 10 000 characters) at the DTO validation layer to prevent regex catastrophic backtracking on adversarial input.
- [ ] **API key / JWT authentication** — Secure the management endpoints (`/api/internal/sensitive-words/**`) behind Spring Security with role-based access (`ROLE_ADMIN`), leaving `/api/sanitize` optionally open for service-to-service calls.
- [ ] **Integration test coverage** — Add `@SpringBootTest` + Testcontainers (SQL Server image) tests for the full sanitize-and-refresh cycle, ensuring cache consistency is validated against a real database.

### Configuration & Deployment

- [ ] **Spring profile separation** — Introduce `application-dev.yml` and `application-prod.yml`
  profiles to scope environment-specific configuration. `show-sql: true` and verbose logging
  would be restricted to the `dev` profile only, ensuring clean, secure defaults in production.
  The active profile would be injected at runtime via `--spring.profiles.active=prod` or
  an environment variable, removing the need to change committed configuration between environments.
- [ ] **Automated database initialisation** — The `sensitivewords` database currently requires
  manual creation via `sqlcmd` on first run. In production this would be handled by a Flyway
  migration or an init SQL script mounted into the SQL Server container, making setup fully
  automated and repeatable with no manual steps.
- [ ] **Scheduled cache auto-refresh** — Add `@Scheduled(fixedRateString = "${sanitizer.cache.refresh-interval:300000}")`
  to `refreshCache()` to periodically sync the in-memory cache with the database without
  manual intervention or a service restart.
- [ ] **CI/CD pipeline** — Introduce a GitHub Actions pipeline to automate the build, test,
    and deployment lifecycle. A typical pipeline would consist of three stages:
- [ ] **Build & Test** — `mvn clean package` + unit tests on every push and pull request,
  ensuring no broken code reaches the main branch.
- [ ] **Docker Build & Push** — Build the Docker image and push to a container registry
  (e.g. Docker Hub, Azure Container Registry) on merge to main.
- [ ] **Deploy** — Pull the latest image and redeploy the container to the target environment
  (e.g. Azure Container Apps, AWS ECS, Kubernetes) with zero-downtime rolling updates.

### Infrastructure

The service is fully containerised and designed to run on any container orchestration
platform. The recommended production stack is:

| Component | Production Choice | Rationale |
|---|---|---|
| Container orchestration | Kubernetes or Azure Container Apps | Auto-scaling, rolling updates, health-based restarts |
| Database | Azure SQL or AWS RDS (SQL Server) | Managed, highly available, automated backups |
| Container registry | Azure Container Registry or Docker Hub | Stores versioned Docker images built by CI/CD |
| Secrets management | Azure Key Vault or AWS Secrets Manager | Credentials never stored in code or environment files |
| Reverse proxy / gateway | Azure API Management or AWS API Gateway | Rate limiting, authentication, request routing |

---

### Deployment Flow
```
Developer pushes to main
        │
        ▼
GitHub Actions — Build & Test
  mvn clean package + unit tests
        │
        ▼
GitHub Actions — Docker Build & Push
  docker build → push to container registry
        │
        ▼
GitHub Actions — Deploy
  pull latest image → rolling update
        │
        ▼
Container platform pulls new image
  zero-downtime rolling deployment
  health checks verify startup before
  old containers are terminated
```

---