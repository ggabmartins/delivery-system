# Slice 00 — Plan

## 1. Files

| File | Content |
|------|---------|
| `settings.gradle` | `rootProject.name = 'delivery-system'` + `include` of the 4 projects |
| `build.gradle` (root) | Common config for `subprojects`: `java` plugin, Java 25 toolchain, `mavenCentral()`, group `br.com.fiap`, JUnit platform. Boot / dependency-management plugins declared with `apply false` in the root, applied in each module |
| `gradle.properties` | `springCloudVersion` and other shared version properties |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Gradle wrapper (version pinned) |
| `eureka-server/build.gradle` | Plugins + Eureka server starter + Spring Cloud BOM |
| `payment-service/build.gradle` | Plugins + web starter + Eureka client + BOM |
| `review-service/build.gradle` | Plugins + web + Eureka client + AMQP + JPA + H2 + BOM |
| `order-service/build.gradle` | Plugins + web + Eureka client + loadbalancer + JPA + H2 + validation + AMQP + Spring AI + BOM |
| `docker-compose.yml` | Single `rabbitmq` service |
| `.gitignore` | Extend the existing one (section 3) |
| `.env.example` | `OPENAI_API_KEY=` and `AI_BASE_URL=` (names only) |
| `README.md` | Content per spec AC7 |
| `specs/OPEN-QUESTIONS.md` | Open points of this slice |

Per-service dependencies are only the ones the spec requires for that service (see open question 4 on whether they are declared now or per slice).

## 2. Technical decisions

1. **Groovy DSL** (`.gradle`), same as the professor's references (CLAUDE.md allows `.gradle(.kts)`).
2. **Versions**: the professor's reference uses Boot 4.1.1, Spring Cloud 2025.1.1, Gradle 9.7.1. Re-check exact Boot 4 / Cloud 2025.1.x / Spring AI versions and the Boot 4 starter names (modules were reorganized in Boot 4) in the official docs before writing files (CLAUDE.md section 10). This is task T1.
3. **Java 25 via toolchain** (`JavaLanguageVersion.of(25)`), so the build does not depend on the `java` on PATH, as long as a JDK 25 is installed.
4. **Modules without sources**: `bootJar` fails without a main class. Decision (owner): keep the build files clean and verify with `./gradlew build -x bootJar`; the failure of a plain `build` disappears by itself as each slice adds its main class.
5. **RabbitMQ credentials**: compose without custom credentials, i.e. image defaults `guest/guest` (works since all services run on the host via `bootRun`). Avoids committing a password; later slices need no `spring.rabbitmq.*` credentials.
6. **Compose has only RabbitMQ**, as the spec asks.
7. **Shared files** (`settings.gradle`, root `build.gradle`, compose, README, CLAUDE.md, OPEN-QUESTIONS): small commits; tell Orlando when 00 reaches `main`, since side B starts only after that.

### Confirmed versions (T1, checked 2026-10-06 against official docs)

| Item | Version / name | Source |
|------|----------------|--------|
| Spring Boot | **4.1.1** (latest GA) | spring.io/projects/spring-boot |
| Spring Cloud | **2025.1.3** (latest); Boot 4.1.x is supported from 2025.1.2 on. The professor's reference uses 2025.1.1, which is *below* the 4.1.x support line, so we use 2025.1.3 | spring.io/projects/spring-cloud |
| Gradle | **9.7.1** (professor's wrapper, to be copied) | decision of the owner |
| Spring AI | **2.0.1** (`spring-ai-bom`), supports Boot 4.0.x/4.1.x; OpenAI starter `spring-ai-starter-openai`; key property `spring.ai.openai.api-key` | docs.spring.io/spring-ai |
| Eureka starters | `spring-cloud-starter-netflix-eureka-server` / `-client` (unchanged, Netflix 5.0.2) | spring-cloud-netflix docs |
| Boot 4 starter renames | `spring-boot-starter-web` -> **`spring-boot-starter-webmvc`**; `RestTemplate` now needs **`spring-boot-starter-restclient`**; `spring-boot-starter-amqp`, `-data-jpa`, `-validation` keep names; H2 has no starter (`com.h2database:h2` runtime + JPA starter) | Boot 4.0 Migration Guide |

Notes for later slices (not for 00): slice 05 uses `RestTemplate` + `@LoadBalanced`, so order-service needs `spring-boot-starter-restclient`; the exact `@Retryable` attributes (Framework 7) are to be confirmed in slice 05. Dependencies are added per slice (owner's decision); slice 00 only applies plugins and BOMs.

## 3. `.gitignore` additions

Current: `docs/references/`, `docs/spec/`, `.env`, `.idea/`, `.gradle/`, `build/`.
Add: `application-local.*`, `*.iml`, `*.log`, `*.mv.db`, `*.trace.db`, `out/`.

## 4. Risks

| Risk | Mitigation |
|------|-----------|
| **No JDK and no Gradle found on this machine** (`java`/`gradle` not on PATH, no `JAVA_HOME`) | Install JDK 25 before Verify; the wrapper removes the need for a global Gradle. |
| Wrapper must be created without a global Gradle | Copy the wrapper from `docs/references/*` (Gradle 9.7.1) or download a Gradle to generate it. See open question 2. |
| Boot 4 / Spring AI / Cloud version or artifact-name mismatch | Confirm in official docs first (T1). |
| `docs/references/*` contain nested `.git`, `.gradle`, `build` dirs | Ignored by `.gitignore`; never `git add` them. |
| CRLF/LF breaking `gradlew` for the other teammate | Add `.gitattributes` (`* text=auto`, `gradlew text eol=lf`, `*.bat text eol=crlf`). Small addition; drop if not wanted. |

## 5. Decisions from the session owner (Gabriel)

- README: Gabriel Lourenço — RM562194; Orlando Gonçalves — RM561584.
- Wrapper: copy the professor's (Gradle 9.7.1).
- JDK 25 found at `C:\Users\glm15\.jdks\ms-25.0.4.1` (not on PATH; `JAVA_HOME` set per shell).
- Dependencies: **per slice**. Slice 00 module `build.gradle` files contain only plugins + BOM, no starters (the table in section 1 is the eventual set, not the 00 scope).
- RabbitMQ: default `guest/guest`.
- `.gitattributes`: still pending (explained to the owner).

## 6. Original questions (resolved above, kept for history)

1. **RM and full names** of Gabriel and Orlando for the README (placeholders until provided).
2. **Wrapper source**: copy the professor's (Gradle 9.7.1, recommended) or generate a new one?
3. **JDK 25** is not installed here. OK to install it (or point me at an existing install) before the Verify step?
4. **Dependencies in 00**: declare each service's full set now (table in section 1), or only plugins/BOM now and each slice adds its own? Recommended: per slice (no unused libs).
5. **`.gitattributes`** OK?
6. **RabbitMQ credentials**: image default `guest/guest` (recommended) or custom user like the professor's example?
