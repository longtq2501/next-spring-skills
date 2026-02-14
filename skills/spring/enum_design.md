## TL;DR - Quick Reference

### Standard Enum Setup
```java
@Getter
public enum MyStatus {
    ACTIVE("Đang hoạt động"),
    INACTIVE("Ngưng hoạt động");

    private final String displayName;
    MyStatus(String displayName) { this.displayName = displayName; }
}
```

### Critical Rules
1. **Always use `EnumType.STRING`** in JPA entities.
2. **Add boolean helpers** like `isTerminal()` or `isCancelled()` to encode business rules.
3. **Use FSM pattern** (Map of transitions) for complex status flows.
4. **Never use `Ordinal`** — it corrupts data if order changes.
5. **Serialize to `displayName`** or map in DTO for UI labels.

### 📄 Templates
- [Standard Enum Template](./templates/EnumTemplate.java)

---

## Core Principles

### 1. Basic Structure

**Standard enum with display name:**
```java
@Getter  // Lombok — generates getDisplayName()
public enum LessonStatus {

    SCHEDULED("Đã hẹn"),
    CONFIRMED("Đã xác nhận"),
    COMPLETED("Đã dạy"),
    PAID("Đã thanh toán"),
    PENDING_PAYMENT("Chờ xác nhận thanh toán"),
    CANCELLED_BY_STUDENT("Học sinh hủy"),
    CANCELLED_BY_TUTOR("Tutor hủy");

    private final String displayName;

    LessonStatus(String displayName) {
        this.displayName = displayName;
    }
}
```

**Why add a `displayName` field:**

| Without displayName | With displayName |
|---|---|
| UI renders `PENDING_PAYMENT` | UI renders `Chờ xác nhận thanh toán` |
| Frontend must maintain its own label map | Label travels with the enum |
| Risk of label drift between FE and BE | Single source of truth |

**Use `@Getter` (Lombok) instead of writing the getter manually:**
```java
// Bad: Manual boilerplate getter
public String getDisplayName() { return displayName; }

// Good: Clean Lombok annotation
@Getter
public enum LessonStatus { ... }
```

---

### 2. State Classification Methods

**Add boolean methods to encode business rules directly on the enum:**
```java
/**
 * Terminal states — no further transitions allowed.
 */
public boolean isTerminal() {
    return this == PAID ||
           this == CANCELLED_BY_STUDENT ||
           this == CANCELLED_BY_TUTOR;
}

/**
 * Lesson has been taught (regardless of payment status).
 */
public boolean isCompleted() {
    return this == COMPLETED ||
           this == PENDING_PAYMENT ||
           this == PAID;
}

/**
 * Payment has been received.
 */
public boolean isPaid() {
    return this == PAID;
}

/**
 * Lesson was cancelled by any party.
 */
public boolean isCancelled() {
    return this == CANCELLED_BY_STUDENT ||
           this == CANCELLED_BY_TUTOR;
}
```

**Why methods on the enum vs in the service:**
```java
// ❌ Business rule scattered in service — duplicated across methods
if (status == LessonStatus.PAID ||
    status == LessonStatus.CANCELLED_BY_STUDENT ||
    status == LessonStatus.CANCELLED_BY_TUTOR) {
    throw new InvalidInputException("Cannot modify a terminal session");
}

// ✅ Rule lives on the enum — one place, reusable everywhere
if (session.getStatus().isTerminal()) {
    throw new InvalidInputException("Cannot modify a terminal session");
}
```

**Benefits:**
- Single source of truth — add `ARCHIVED` to terminal later, only update `isTerminal()`
- Readable service code — `if (status.isCancelled())` reads like English
- No duplication — the same condition isn't copy-pasted across 5 service methods

---

### 3. Finite State Machine (FSM) Pattern

**Define valid transitions explicitly when status changes must be controlled:**
```java
@Getter
public enum LessonStatus {

    SCHEDULED("Đã hẹn"),
    CONFIRMED("Đã xác nhận"),
    COMPLETED("Đã dạy"),
    PENDING_PAYMENT("Chờ xác nhận thanh toán"),
    PAID("Đã thanh toán"),
    CANCELLED_BY_STUDENT("Học sinh hủy"),
    CANCELLED_BY_TUTOR("Tutor hủy");

    private final String displayName;

    // Define which statuses this one can transition TO
    private static final Map<LessonStatus, Set<LessonStatus>> ALLOWED_TRANSITIONS = Map.of(
        SCHEDULED,        Set.of(CONFIRMED, CANCELLED_BY_STUDENT, CANCELLED_BY_TUTOR),
        CONFIRMED,        Set.of(COMPLETED, CANCELLED_BY_STUDENT, CANCELLED_BY_TUTOR),
        COMPLETED,        Set.of(PENDING_PAYMENT, CANCELLED_BY_TUTOR),
        PENDING_PAYMENT,  Set.of(PAID, COMPLETED),   // COMPLETED = reject payment
        PAID,             Set.of(),                   // terminal
        CANCELLED_BY_STUDENT, Set.of(),               // terminal
        CANCELLED_BY_TUTOR,   Set.of()                // terminal
    );

    LessonStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Validates and performs a status transition.
     * @throws InvalidInputException if the transition is not allowed.
     */
    public LessonStatus transitionTo(LessonStatus next) {
        if (!ALLOWED_TRANSITIONS.get(this).contains(next)) {
            throw new InvalidInputException(
                String.format("Cannot transition from %s to %s", this.displayName, next.displayName)
            );
        }
        return next;
    }

    public boolean canTransitionTo(LessonStatus next) {
        return ALLOWED_TRANSITIONS.get(this).contains(next);
    }
}
```

**Usage in service layer:**
```java
// ✅ Transition validated by the enum itself
LessonStatus newStatus = session.getStatus().transitionTo(LessonStatus.COMPLETED);
session.setStatus(newStatus);

// ✅ Check before acting
if (!session.getStatus().canTransitionTo(newStatus)) {
    throw new InvalidInputException("Invalid status transition");
}
```

**When to use FSM:**

| Situation | Use FSM? |
|---|---|
| Status has 3+ values with business rules | ✅ Yes |
| Not all transitions are valid | ✅ Yes |
| Simple 2-state toggle (active/inactive) | ❌ Overkill |
| Status is informational only | ❌ Not needed |

---

### 4. JPA Integration

**Always use `EnumType.STRING` on entity fields — never `ORDINAL`:**
```java
@Entity
public class SessionRecord {

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50)             // match your longest enum name
    private LessonStatus status = LessonStatus.SCHEDULED;
}
```

**Why `STRING` over `ORDINAL`:**
```java
// ORDINAL stores: SCHEDULED=0, CONFIRMED=1, COMPLETED=2 ...
// Adding RESCHEDULED between CONFIRMED and COMPLETED:
// RESCHEDULED=2, COMPLETED=3 → all existing COMPLETED rows now read as RESCHEDULED ❌

// STRING stores: "SCHEDULED", "CONFIRMED", "COMPLETED" ...
// Adding RESCHEDULED: no impact on existing data ✅
```

**Set a sensible default on the entity field:**
```java
// ✅ New records start as SCHEDULED — no null checks needed
@Builder.Default
@Enumerated(EnumType.STRING)
@Column(length = 50)
private LessonStatus status = LessonStatus.SCHEDULED;
```

---

### 5. JSON Integration (API Layer)

**Spring Boot serializes enums as their name by default — add `@JsonValue` for display name:**
```java
// Default serialization: { "status": "PENDING_PAYMENT" }
// With @JsonValue:        { "status": "Chờ xác nhận thanh toán" }

@Getter
public enum LessonStatus {

    PENDING_PAYMENT("Chờ xác nhận thanh toán");

    private final String displayName;

    // ✅ API returns displayName, not enum constant name
    @JsonValue
    public String getDisplayName() { return displayName; }

    // ✅ Deserialize from displayName string back to enum
    @JsonCreator
    public static LessonStatus fromDisplayName(String value) {
        for (LessonStatus status : values()) {
            if (status.displayName.equals(value)) return status;
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
```

**Serialization strategy comparison:**

| Strategy | API sends | Frontend receives | Use When |
|---|---|---|---|
| Default (none) | `"PENDING_PAYMENT"` | Constant name | Internal APIs, developer tools |
| `@JsonValue` on `displayName` | `"Chờ xác nhận..."` | Human-readable | Consumer-facing APIs |
| Response DTO mapping | Transform in mapper | Either format | Most flexible — recommended |

**Recommended approach — map in Response DTO, keep enum clean:**
```java
// ✅ Preferred — transform at DTO boundary, enum stays pure
@Data
@Builder
public class SessionResponse {
    private String status;          // "Đã dạy"
    private String statusCode;      // "COMPLETED" — for frontend logic
}

// In service/mapper
response.setStatus(session.getStatus().getDisplayName());
response.setStatusCode(session.getStatus().name());
```

---

### 6. Enum with Additional Metadata

**Extend the pattern when each value needs more than a display name:**
```java
@Getter
public enum DocumentCategory {

    LECTURE_NOTES("Bài giảng",     "lecture",  true),
    EXERCISES(    "Bài tập",       "exercise", true),
    EXAM_PREP(    "Ôn thi",        "exam",     true),
    INTERNAL(     "Nội bộ",        "internal", false);  // not visible to students

    private final String displayName;
    private final String slug;           // URL-safe identifier
    private final boolean visibleToStudent;

    DocumentCategory(String displayName, String slug, boolean visibleToStudent) {
        this.displayName = displayName;
        this.slug = slug;
        this.visibleToStudent = visibleToStudent;
    }

    public static DocumentCategory fromSlug(String slug) {
        for (DocumentCategory cat : values()) {
            if (cat.slug.equals(slug)) return cat;
        }
        throw new IllegalArgumentException("Unknown category slug: " + slug);
    }

    public static List<DocumentCategory> studentVisible() {
        return Arrays.stream(values())
                .filter(DocumentCategory::isVisibleToStudent)
                .collect(Collectors.toList());
    }
}
```

---

### 7. Lookup Methods (fromValue)

**Add static factory methods for safe deserialization from external values:**
```java
@Getter
public enum LessonStatus {

    SCHEDULED("Đã hẹn"),
    // ...

    private final String displayName;

    // Safe lookup by name — returns Optional instead of throwing
    public static Optional<LessonStatus> fromName(String name) {
        try {
            return Optional.of(LessonStatus.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // Strict lookup — throws with clear message
    public static LessonStatus fromNameOrThrow(String name) {
        return fromName(name)
                .orElseThrow(() -> new InvalidInputException(
                        "Invalid lesson status: '" + name + "'. " +
                        "Valid values: " + Arrays.toString(values())));
    }
}
```

**Usage:**
```java
// From request param or path variable
LessonStatus status = LessonStatus.fromNameOrThrow(request.getStatus());

// Safe check without exception
LessonStatus.fromName(rawValue).ifPresent(session::setStatus);
```

---

### 8. Naming Conventions

**Enum class names:**

| Pattern | Example |
|---|---|
| State/lifecycle | `LessonStatus`, `OrderStatus`, `PaymentStatus` |
| Category/type | `DocumentCategoryType`, `UserRole`, `NotificationType` |
| Mode/strategy | `InvoiceMode`, `PaymentMethod` |
| Always PascalCase | ✅ `LessonStatus` — never `lesson_status` |

**Enum constant names:**

| Rule | Example |
|---|---|
| UPPER_SNAKE_CASE | `PENDING_PAYMENT`, `CANCELLED_BY_STUDENT` |
| Descriptive, not abbreviated | `CANCELLED_BY_STUDENT` not `CANC_STU` |
| Suffix terminal states with actor | `CANCELLED_BY_STUDENT`, `CANCELLED_BY_TUTOR` |
| Prefix pending states | `PENDING_PAYMENT`, `PENDING_REVIEW` |

**Boolean method names:**

| Pattern | Example |
|---|---|
| `is{State}()` | `isTerminal()`, `isCancelled()`, `isPaid()` |
| `is{Adjective}()` | `isCompleted()`, `isActive()` |
| `can{Action}()` | `canTransitionTo()`, `canBeEdited()` |

---

### 9. Javadoc on Enums

**Document the enum class, terminal states, and non-obvious constants:**
```java
/**
 * Lifecycle states of a tutoring session.
 *
 * State transitions follow an FSM pattern:
 * SCHEDULED → CONFIRMED → COMPLETED → PENDING_PAYMENT → PAID
 *
 * Terminal states (no further transitions): PAID, CANCELLED_BY_STUDENT, CANCELLED_BY_TUTOR
 */
@Getter
public enum LessonStatus {

    /**
     * Initial state — session scheduled but not yet confirmed by student.
     */
    SCHEDULED("Đã hẹn"),

    /**
     * Payment received. Terminal state — no further transitions allowed.
     */
    PAID("Đã thanh toán"),

    /**
     * Cancelled by student. Terminal state.
     */
    CANCELLED_BY_STUDENT("Học sinh hủy");
}
```

**What to document:**

| Document | Skip |
|---|---|
| Class-level: overall FSM flow | Self-explanatory constants (`ACTIVE`, `INACTIVE`) |
| Terminal states — explicit warning | Constants whose name = display name |
| Constants with non-obvious business meaning | Getters generated by Lombok |
| `@since` for newly added constants | `isCancelled()` if comment = code |

---

## Full Enum Template

See [EnumTemplate.java](./templates/EnumTemplate.java) for a complete FSM-capable enum boilerplate.

---

## Common Patterns Summary

### ✅ DO's:

1. **Add `displayName` field** — single source of truth for UI labels
2. **Use `@Getter` (Lombok)** — no manual getter boilerplate
3. **Add boolean classification methods** (`isTerminal()`, `isCancelled()`) — encode rules on the enum
4. **Use `EnumType.STRING` on JPA fields** — never `ORDINAL`
5. **Set default value on entity field** (`@Builder.Default`) — avoid null status
6. **Use FSM `transitionTo()`** when not all transitions are valid
7. **Add `fromNameOrThrow()`** for safe deserialization with clear error messages
8. **Mark terminal states clearly** in Javadoc
9. **Name constants in UPPER_SNAKE_CASE** — descriptive, include actor if relevant
10. **Transform to displayName at DTO boundary** — keep enum serialization clean

### ❌ DON'Ts:

1. **Don't use `EnumType.ORDINAL`** — silently corrupts data when enum is reordered
2. **Don't scatter status-check conditions across service methods** — put them on the enum
3. **Don't use `Enum.valueOf()` directly** without try-catch — throws unchecked exception
4. **Don't abbreviate constant names** — `CANCELLED_BY_STUDENT` not `CANC_STU`
5. **Don't add mutable state to enums** — enums are singletons, shared across all threads
6. **Don't skip default value on entity field** — null status causes NPE in classification methods
7. **Don't skip terminal state documentation** — future developers need to know transitions stop here
8. **Don't use `@Data` on enums** — Lombok `@Data` is for classes, not enums
9. **Don't return raw enum names in API responses** — map to displayName in response DTO
10. **Don't add too many responsibilities to one enum** — if it needs a service dependency, extract to a service

---

## Related Skills

- **Entity Design**: `skills/spring/entity_design.md`
- **DTO Design**: `skills/spring/dto_design.md`
- **Error Handling**: `skills/spring/error_handling.md`
- **Repository Design**: `skills/spring/repository_design.md`

---

**Last Updated:** 2025-02-13
**Status:** ✅ Production-ready (generic, reusable across projects)