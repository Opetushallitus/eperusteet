# eperusteet-service — Improvement Plan

**Review date:** 2026-06-29
**Review basis:** [java-spring-boot](https://skills.sh/pluginagentmarketplace/custom-plugin-java/java-spring-boot) skill (Spring Boot 3.x production patterns). This plan consolidates two independent review passes; every finding below was verified against source (file + line).
**Stack:** Java 17, Spring Boot 3.x, Spring Data JPA, Hibernate + Envers, PostgreSQL (JSONB), Flyway, EhCache (JCache), CAS security

---

## Executive summary

The service has a recognizable layered architecture and several mature patterns (Envers auditing, Flyway migrations, OpenAPI, optimistic locking with ETags, education-type-specific validators). However, legacy structural choices and a handful of concrete bugs create real maintainability, correctness, and security risks:

| Priority | Theme | Risk |
|----------|-------|------|
| **P0** | `JsonBType` user type broken (`nullSafeGet` always `null`, `hashCode` always `0`); appears unreferenced | Latent data-loss / dead-code trap |
| **P0** | NPE inside the global `IOException` handler (`ex.getCause()` unguarded) | Exception handler can itself throw |
| **P0** | Catch-all exception handler maps unknown errors to HTTP 400 | Masks server bugs; breaks monitoring/alerting |
| **P0** | Anonymous GET on all `/api/**`; controllers carry almost no `@PreAuthorize` | Unannotated endpoints may leak data |
| **P1** | Swallowed exceptions (empty `catch`, `InterruptedException` not restored) | Silent failures; lost cancellation signal |
| **P1** | `new RestTemplate()` per call in 5+ integration clients | No timeouts/pooling; thread + socket exhaustion |
| **P1** | God classes (`PerusteServiceImpl` ~2,843 lines, 41 injections) | Hard to test, change, and reason about |
| **P1** | Field injection + circular deps masked by `allow-circular-references` | Hidden wiring; brittle startup |
| **P1** | Layer violations (domain → service, controller → repository) | Tight coupling; bypasses auth/tx boundaries |
| **P2** | No `@Valid`/`@Validated` anywhere in `resource/` | Bean Validation never runs at the API edge |
| **P2** | `Page.map()` used for side effects, result discarded | Works by accident; fragile |
| **P2** | Thin controller test coverage (3 IT classes vs ~50 service ITs) | API contract regressions go unnoticed |

Recommended approach: fix the concrete bugs and security/exception-handling issues first, then incrementally decompose the largest service classes and migrate to constructor injection.

---

## What works well

- **Package layout** is clear: `resource` → `service` → `repository` → `domain`
- **Service interfaces** with `@PreAuthorize` on interface methods provide a consistent authorization model
- **Custom validators** per education type (`ValidatorPeruste`, `ValidatorLops2019`, etc.) separate business rules from controllers
- **Navigation builders** use a strategy pattern per curriculum type
- **Lock services** and `AbstractLockController` give a solid concurrent-editing model
- **Envers** + `JpaWithVersioningRepository` support audit trails and pessimistic locking
- **337 Flyway migrations** keep schema evolution explicit
- **Custom repository implementations** (`PerusteRepositoryImpl`) correctly isolate complex Criteria API queries

---

## P0 — Concrete bugs and security issues

### 1. `JsonBType` is broken and (apparently) dead code

**File:** `repository/dialect/JsonBType.java`

```74:76:src/main/java/fi/vm/sade/eperusteet/repository/dialect/JsonBType.java
    public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        return null;
    }
```

Two real problems in one class:

- `nullSafeGet` **always returns `null`** — if any entity field were mapped with this `UserType`, reads of that column would silently come back empty. A data-correctness landmine.
- `hashCode(Object x)` **always returns `0`** (lines 69–71), defeating any hash-based dirty-checking/caching that relies on it.
- A repo-wide search for `JsonBType` finds **only the declaration** — nothing references it. So today it is dead code, but it is the kind of helper someone will wire up later and be burned by.

Also note `e.printStackTrace()` inside `nullSafeSet` (line 47) and `deepCopy` (line 63).

**Recommendation:** Either delete the class, or, if it is meant to be used, implement `nullSafeGet`/`hashCode` correctly (deserialize the column, hash on content) and replace `printStackTrace` with proper logging before re-introducing it.

### 2. Potential NPE in the IOException handler

**File:** `config/ExceptionHandlingConfig.java`

```87:89:src/main/java/fi/vm/sade/eperusteet/config/ExceptionHandlingConfig.java
    public ResponseEntity<Object> clientAbortExceptionHandler(HttpServletRequest request, WebRequest webRequest, IOException ex) throws Exception {
        String exceptionSimpleName = ex.getCause().getClass().getSimpleName();
        if ("ClientAbortException".equals(exceptionSimpleName)) {
```

`ex.getCause()` can be `null` (many `IOException`s have no cause). When it is, the handler throws an NPE, masking the original error and producing a confusing 500.

**Fix:**
```java
Throwable cause = ex.getCause();
if (cause != null && "ClientAbortException".equals(cause.getClass().getSimpleName())) {
    ...
}
```

### 3. Catch-all exception handler returns HTTP 400 for unknown errors

**File:** `config/ExceptionHandlingConfig.java`

```109:115:src/main/java/fi/vm/sade/eperusteet/config/ExceptionHandlingConfig.java
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseStatus rs = e.getClass().getAnnotation(ResponseStatus.class);
        if (rs != null) {
            status = rs.value();
        }
        return handleExceptionInternal(e, null, new HttpHeaders(), status, request);
```

`handleAllExceptions` is the `@ExceptionHandler(Exception.class)` fallback. Any `NullPointerException`, `IllegalStateException`, etc. without a `@ResponseStatus` is returned as **400 Bad Request**, so clients and monitoring treat genuine server faults as client mistakes.

Note: the lower-level `handleExceptionInternal` does default to 500 in its final `else` (lines 201–206), but `handleAllExceptions` passes an explicit `BAD_REQUEST`, so that 500 branch is never reached for truly-unknown exceptions.

**Recommendation:**
- Default to `HttpStatus.INTERNAL_SERVER_ERROR` for unknown exceptions; reserve 400 for the explicitly-classified client-input types.
- Consider migrating to Spring 6 `ProblemDetail` responses (per java-spring-boot skill).

### 4. All GET `/api/**` requests are anonymous at the HTTP layer

**File:** `config/WebSecurityConfiguration.java`

```144:149:src/main/java/fi/vm/sade/eperusteet/config/WebSecurityConfiguration.java
                .authorizeHttpRequests((authorize) -> authorize
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api-docs/external").permitAll()
                    .requestMatchers(HttpMethod.GET, "/").permitAll()
                    .anyRequest().authenticated())
```

Read authorization rests **entirely** on method-level `@PreAuthorize` on service interfaces. That is acceptable *only if every GET path goes through a secured service method*. Controllers have almost no `@PreAuthorize` (only `LiitetiedostoController`, 3 usages), and two confirmed controller→repository shortcuts (finding 9) bypass the service layer entirely. Any future GET endpoint that forgets `@PreAuthorize` is silently public.

**Recommendation:**
1. Audit all GET endpoints, especially in `ExternalController`, `JulkinenController`, and `DokumenttiController`.
2. Document which endpoints are intentionally public vs internal.
3. Restrict `permitAll` to explicit public prefixes (`/api/julkinen/**`, `/api/external/**`, `/actuator/health`); require auth for the rest.
4. Add controller integration tests that verify 401/403 for protected GETs.

### 5. Security headers disabled in production

**File:** `config/WebSecurityConfiguration.java`

```141:141:src/main/java/fi/vm/sade/eperusteet/config/WebSecurityConfiguration.java
                .headers(AbstractHttpConfigurer::disable)
```

Disables HSTS, X-Frame-Options, X-Content-Type-Options, etc.

**Recommendation:** Re-enable a sensible default set, or configure them explicitly at the reverse-proxy layer and document the decision.

### 6. Deprecated password encoder in local dev

**File:** `config/WebSecurityConfigurationDev.java`

Uses `User.withDefaultPasswordEncoder()` (lines 63/109/120) — deprecated, plain-text comparison internally. Local-only, low risk, but trivially replaceable with `BCryptPasswordEncoder`. The file also has unused imports (`StrictHttpFirewall`, `EnableGlobalMethodSecurity`).

---

## P1 — Silent failures, resource handling, and structural debt

### 7. Swallowed exceptions

Three empty/near-empty `catch` blocks hide failures:

**a) `InterruptedException` swallowed without restoring interrupt status**
```116:117:src/main/java/fi/vm/sade/eperusteet/service/impl/LockManagerImpl.java
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            }
```
Catching `InterruptedException` and doing nothing throws away the thread's cancellation signal. At minimum call `Thread.currentThread().interrupt()` and log.

**b) HTTP errors from Koodisto swallowed (and cached)**
```48:50:src/main/java/fi/vm/sade/eperusteet/service/impl/OpintoalaServiceImpl.java
        catch (HttpServerErrorException ex) {
        }
        return new ArrayList<>();
```
A 5xx from the koodisto service becomes an empty list with no log — callers cannot distinguish "no data" from "upstream down", and the empty result is then cached (`@Cacheable("opintoalat")`), so a transient outage can be cached as "empty" until eviction.

**c) Blanket `catch (Exception)` ignored**
```467:468:src/main/java/fi/vm/sade/eperusteet/service/impl/PerusteServiceImpl.java
                } catch (Exception ex) {
                }
```
Inside `taytaLaajuus`; any failure computing `laajuus` is silently dropped.

**Recommendation:** Never leave a `catch` empty. Log at the appropriate level; for `InterruptedException` restore the interrupt; avoid caching error-derived empty results.

### 8. `new RestTemplate()` per call (no timeouts, no pooling)

`RestTemplate` is instantiated on every request in several integration clients:

```110:110:src/main/java/fi/vm/sade/eperusteet/service/impl/KoodistoClientImpl.java
        RestTemplate restTemplate = new RestTemplate();
```
Same pattern at `KoodistoClientImpl` lines 137/162/171/179/188/198, `LokalisointiServiceImpl` 55/69/138, `KoulutusalaServiceImpl` 37, `OpintoalaServiceImpl` 42, `PerusteprojektiServiceImpl` 1031.

Default `RestTemplate` has **no connect/read timeout**, so a slow upstream can pin request threads indefinitely (the Hikari pool is only 30 connections; thread starvation cascades). Re-creating it per call also forgoes connection reuse.

**Recommendation:** Define one (or a few) `RestTemplate`/`RestClient` beans via `RestTemplateBuilder` with explicit connect/read timeouts and a pooling `ClientHttpRequestFactory`, and inject them.

### 9. God class: `PerusteServiceImpl`

**File:** `service/impl/PerusteServiceImpl.java` — **~2,843 lines**, **41** `@Autowired` fields.

Responsibilities mixed in one class: CRUD and search, import/export (ZIP, LOPS 2019), navigation building, publishing coordination, permission checks, file/attachment handling, koodisto integration, validation orchestration.

Confirmed smells:
- **Duplicate injections of the same type:**
  - `LiiteRepository liitteet` (line 284) and `LiiteRepository liiteRepository` (line 290)
  - `KoodistoClient koodistoService` (line 278) and `KoodistoClient koodistoClient` (line 311)
- **Self-injection** for transactional self-invocation:
  ```229:229:src/main/java/fi/vm/sade/eperusteet/service/impl/PerusteServiceImpl.java
      PerusteService self;
  ```

**Recommendation — incremental decomposition:**

| Extract to | Responsibility |
|------------|----------------|
| `PerusteSearchService` | Search, pagination, filtering |
| `PerusteImportService` | ZIP/LOPS import pipelines |
| `PerusteExportService` | Export and document generation coordination |
| `PerusteValidationFacade` | Orchestrates existing `Validator*` classes |
| Keep in `PerusteServiceImpl` | Core CRUD and coordination only |

Quick win first: collapse the duplicate injections to one field each. Then extract the most isolated concern (import/export) behind tests. Replace `self`-invocation with extracted beans so the proxy hop disappears.

### 10. Other oversized service classes

| Class | Lines | `@Autowired` fields |
|-------|-------|-------------------|
| `PerusteprojektiServiceImpl` | ~915 | 27 |
| `JulkaisutServiceImpl` | ~681 | 22 |
| `PerusteenOsaServiceImpl` | ~636 | 19 |
| `PermissionManager` | ~551 | 8 |

`JulkaisutServiceImpl` ↔ `PerusteprojektiServiceImpl` circular dependency is resolved with `@Lazy` and self-injection (`JulkaisutService self`). `PermissionManager` mixes authorization logic with direct repository access and is hard to unit test in isolation.

**Recommendation:** Break the publish/project lifecycle into a dedicated `JulkaisuOrchestrator` or event-driven flow (publish requested → handler) to eliminate the cycle. Split `PermissionManager` into role-based checks vs project-scoped permission queries; inject read-only repositories via constructor.

### 11. Field injection everywhere + circular references masked by config

Constructor injection is the Spring Boot 3.x best practice (testability, immutability, explicit dependencies). The codebase uses field injection almost exclusively (~200+ `@Autowired` on fields), and two flags hide design problems:

```5:6:src/main/resources/application.properties
spring.main.allow-bean-definition-overriding=true
spring.main.allow-circular-references=true
```

`allow-circular-references=true` is a workaround for genuine cycles and makes startup ordering brittle.

**Recommendation:**
- Mandate constructor injection for all new classes; migrate high-churn classes first (`PerusteServiceImpl`, controllers).
- Break cycles (orchestrator or events), then remove both flags.

### 12. Layer violations

#### Domain → service layer

Domain entities import service types:

| Entity | Imports from service layer |
|--------|---------------------------|
| `Peruste.java` | `BusinessRuleViolationException`, `PerusteUtils`, `PerusteIdentifiable` |
| `Koodi.java`, `Oppiaine.java`, `AIPEOppiaine.java`, … | `BusinessRuleViolationException` |
| `Lukko.java`, `JulkaistuPeruste.java` | `SecurityUtil` |
| `RevisionInfo.java` | `AuditRevisionListener` |
| `PerusteenOsaViite.java` | `DtoMapper` |

Domain entities should not throw service-layer exceptions or depend on mappers/security utils.

**Recommendation:**
- Move `BusinessRuleViolationException` to a `domain.exception` or shared `common.exception` package.
- Move validation logic out of entity methods into validators (many `Validator*` classes already exist).
- Keep `SecurityUtil` out of entities — pass principal/context from the service layer.

#### Controller → repository (bypassing service layer)

```113:113:src/main/java/fi/vm/sade/eperusteet/resource/DokumenttiController.java
        Peruste peruste = perusteRepository.findOne(dokumenttiDto.getPerusteId());
```
```252:252:src/main/java/fi/vm/sade/eperusteet/resource/julkinen/ExternalController.java
        return ResponseEntity.ofNullable(julkaisutRepository.findTutkinnonOsaByTutkinnonOsaKoodi(tutkinnonOsaKoodiUri));
```

These reach the repository directly, skipping the `@PreAuthorize`/transaction boundary the rest of the system relies on (compounding finding 4).

**Recommendation:** Move data access behind service methods so authorization and transaction boundaries are always applied.

---

## P2 — API, validation, correctness smells, and consistency

### 13. No Bean Validation at the REST boundary

A search for `@Valid` / `@Validated` across the entire `resource/` package returns **zero** matches. Validation annotations live on entities and are checked ad hoc in services, but the controller pipeline never triggers Jakarta Bean Validation, and the `MethodArgumentNotValidException` branch returns a generic message with no field details:

```146:147:src/main/java/fi/vm/sade/eperusteet/config/ExceptionHandlingConfig.java
        } else if (ex instanceof MethodArgumentNotValidException) {
            describe(map, "palvelin-ei-pystynyt-käsittelemään-pyyntöä", "Palvelin ei pystynyt käsittelemään http-pyyntöä.");
```

**Recommendation (java-spring-boot pattern):**
```java
public record CreatePerusteRequest(
    @NotBlank @Size(max = 200) String nimi,
    @NotNull Koulutustyyppi koulutustyyppi
) {}

@PostMapping
public ResponseEntity<PerusteDto> create(@Valid @RequestBody CreatePerusteRequest request) {
```
- Add validation annotations to DTOs (not entities).
- Add `@Validated` on controllers / `@Valid` on `@RequestBody` params.
- Improve the `MethodArgumentNotValidException` handler to return field-level errors.

### 14. `Page.map()` used for side effects, return discarded

**File:** `service/impl/JulkaisutServiceImpl.java`

```562:570:src/main/java/fi/vm/sade/eperusteet/service/impl/JulkaisutServiceImpl.java
            julkaisut.map(julkaisu -> {
                if (julkaisu.getTutkinnonosa() != null && perusteetTutkinnonosanKoodilla.containsKey(julkaisu.getTutkinnonosa().getKoodiUri())) {
                    if (julkaisu.getPerusteet() == null) {
                        julkaisu.setPerusteet(new ArrayList<>());
                    }
                    julkaisu.getPerusteet().addAll(perusteetTutkinnonosanKoodilla.get(julkaisu.getTutkinnonosa().getKoodiUri()));
                }
                return julkaisu;
            });
```

`Page.map()` returns a **new** `Page`; the returned value here is thrown away. The method "works" only because the lambda mutates the existing element objects in place as a side effect of the discarded mapping. Fragile (breaks the moment the converter returns new instances) and confusing.

**Recommendation:** Replace with an explicit `julkaisut.getContent().forEach(...)` that documents the in-place enrichment intent.

### 15. `printStackTrace` in production code paths

```580:581:src/main/java/fi/vm/sade/eperusteet/service/impl/JulkaisutServiceImpl.java
            e.printStackTrace();
            throw new RuntimeException(e);
```
Also `JsonBType` (lines 47, 63). `printStackTrace` bypasses the logging framework (no correlation, no level control). Replace with `log.error(...)`; prefer a domain exception over bare `RuntimeException`.

### 16. Inconsistent REST style

Mixed patterns across ~54 controllers:
- `@RequestMapping(method = RequestMethod.GET)` vs `@GetMapping`
- Some controllers use `ResponseEntity`, others return raw types
- `DokumenttiController` uses a manual `Logger` instead of `@Slf4j`

**Recommendation:** Adopt a single controller style guide; prefer `@GetMapping`/`@PostMapping`, `ResponseEntity`, and `@Slf4j`.

### 17. Commented-out controllers (dead code)

Six Lukio lock controllers have their `@RestController` commented out:
`LukiokurssiLockController`, `LukioYleisetTavoitteetLockController`, `LukioOppiaineLockController`, `LukioAihekokonaisuudetLockController`, `LukioOpetussuunnitelmaRakenneLockController`, `LukioAihekokonaisuusLockController`.

**Recommendation:** Remove dead code or restore and test; commented controllers confuse readers and static analysis.

### 18. Deprecated configuration

**File:** `application.properties`
```18:19:src/main/resources/application.properties
#deprecated?
spring.mvc.pathmatch.matching-strategy=ant-path-matcher
```
`ant-path-matcher` is deprecated in Spring Boot 3.x; the default `path-pattern-parser` is preferred. Remove this line unless a specific pattern requires the legacy matcher.

---

## P2 — Data access and performance

### 19. Hard-coded enum strings in JPQL

**File:** `repository/PerusteRepository.java`

```java
@Query("SELECT p from Peruste p WHERE ... p.tyyppi = 'NORMAALI' and p.tila = 'VALMIS'")
```

String literals silently break when enums are renamed.

**Recommendation:** Use enum parameters: `p.tyyppi = :tyyppi` with `@Param("tyyppi") PerusteTyyppi tyyppi`.

### 20. JSONPath built by string concatenation in native query

```230:236:src/main/java/fi/vm/sade/eperusteet/repository/JulkaisutRepository.java
                    SELECT jsonb_path_query(data,concat('$.tutkinnonOsat[*] ? (@.koodi.uri == "', :tutkinnonOsaKoodiUri, '")')::jsonpath)
                    ...
                        concat('$.tutkinnonOsat[*].koodi ? (@.uri == "', :tutkinnonOsaKoodiUri, '")')::jsonpath
```

The value is a JDBC bind parameter (so no classic SQL injection), but it is concatenated into a **jsonpath** string. A koodi URI containing a `"` would corrupt the path or allow jsonpath manipulation.

**Recommendation:** Validate/escape the input, or use parameterized jsonpath (`$varname` with a vars object) instead of string building.

### 21. `spring.jpa.open-in-view` not set (defaults to `true`)

`open-in-view` is not present in any `application*.properties` (search returned no matches), so it defaults to `true` — lazy loading during view rendering, holding the session open and inviting N+1. The java-spring-boot skill recommends `false`.

**Recommendation:** Set `spring.jpa.open-in-view=false` explicitly (verify it isn't relied upon by lazy-loading code paths first).

### 22. Second-level cache without documented invalidation strategy

EhCache L2 cache + `@Cacheable` on services. Risk of stale reads after writes if invalidation is incomplete (see also finding 7b, where an error result gets cached).

**Recommendation:** Document cache regions and invalidation points; add tests for cache coherence on publish/update flows.

---

## P2 — Testing gaps

### Current test structure

| Type | Count | Notes |
|------|-------|-------|
| Service integration tests (`*IT.java`) | ~50 | Good coverage at service layer |
| Controller integration tests | **3** | `MaaraysControllerIT`, `ExternalControllerIT`, `ControllerPermissionIT` |
| Unit tests (`*Test.java`) | ~8 | Validators, mapping, domain |

**Framework:** JUnit 4 + `SpringJUnit4ClassRunner` (not JUnit 5)

### Gaps

- ~54 controllers with only 3 controller ITs — API contracts largely untested
- `PerusteServiceImpl` has several ITs but cannot cover all branches of a ~2,843-line class
- `AbstractDbIntegrationTest` requires `db-it-tests=true` profile — may be skipped in CI
- No dedicated repository unit tests (acceptable if service ITs cover query logic)

**Recommendation:**
1. Add `@WebMvcTest` or MockMvc ITs for security-sensitive endpoints first.
2. Expand `ControllerPermissionIT` to cover GET endpoints that should require auth.
3. Plan JUnit 5 migration alongside Spring Boot test modernization.
4. Ensure CI always runs DB integration tests or documents why not.

---

## Suggested phased roadmap

### Phase 1 — bug fixes & quick wins (1–2 sprints)

- [ ] Delete or correctly implement `JsonBType` (finding 1)
- [ ] Guard `ex.getCause()` in `clientAbortExceptionHandler` (finding 2)
- [ ] Default catch-all handler to 500; keep 400 for client-input types (finding 3)
- [ ] Fill in the three empty `catch` blocks; restore interrupt in `LockManagerImpl` (finding 7)
- [ ] Replace `printStackTrace` with logging (finding 15)
- [ ] Remove duplicate `LiiteRepository` / `KoodistoClient` injections in `PerusteServiceImpl` (finding 9)
- [ ] Move `DokumenttiController` and `ExternalController` repository calls behind services (finding 12)
- [ ] Audit GET endpoints for missing `@PreAuthorize`; document public API surface (finding 4)
- [ ] Replace `withDefaultPasswordEncoder` in dev security config (finding 6)

### Phase 2 — robustness & structural improvements (ongoing)

- [ ] Introduce shared `RestTemplate`/`RestClient` beans with timeouts + pooling (finding 8)
- [ ] Add `@Valid` to POST/PUT endpoints with DTO validation; return field-level errors (finding 13)
- [ ] Replace the `Page.map()` side-effect pattern with explicit `forEach` (finding 14)
- [ ] Parameterize enum literals in JPQL; harden the jsonpath concat (findings 19, 20)
- [ ] Extract `PerusteImportService` and `PerusteExportService` from `PerusteServiceImpl` (findings 9, 10)
- [ ] Break `JulkaisutServiceImpl` ↔ `PerusteprojektiServiceImpl` cycle via orchestrator or events (finding 10)
- [ ] Move `BusinessRuleViolationException` to a neutral package; decouple domain from service (finding 12)
- [ ] Introduce constructor injection in new code; migrate one god class at a time (finding 11)

### Phase 3 — hardening and modernization

- [ ] Tighten `WebSecurityConfiguration` path rules (explicit public paths only) (finding 4)
- [ ] Re-enable security headers (finding 5)
- [ ] Set `spring.jpa.open-in-view=false` explicitly (finding 21)
- [ ] Remove `allow-circular-references` after dependency cleanup (finding 11)
- [ ] Remove `ant-path-matcher` (finding 18)
- [ ] Migrate tests to JUnit 5; add controller IT coverage for top 10 API endpoints (testing gaps)
- [ ] Remove commented-out controllers or restore them with tests (finding 17)

---

## Key files reference

| File | Issue(s) |
|------|----------|
| `repository/dialect/JsonBType.java` | `nullSafeGet` returns null, `hashCode` returns 0, dead code, `printStackTrace` |
| `config/ExceptionHandlingConfig.java` | NPE on `getCause()`, 400 default, generic validation message |
| `config/WebSecurityConfiguration.java` | GET `permitAll`, headers disabled |
| `config/WebSecurityConfigurationDev.java` | Deprecated password encoder, unused imports |
| `service/impl/PerusteServiceImpl.java` | God class, duplicate injections, self-proxy, empty catch |
| `service/impl/JulkaisutServiceImpl.java` | Circular deps, `Page.map` side effect, `printStackTrace` |
| `service/impl/PerusteprojektiServiceImpl.java` | Project lifecycle complexity, `new RestTemplate()` |
| `service/impl/OpintoalaServiceImpl.java` | Swallowed HTTP error cached as empty, `new RestTemplate()` |
| `service/impl/LockManagerImpl.java` | `InterruptedException` swallowed without restore |
| `service/impl/KoodistoClientImpl.java` | `new RestTemplate()` per call (×7) |
| `service/security/PermissionManager.java` | Authorization + data access mixed |
| `resource/DokumenttiController.java` | Repository bypass |
| `resource/julkinen/ExternalController.java` | Repository bypass |
| `repository/JulkaisutRepository.java` | JSONPath string concatenation |
| `repository/PerusteRepository.java` | Hard-coded JPQL enum strings |
| `domain/Peruste.java` | Service-layer imports |
| `application.properties` | Circular refs flag, `ant-path-matcher`, `open-in-view` unset |

---

## Review methodology

This plan consolidates two independent review passes that applied the java-spring-boot skill checklists across:

- Layer architecture (Controller → Service → Repository)
- REST API validation and exception handling (`@ControllerAdvice`, `ProblemDetail`)
- Spring Security (`SecurityFilterChain`, `@PreAuthorize`, permit rules)
- Spring Data JPA (repository patterns, query hygiene, JSONB usage)
- Concrete bug hunting (NPE, swallowed exceptions, broken `UserType`, resource handling)
- Actuator and production configuration
- Test structure and coverage balance

Every line reference was read directly from source on the review date. Counts (`@Autowired` in `PerusteServiceImpl` = 41; `@Valid`/`@Validated` in `resource/` = 0; `new RestTemplate()` occurrences; commented `@RestController` files = 6) were produced by repository-wide search and confirmed against the cited files.
