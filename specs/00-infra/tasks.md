# Slice 00 — Tasks

Rule: one task at a time; stop after each task and wait for the session owner's "ok".

- [x] **T1** — Confirm versions in the official docs (Boot 4.x, Spring Cloud 2025.1.x, Gradle, Boot 4 starter names) and record them in `plan.md`. *(no code)*
- [x] **T2** — Gradle wrapper + root `settings.gradle` (4 includes) + root `build.gradle` / `gradle.properties` (common config, Java 25 toolchain). Verify: `./gradlew projects` lists the 4 projects.
- [x] **T3** — Per-module `build.gradle` (plugins + BOM only; starters are added per slice). Verify: `./gradlew build -x bootJar` -> `BUILD SUCCESSFUL` (plain `build` fails on `bootJar` until each module has a main class; accepted by the owner).
- [x] **T4** — `docker-compose.yml` (RabbitMQ). Verify: `docker compose config`, `docker compose up -d`, UI on :15672, then `docker compose down`.
- [x] **T5** — Complete `.gitignore`, add `.env.example` (and `.gitattributes` if approved). Verify: AC5, AC6.
- [x] **T6** — `README.md` (description, run order, ports, env vars, names + RM). Verify: AC7, AC8.
- [x] **T7** — Verify slice: run AC1–AC8 and report. Then push `feat/00-infra` and open the PR for Orlando's review. **Stop** (do not start slice 01 without request).

Commits: one per task, English, `type(scope): message` (e.g. `build(infra): add gradle multi-project skeleton`).
