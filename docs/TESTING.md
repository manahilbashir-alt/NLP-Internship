# Testing

## Running the tests

```bash
cd Spring-Backend
./mvnw clean test
```

No external services needed — tests run against an isolated in-memory H2 database via the `test`
Spring profile (`src/test/resources/application-test.properties`), so this works the same on your
machine and in CI.

## What's covered

| Test class | Type | What it checks |
|---|---|---|
| `SmartDocumentBackendApplicationTests` | `@SpringBootTest` (context test) | The full application context wires up correctly — every bean, every `@Configuration` class, no missing dependencies |
| `JwtServiceTest` | Unit test | Token generation, username extraction, token validity for the correct user, invalidity for a mismatched user |
| `DocumentServiceTest` | Unit test (Mockito) | Document creation persists via the repository *and* triggers a call to `NotificationClientService`; `getDocument` throws when the document doesn't exist; `getUserDocuments` filters correctly |
| `AuthServiceTest` | Unit test (Mockito) | Signup is rejected when the email is already registered |

## What's intentionally not covered yet

This is a training project, not a production codebase, so test depth is deliberately proportionate.
If your evaluator asks for more, the natural next additions (roughly in priority order) are:

1. **Controller-level tests** (`@WebMvcTest` + `MockMvc`) for `DocumentController` and
   `AuthController`, asserting HTTP status codes and JSON shape, with the security filter chain
   mocked or a test JWT.
2. **Repository tests** (`@DataJpaTest`) for the custom query methods (`findByOwnerEmail`,
   `findByIdAndOwnerEmail`) against the H2 test database.
3. **Kafka integration test** using `spring-kafka-test`'s embedded broker, asserting a message
   published by `KafkaProducerService` is actually received by `KafkaConsumerService`.
4. **Validation tests** confirming `DocumentRequest`/`SignupRequest` reject invalid input (blank
   title, short password, etc.) and that `GlobalExceptionHandler` returns the expected 400 body.

## CI

`.github/workflows/ci.yml` runs `./mvnw clean package -DskipTests` followed by `./mvnw test` on
every push and pull request to `main`. Because tests use the isolated `test` profile, this passes
without any database/Redis/Kafka setup in the CI runner.
