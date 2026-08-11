# Setup Guide

Two ways to run this: **Docker Compose** (easiest — brings up everything) or **manual/local**
(more control, useful while developing).

## Option A — Docker Compose (recommended)

Requires Docker Desktop (or Docker Engine + Compose plugin) only. No local Java/Postgres/Redis/Kafka
install needed.

```bash
cd Spring-Backend

# Package both services first so their jars exist for the Dockerfiles
./mvnw clean package -DskipTests
cd ../Notification-Service
./mvnw clean package -DskipTests
cd ../Spring-Backend

# Bring up everything: Postgres, Redis, Kafka, Notification-Service, Spring-Backend
docker-compose up --build
```

This starts:

| Service | Port |
|---|---|
| Spring-Backend | 8080 |
| Notification-Service | 8081 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Kafka | 9092 |

Check it's up: `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`

Swagger UI: http://localhost:8080/swagger-ui/index.html

Stop everything: `docker-compose down` (add `-v` to also wipe the Postgres volume).

## Option B — Run locally without Docker

You'll need locally running: PostgreSQL 16, Redis 7, Kafka (with a broker on `localhost:9092`).

1. Create the database:
   ```sql
   CREATE DATABASE smart_document_db;
   ```
   Matches `spring.datasource.username=postgres` / `password=123` in
   `Spring-Backend/src/main/resources/application.properties` — change both if your local Postgres
   uses different credentials.

2. Start Redis and Kafka locally (or via standalone Docker containers, e.g.
   `docker run -p 6379:6379 redis:7`).

3. Run the backend:
   ```bash
   cd Spring-Backend
   ./mvnw spring-boot:run
   ```
   Runs on `http://localhost:8080`.

4. In a second terminal, run the notification service:
   ```bash
   cd Notification-Service
   ./mvnw spring-boot:run
   ```
   Runs on `http://localhost:8081`.

## Running the automated tests

Tests use an isolated in-memory H2 database (`src/test/resources/application-test.properties`), so
**no external services are required** to run them:

```bash
cd Spring-Backend
./mvnw clean test
```

This is also exactly what `.github/workflows/ci.yml` runs on every push/PR.

## Trying it out with Postman

1. Import `Spring-Backend/Postman_Complete_Backend_Collection.json` and
   `Notification-Service/Postman_Notification_Service_Collection.json`.
2. Set the collection variable `baseUrl` to `http://localhost:8080` (already the default).
3. Run **Authentication → Signup** (or **Login**), copy the `token` from the response.
4. Set the collection variable `token` to that value — every protected request already sends
   `Authorization: Bearer {{token}}`.
5. Run the rest of the requests in any order (Documents, Kafka, Reactive, SSE).

## Trying the WebSocket manually

With `Spring-Backend` running, open `websocket-test.html` (project root) directly in a browser.
Click **Connect**, then **Subscribe**, then **Send Message** — you should see the message echoed
back via `/topic/messages`.

## Troubleshooting

- **`Connection refused` to Postgres/Redis/Kafka** — they're not running yet, or not on the
  expected port. If using Docker Compose, check `docker-compose ps`.
- **Batch job errors about multiple `Job` beans** — make sure you're on the latest source; an
  earlier version of this project had a duplicate `Job` bean across two config classes, which has
  since been removed.
- **401 Unauthorized on a protected route** — check the `Authorization` header is present and the
  token hasn't expired (24h lifetime, see `JwtService`).
