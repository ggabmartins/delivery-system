# delivery-system

Delivery ordering system built with microservices (Java Advanced, Projeto Diamante 02).
The public API follows the professor's contract so the React Native app
([diamante-delivery](https://github.com/joaocarloslima/diamante-delivery)) works without changes.

## Team

| Name | RM |
|------|----|
| Gabriel Lourenço | RM562194 |
| Orlando Gonçalves | RM561584 |

## Stack

Java 25 · Spring Boot 4.1 · Spring Cloud 2025.1 · Gradle (multi-project) · H2 · RabbitMQ · Spring AI

## Projects

| Project | Port | Role |
|---------|------|------|
| `eureka-server` | 8761 | Service discovery |
| `payment-service` | 8081 / 8082 | Payments (two instances, fails ~50% of the calls) |
| `review-service` | 8083 | Consumes reviews from RabbitMQ and serves the ranking |
| `order-service` | 8080 | Dishes, orders, review producer and AI assistant |
| RabbitMQ (Docker) | 5672 / 15672 | AMQP / management UI |

The React Native app uses only ports 8080 and 8083.

## Requirements

- JDK 25 (`JAVA_HOME` pointing to it)
- Docker (for RabbitMQ)

The Gradle wrapper is included; no global Gradle is needed.

## Running

Start in this order, each command in its own terminal:

```bash
docker compose up -d                                             # RabbitMQ
./gradlew :eureka-server:bootRun                                 # :8761
./gradlew :payment-service:bootRun                               # :8081
./gradlew :payment-service:bootRun --args='--server.port=8082'   # :8082
./gradlew :review-service:bootRun                                # :8083
./gradlew :order-service:bootRun                                 # :8080
```

On Windows (CMD) use `gradlew` instead of `./gradlew`.

RabbitMQ UI: <http://localhost:15672> (user `guest`, password `guest`).
Eureka dashboard: <http://localhost:8761>.

## Environment variables

Copy `.env.example` as a reference; **never commit real values**. Set the variables in your terminal or IDE before starting `order-service`:

| Variable | Used by | Description |
|----------|---------|-------------|
| `OPENAI_API_KEY` | order-service | API key of the AI provider (assistant) |
| `AI_BASE_URL` | order-service | Optional: base URL of a local model (LM Studio / Ollama) |

## Build

```bash
./gradlew build -x bootJar
```

`bootJar` is excluded while the services have no main class yet; once every service is implemented a plain `./gradlew build` works.

## Development

The project follows Spec-Driven Development: each slice has `spec.md`, `plan.md` and `tasks.md` under `specs/<NN>-<slice>/`. See `CLAUDE.md` for the full workflow and `specs/OPEN-QUESTIONS.md` for pending decisions.
