# Slice 01 — eureka-server + service registration

Owner: Gabriel (side A) · Value: 10 pts (Eureka) · Depends on: slice 00 (in `develop`).

## 1. Goal

Run a Eureka discovery server on port 8761 so every business service can register by `spring.application.name` and be called by name (never by `localhost`).

## 2. Expected behavior

- `eureka-server` starts on **8761** with `@EnableEurekaServer`.
- It does **not** register in itself (`register-with-eureka: false`, `fetch-registry: false`).
- The dashboard (`/`) and the registry API (`/eureka/apps`) answer.
- Services register with Eureka under `ORDER-SERVICE`, `PAYMENT-SERVICE` and `REVIEW-SERVICE`; payment shows two separate instances (instance-id with the port, slice 02).

## 3. Scope split (see open question Q1)

| Part | Where it is done |
|------|------------------|
| Eureka server (this slice) | `eureka-server/` |
| Eureka client config of `payment-service` | slice 02 (Gabriel) |
| Eureka client config of `order-service` | slice 03 (Gabriel) |
| Eureka client config of `review-service` | slice 07 (Orlando) |
| Check that the 3 services appear registered | slice 10 (final verification, both) |

This slice only delivers the server and proves it can accept registrations.

## 4. Contract

No public HTTP contract; the Eureka endpoints are standard:

| Item | Value |
|------|-------|
| Port | 8761 |
| `spring.application.name` | `eureka-server` |
| Dashboard | `GET /` -> 200 |
| Registry | `GET /eureka/apps` (JSON with `Accept: application/json`) -> 200 |
| Client URL used by the other services | `http://localhost:8761/eureka/` (allowed exception: the Eureka server itself) |

## 5. Acceptance criteria (verifiable)

- [ ] AC1 — `./gradlew :eureka-server:bootRun` starts without errors and listens on 8761.
- [ ] AC2 — `GET /` (dashboard) returns 200.
- [ ] AC3 — `GET /eureka/apps` with `Accept: application/json` returns 200 and an empty or valid application list.
- [ ] AC4 — The server does not appear registered in itself (no `EUREKA-SERVER` in `/eureka/apps`) and the log shows no registration/fetch errors against itself.
- [ ] AC5 — `./gradlew :eureka-server:build` passes (including `bootJar` and the context-load test, now that the module has a main class).
- [ ] AC6 — No `localhost` in the eureka-server sources/config; no secrets.
- [ ] AC7 — A `@SpringBootTest` context-load test exists in `eureka-server` and passes (`./gradlew :eureka-server:test`).

## 6. Out of scope

Eureka client config in other modules, any other service code, authentication on Eureka, high availability (multiple Eureka nodes). Further tests beyond the context-load test.

## 7. Open questions

See `plan.md` section 5 and `specs/OPEN-QUESTIONS.md`.
