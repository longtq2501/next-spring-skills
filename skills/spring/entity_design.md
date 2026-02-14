## TL;DR - Quick Reference

### Standard Entity Setup
```java
@Entity
@Table(name = "snake_case_table")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"lazyRelation1", "lazyRelation2"})
public class MyEntity extends BaseEntity { ... }
```

### Critical Rules
1. **Always use `FetchType.LAZY`** for `@ManyToOne` and `@OneToOne`.
2. **Never use `@Data`** on entities (circular refs & N+1).
3. **Always use `EnumType.STRING`** for enums.
4. **Initialize Collections** to `new ArrayList<>()` or `new HashSet<>()`.
5. **Use `Set` + `@BatchSize`** for `@ManyToMany` to avoid bulk-delete bugs.

### 📄 Templates
- [Standard Entity Template](./templates/EntityTemplate.java)

---

## Core Principles

### 1. Class-Level Annotations

**Standard annotation set for every entity:**
```java
@Entity
@Table(name = "documents")   // explicit snake_case table name
@Getter
@Setter
@NoArgsConstructor           // required by JPA spec
@AllArgsConstructor
@Builder
public class Document { }
```

**Rationale for each:**

| Annotation | Reason |
|---|---|
| `@Entity` | Marks class as JPA-managed |
| `@Table(name = "...")` | Explicit name — don't rely on Hibernate's default naming |
| `@Getter` / `@Setter` | Lombok — no boilerplate |
| `@NoArgsConstructor` | **Required by JPA** — Hibernate needs to instantiate entities via reflection |
| `@AllArgsConstructor` | Needed alongside `@Builder` |
| `@Builder` | Fluent object construction in service layer |

// Bad: generates equals/hashCode using all fields which causes infinite loops
@Data
@Entity
public class Document { }

// Good: use specific Lombok annotations to avoid circular references
@Getter
@Setter
@Entity
public class Document { }
```

**`@ToString(exclude)` — prevent infinite loops and lazy-load explosions:**
```java
// Good: Exclude all lazy relations and collections
@ToString(exclude = {"lessons", "documents", "student"})
@Entity
public class SessionRecord { }
```

Without `exclude`, calling `toString()` (e.g. in logs) will:
- Trigger lazy loading of every relation → unexpected N+1 queries
- Cause `StackOverflowError` on bidirectional relationships

**Rule of thumb:** exclude every `@ManyToOne`, `@OneToMany`, `@ManyToMany` field from `@ToString`.

---

### 2. Primary Key

**Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for most cases:**
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**Strategy comparison:**

| Strategy | Behavior | Use Case |
|---|---|---|
| `IDENTITY` | DB auto-increment | Yes - Default for MySQL, PostgreSQL |
| `SEQUENCE` | DB sequence object | PostgreSQL at high insert volume |
| `UUID` | App-generated UUID | Public-facing IDs (hide row count) |
| `AUTO` | Hibernate decides | No - Avoid as it is unpredictable across DBs |

**Optional: UUID for public-facing APIs:**
```java
// Hide internal row counts from API consumers
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

---

### 3. Database Indexing (Code-First)

**Define indexes directly on the entity — no separate migration scripts needed:**
```java
@Entity
@Table(name = "session_records", indexes = {
        // Single-column index — for filtering by student
        @Index(name = "idx_session_student_id", columnList = "student_id"),

        // Single-column index — for filtering by month
        @Index(name = "idx_session_month", columnList = "month"),

        // Composite index — for the most common combined query
        @Index(name = "idx_session_student_month", columnList = "student_id, month"),

        // Single-column index — for date range queries
        @Index(name = "idx_session_date", columnList = "sessionDate")
})
public class SessionRecord { }
```

**Naming convention:** `idx_{table}_{columns}` — always lowercase, no spaces.

**When to add an index:**

| Situation | Add Index? |
|---|---|
| Column used in `WHERE` clause frequently | Yes |
| Column used in `JOIN` condition | Yes |
| Column used in `ORDER BY` on large table | Yes |
| Two columns always queried together | Composite index |
| Column rarely queried | No — indexes slow down writes |
| Table has < 1,000 rows | No — full scan is faster |
| Primary key / unique constraint column | Already indexed automatically |

**Composite index — column order matters:**
```java
// Query: WHERE student_id = ? AND month = ?
// Good: student_id first — highest cardinality (more unique values) goes first
@Index(name = "idx_session_student_month", columnList = "student_id, month")

// This index also covers: WHERE student_id = ?  (leftmost prefix rule)
// But does NOT cover: WHERE month = ?  (missing the leading column)
```

### 4. Column Definitions

**Be explicit — don't rely on defaults:**
```java
// Required field
@Column(nullable = false)
private String title;

// With length constraint
@Column(length = 1000)
private String description;

// Immutable after insert
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;

// Computed/legacy field — managed elsewhere
@Column(name = "category", insertable = false, updatable = false, nullable = true)
private DocumentCategoryType categoryType;

// Default value managed by application (not DB)
@Column(nullable = false)
@Builder.Default
private Long downloadCount = 0L;
```

**`@Builder.Default` — required when using `@Builder` with default values:**
```java
// Bad: @Builder ignores field initializer, downloadCount is null after build
private Long downloadCount = 0L;

// Good: @Builder.Default preserves the default
@Builder.Default
private Long downloadCount = 0L;
```

**Always use `EnumType.STRING` — never `ORDINAL`:**
```java
// Good: Stores "PDF", "VIDEO", "IMAGE" — readable, safe to reorder enum
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private DocumentCategoryType categoryType;
```

**❌ Never use `EnumType.ORDINAL`:**
```java
// BAD — stores 0, 1, 2... inserting a new enum value shifts all indexes
@Enumerated(EnumType.ORDINAL)
private DocumentCategoryType categoryType;
// Adding NEW_TYPE at position 1 silently corrupts all existing data
```

---



**Always use `FetchType.LAZY` on `@ManyToOne` and `@OneToMany`:**
```java
// ✅ Lazy — only loads when accessed
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "student_id")
private Student student;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "tutor_id")
private Tutor tutor;
```

**❌ Never use `FetchType.EAGER` (default for `@ManyToOne`):**
```java
// BAD — loads Student + Tutor + their relations on EVERY Document query
@ManyToOne(fetch = FetchType.EAGER)  // ← default if you omit fetch
private Student student;
```

**Relationship fetch type reference:**

| Annotation | Default | Recommendation |
|---|---|---|
| `@ManyToOne` | EAGER | **Always override to LAZY** |
| `@OneToOne` | EAGER | **Always override to LAZY** |
| `@OneToMany` | LAZY | Keep LAZY |
| `@ManyToMany` | LAZY | Keep LAZY |

**`@JoinColumn` — always name the FK column explicitly:**
```java
// ✅ Explicit FK column name
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "student_id")
private Student student;

// ❌ Relies on Hibernate naming convention — fragile
@ManyToOne(fetch = FetchType.LAZY)
private Student student;   // generates "student_id" but not guaranteed across versions
```

**Nullable relationships — document intent clearly:**
```java
/**
 * Owner student if the document is private.
 * If null, the document is shared (available to all students).
 */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "student_id")   // nullable = true by default → intentionally optional
private Student student;
```

---



**Use `@PrePersist` / `@PreUpdate` for simple projects:**
```java
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;

@Column(nullable = false)
private LocalDateTime updatedAt;

@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

**Alternative: `@EntityListeners` for reuse across many entities:**
```java
// Auditable base class
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

// Entity extends base
@Entity
public class Document extends BaseEntity {
    // no timestamp fields needed here
}

// Enable in main class
@SpringBootApplication
@EnableJpaAuditing
public class Application { }
```

**Which approach to use:**

| Approach | Use When |
|---|---|
| `@PrePersist` / `@PreUpdate` | 1–2 entities, simple project |
| `BaseEntity` + `@EnableJpaAuditing` | 3+ entities share timestamps — eliminate duplication |

---



**Document non-obvious fields — especially nullable relationships and business rules:**
```java
/**
 * Owner student if the document is private.
 * If null, the document is available to all students (shared resource).
 */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "student_id")
private Student student;

/**
 * Owner tutor who uploaded the document.
 * Ensures tutors only see/manage their own files (multi-tenancy).
 */
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "tutor_id")
private Tutor tutor;
```

**What to document vs skip:**

| Document | Skip |
|---|---|
| Nullable fields and what null means | Self-evident fields (`title`, `email`) |
| Business rules encoded in the field | Standard timestamps |
| Multi-tenancy ownership fields | Simple counters |
| Legacy / backwards-compat fields | Fields explained by their type |

---



**Never return entities directly from controllers — always map to DTOs:**
```java
// Entity — DB representation
@Entity
public class Document {
    private Long id;
    private String title;
    private String filePath;    // internal storage path
    private Tutor tutor;        // full relation, not for API
    private Long downloadCount;
}

// Response DTO — API representation
@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String title;
    private String fileUrl;     // public URL, not internal path
    private String tutorName;   // flattened, not full Tutor object
    private Long downloadCount;
}

// Request DTO — input validation
@Data
public class DocumentRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
```

**Why separate:**

| Concern | Entity | DTO |
|---|---|---|
| Maps to DB table | ✅ | ❌ |
| Sent in API response | ❌ | ✅ |
| Has `@Valid` constraints | ❌ | ✅ |
| Exposes only needed fields | ❌ | ✅ |
| Changes with DB schema | Yes | No |

---



```java
// ✅ Safe — parent owns child's lifecycle
@OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
private List<DocumentTag> tags = new ArrayList<>();

// ❌ Dangerous on ManyToOne — deleting Document would delete the Student
@ManyToOne(cascade = CascadeType.ALL)  // never cascade on the "many" side
private Student student;
```

**Cascade type reference:**

| CascadeType | Effect | Use |
|---|---|---|
| `PERSIST` | Save children when parent saved | Safe |
| `MERGE` | Merge children when parent merged | Safe |
| `REMOVE` | Delete children when parent deleted | Only on owned collections |
| `ALL` | All of the above | Only for tight parent-child ownership |
| `DETACH` | Rarely needed | Avoid |

**`orphanRemoval = true`** — delete child records when removed from parent's collection:
```java
// Removing a tag from the list will DELETE it from DB
@OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
private List<DocumentTag> tags = new ArrayList<>();
```

---

### 11. Collection Initialization

**Always initialize collections to empty list/set — never leave null:**
```java
// ✅ Never null — safe to iterate without null check
@OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
private List<DocumentTag> tags = new ArrayList<>();

// ❌ Null until Hibernate initializes — NullPointerException risk
@OneToMany(mappedBy = "document")
private List<DocumentTag> tags;
```

---

### 12. ManyToMany Relationships + @BatchSize

**Use `@JoinTable` with explicit names and `@BatchSize` to prevent N+1:**
```java
@Builder.Default
@ManyToMany(fetch = FetchType.LAZY)
@BatchSize(size = 50)
@JoinTable(
    name = "session_lessons",
    joinColumns = @JoinColumn(name = "session_id"),
    inverseJoinColumns = @JoinColumn(name = "lesson_id")
)
private Set<Lesson> lessons = new HashSet<>();
```

**Why `Set` instead of `List` for `@ManyToMany`:**
```java
// ✅ Set — no duplicates, correct equals/hashCode behavior
private Set<Lesson> lessons = new HashSet<>();

// ❌ List — Hibernate deletes ALL join table rows and re-inserts on any change
//    (known as the "HHH-000179" bulk delete problem)
private List<Lesson> lessons = new ArrayList<>();
```

**`@BatchSize(size = 50)` — batch loading to prevent N+1:**

Without `@BatchSize`, loading 100 sessions each with lessons = 101 queries (1 + 100).
With `@BatchSize(size = 50)`, Hibernate groups loads into batches = 3 queries (1 + 2 batches).

```java
// Fetches lessons for up to 50 sessions in one IN query:
// SELECT * FROM lessons WHERE id IN (?, ?, ... 50 values)
@BatchSize(size = 50)
@ManyToMany(fetch = FetchType.LAZY)
private Set<Lesson> lessons = new HashSet<>();
```

**`@JoinTable` naming convention:**
```java
// Pattern: {owning_table}_{owned_table}
@JoinTable(
    name = "session_lessons",                           // junction table name
    joinColumns = @JoinColumn(name = "session_id"),     // FK to this entity
    inverseJoinColumns = @JoinColumn(name = "lesson_id") // FK to the other entity
)
```

---

### 13. Optimistic Locking — @Version

**Prevent lost updates in concurrent environments:**
```java
@Builder.Default
@Version
private Integer version = 0;
```

How it works: Hibernate adds `WHERE version = ?` to every UPDATE. If two transactions try to update the same row simultaneously, the second one gets `OptimisticLockException` instead of silently overwriting.

```sql
-- Hibernate generates:
UPDATE session_records SET status = ?, version = 2 WHERE id = ? AND version = 1
-- If version has already changed to 2, 0 rows updated → OptimisticLockException
```

**When to use `@Version`:**

| Situation | Use `@Version`? |
|---|---|
| Financial records, payment status | ✅ Yes — data integrity critical |
| Concurrent multi-user editing | ✅ Yes |
| Status fields that change often | ✅ Yes |
| Read-heavy entities, rarely updated | ❌ Overhead not worth it |
| Append-only / audit log tables | ❌ Not needed |

**Always initialize to 0:**
```java
// ✅ Initialize explicitly — @Builder would set it to null otherwise
@Builder.Default
@Version
private Integer version = 0;
```

---

### 14. Deprecated Fields

**Mark legacy fields clearly — document migration path:**
```java
/**
 * @deprecated Use {@link #status} instead.
 * Kept for backward compatibility with existing data migrations.
 */
@Builder.Default
@Column(nullable = false)
@Deprecated
private Boolean completed = false;

// Current replacement field
@Builder.Default
@Enumerated(EnumType.STRING)
@Column(length = 50)
private LessonStatus status = LessonStatus.SCHEDULED;
```

**Rules for deprecated fields:**
- Always add `@Deprecated` annotation
- Always add Javadoc with `@deprecated` tag pointing to the replacement
- Keep `@Column` constraints intact — data still exists in DB
- Never delete a `@Deprecated` field until data migration is complete and deployed

---

## Full Entity Template

See [EntityTemplate.java](./templates/EntityTemplate.java) for a complete, production-ready starter.

---

## Common Patterns Summary

### ✅ DO's:

1. **Use `@Table(name = "...")`** — explicit snake_case table name
2. **Always add `@NoArgsConstructor`** — JPA requires it
3. **Use `@Builder.Default`** for fields with default values
4. **Always `FetchType.LAZY`** on `@ManyToOne` and `@OneToOne`
5. **Use `@Enumerated(EnumType.STRING)`** — never ORDINAL
6. **Name FK columns explicitly** with `@JoinColumn(name = "...")`
7. **Initialize collections** to `new ArrayList<>()` / `new HashSet<>()` — never null
8. **Document nullable relationships** — explain what null means in context
9. **Use `BaseEntity`** for timestamp reuse across 3+ entities
10. **Separate entity from DTO** — never return entity directly from controller
11. **Add `@Index` on `@Table`** for columns used in `WHERE`, `JOIN`, `ORDER BY`
12. **Put highest-cardinality column first** in composite indexes
13. **Use `Set` + `@BatchSize`** for `@ManyToMany` — prevents N+1 and bulk-delete bug
14. **Add `@Version`** on entities with concurrent write risk (payments, status fields)
15. **Use `@ToString(exclude = {...})`** — exclude all relation fields

### ❌ DON'Ts:

1. **Don't use `@Data`** on entities — broken `equals/hashCode` with relations
2. **Don't use `EnumType.ORDINAL`** — silently corrupts data on enum reorder
3. **Don't use `FetchType.EAGER`** — causes N+1 and unnecessary joins
4. **Don't cascade on `@ManyToOne`** — risks deleting unrelated data
5. **Don't leave collections uninitialized** — NullPointerException risk
6. **Don't rely on Hibernate's default column/table naming** — fragile across configs
7. **Don't use `@GeneratedValue(strategy = AUTO)`** — unpredictable across DBs
8. **Don't put `@Valid` constraints on entity fields** — belongs in DTOs
9. **Don't expose entity directly in API response** — use response DTOs
10. **Don't duplicate timestamp logic** in every entity — use `BaseEntity`
11. **Don't index every column** — indexes slow down writes; index selectively
12. **Don't use `List` for `@ManyToMany`** — use `Set` to avoid Hibernate bulk-delete bug
13. **Don't omit `@ToString(exclude)`** on entities with relations — causes N+1 in logs
14. **Don't delete `@Deprecated` fields** until DB migration is complete
15. **Don't forget `@Builder.Default` on `@Version`** — null version breaks optimistic locking

---

## Related Skills

- **Error Handling**: `skills/spring/error_handling.md`
- **Security Configuration**: `skills/spring/security_config.md`
- **JWT Service**: `skills/spring/jwt_service.md`
- **Validation Patterns**: `skills/spring/validation.md`

---

**Last Updated:** 2025-02-13 (v2 — added indexing, ManyToMany, @Version, @ToString, deprecated fields)
**Status:** ✅ Production-ready (generic, reusable across projects)