# Slice 02 — payment-service (2 instances, ~50% failure)

Owner: Gabriel (side A) · Value: 15 pts (load balance, shared with slice 05) · Depends on: slices 00 and 01 (in `develop`).

## 1. Goal

Provide the unstable payment service the order-service will call by name: `POST /payments` fails with 500 in about half of the calls, and the service runs in two instances (8081 and 8082) registered in Eureka. The response and the log say which instance answered, so the alternation is visible.

## 2. Expected behavior

- `payment-service` registers in Eureka as `PAYMENT-SERVICE`.
- Instance 1 runs on **8081** (default); instance 2 on **8082** (`bootRun --args='--server.port=8082'`).
- The two instances appear as separate entries in Eureka (`instance-id` includes the port): `payment-service:8081` and `payment-service:8082`.
- `POST /payments` with `{"amount": 79.80}`:
  - ~50% of the calls -> **200** `{"status": "APPROVED", "instance": 8081}`
  - ~50% of the calls -> **500** (simulated failure)
- The decision uses `java.util.Random`.
- Every call writes a log line with the instance port and the result (approved / failed).

## 3. Contract (from the professor's spec, not changeable)

| Method | Route | Body | Responses |
|--------|-------|------|-----------|
| POST | `/payments` | `{"amount": 79.80}` | 200 `{"status": "APPROVED", "instance": 8081}` · 500 simulated failure |

`instance` is the port of the instance that answered (a number). `amount` is a decimal number with a dot (`BigDecimal`).

The body of the 500 is not defined by the professor; the project uses `{"error": "Payment failed"}` (owner's decision, same error format as the rest of the project).

## 4. Acceptance criteria (verifiable)

- [x] AC1 — `./gradlew :payment-service:bootRun` starts on 8081 and registers in Eureka as `PAYMENT-SERVICE`.
- [x] AC2 — A second instance with `--server.port=8082` starts and both appear in Eureka as separate instances (`payment-service:8081` and `payment-service:8082`).
- [x] AC3 — `POST /payments` `{"amount": 79.80}` returns either 200 `{"status":"APPROVED","instance":<port>}` or 500; `instance` equals the port of the instance called.
- [x] AC4 — In 200 sequential calls to one instance, the share of 500s is between 35% and 65%.
- [x] AC5 — Each call logs the instance port and the result; calls to 8081 and 8082 are distinguishable in their logs.
- [x] AC6 — `./gradlew :payment-service:build` passes (including `bootJar` and the tests).
- [x] AC7 — No `localhost` in service-to-service calls (only the Eureka `defaultZone` URL, which CLAUDE.md allows) and no secrets.
- [x] AC8 — Automated tests (see plan) pass.

## 5. Out of scope

- The load balancing and the retry themselves (order-service, slice 05); here we only provide the two instances.
- Validation of `amount` (the professor's contract does not define a 400; order-service always sends a valid value).
- Persistence, authentication, any other endpoint.

## 6. Open questions

See `plan.md` section 5 and `specs/OPEN-QUESTIONS.md`.
