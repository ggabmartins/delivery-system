# Slice 02 — Plan

## 1. Files

Package: `br.com.fiap.delivery.paymentservice` (same style as `eurekaserver`).

| File | Content |
|------|---------|
| `payment-service/build.gradle` | Add `spring-boot-starter-webmvc`, `spring-cloud-starter-netflix-eureka-client`; tests: `spring-boot-starter-test` + `junit-platform-launcher` (as in slice 01) |
| `PaymentServiceApplication` | `@SpringBootApplication` + `main` (the Eureka client is enabled by the starter; no annotation needed) |
| `controller/PaymentController` | `POST /payments` -> calls `PaymentService`, returns 200 |
| `service/PaymentService` | Decides success/failure with `Random`, knows the instance port, logs |
| `dto/PaymentRequest` | `record PaymentRequest(BigDecimal amount)` |
| `dto/PaymentResponse` | `record PaymentResponse(String status, int instance)` |
| `exception/PaymentFailedException` | Thrown by the service on a simulated failure |
| `exception/GlobalExceptionHandler` | `@RestControllerAdvice`: `PaymentFailedException` -> 500 `{"error": "..."}` |
| `config/RandomConfig` | `@Bean Random` (injectable, so tests can control it) |
| `src/main/resources/application.yml` | name, port 8081, Eureka client |
| `src/test/.../PaymentServiceTest` | Unit tests of the service with a controlled `Random` |
| `src/test/.../PaymentServiceApplicationTests` | Context-load test |

## 2. Technical decisions

1. **`application.yml`**:
   ```yaml
   spring:
     application:
       name: payment-service

   server:
     port: 8081

   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/
     instance:
       instance-id: ${spring.application.name}:${server.port}
   ```
   The `localhost` in `defaultZone` is the Eureka server URL, the only exception CLAUDE.md allows. `instance-id` with the port makes the two instances appear separately.
2. **Second instance**: same code, `--server.port=8082` overrides `server.port`; `instance-id` follows because it uses the property.
3. **Instance port in the response**: read from `server.port` (`@Value("${server.port}")`), which is what `--server.port=8082` changes. Works because the ports are fixed (never `0`).
4. **~50% failure**: `random.nextBoolean()` with a single `java.util.Random` bean (required by the spec). Injected so tests control it with a fake `Random`.
5. **Failure -> 500**: the service throws `PaymentFailedException`; the advice returns 500 `{"error": "Payment failed"}` (Q1). Centralizing it follows CLAUDE.md section 5, item 4, and order-service (slice 05) sees a real 500 that `RestTemplate` turns into an exception to retry.
6. **Logs** (English), one per call, with the port: e.g. `Payment APPROVED by instance 8081, amount=79.80` and `Payment FAILED on instance 8082, amount=79.80`.
7. **Dependencies per slice**: the web starter is `spring-boot-starter-webmvc` in Boot 4 (confirmed in slice 00 T1); the client starter is `spring-cloud-starter-netflix-eureka-client`. JSON (de)serialization comes with the web starter; confirmed at implementation.
8. **Tests**:
   - `PaymentServiceTest`: with a fake `Random` returning `true` -> approved with the right port; returning `false` -> `PaymentFailedException`.
   - `PaymentServiceApplicationTests`: `@SpringBootTest(properties = "eureka.client.enabled=false")`, so the build does not depend on a running Eureka.
   - No MockMvc test (would need extra test starters); the HTTP part is verified with `curl` in the verification task.

## 3. Risks

| Risk | Mitigation |
|------|-----------|
| Eureka registration delay (up to ~30 s) makes the first check look empty | Wait and re-check; not an error |
| Both instances started from Gradle may collide on build output | Start the second only after the first is up; if there is a lock issue, report it |
| Statistical test (AC4) is randomly flaky | 200 calls and a wide 35-65% band; failure probability is negligible |
| `instance` read as a string from the property | Parse as `int`; fails fast at startup if the property is not numeric |
| Boot 4 / Cloud config property names differ | Confirm `eureka.instance.instance-id` and `service-url.defaultZone` against the docs at T2 |

## 4. Dependencies between slices

- Needs Eureka running (slice 01) for AC1/AC2.
- Slice 05 (`PaymentClient`) depends on the 200/500 contract and on both instances existing.

## 5. Questions for the session owner (also in OPEN-QUESTIONS.md)

1. **Q1 — Body of the 500**: DECIDED by the owner: `{"error": "Payment failed"}`.
2. **Q2 — Package** `br.com.fiap.delivery.paymentservice`: DECIDED by the owner.
3. **Q3 — Tests**: the unit tests of `PaymentService` plus a context-load test (listed in section 2, item 8). OK?
4. **Q4 — Extra classes** (`RandomConfig`, `GlobalExceptionHandler`): DECIDED by the owner: keep.
