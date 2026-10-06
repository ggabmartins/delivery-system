# Slice 01 — Plan

## 1. Files

| File | Content |
|------|---------|
| `eureka-server/build.gradle` | Add `implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'` (BOM and plugins already there) |
| `eureka-server/src/main/java/br/com/fiap/delivery/eurekaserver/EurekaServerApplication.java` | `@SpringBootApplication` + `@EnableEurekaServer` + `main` |
| `eureka-server/src/main/resources/application.yml` | name, port, no self-registration |

Package follows the convention of CLAUDE.md section 7: `br.com.fiap.delivery.<service>` (owner chose `br.com.fiap.delivery.eurekaserver` for this module, without hyphen).

## 2. Technical decisions

1. **Configuration (`application.yml`)**:
   ```yaml
   spring:
     application:
       name: eureka-server
   server:
     port: 8761
   eureka:
     client:
       register-with-eureka: false
       fetch-registry: false
   ```
2. **Style reference**: the professor's `eureka-server` (`docs/references/rabbitMQ/eureka-server`) has the same shape: one class with `@EnableEurekaServer` and these properties. Adapted to our package and to `application.yml` (CLAUDE.md asks for yml).
3. **`spring.cloud.compatibility-verifier.enabled=false` is NOT copied.** The professor needed it because his Spring Cloud version did not match the Boot version. We use Cloud 2025.1.3 with Boot 4.1.1 (supported), so the verifier can stay on and will warn us if versions drift.
4. **Dependencies**: Eureka server starter (main) and, by the owner's decision (Q3), `spring-boot-starter-test` (test) plus `org.junit.platform:junit-platform-launcher` (testRuntimeOnly, as in the professor's example). The exact Boot 4 test artifact name is confirmed in the docs at implementation. If Boot 4 needs an extra web starter for the dashboard (the starter should bring it transitively), it is added only if the app fails to start.
5. **Tests**: one `@SpringBootTest` context-load test (`EurekaServerApplicationTests`) in `src/test/java/br/com/fiap/delivery/eurekaserver/`. Besides it, the slice is verified with `bootRun` + `curl`.
6. **Plain `./gradlew build` for the eureka module** now works (it has a main class); the other modules still need `-x bootJar` until their slices.

## 3. Risks

| Risk | Mitigation |
|------|-----------|
| Boot 4 / Cloud 2025.1.3 starter transitive web dependency missing | Try startup first; add the web starter only if needed and say so |
| Port 8761 already in use on the dev machine | Check before running; stop the other process |
| Eureka "self-preservation" warning in the dashboard with no clients | Expected in dev with few instances; not an error. Do not disable it without asking |
| `127.0.0.1` vs `localhost` in the client `defaultZone` of other services | Allowed only for the Eureka URL (CLAUDE.md section 9); decided in slices 02/03/07 |

## 4. Dependencies between slices

- Needs slice 00 in `develop` (done).
- Slices 02, 03 and 07 need this server running to register.

## 5. Questions for the session owner (also in OPEN-QUESTIONS.md)

1. **Q1 — "registration of the services" in this slice.** The three business services have no code yet, so they cannot register today. Proposal (recommended): slice 01 delivers only the server; each service adds its Eureka client in its own slice (02, 03, 07) and the registration is checked in the final verification (slice 10). Alternative: create minimal main classes for all three now, which touches `review-service`, owned by Orlando, and goes against the rule "do not edit the other side's slices". Which do you want?
2. **Q2 — Package name** `br.com.fiap.delivery.eureka` OK?
3. **Q3 — Automated test.** The slice can be verified with `bootRun` + `curl` (no new library). A `@SpringBootTest` context-load test would need `spring-boot-starter-test`. Recommended: no test now (CLAUDE.md: no extra libs; the project is ~4h). OK?
4. **Q4 — Compatibility verifier**: DECIDED by the owner: keep it enabled.
