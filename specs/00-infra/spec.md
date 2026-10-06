# Slice 00 — Infra (monorepo Gradle, docker-compose, README)

Owner: Gabriel (side A) · Value: part of the 5 pts "Docker and contract" · Prerequisite for every other slice.

## 1. Goal

Create the repository skeleton so that all later slices only add code: a Gradle multi-project build with the 4 required projects, a `docker-compose.yml` that starts RabbitMQ, a complete `.gitignore`, an `.env.example` and the README required for delivery.

No application code (no `@SpringBootApplication` classes, no controllers) is part of this slice.

## 2. Expected behavior

- `./gradlew projects` lists exactly 4 subprojects: `eureka-server`, `order-service`, `payment-service`, `review-service`.
- `docker compose up -d` starts RabbitMQ (`rabbitmq:4-management`) exposing AMQP on **5672** and the management UI on **15672**.
- Each subproject is configured with the common stack (Java 25, Spring Boot 4, Spring Cloud 2025.1.x), so slices 01+ only add their own dependencies and sources.
- The repository follows the layout of CLAUDE.md section 7 and is safe to push (no secrets, no professor material, no build output).

## 3. Contract

This slice exposes no HTTP contract. It fixes the following infrastructure contract (from the spec / CLAUDE.md sections 2 and 8):

| Item | Value |
|------|-------|
| Gradle projects | `eureka-server`, `order-service`, `payment-service`, `review-service` |
| Java | 25 (Gradle toolchain) |
| Spring Boot / Cloud | Boot 4.x / Spring Cloud 2025.1.x |
| RabbitMQ image | `rabbitmq:4-management` |
| RabbitMQ ports | 5672 (AMQP), 15672 (UI) |
| Compose file | `docker-compose.yml` at the repo root |
| Run order | Eureka -> payment (x2) -> review -> order |

## 4. Acceptance criteria (verifiable)

- [ ] AC1 — `./gradlew projects` prints the 4 subprojects and nothing else under the root.
- [ ] AC2 — `./gradlew build` finishes with `BUILD SUCCESSFUL` using Java 25 (no sources yet, so no `bootJar`/test failures).
- [ ] AC3 — `docker compose config` is valid and `docker compose up -d` leaves the `rabbitmq` container running.
- [ ] AC4 — RabbitMQ UI answers on port 15672 (login with the compose credentials) and port 5672 accepts connections.
- [ ] AC5 — `git status` after a build shows no `build/`, `.gradle/`, `.idea/`, `*.iml` files; `docs/references/` and `docs/spec/` are ignored.
- [ ] AC6 — `.env.example` exists with variable **names only** (empty values); `.env` and `application-local.*` are ignored.
- [ ] AC7 — README has: project description, how to run (compose + 4 services in order, second payment instance on 8082), ports table, env variables, and name + RM of both members.
- [ ] AC8 — `git grep -nE "sk-|api[-_]?key\s*[:=]\s*['\"]?[A-Za-z0-9]{10,}"` returns nothing.

## 5. Out of scope

Any Java class, `application.yml`, Eureka config, Dockerfiles for the services (delivery format is `bootRun`, not containers), CI.

## 6. Open points

See `plan.md` section 5 and `specs/OPEN-QUESTIONS.md`.
