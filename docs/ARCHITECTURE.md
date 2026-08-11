# Architecture

## System overview

```
                     ┌────────────────────────────┐
                     │        Client (Postman /    │
                     │    browser / websocket-test) │
                     └───────────────┬──────────────┘
                                     │ HTTP / WS / SSE
                                     ▼
                     ┌────────────────────────────┐
                     │      Spring-Backend :8080    │
                     │  (Smart Document Backend)    │
                     ├────────────────────────────┤
                     │ AuthController               │
                     │ DocumentController            │
                     │ ExternalApiController         │
                     │ ReactiveController             │
                     │ SseController                  │
                     │ NotificationController (SSE)   │
                     │ KafkaController                │
                     │ WebSocketController             │
                     └───┬───────┬───────┬────────┬──┘
                         │       │       │        │
              JDBC       │  Redis│  Kafka│  WebClient (REST)
                         ▼       ▼       ▼        ▼
                   ┌──────────┐ ┌─────┐ ┌───────┐ ┌───────────────────┐
                   │PostgreSQL│ │Redis│ │ Kafka │ │Notification-Service│
                   │  :5432   │ │:6379│ │ :9092 │ │       :8081        │
                   └──────────┘ └─────┘ └───────┘ └───────────────────┘
```

## Why each piece exists

| Concern | Component | Notes |
|---|---|---|
| Persistence | Spring Data JPA + PostgreSQL | `User`, `Document` entities |
| Auth | Spring Security + JWT (`jjwt`) | Stateless, `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter` |
| Validation | Jakarta Bean Validation | `@Valid` on request DTOs, errors mapped centrally |
| Error handling | `GlobalExceptionHandler` (`@RestControllerAdvice`) | Maps `RuntimeException` → 404, `MethodArgumentNotValidException` → 400 |
| Caching | Spring Cache + Redis | `@Cacheable`/`@CacheEvict` on `DocumentService` |
| Reactive | Spring WebFlux (`Mono`/`Flux`) | `ReactiveController`, `ExternalApiService` |
| Real-time push | SSE (`SseEmitter`) + WebSocket (STOMP) | Two different real-time mechanisms, deliberately both included to demonstrate each |
| Messaging | Spring Kafka | Producer (`KafkaProducerService`) + Consumer (`KafkaConsumerService`) on `document-topic` |
| Scheduling | `@Scheduled` | `ScheduledTaskService`, `ScheduledDocumentService` — periodic background logging |
| Batch | Spring Batch | One `Job`/`Step` pair (`BatchConfig`) — a placeholder tasklet that logs and finishes |
| Microservices | Separate Maven project | `Notification-Service`, called over HTTP via `WebClient` |
| API docs | springdoc-openapi | Auto-generated Swagger UI |
| Containerization | Docker + docker-compose | Both services + their infra (Postgres/Redis/Kafka) in one compose file |
| CI | GitHub Actions | Builds and runs tests on every push/PR |

## Request flow: creating a document

1. Client sends `POST /api/documents` with a JWT.
2. `JwtAuthenticationFilter` validates the token and puts an `Authentication` into the
   `SecurityContext` (email as principal).
3. `DocumentController` reads the owner email from `Authentication`, not from the request body —
   this is what prevents one user from creating documents "as" another user.
4. `DocumentService.createDocument()` saves the entity via `DocumentRepository`, then calls
   `NotificationClientService.notify(...)`, which makes a **real HTTP call** to
   `Notification-Service` (`:8081`) using `WebClient`. This call is fire-and-forget
   (`.subscribe()`) with an error fallback, so a notification-service outage never breaks document
   creation.

## Package layout (`Spring-Backend`)

```
smart.document.backend
├── config/        Spring configuration (@Configuration classes): security, cache, kafka,
│                  batch, websocket, webclient, openapi
├── controller/    REST/WebSocket entry points — thin, delegate to services
├── dto/           Request/response shapes, decoupled from entities
├── entity/        JPA entities
├── exception/     Centralized error handling
├── repository/    Spring Data JPA repositories
├── security/      JWT filter, JWT service, UserDetailsService
└── service/       Business logic
```

This is a standard layered architecture (controller → service → repository), which is why adding
the `NotificationClientService` fit naturally as another service dependency of `DocumentService`
rather than requiring controller changes.

## Two independent Spring Boot processes

`Spring-Backend` and `Notification-Service` are **separate Maven projects**, each with its own
`pom.xml`, and each independently runnable/deployable — that's what makes this a microservices
example rather than just a modular monolith. They communicate only over HTTP, never share a
database or JVM.
