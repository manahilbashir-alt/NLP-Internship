# API Documentation — Smart Document Intelligence Backend

Base URL (local): `http://localhost:8080`
Interactive docs (Swagger UI): `http://localhost:8080/swagger-ui/index.html`

## Authentication

Most endpoints require a JWT bearer token. Get one from **Signup** or **Login**, then send it on every
protected request:

```
Authorization: Bearer <token>
```

Public endpoints (no token required): `POST /api/auth/signup`, `POST /api/auth/login`,
`GET /api/external-post`, `/ws`, `GET /actuator/health`, Swagger/OpenAPI routes.

Every other endpoint below requires the header. This matches `SecurityConfig.java`.

---

## Auth

### `POST /api/auth/signup`
Creates a new user account and returns a token.

Request body:
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "password123"
}
```
Response `200 OK`:
```json
{ "token": "<jwt>", "message": "Account created successfully" }
```
Response `400 Bad Request` — validation errors, or `404 Not Found` if the email is already registered
(the app currently returns 404 for this case via `GlobalExceptionHandler`'s generic `RuntimeException`
handler — see [Known Limitations](#known-limitations)).

### `POST /api/auth/login`
Authenticates an existing user.

Request body:
```json
{ "email": "jane@example.com", "password": "password123" }
```
Response `200 OK`:
```json
{ "token": "<jwt>", "message": "Login successful" }
```
Response `401 Unauthorized` on bad credentials.

---

## Documents (JWT required)

Documents are scoped to the authenticated user (`ownerEmail` is taken from the JWT subject, not the
request body — a client cannot read or edit another user's documents).

### `POST /api/documents`
Creates a document for the current user. Also triggers a call to `Notification-Service`
(fire-and-forget) via `NotificationClientService`.

Request body:
```json
{ "title": "My Notes", "content": "Some content here" }
```
Response `200 OK`: the created `Document` object.

### `GET /api/documents`
Returns all documents owned by the current user.

### `GET /api/documents/{id}`
Returns one document. Cached in Redis (`@Cacheable("documents")`, key `id + ownerEmail`).
Response `404 Not Found` if it doesn't exist or isn't owned by the current user.

### `PUT /api/documents/{id}`
Updates title/content of a document. Evicts the Redis cache entry for that document.

### `DELETE /api/documents/{id}`
Deletes a document. Evicts the Redis cache entry.

---

## External API (public)

### `GET /api/external-post`
Reactive proxy call (WebClient) to `https://jsonplaceholder.typicode.com/posts/1`. Returns
`Mono<String>`. Demonstrates the REST-client / reactive integration.

---

## Reactive (JWT required)

### `GET /api/reactive/message`
Returns a single reactive `Mono<String>` message.

### `GET /api/reactive/stream`
Returns a `Flux<String>` stream.

---

## Server-Sent Events (JWT required)

### `GET /api/events`
`text/event-stream` — emits a one-off `document-update` event confirming the backend is running.

### `GET /api/notifications/stream`
`text/event-stream` — emits a `message` event on connect, then a `document` event ~3 seconds later.
Useful to demonstrate a longer-lived SSE connection.

---

## WebSocket (public handshake at `/ws`)

STOMP over raw WebSocket (no SockJS fallback).

- Connect: `ws://localhost:8080/ws`
- Subscribe to: `/topic/messages`
- Send to: `/app/message` → handled by `WebSocketController#sendMessage`, broadcast back out on
  `/topic/messages`.

A working manual test page is included at the project root: `websocket-test.html`. Open it in a
browser (with the backend running) and use the **Connect → Subscribe → Send Message** buttons in
order.

---

## Kafka (JWT required)

### `POST /api/kafka/send?message=hello`
Publishes `message` to the `document-topic` Kafka topic. `KafkaConsumerService` logs it on
consumption (see application logs, `INFO` level).

---

## Microservice: Notification-Service

Runs separately on `http://localhost:8081`. See `Notification-Service/README.md`.

Called internally by `Spring-Backend`'s `NotificationClientService` whenever a document is created
(see `POST /api/documents` above). If the notification service is unreachable, document creation
still succeeds — the call is best-effort with a fallback (`onErrorReturn`).

### `GET /api/notifications?message=hello`
Public endpoint (no auth configured on this service). Echoes the message back — a placeholder for
real notification logic (email/push/etc.).

---

## Known limitations

- `GlobalExceptionHandler` maps every `RuntimeException` to `404 Not Found`, including cases that are
  really `400`/`409` (e.g. "email already registered" on signup, or an invalid document ID format).
  This is simple and consistent but not fully REST-accurate — worth calling out if your evaluator asks
  about HTTP status code correctness.
- `Notification-Service` doesn't persist notifications or send anything externally — it's a working
  skeleton, not a full notification pipeline. That's intentional for a training project.
