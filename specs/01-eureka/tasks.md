# Slice 01 — Tasks

Rule: one task at a time; stop after each task and wait for the session owner's "ok".

- [x] **T1** — Add the Eureka server starter to `eureka-server/build.gradle` and create `EurekaServerApplication` (`@SpringBootApplication` + `@EnableEurekaServer`). Verify: `./gradlew :eureka-server:compileJava` -> `BUILD SUCCESSFUL`.
- [x] **T2** — Create `eureka-server/src/main/resources/application.yml` (name, port 8761, no self-registration). Verify: `./gradlew :eureka-server:bootRun` starts on 8761 without errors (AC1).
- [ ] **T3** — Add the test dependencies and `EurekaServerApplicationTests` (`@SpringBootTest` context load). Verify: `./gradlew :eureka-server:test` passes (AC7).
- [ ] **T4** — Verify the slice: AC1 to AC7 (`curl` on `/` and `/eureka/apps`, `./gradlew :eureka-server:build`, grep for `localhost`/secrets) and report. Update `specs/OPEN-QUESTIONS.md` if anything new appears.
- [ ] **T5** — Hand over for the PR `feat/01-eureka` -> `develop` (title `feat(eureka): add eureka server`). **Stop** (do not start slice 02 without request).

Commits: one per task, English, `type(scope): message` (e.g. `feat(eureka): add eureka server application`). Gabriel makes the commits; Claude suggests the commands.
