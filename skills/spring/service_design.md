## TL;DR - Quick Reference

### Standard Service Setup
```java
public interface MyService { ... }

@Service @RequiredArgsConstructor
public class MyServiceImpl implements MyService { ... }
```

### Critical Rules
1. **Always split interface/impl** — inject the interface, hide the implementation.
2. **Method Order**: Validate → Load → Mutate → Save → Return.
3. **Transactions**: Use `@Transactional(readOnly = true)` for GET, and `@Transactional` for POST/PUT/DELETE.
4. **Exceptions**: Throw domain-specific exceptions (ResourceNotFound, AlreadyExists).
5. **Mapping**: Use private `mapToResponse()` or a dedicated Mapper.

### 📄 Templates
- [Standard Service Template](./templates/ServiceTemplate.java)

---

## Core Principles

### 1. Interface + Impl Separation

**Always split service into interface and implementation:**

```
modules/
└── tutor/
    └── service/
        ├── TutorService.java        ← interface (contract)
        └── impl/
            └── TutorServiceImpl.java ← implementation
```

**Interface — defines the contract:**
```java
public interface TutorService {

    Page<TutorResponse> getAllTutors(String search, String status, Pageable pageable);

    TutorResponse getTutorById(Long id);

    TutorResponse createTutor(TutorRequest request);

    TutorResponse updateTutor(Long id, TutorRequest request);

    TutorStatsDTO getTutorStats(Long id);

    void deleteTutor(Long id);
}
```

**Implementation:**
```java
@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TutorResponse getTutorById(Long id) {
        // ...
    }
    // all other methods...
}
```

**Why interface + impl:**

| Concern | Benefit |
|---|---|
| Testing | Mock the interface, not the concrete class |
| Swapping implementations | Add `CachedTutorServiceImpl` without touching callers |
| Clear contract | Interface shows what the service does — impl shows how |
| Bug fixing | Know exactly where to look — interface = what, impl = where |
| Dependency injection | `@Autowired TutorService` injects impl automatically |

// Bad: controller tightly coupled to implementation
@RequiredArgsConstructor
public class TutorController {
    private final TutorServiceImpl tutorService;
}

// Good: controller depends on contract, not implementation
@RequiredArgsConstructor
public class TutorController {
    private final TutorService tutorService;
}
```

---

### 2. Class-Level Annotations

**Standard annotation set for every service impl:**
```java
@Service
@RequiredArgsConstructor
@Slf4j              // if the service needs logging
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    // other dependencies via constructor injection
}
```

**Rationale:**

| Annotation | Reason |
|---|---|
| `@Service` | Marks as Spring-managed bean, enables component scan |
| `@RequiredArgsConstructor` | Lombok constructor injection — no `@Autowired` needed |
| `@Slf4j` | Lombok logger — `log.info(...)`, `log.error(...)` |

**Bad: Don't use field injection:**
```java
// Bad: field injection hides dependencies, harder to test
@Autowired
private TutorRepository tutorRepository;

// Good: constructor injection via @RequiredArgsConstructor
private final TutorRepository tutorRepository;
```

---

### 3. Transaction Management

**Rule: `@Transactional` belongs on the service layer — not repositories or controllers.**

```java
// Write operations — default propagation (REQUIRED)
@Override
@Transactional
public TutorResponse createTutor(TutorRequest request) { ... }

@Override
@Transactional
public TutorResponse updateTutor(Long id, TutorRequest request) { ... }

@Override
@Transactional
public void deleteTutor(Long id) { ... }

// Read operations — readOnly = true (performance optimization)
@Override
@Transactional(readOnly = true)
public Page<TutorResponse> getAllTutors(String search, String status, Pageable pageable) { ... }

@Override
@Transactional(readOnly = true)
public TutorResponse getTutorById(Long id) { ... }
```

**`readOnly = true` benefits:**
- Hibernate skips dirty checking (no need to track changes)
- Some databases route read-only transactions to read replicas
- Prevents accidental writes in read methods

**When to use `@Transactional`:**

| Scenario | Annotation |
|---|---|
| Single write operation | `@Transactional` |
| Multiple writes that must be atomic | `@Transactional` — rolls back all on failure |
| Read + write in one method | `@Transactional` |
| Read-only (no writes) | `@Transactional(readOnly = true)` |
| Multiple reads, no writes | `@Transactional(readOnly = true)` |

// Good: Both saves are atomic — if tutorRepository.save() fails, userRepository.save() rolls back
@Transactional
public TutorResponse createTutor(TutorRequest request) {
    User user = userRepository.save(newUser);   // step 1
    Tutor tutor = tutorRepository.save(newTutor); // step 2 — if this fails, step 1 rolls back
    return mapToResponse(tutor);
}
```

---

### 4. Validation Flow

**Standard order inside a write method: validate → load → mutate → save → return.**

```java
@Override
@Transactional
public TutorResponse createTutor(TutorRequest request) {

    // ─── Step 1: Validate uniqueness / preconditions ──────────────────────
    if (tutorRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new AlreadyExistsException("Tutor with this email already exists: " + request.getEmail());
    }

    // ─── Step 2: Conditional logic / load related entities ───────────────
    User user;
    if (request.getUserId() != null) {
        user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        if (!user.getEmail().equals(request.getEmail())) {
            throw new InvalidInputException("User email does not match Tutor email");
        }

        if (tutorRepository.findByUserId(user.getId()).isPresent()) {
            throw new AlreadyExistsException("User already has a Tutor profile");
        }
    } else {
        // ─── Step 3: Create dependent resources if needed ─────────────────
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new InvalidInputException("Password is required and must be at least 8 characters");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AlreadyExistsException("User account with this email already exists");
        }

        user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.TUTOR)
                .enabled(true)
                .accountNonLocked(true)
                .build();
        user = userRepository.save(user);
    }

    // ─── Step 4: Build and save the main entity ───────────────────────────
    Tutor tutor = Tutor.builder()
            .user(user)
            .fullName(request.getFullName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .subscriptionPlan(request.getSubscriptionPlan())
            .subscriptionStatus(request.getSubscriptionStatus() != null
                    ? request.getSubscriptionStatus()
                    : "ACTIVE")
            .build();

    // ─── Step 5: Return mapped response ──────────────────────────────────
    return mapToResponse(tutorRepository.save(tutor));
}
```

**The 5-step pattern:**

| Step | Action | Example |
|---|---|---|
| 1. Validate | Check preconditions, uniqueness | `existsByEmail` → throw |
| 2. Load | Fetch related entities | `findById` → `orElseThrow` |
| 3. Mutate | Build or update the entity | `Entity.builder()...` or `entity.setX(...)` |
| 4. Save | Persist to DB | `repository.save(entity)` |
| 5. Return | Map to response DTO | `mapToResponse(saved)` |

---

### 5. Update Pattern

**Load → verify ownership → check uniqueness of changed fields → mutate → save:**
```java
@Override
@Transactional
public TutorResponse updateTutor(Long id, TutorRequest request) {

    // 1. Load — fail fast if not found
    Tutor tutor = tutorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor not found: " + id));

    // 2. Uniqueness check — only if the field actually changed
    if (!tutor.getEmail().equals(request.getEmail())) {
        tutorRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new AlreadyExistsException("Email already in use: " + request.getEmail());
        });
    }

    // 3. Mutate — only update what the request provides
    tutor.setFullName(request.getFullName());
    tutor.setEmail(request.getEmail());
    tutor.setPhone(request.getPhone());
    tutor.setSubscriptionPlan(request.getSubscriptionPlan());

    // Conditional update — only if provided
    if (request.getSubscriptionStatus() != null) {
        tutor.setSubscriptionStatus(request.getSubscriptionStatus());
    }

    // 4. Save + return
    return mapToResponse(tutorRepository.save(tutor));
}
```

**Key patterns in update:**

| Pattern | Reason |
|---|---|
| Skip uniqueness check if field unchanged | Avoid false conflict on self |
| Only set fields provided in request | Partial update — don't overwrite unrelated data |
| `orElseThrow` on load | Fail fast, consistent error |

---

### 6. Exception Usage

**Use domain-specific exceptions — never raw `RuntimeException`:**
```java
// Bad: loses context, generic message
throw new RuntimeException("Tutor not found with id: " + id);

// Good: specifically handled exception type
throw new ResourceNotFoundException("Tutor not found with id: " + id);   // -> 404
throw new AlreadyExistsException("Email already exists: " + email);       // -> 409
throw new InvalidInputException("Password must be at least 8 characters"); // -> 400
```

**Common exception → HTTP status mapping:**

| Exception | HTTP Status | Use Case |
|---|---|---|
| `ResourceNotFoundException` | 404 | Entity not found by ID |
| `AlreadyExistsException` | 409 | Duplicate email, duplicate name |
| `InvalidInputException` | 400 | Business rule violation |
| `AccessDeniedException` | 403 | Wrong owner, insufficient permission |

*See `skills/spring/error_handling.md` for the full `GlobalExceptionHandler` setup.*

---

### 7. The `mapToResponse` Pattern

**Private mapping method — keep it in the service, close to the data:**
```java
private TutorResponse mapToResponse(Tutor tutor) {
    // Defensive check — catch lazy-load misses early
    if (tutor.getUser() == null) {
        throw new IllegalStateException("Tutor.user not loaded for id: " + tutor.getId());
    }

    return TutorResponse.builder()
            .id(tutor.getId())
            .userId(tutor.getUser().getId())
            .fullName(tutor.getFullName())
            .email(tutor.getEmail())
            .phone(tutor.getPhone())
            .subscriptionPlan(tutor.getSubscriptionPlan())
            .subscriptionStatus(tutor.getSubscriptionStatus())
            .createdAt(tutor.getCreatedAt())
            .updatedAt(tutor.getUpdatedAt())
            .build();
}
```

**Why private, not a separate mapper class:**

| Approach | Use When |
|---|---|
| Private `mapToResponse()` in service | Simple mapping, single service uses it |
| Dedicated `TutorMapper` class | Complex mapping, reused across services |
| MapStruct | Large project, many entities, zero boilerplate |

**Defensive null check on lazy-loaded relations:**
```java
// If Tutor.user was not fetched (wrong query / missing JOIN FETCH),
// this gives a clear error instead of cryptic LazyInitializationException
if (tutor.getUser() == null) {
    throw new IllegalStateException("Tutor.user not loaded for id: " + tutor.getId());
}
```

---

### 8. Filtering + Pagination Pattern

**Branch on filter combinations — delegate to specific repository methods:**
```java
@Override
@Transactional(readOnly = true)
public Page<TutorResponse> getAllTutors(String search, String status, Pageable pageable) {

    Page<Tutor> tutors;

    boolean hasSearch = search != null && !search.isBlank();
    boolean hasStatus = status != null && !status.isBlank();

    if (hasSearch && hasStatus) {
        tutors = tutorRepository.searchByNameOrEmailAndStatus(search, status, pageable);
    } else if (hasSearch) {
        tutors = tutorRepository.searchByNameOrEmail(search, pageable);
    } else if (hasStatus) {
        tutors = tutorRepository.findBySubscriptionStatus(status, pageable);
    } else {
        tutors = tutorRepository.findAll(pageable);
    }

    return tutors.map(this::mapToResponse);
}
```

**`Page.map()` — transform Page contents without losing pagination metadata:**
// Good: map() preserves total count, page number, page size
return tutors.map(this::mapToResponse);

// Bad: Manual stream — loses all pagination metadata
return tutors.getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
```

---

### 9. Aggregation / Stats Methods

**Null-safe aggregation — always handle null returns from repository queries:**
```java
@Override
@Transactional(readOnly = true)
public TutorStatsDTO getTutorStats(Long id) {

    // Validate existence first
    tutorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor not found: " + id));

    // Safe aggregation — treat null as 0
    String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    int sessionCount = Optional.ofNullable(
            sessionRecordRepository.sumSessionsByMonthAndTutorId(currentMonth, id)
    ).orElse(0);

    DashboardStats stats = sessionRecordRepository.getFinanceSummaryByTutorId(currentMonth, id);
    long paid   = stats != null && stats.getTotalPaidRaw()   != null ? stats.getTotalPaidRaw()   : 0L;
    long unpaid = stats != null && stats.getTotalUnpaidRaw() != null ? stats.getTotalUnpaidRaw() : 0L;

    return TutorStatsDTO.builder()
            .studentCount((int) studentRepository.countByTutorIdAndActiveTrue(id))
            .sessionCount(sessionCount)
            .totalRevenue((double) (paid + unpaid))
            .build();
}
```

**Rules for aggregation methods:**
- Always validate entity existence before querying stats
- Wrap nullable aggregation results with `Optional.ofNullable(...).orElse(0)`
- Use `COALESCE` in the repository query as first line of defence (see `repository_design.md`)
- `@Transactional(readOnly = true)` — stats are always read-only

---

### 10. Naming Conventions

**Method names on the interface:**

| Pattern | Example |
|---|---|
| `get{Entity}ById` | `getTutorById` |
| `getAll{Entities}` | `getAllTutors` |
| `create{Entity}` | `createTutor` |
| `update{Entity}` | `updateTutor` |
| `delete{Entity}` | `deleteTutor` |
| `get{Entity}Stats` | `getTutorStats` |
| `get{Entity}By{Field}` | `getTutorByEmail` |
| `{action}{Entity}` | `activateTutor`, `suspendTutor` |

**Class names:**

| Class | Naming |
|---|---|
| Interface | `TutorService` |
| Implementation | `TutorServiceImpl` |
| Package (interface) | `modules/tutor/service/` |
| Package (impl) | `modules/tutor/service/impl/` |

---

## Quick Reference — Service Method Checklist

```
New service method?
        │
        ├─► Read method?
        │       ├─► Add @Transactional(readOnly = true)
        │       ├─► Use orElseThrow() — never return null
        │       └─► Return Response DTO — never raw entity
        │
        ├─► Write method (create)?
        │       ├─► Add @Transactional
        │       ├─► Step 1: validate uniqueness + preconditions
        │       ├─► Step 2: load related entities with orElseThrow
        │       ├─► Step 3: build entity
        │       ├─► Step 4: save
        │       └─► Step 5: return mapToResponse()
        │
        ├─► Write method (update)?
        │       ├─► Add @Transactional
        │       ├─► Load entity → orElseThrow
        │       ├─► Check uniqueness of changed fields only
        │       ├─► Mutate only provided fields
        │       └─► Save + return mapToResponse()
        │
        └─► Aggregation / stats?
                ├─► Add @Transactional(readOnly = true)
                ├─► Validate existence first
                └─► Null-safe all aggregation results
```

---

## Full Structure Template

See [ServiceTemplate.java](./templates/ServiceTemplate.java) for a complete Service Interface/Impl boilerplate.

---

## Common Patterns Summary

### DO's:

1. **Split into interface + impl** — `TutorService` + `TutorServiceImpl` in `impl/`
2. **Inject the interface**, not the impl, in controllers and other services
3. **Use `@Transactional(readOnly = true)`** on all read methods
4. **Use `@Transactional`** on all write methods with multiple steps
5. **Follow the 5-step create pattern** — validate → load → mutate → save → return
6. **Throw domain-specific exceptions** — never raw `RuntimeException`
7. **Use `orElseThrow()`** — never return `null` from a find method
8. **Skip uniqueness check when field hasn't changed** in update methods
9. **Use `Page.map()`** to transform paginated results — preserves metadata
10. **Add defensive null check** in `mapToResponse()` for lazy-loaded relations

### DON'Ts:

1. **Don't put `@Service` on the interface** — only on the impl
2. **Don't inject `TutorServiceImpl` directly** — always inject `TutorService`
3. **Don't throw raw `RuntimeException`** — use domain-specific exceptions
4. **Don't return `null`** from service methods — throw or return `Optional`
5. **Don't put business logic in controllers** — service layer owns validation and flow
6. **Don't put `@Transactional` on repositories** — service owns the transaction boundary
7. **Don't skip `@Transactional` on multi-step writes** — partial failure leaves dirty state
8. **Don't map entity → DTO in the controller** — `mapToResponse()` stays in the service
9. **Don't skip the uniqueness-unchanged check in updates** — causes false 409 on self
10. **Don't call `getContent().stream()` on a Page** — use `Page.map()` to keep metadata

---

## Related Skills

- **Entity Design**: `skills/spring/entity_design.md`
- **DTO Design**: `skills/spring/dto_design.md`
- **Repository Design**: `skills/spring/repository_design.md`
- **Error Handling**: `skills/spring/error_handling.md`
- **Security Configuration**: `skills/spring/security_config.md`

---

**Last Updated:** 2025-02-13
**Status: Production-ready (generic, reusable across projects)
