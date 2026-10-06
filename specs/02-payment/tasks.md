# Slice 02 — Tasks

Rule: one task at a time; stop after each task and wait for the session owner's "ok".

- [x] **T1** — Dependencies in `payment-service/build.gradle` (web, Eureka client, tests), `PaymentServiceApplication`, `application.yml` (name, port 8081, Eureka client, `instance-id`). Verify: `./gradlew :payment-service:compileJava` passes; with Eureka up, `bootRun` registers `PAYMENT-SERVICE` (AC1).
- [ ] **T2** — Payment logic: `PaymentRequest`, `PaymentResponse`, `RandomConfig`, `PaymentFailedException`, `PaymentService` (Random, port, logs). Verify: `compileJava` passes.
- [ ] **T3** — HTTP layer: `PaymentController` + `GlobalExceptionHandler` (500 `{"error": ...}`). Verify: `curl -X POST /payments` returns 200 or 500 (AC3).
- [ ] **T4** — Tests: `PaymentServiceTest` (fake `Random`) and `PaymentServiceApplicationTests`. Verify: `./gradlew :payment-service:test` passes (AC8).
- [ ] **T5** — Verify the slice: AC1 to AC8 with Eureka + the two instances (Eureka shows both, 200 calls for the ~50% rate, logs per port, `build`, grep) and report. Update `specs/OPEN-QUESTIONS.md` if anything new appears.
- [ ] **T6** — Hand over for the PR `feat/02-payment` -> `develop` (title `feat(payment): add unstable payment service`). **Stop** (do not start slice 03 without request).

Commits: one per task, English, `type(scope): message` (e.g. `feat(payment): add payment service with simulated failures`). Gabriel makes the commits; Claude suggests the commands.
