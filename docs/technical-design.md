# Technical Design Document - Fintech Service

## 1. Muc tieu tai lieu

Tai lieu nay thong nhat thiet ke ky thuat cho `fintech-service`, bao gom kien truc tong the, cac module chinh, quan he giua cac module, cong nghe su dung, convention va nguyen tac thiet ke khi mo rong he thong.

Pham vi hien tai cua codebase la backend service Spring Boot 3.5, tap trung vao cau truc nen tang: domain model, JPA persistence, JWT security, auditing, exception handling va cau hinh runtime. Hien tai chua co `@RestController`; cac endpoint moi can duoc bo sung theo convention trong tai lieu nay.

## 2. Tong quan he thong

`fintech-service` la backend REST service cho ung dung fintech, duoc xay dung theo huong layered architecture ket hop mot so y tuong cua clean architecture:

- `domain`: chua entity, DTO, mapper va cac thanh phan nghiep vu cot loi.
- `infrastructure.primary`: bien vao he thong, hien tai co request/response DTO cho REST.
- `infrastructure.secondary`: bien ra he thong, hien tai co JPA repository va adapter nen tang.
- `wire`: cau hinh framework, security, database, web va audit.
- `exception`: chuan hoa loi domain va loi API.
- `shared`: hang so, enum va helper dung chung.

Luon uu tien de domain va application logic doc lap voi chi tiet framework. Spring configuration, security filter, database setup va web setup nam trong `wire`.

## 3. Kien truc tong the

### 3.1 Luong xu ly request

```text
Client
  -> Spring MVC / REST Controller
  -> Request DTO validation
  -> Application service / use case
  -> Domain DTO / Entity / Mapper
  -> Secondary adapter
  -> JPA repository
  -> PostgreSQL
```

Voi request can xac thuc:

```text
Client
  -> Authorization: Bearer <jwt>
  -> JwtAuthenticationFilter
  -> SecurityContext
  -> Controller / Service
  -> AuditorAware
  -> JPA auditing fields
```

Voi loi runtime:

```text
Controller / Service / Repository
  -> Exception
  -> GlobalExceptionHandler
  -> Response body: { error, message, details }
```

### 3.2 Nguyen tac phu thuoc

Huong phu thuoc mong muon:

```text
primary adapter -> application/use case -> domain <- secondary adapter
wire -> tat ca module can cau hinh
exception/shared -> duoc dung boi cac module khac
```

Khong de domain phu thuoc vao REST controller, database adapter hoac security filter. Entity co dung JPA annotation vi hien tai domain model dong thoi la persistence model; khi he thong lon hon co the tach domain aggregate va persistence entity rieng.

## 4. Module chinh

### 4.1 Entry point

Package: `com.university.fintech`

- `FintechApplication`: khoi dong Spring Boot application.
- `ApplicationStartupTraces`: log thong tin startup.
- `SharedKernel`: vung khai bao chia se chung neu can.

Trach nhiem:

- Khoi dong ung dung.
- Khong dat business logic trong entry point.
- Startup log chi dung cho thong tin van hanh can thiet.

### 4.2 Domain module

Package: `com.university.fintech.domain`

Thanh phan hien co:

- `entity`: `UserEntity`, `RoleEntity`, `PermissionEntity`.
- `dto`: `UserDTO`, `RoleDTO`, `PermissionDTO`.
- `mapper`: MapStruct mappers, implement `BaseMapper`.
- `audit`: `BaseAuditing`.

Quan he du lieu chinh:

- User co nhieu Role qua bang join `user_role`.
- Role co nhieu Permission qua quan he many-to-many.
- Permission co `code` duy nhat, dung lam authority trong security.
- Entity ke thua `BaseAuditing` de co `createdAt`, `createdBy`, `updatedAt`, `updatedBy`.

Convention:

- ID dung `UUID`.
- UUID duoc tao trong `@PrePersist` bang `UuidCreator.getTimeOrderedEpoch()`.
- Entity ke thua `BaseAuditing` neu can audit.
- DTO la bien trung gian giua domain/application va adapter.
- Mapper dung MapStruct voi `componentModel = "spring"`.
- Khi map DTO sang entity ma Lombok builder gay xung dot, cau hinh MapStruct disable builder theo pattern cua `UserMapper`.

### 4.3 Primary infrastructure module

Package: `com.university.fintech.infrastructure.primary`

Thanh phan hien co:

- `restful.request.BaseRequest`
- `restful.response.PageResponse`

Trach nhiem:

- Dinh nghia request/response model cho REST API.
- Chua controller khi bo sung endpoint moi.
- Chuan hoa pagination response.

Convention endpoint:

- Public API nam duoi `${application.path.base.public}`.
- Private API nam duoi `${application.path.base.private}`.
- Mac dinh hien tai:
  - Public: `/public/api/v1/**`
  - Private: `/private/api/v1/**`
- Private endpoint bat buoc co JWT hop le.
- Request body can dung Bean Validation annotation khi co rang buoc dau vao.
- Response loi phai di qua `GlobalExceptionHandler`, khong tu tao response loi rieng trong controller.

### 4.4 Secondary infrastructure module

Package: `com.university.fintech.infrastructure.secondary`

Thanh phan hien co:

- `jpa.repository.BaseJPARepository`
- `jpa.adapter.BaseJPAAdapterVer2`

Trach nhiem:

- Ket noi domain/application voi persistence.
- Dong goi thao tac JPA repository.
- Chuyen doi entity va DTO thong qua mapper.

Convention:

- Repository moi extend `BaseJPARepository<T, ID>`.
- Adapter persistence nen ke thua hoac follow behavior cua `BaseJPAAdapterVer2`.
- Transaction boundary nen dat o application service/use case, khong dat tuy tien trong controller.
- `spring.jpa.open-in-view=false`, vi vay moi lazy relation can duoc xu ly trong transaction.

### 4.5 Wire module

Package: `com.university.fintech.wire`

Thanh phan hien co:

- `WebConfig`: CORS, locale resolver, Spring Data web support.
- `SecurityConfig`: security filter chain, stateless session, password encoder.
- `JwtAuthenticationFilter`: doc Bearer token va set `SecurityContext`.
- `JwtUtil`: generate/parse/validate JWT.
- `DatabaseConfiguration`: JPA repositories, transaction management, auditing.
- `CustomeAuditingEntityListener`: cap nhat timestamp audit.

Trach nhiem:

- Chua cau hinh framework va integration wiring.
- Khong dat business logic trong config/filter.
- Dam bao cac component khac co bean va policy dung.

### 4.6 Exception module

Package: `com.university.fintech.exception`

Thanh phan hien co:

- `GlobalExceptionHandler`
- Domain assertion exceptions trong `exception.domain`
- `Assert` helper cho validate domain invariant

Response loi chuan:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "details": {}
}
```

Convention:

- Business/domain validation dung `exception.domain.Assert` khi phu hop.
- Controller/service khong tra ve loi bang format rieng.
- Loi he thong phai log o handler neu can thiet, nhung response khong nen lo thong tin nhay cam.

### 4.7 Shared module

Package: `com.university.fintech.shared`

Trach nhiem:

- Hang so dung chung.
- Enum dung chung.
- Annotation/helper cho generated code coverage.

Convention:

- Chi dua vao `shared` nhung thu that su dung chung nhieu module.
- Khong dua business flow cu the vao `shared`.

## 5. Security design

### 5.1 Authentication

He thong dung JWT Bearer token.

- Token duoc tao tu `UserDTO`.
- Subject cua token la `username`.
- Claims hien co:
  - `userId`
  - `fullName`
  - `roles`
  - `permissions`
  - `role` legacy claim cho role dau tien

`JwtUtil` ky token bang HS256. `application.jwt.secret` phai co toi thieu 32 bytes.

### 5.2 Authorization

`SecurityConfig`:

- Cho phep `OPTIONS /**`.
- Cho phep Swagger paths.
- Cho phep public path.
- Bat buoc auth voi private path.
- Stateless session.
- Gan `JwtAuthenticationFilter` truoc `UsernamePasswordAuthenticationFilter`.

Mapping authority trong `JwtAuthenticationFilter`:

- Role: `ROLE_<CODE>` va uppercase.
- Permission: uppercase nguyen code.

Vi du:

- Role `admin` -> `ROLE_ADMIN`
- Permission `user.read` -> `USER.READ`

### 5.3 Principal

Principal trong `SecurityContext` la `Map<String, String>` gom:

- `username`
- `userId`
- `fullName`

`DatabaseConfiguration` dung principal nay de resolve `createdBy` va `updatedBy`. Neu khong co authenticated principal, auditor mac dinh la `system`.

## 6. Persistence design

### 6.1 Database

Database chinh: PostgreSQL.

Datasource:

- Driver: `org.postgresql.Driver`
- Connection pool: HikariCP
- `auto-commit=false`
- `maximum-pool-size=40`
- `minimum-idle=10`

JPA/Hibernate:

- `ddl-auto=none`
- Naming strategy: camelCase sang snake_case.
- `open-in-view=false`
- JDBC batch size: `25`
- Hibernate timezone: `UTC`
- Second-level cache va query cache tat.

### 6.2 Audit

Tat ca entity can audit ke thua `BaseAuditing`.

Fields:

- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`

`AuditingEntityListener` xu ly auditor, `CustomeAuditingEntityListener` xu ly timestamp. Auditor lay tu Spring SecurityContext.

### 6.3 Repository va adapter

Repository:

```java
public interface UserRepository extends BaseJPARepository<UserEntity, UUID> {
}
```

Adapter nen dong vai tro chuyen doi entity/DTO va an chi tiet JPA khoi application service.

## 7. External integrations va runtime configuration

File config chinh: `src/main/resources/config/application.yaml`

Bien moi truong:

| Nhom | Bien | Mo ta |
| --- | --- | --- |
| Server | `SERVER_CTX_PATH` | Context path cua service |
| Database | `DB_URL` | JDBC URL PostgreSQL |
| Database | `DB_USERNAME` | Username PostgreSQL |
| Database | `DB_PASSWORD` | Password PostgreSQL |
| JWT | `JWT_SECRET` | Secret HS256, toi thieu 32 bytes |
| JWT | `JWT_EXPIRATION_MS` | Thoi gian het han token |
| Redis REST | `REDIS_REST_URL` | Redis REST endpoint |
| Redis REST | `REDIS_REST_TOKEN` | Redis REST token |
| SMTP | `SPRING_MAIL_USERNAME` | Gmail username |
| SMTP | `SPRING_MAIL_PASSWORD` | Gmail password/app password |
| S3 | `S3_ENDPOINT` | S3 endpoint |
| S3 | `S3_REGION` | S3 region |
| S3 | `S3_KEY_ID` | Access key id |
| S3 | `S3_ACCESS_KEY` | Secret access key |
| S3 | `S3_BUCKET_NAME` | Bucket name |

Server mac dinh:

- Port: `8082`
- Application name: `fintech-service`
- API version: `api/v1`

## 8. Cong nghe su dung

| Hang muc | Cong nghe |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5.x |
| Web | Spring MVC / Spring Web |
| Security | Spring Security, JWT |
| JWT library | JJWT 0.11.5 |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Connection pool | HikariCP |
| Mapping | MapStruct 1.5.5.Final |
| Boilerplate | Lombok |
| Validation | Jakarta Bean Validation |
| UUID | uuid-creator |
| Build | Maven Wrapper |
| Container | Docker multi-stage build |

## 9. Convention phat trien

### 9.1 Package convention

```text
com.university.fintech
  domain
    entity
    dto
    mapper
    audit
  infrastructure
    primary
      restful
        request
        response
    secondary
      jpa
        repository
        adapter
  wire
    security
    database
    audit
  exception
    domain
  shared
```

Khi them feature lon, nen nhom theo package con cua feature neu so luong class tang nhanh, nhung van giu bien primary/secondary/domain ro rang.

### 9.2 REST API convention

- Controller chi nhan request, validate, goi service/use case va tra response.
- Khong dat logic persistence trong controller.
- Public endpoint: `/public/api/v1/...`
- Private endpoint: `/private/api/v1/...`
- Dung HTTP status theo y nghia:
  - `200 OK` cho read/update thanh cong.
  - `201 Created` cho create.
  - `204 No Content` cho delete thanh cong khong can body.
  - `400 Bad Request` cho validation/business input error.
  - `401 Unauthorized` cho thieu/sai auth.
  - `403 Forbidden` cho khong du quyen.
  - `404 Not Found` cho resource khong ton tai.

### 9.3 DTO va mapper convention

- Request/response DTO khong nen la JPA entity.
- Domain DTO co the dung giua service va adapter.
- Mapper dung MapStruct, khong map thu cong lap lai neu MapStruct xu ly duoc.
- Mapper collection qua `BaseMapper`.

### 9.4 Validation convention

- Validate format dau vao bang Bean Validation tren request DTO.
- Validate invariant nghiep vu bang `Assert` trong domain/service.
- Loi validation di qua `GlobalExceptionHandler`.

### 9.5 Transaction convention

- Dat `@Transactional` o service/use case.
- Read-only query nen dung `@Transactional(readOnly = true)`.
- Khong dua lazy relation ra ngoai transaction khi `open-in-view=false`.
- Can can nhac fetch join, entity graph hoac DTO projection cho query co relation.

### 9.6 Naming convention

- Class entity ket thuc bang `Entity`.
- DTO ket thuc bang `DTO`.
- Mapper ket thuc bang `Mapper`.
- Repository ket thuc bang `Repository`.
- Adapter ket thuc bang `Adapter`.
- Config class ket thuc bang `Config` hoac `Configuration`.
- Database column theo snake_case; Java field theo camelCase.

## 10. Nguyen tac thiet ke

- Tach ro bien vao, business logic va bien ra.
- Khong de controller goi truc tiep nhieu repository khi co business flow.
- Khong de entity leak ra API response neu endpoint public/private co contract rieng.
- JWT la stateless; khong dua session server-side vao flow auth.
- Moi loi API dung format thong nhat `{ error, message, details }`.
- Configuration doc tu environment variable, khong hard-code secret.
- Repository chi la persistence access; business decision nam trong service/use case.
- Them abstraction khi co nhu cau that su: giam duplication, bao ve boundary hoac the hien pattern da co.
- Uu tien test cho service/use case va security-sensitive flow.

## 11. Build, test va deploy

Build:

```powershell
mvnw.cmd clean package
```

Test:

```powershell
mvnw.cmd test
```

Docker:

- Dockerfile hien tai dung multi-stage build.
- Stage build dung Maven.
- Stage runtime dung JRE.

Truoc khi deploy can dam bao:

- `JWT_SECRET` hop le va toi thieu 32 bytes.
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` dung moi truong.
- Migration/schema database da duoc apply vi `ddl-auto=none`.
- Endpoint private duoc test voi JWT va authority phu hop.

## 12. Huong dan mo rong feature moi

Quy trinh de them mot feature REST moi:

1. Dinh nghia request/response DTO trong `infrastructure.primary.restful`.
2. Dinh nghia domain DTO/entity neu can trong `domain`.
3. Tao mapper MapStruct trong `domain.mapper`.
4. Tao repository extend `BaseJPARepository` neu can persistence.
5. Tao adapter secondary de dong goi repository.
6. Tao service/use case chua business flow va transaction boundary.
7. Tao controller duoi public/private base path phu hop.
8. Dung `Assert` va Bean Validation cho validation.
9. Bo sung test cho service/use case va controller/security neu co authorization.

## 13. Rui ro va diem can thong nhat tiep

- Chua co controller va service/use case trong codebase, nen can thong nhat package chinh thuc cho application service truoc khi them nhieu endpoint.
- Hien tai JPA entity dong vai tro domain model; neu logic nghiep vu phuc tap hon, nen can nhac tach aggregate/domain model khoi persistence entity.
- `anyRequest().permitAll()` trong `SecurityConfig` cho phep cac path ngoai public/private. Neu muon security mac dinh chat hon, nen doi sang deny/authenticated theo chien luoc API.
- `ddl-auto=none` yeu cau co quy trinh migration rieng. Nen bo sung Flyway hoac Liquibase neu du an can quan ly schema version.
- JWT invalid hien tai filter bo qua va tiep tuc request; private path se bi chan boi Spring Security. Neu can response 401 chi tiet hon, can bo sung authentication entry point.
