# Open questions

Append-only; both members may add entries.

## Slice 00 — infra (Gabriel)

- [x] **RM / full names**: Gabriel Lourenço — RM562194; Orlando Gonçalves — RM561584.
- [x] **Gradle wrapper source**: copy the professor's (Gradle 9.7.1) — decided by Gabriel.
- [x] **JDK 25**: found at `C:\Users\glm15\.jdks\ms-25.0.4.1` (not on PATH; set `JAVA_HOME` per shell for Gradle).
- [x] **Dependencies**: added per slice, not all in 00 — decided by Gabriel.
- [x] **`.gitattributes`**: approved by Gabriel and added in slice 00 (T5).
- [x] **RabbitMQ credentials**: image default `guest/guest` — decided by Gabriel.

## Slice 01 — eureka (Gabriel)

- [x] **Q1 — Registration of the services**: decided by Gabriel: slice 01 delivers only the Eureka server; `review-service` is left untouched for Orlando, who is told that it is his to implement fully (including its Eureka client) in slice 07.
- [x] **Q2 — Package**: `br.com.fiap.delivery.eurekaserver` (decided by Gabriel).
- [x] **Q3 — Automated test**: decided by Gabriel: add `spring-boot-starter-test` and a `@SpringBootTest` context-load test in `eureka-server`.
- [x] **Q4 — Compatibility verifier**: decided by Gabriel: keep it enabled.

## Slice 02 — payment (Gabriel)

- [x] **Q1 — Body of the 500**: decided by Gabriel: `{"error": "Payment failed"}`.
- [x] **Q2 — Package**: `br.com.fiap.delivery.paymentservice` (decided by Gabriel).
- [ ] **Q3 — Tests**: unit tests of `PaymentService` (fake `Random`) + context-load test. OK?
- [x] **Q4 — Extra classes**: keep `RandomConfig` and `GlobalExceptionHandler` (decided by Gabriel).
