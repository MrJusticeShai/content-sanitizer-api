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

### Local Build & Run

```bash
# Clone the repository
git clone <repository-url>
cd content-sanitizer

# Build the project
mvn clean package

# Run the Spring Boot app
java -jar target/content-sanitizer-0.0.1-SNAPSHOT.jar
```

### Docker Compose (Recommended)
```bash
# Start the app and database
docker-compose up -d
```

> **First run only** — SQL Server does not create the application database automatically.
> Once the `sqlserver` container is healthy, run:
```bash
docker exec -it sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'your_password' \
  -Q 'CREATE DATABASE sensitivewords' \
  -C
```

> Then restart the app container so it can connect:
```bash
docker-compose restart sanitizer-api
```
```bash
# Stop containers
docker-compose down
```

Spins up:
- `sqlserver` — SQL Server 2022 container with a persistent volume
- `content-sanitizer` — Spring Boot API container
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
version: '3.8'

services:
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: sqlserver
    environment:
      SA_PASSWORD: ${DB_PASS}
      ACCEPT_EULA: "Y"
    ports:
      - "1433:1433"
    volumes:
      - sqlserver_data:/var/opt/mssql
    healthcheck:
      test: ["CMD-SHELL", "/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P ${DB_PASS} -Q 'SELECT 1'"]
      interval: 10s
      timeout: 5s
      retries: 10

  content-sanitizer:
    build: .
    container_name: content-sanitizer
    env_file:
      - .env
    environment:
      DB_HOST: sqlserver
      DB_PORT: 1433
      DB_NAME: sensitivewords
      DB_USER: sa
      DB_PASS: ${DB_PASS}
    depends_on:
      sqlserver:
        condition: service_healthy
    ports:
      - "8080:8080"

volumes:
  sqlserver_data:
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
- [] **Distributed cache (Redis)** — Replace the local `AtomicReference` cache with a Redis-backed store so that all horizontal replicas share one consistent word list. Add `spring-boot-starter-data-redis` and annotate with `@Cacheable` / `@CacheEvict`.
- [] **Scheduled auto-refresh** — Add `@Scheduled(fixedRateString = "${sanitizer.cache.refresh-interval:300000}")` to `refreshCache()` to periodically sync the cache with the DB without manual intervention.

### Sanitization Performance

- [x] **Single-pass string replacement** — Each pattern iterates through the message once; no redundant scanning.
- [] **Aho-Corasick multi-pattern matching** — For large word lists (1 000+), replace sequential `Pattern` iteration with an [Aho-Corasick](https://github.com/robert-bor/aho-corasick) automaton. This reduces time complexity from O(n × m) to O(n + m + z), where z is the number of matches.
- [] **Parallel sanitization** — For very long messages, split into chunks and apply patterns in parallel using `ForkJoinPool` or a structured `CompletableFuture` pipeline.
- [] **Lookahead/lookbehind boundary matching** — The current implementation uses `\b` word boundaries
    which break down for sensitive words ending in non-word characters (e.g. `c++`, `f#`, `100%`).
    These words are protected from `PatternSyntaxException` via `Pattern.quote()` but will not be
    masked in practice.

### Database & Persistence

- [] **Indexed `word` column** — Ensure a unique, case-insensitive index on the `word` column (`CREATE UNIQUE INDEX ... ON sensitive_words (LOWER(word))`), so duplicate checks are handled at the DB level, not just application level.
- [] **Bulk word import endpoint** — Add `POST /api/internal/sensitive-words/bulk` accepting a JSON array of words, processed in a single transaction with `saveAll()`, to avoid N+1 insert overhead.
- [] **Soft deletes** — Add an `active` boolean flag instead of hard-deleting rows, allowing audit history and easier rollback via an `/api/internal/sensitive-words/{word}/restore` endpoint.
- [] **Optimistic locking** — Add `@Version` to the `SensitiveWord` entity to guard against concurrent update conflicts.

### API & Observability

- [] **Pagination on `GET /api/words`** — Return a `Page<WordDTO>` using `Pageable` to avoid loading the full word list into the HTTP response for large datasets.
- [] **Rate limiting** — Apply `Bucket4j` or Spring Cloud Gateway rate limiting on `/api/sanitize` to prevent abuse and protect throughput SLAs.
- [] **Actuator + Micrometer metrics** — Expose `sanitize.requests.count`, `sanitize.duration`, and `cache.size` as Micrometer gauges/counters, scraped by Prometheus and visualised in Grafana.
- [] **Structured logging** — Replace plain log strings with structured JSON logs (Logstash encoder) so log aggregators (ELK, Loki) can filter by `wordCount`, `messageLength`, and `cacheMiss` fields.
- [] **Correlation IDs** — Propagate an `X-Request-ID` header through MDC so all log lines for a single request are traceable end-to-end.

### Resilience & Security

- [] **Input length guard** — Reject messages exceeding a configurable maximum length (e.g. 10 000 characters) at the DTO validation layer to prevent regex catastrophic backtracking on adversarial input.
- [] **ReDoS protection** — Audit all compiled patterns; ensure `Pattern.quote()` is always used for user-supplied words (already done) and add a CI lint step to catch unsafe patterns introduced in future.
- [] **API key / JWT authentication** — Secure the management endpoints (`/api/internal/sensitive-words/**`) behind Spring Security with role-based access (`ROLE_ADMIN`), leaving `/api/sanitize` optionally open for service-to-service calls.
- [] **Integration test coverage** — Add `@SpringBootTest` + Testcontainers (SQL Server image) tests for the full sanitize-and-refresh cycle, ensuring cache consistency is validated against a real database.
