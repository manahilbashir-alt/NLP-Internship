# Notification Service

A small, independently deployable Spring Boot microservice, called by `Spring-Backend` whenever a
document is created. See `../docs/ARCHITECTURE.md` for how it fits into the overall system.

## Running

```bash
./mvnw spring-boot:run
```

Runs on `http://localhost:8081`.

## Endpoint

### `GET /api/notifications?message=<text>`

Echoes the message back, prefixed with `"Notification Service received: "`. This stands in for
real notification logic (email, push, SMS, etc.) — the point of this service is to demonstrate
service-to-service communication, not to be a full notification system.

Example:
```bash
curl "http://localhost:8081/api/notifications?message=Hello"
```

## Postman

Import `Postman_Notification_Service_Collection.json` — sets `notificationBaseUrl` to
`http://localhost:8081` by default.

## Health check

`GET /actuator/health` (enabled via `management.endpoints.web.exposure.include=health,info`).

## Called from

`Spring-Backend`'s `NotificationClientService.notify(...)`, triggered from
`DocumentService.createDocument()`. If this service is down, document creation in `Spring-Backend`
still succeeds — the call has an error fallback and does not block the response.
