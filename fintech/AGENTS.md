# AGENTS

## Big picture
- Spring Boot 3.5 app with layered packages under `src/main/java/com/university/fintech`.
- Entry point is `FintechApplication` which logs startup info via `ApplicationStartupTraces`.
- Domain model lives in `domain/` (entities, DTOs, mappers) with JPA and auditing.
- Infra wiring lives in `wire/` (security, web config, database config, auditing listener).

## Architecture and data flow
- Entities extend `BaseAuditing` which wires `AuditingEntityListener` plus `CustomeAuditingEntityListener` for timestamps.
- `DatabaseConfiguration` enables JPA repositories for `com.university.fintech` and resolves auditor from SecurityContext.
- Security is JWT-based: `JwtUtil` builds tokens from `UserDTO` claims; `JwtAuthenticationFilter` maps claims to authorities.
- Mappers use MapStruct with `componentModel = "spring"` (see `UserMapper`) and implement `BaseMapper`.
- Error responses are centralized in `GlobalExceptionHandler` and return `{ error, message, details }` maps.

## Configuration and external dependencies
- Runtime config is in `src/main/resources/config/application.yaml`.
- Server defaults: port `8082`, context path `SERVER_CTX_PATH` if set.
- JWT requires `application.jwt.secret` to be >= 32 bytes (see `JwtUtil`).
- External integrations configured by env vars:
  - Postgres: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
  - Redis REST: `REDIS_REST_URL`, `REDIS_REST_TOKEN`.
  - SMTP: `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` (Gmail defaults).
  - S3: `S3_ENDPOINT`, `S3_REGION`, `S3_KEY_ID`, `S3_ACCESS_KEY`, `S3_BUCKET_NAME`.

## Security and API paths
- Public vs private routes are derived from `application.path.base.public` and `.private` in `application.yaml`.
- `SecurityConfig` permits public path and requires auth for private path; JWT filter is inserted before `UsernamePasswordAuthenticationFilter`.
- Authority mapping rules: roles become `ROLE_<CODE>`; permissions are uppercased as-is (`JwtAuthenticationFilter`).

## Conventions and patterns
- UUIDs for entities are generated on `@PrePersist` using `UuidCreator.getTimeOrderedEpoch()` (see `UserEntity`).
- MapStruct mappers disable builder when mapping DTO->entity where needed (see `UserMapper`).
- Assertions use `exception.domain.Assert` instead of ad-hoc null/blank checks.

## Build and test workflows
- Maven wrapper is included (`mvnw`, `mvnw.cmd`). Typical commands:
  - Build jar: `./mvnw clean package` (Windows: `mvnw.cmd clean package`).
  - Tests: `./mvnw test` (Windows: `mvnw.cmd test`).
- Docker build uses a multi-stage image in `Dockerfile` (Maven build, then JRE runtime).

## Notes for agents
- There are no `@RestController` classes yet; request/response DTOs are under `infrastructure/primary/restful`.
- For new persistence code, extend `BaseJPARepository` in `infrastructure/secondary/jpa/repository`.
- When adding endpoints, ensure they fit under the public/private base paths from config and the error format from `GlobalExceptionHandler`.

