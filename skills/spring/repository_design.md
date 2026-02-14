## TL;DR - Quick Reference

### Standard Repo Setup
```java
@Repository
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    Optional<MyEntity> findByEmail(String email);
    Page<MyEntity> findAllByStatus(MyStatus status, Pageable pageable);
}
```

### Critical Rules
1. **Derived Queries**: Use for 1-2 conditions (`findByStudentId`).
2. **@Query (JPQL)**: Use for 3+ conditions or joins.
3. **Named Parameters**: Always use `@Param("name")` instead of `?1`.
4. **Aggregations**: Always wrap with `COALESCE(SUM(...), 0)` to avoid NPE.
5. **Paginated Results**: Use `Page<T>` for UI, `Slice<T>` for infinite scroll.
6. **Native Queries**: Use ONLY for UPSERT or join-table operations.

**Performance note:** JOIN FETCH, `@EntityGraph`, `@BatchSize`, and N+1 avoidance are covered in `skills/spring/query_optimization.md`. This skill focuses on **design and structure** — what type of query to write and how to name/organize it.

---

## Core Principles

### 1. Interface Declaration

**Standard repository setup:**
```java
@Repository
public interface SessionRecordRepository extends JpaRepository<SessionRecord, Long> {
    // custom methods here
}
```

**`JpaRepository<T, ID>` gives you for free:**

| Method | Description |
|---|---|
| `save(entity)` | Insert or update |
| `findById(id)` | Returns `Optional<T>` |
| `findAll()` | All records |
| `findAll(Pageable)` | Paginated |
| `deleteById(id)` | Delete by PK |
| `existsById(id)` | Existence check |
| `count()` | Row count |

// Bad: Extending basic repository interfaces
public interface MyRepo extends CrudRepository<T, ID> { }

// Good: JpaRepository includes all the essentials
public interface MyRepo extends JpaRepository<T, ID> { }

---

### 2. Derived Query Methods (Method Name Queries)

**Use for simple, single-condition queries — let Spring generate the SQL:**
```java
// Spring generates: SELECT * FROM session_records WHERE student_id = ?
List<SessionRecord> findByStudentId(Long studentId);

// WHERE tutor_id = ?
List<SessionRecord> findByTutorId(Long tutorId);

// WHERE id = ? AND tutor_id = ?
Optional<SessionRecord> findByIdAndTutorId(Long id, Long tutorId);

// DELETE WHERE month = ?
void deleteByMonth(String month);

// DELETE WHERE month = ? AND tutor_id = ?
void deleteByMonthAndTutorId(String month, Long tutorId);

// COUNT WHERE student_id = ? AND lesson_id = ?
boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);
```

**Derived query keyword reference:**

| Keyword | Example | Generated clause |
|---|---|---|
| `findBy` | `findByEmail` | `WHERE email = ?` |
| `findByAnd` | `findByIdAndTutorId` | `WHERE id = ? AND tutor_id = ?` |
| `findByOr` | `findByEmailOrPhone` | `WHERE email = ? OR phone = ?` |
| `findByIn` | `findByIdIn(List<Long>)` | `WHERE id IN (...)` |
| `findByNot` | `findByStatusNot` | `WHERE status != ?` |
| `findByIsNull` | `findByStudentIdIsNull` | `WHERE student_id IS NULL` |
| `findByIsNotNull` | `findByStudentIdIsNotNull` | `WHERE student_id IS NOT NULL` |
| `deleteBy` | `deleteByMonth` | `DELETE WHERE month = ?` |
| `existsBy` | `existsByEmail` | `SELECT COUNT(*) > 0 WHERE email = ?` |
| `countBy` | `countByTutorId` | `SELECT COUNT(*) WHERE tutor_id = ?` |

**When to stop using derived queries — use `@Query` instead:**
```java
// ❌ Too complex — derived query becomes unreadable
List<SessionRecord> findByStudentIdAndTutorIdAndMonthAndPaidAndStatusNotIn(...);

// ✅ Use @Query for anything with 3+ conditions or joins
@Query("SELECT sr FROM SessionRecord sr WHERE ...")
```

**Rule of thumb:** derived query with more than 2 conditions → switch to `@Query`.

---

### 3. JPQL Custom Queries

**Use `@Query` for joins, aggregations, or complex filtering:**
```java
// Paginated query with JOIN FETCH (see query_optimization.md for fetch strategy)
@Query("SELECT sr FROM SessionRecord sr " +
       "LEFT JOIN FETCH sr.student " +
       "WHERE sr.tutorId = :tutorId " +
       "ORDER BY sr.createdAt DESC")
Page<SessionRecord> findAllByTutorIdOrderByCreatedAtDesc(
        @Param("tutorId") Long tutorId,
        Pageable pageable);

// Aggregation — SUM with CASE WHEN
@Query("SELECT COALESCE(SUM(sr.totalAmount), 0) FROM SessionRecord sr " +
       "WHERE sr.paid = true " +
       "AND sr.status NOT IN (:cancelledStatuses)")
Long sumTotalPaid(@Param("cancelledStatuses") List<LessonStatus> cancelledStatuses);

// Distinct list
@Query("SELECT DISTINCT sr.month FROM SessionRecord sr ORDER BY sr.month DESC")
List<String> findDistinctMonths();
```

**`@Param` — always name parameters explicitly:**
```java
// Good: Named parameter — matches :tutorId in query
@Query("... WHERE sr.tutorId = :tutorId")
Page<SessionRecord> findByTutorId(@Param("tutorId") Long tutorId, Pageable pageable);

// Bad: Positional parameter — fragile, breaks on reorder
@Query("... WHERE sr.tutorId = ?1")
Page<SessionRecord> findByTutorId(Long tutorId, Pageable pageable);
```

**`COALESCE` — always wrap aggregations to avoid null:**
```java
// Good: Returns 0 instead of null when no rows match
@Query("SELECT COALESCE(SUM(sr.totalAmount), 0) FROM SessionRecord sr WHERE ...")
Long sumTotalPaid();

// Bad: Returns null when no rows — causes NullPointerException in service layer
@Query("SELECT SUM(sr.totalAmount) FROM SessionRecord sr WHERE ...")
Long sumTotalPaid();
```

---

### 4. Constructor Expression Queries (Projection DTOs)

**Map query results directly to a DTO — avoids loading full entities for reporting:**
```java
// DTO must have a matching constructor
@Query("SELECT new com.example.dto.MonthlyStats(" +
       "sr.month, " +
       "SUM(CASE WHEN sr.paid = true THEN sr.totalAmount ELSE 0L END), " +
       "SUM(CASE WHEN sr.paid = false THEN sr.totalAmount ELSE 0L END), " +
       "CAST(SUM(sr.sessions) AS integer)) " +
       "FROM SessionRecord sr " +
       "GROUP BY sr.month " +
       "ORDER BY sr.month DESC")
List<MonthlyStats> findMonthlyStatsAggregated();
```

**The DTO must have an all-args constructor matching the SELECT order:**
```java
@AllArgsConstructor  // ← required — JPQL calls this constructor
@Getter
public class MonthlyStats {
    private String month;
    private Long paidAmount;
    private Long unpaidAmount;
    private Integer sessionCount;
}
```

**When to use constructor expressions:**

| Use Case | Approach |
|---|---|
| Dashboard/reporting aggregations | Yes - Constructor expression |
| Financial summaries (SUM, COUNT, GROUP BY) | Yes - Constructor expression |
| Loading full entity with relations | No - Use entity query + JOIN FETCH |
| Simple scalar values (single SUM) | No - Return `Long` / `Integer` directly |

---

### 5. Native Queries

**Use native SQL only for operations JPQL cannot express:**
```java
// UPSERT — not possible in JPQL
@Modifying
@Query(value = """
    INSERT INTO refresh_tokens (user_id, token, expiry_date)
    VALUES (:userId, :token, :expiryDate)
    ON CONFLICT (user_id)
    DO UPDATE SET token = :token, expiry_date = :expiryDate
    """, nativeQuery = true)
void upsert(@Param("userId") Long userId,
            @Param("token") String token,
            @Param("expiryDate") Instant expiryDate);

// Bulk delete on join table — JPQL can't target join tables directly
@Modifying
@Query(value = "DELETE FROM session_documents WHERE document_id = :documentId",
       nativeQuery = true)
void deleteDocumentReferences(@Param("documentId") Long documentId);
```

**When to use native vs JPQL:**

| Situation | Use |
|---|---|
| UPSERT (`ON CONFLICT`) | Native |
| Delete from join table | Native |
| DB-specific functions (`ILIKE`, `JSON_EXTRACT`) | Native |
| Standard SELECT / WHERE / JOIN / GROUP BY | JPQL |
| Aggregations with CASE WHEN | JPQL |
| Pagination | JPQL (native pagination is fragile) |

**⚠️ Always pair `@Modifying` with `@Query` for write operations:**
```java
// ✅ Required for UPDATE and DELETE queries
@Modifying
@Query("UPDATE SessionRecord sr SET sr.paid = true WHERE sr.id = :id")
void markAsPaid(@Param("id") Long id);

// Add @Transactional if not already in service layer
@Modifying
@Transactional
@Query(value = "DELETE FROM session_documents WHERE document_id = :documentId",
       nativeQuery = true)
void deleteDocumentReferences(@Param("documentId") Long documentId);
```

---

### 6. Pessimistic Locking

**Use `@Lock` for critical write operations that must not have concurrent conflicts:**
```java
// Pessimistic write lock — blocks other reads and writes until transaction completes
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT sr FROM SessionRecord sr WHERE sr.id = :id")
Optional<SessionRecord> findByIdForUpdate(@Param("id") Long id);
```

**Lock type reference:**

| Lock Type | Behavior | Use When |
|---|---|---|
| `PESSIMISTIC_WRITE` | Exclusive lock — blocks all other access | Payment processing, balance updates |
| `PESSIMISTIC_READ` | Shared lock — blocks writes, allows reads | Prevent dirty reads during report generation |
| `OPTIMISTIC` | No DB lock — version check on commit | General concurrent writes (use `@Version` on entity) |
| `OPTIMISTIC_FORCE_INCREMENT` | Always increments version | Force version bump even on reads |

**Pessimistic vs Optimistic — when to use each:**

| Scenario | Locking Strategy |
|---|---|
| Payment status update | `PESSIMISTIC_WRITE` — no retry tolerance |
| Booking / seat reservation | `PESSIMISTIC_WRITE` — must not double-book |
| General record update (low contention) | `@Version` (Optimistic) on entity |
| High-read, low-write | `@Version` (Optimistic) — less contention |

**⚠️ Always call pessimistic-locked queries inside a `@Transactional` service method** — the lock is held for the duration of the transaction.

---

### 7. Pageable Queries

**Accept `Pageable` as last parameter for paginated results:**
```java
// Returns Page<T> — includes total count, page info
Page<SessionRecord> findAllByTutorIdOrderByCreatedAtDesc(
        @Param("tutorId") Long tutorId,
        Pageable pageable);

// Returns Slice<T> — no total count, lighter than Page (use for infinite scroll)
Slice<SessionRecord> findByMonthAndTutorId(String month, Long tutorId, Pageable pageable);
```

**`Page` vs `Slice` vs `List`:**

| Return Type | Total count query? | Use When |
|---|---|---|
| `Page<T>` | ✅ Yes (extra query) | UI pagination with page numbers |
| `Slice<T>` | ❌ No | Infinite scroll / "load more" |
| `List<T>` | ❌ No | Known small result set, no pagination needed |

**Creating `Pageable` in service/controller:**
```java
// Page 0, 20 items, sorted by createdAt DESC
Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

// Multiple sort fields
Pageable pageable = PageRequest.of(page, size,
        Sort.by("month").descending().and(Sort.by("createdAt").descending()));
```

---

### 8. Returning `Object[]` for Multi-Value Projections

**Use `Object[]` when projecting multiple unrelated columns without a DTO:**
```java
// Returns [student_id, sum_of_total_amount] pairs
@Query("SELECT sr.student.id, SUM(sr.totalAmount) FROM SessionRecord sr " +
       "WHERE sr.student.id IN :studentIds AND sr.paid = false " +
       "GROUP BY sr.student.id")
List<Object[]> sumTotalUnpaidByStudentIdIn(@Param("studentIds") List<Long> studentIds);
```

**Map `Object[]` results in service layer:**
```java
Map<Long, Long> unpaidByStudent = repository.sumTotalUnpaidByStudentIdIn(studentIds)
        .stream()
        .collect(Collectors.toMap(
                row -> (Long) row[0],    // student_id
                row -> (Long) row[1]     // sum
        ));
```

**When to use `Object[]` vs constructor expression:**

| Approach | Use When |
|---|---|
| `Object[]` | Quick projection, result used only in one place |
| Constructor expression (`new DTO(...)`) | Result reused, needs named fields, cleaner code |
| Interface projection | Read-only DTO with `getX()` methods, no constructor needed |

**Interface projection (alternative to `Object[]`):**
```java
// Define projection interface
public interface StudentAmountSummary {
    Long getStudentId();
    Long getTotalAmount();
}

// Repository method
@Query("SELECT sr.student.id AS studentId, SUM(sr.totalAmount) AS totalAmount " +
       "FROM SessionRecord sr WHERE sr.student.id IN :studentIds " +
       "GROUP BY sr.student.id")
List<StudentAmountSummary> sumAmountByStudentIdIn(@Param("studentIds") List<Long> studentIds);
```

---

### 9. Naming Conventions

**Be explicit and descriptive — repository method names are documentation:**

| Pattern | Example |
|---|---|
| `findBy{Field}` | `findByTutorId` |
| `findBy{Field}And{Field}` | `findByIdAndTutorId` |
| `findBy{Field}In` | `findByStudentIdIn` |
| `findAllBy{Field}OrderBy{Field}Desc` | `findAllByTutorIdOrderByCreatedAtDesc` |
| `findBy{Field}With{Relation}` | `findByIdWithAttachments` (custom `@Query`) |
| `findBy{Field}ForUpdate` | `findByIdForUpdate` (locked query) |
| `sum{Field}By{Condition}` | `sumTotalPaidByMonth` |
| `existsBy{Field}And{Field}` | `existsByStudentIdAndLessonId` |
| `deleteBy{Field}` | `deleteByMonth` |
| `{noun}References` | `deleteDocumentReferences` (native join table ops) |

**Distinguish query variants clearly:**
```java
// Without attachments — lightweight
Optional<SessionRecord> findByIdAndTutorId(Long id, Long tutorId);

// With attachments — fetches documents + lessons
Optional<SessionRecord> findByIdAndTutorIdWithAttachments(Long id, Long tutorId);

// All months
List<String> findDistinctMonths();

// Months for specific tutor
List<String> findDistinctMonthsByTutorId(Long tutorId);
```

---

### 10. Repository Javadoc

**Document the non-obvious — especially aggregations and business rules:**
```java
/**
 * Aggregates revenue and session counts grouped by month.
 * Excludes cancelled sessions from financial totals.
 */
@Query("SELECT new MonthlyStats(...) FROM SessionRecord sr GROUP BY sr.month")
List<MonthlyStats> findMonthlyStatsAggregated();

/**
 * Checks if a student has access to a lesson via any session record.
 */
@Query("SELECT COUNT(sr) > 0 FROM SessionRecord sr JOIN sr.lessons l " +
       "WHERE sr.student.id = :studentId AND l.id = :lessonId")
boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);

/**
 * Retrieves all session records for a specific student.
 * Simplified version without ordering — used for cascade deletion and bulk operations.
 */
List<SessionRecord> findByStudentId(Long studentId);
```

**What to document:**

| Document | Skip |
|---|---|
| Aggregations and their exclusion rules | Simple `findBy` derived queries |
| Locking intent (`findByIdForUpdate`) | Self-explanatory method names |
| Access control semantics (multi-tenancy) | Standard pagination methods |
| Native queries (non-obvious SQL) | Basic `deleteBy` / `existsBy` |

---

## Quick Reference — Query Type Decision

```
New repository method needed?
        │
        ├─► Simple filter (1–2 conditions, no join)?
        │       └─► Derived query method  →  findByStudentIdAndTutorId(...)
        │
        ├─► Complex filter (3+ conditions, JOIN, ORDER BY)?
        │       └─► @Query JPQL  →  @Query("SELECT sr FROM SessionRecord sr ...")
        │
        ├─► Aggregation (SUM, COUNT, GROUP BY)?
        │       ├─► Single value  →  @Query returning Long/Integer + COALESCE
        │       └─► Multiple fields  →  Constructor expression @Query("SELECT new DTO(...)")
        │
        ├─► Paginated result?
        │       └─► @Query + Pageable param  →  Page<T> or Slice<T>
        │
        ├─► UPSERT or join-table delete?
        │       └─► Native @Query + @Modifying + nativeQuery = true
        │
        └─► Concurrent write, financial critical?
                └─► @Lock(PESSIMISTIC_WRITE) + @Query + @Transactional in service
```

---

## Common Patterns Summary

### ✅ DO's:

1. **Use derived query methods** for 1–2 condition filters — no `@Query` needed
2. **Switch to `@Query`** when derived method has 3+ conditions or joins
3. **Always use `@Param` with named parameters** — never positional `?1`
4. **Wrap aggregations with `COALESCE`** — prevents null returns
5. **Use constructor expressions** for reporting/aggregation DTOs
6. **Use `@Modifying`** for all UPDATE and DELETE `@Query` methods
7. **Use `nativeQuery = true`** only for UPSERT, join-table deletes, DB-specific functions
8. **Name variants clearly** — `findById` vs `findByIdWithAttachments` vs `findByIdForUpdate`
9. **Add Javadoc** on aggregations, locked queries, and non-obvious business rules
10. **Use `Page<T>`** for UI pagination, `Slice<T>` for infinite scroll, `List<T>` for small sets

### ❌ DON'Ts:

1. **Don't use derived queries with 3+ conditions** — unreadable, use `@Query`
2. **Don't use positional parameters (`?1`)** — fragile on refactor, use `@Param`
3. **Don't return raw `SUM` without `COALESCE`** — causes NPE in service layer
4. **Don't call `@Lock` queries outside `@Transactional`** — lock is released immediately
5. **Don't use native queries for standard SELECT/JOIN** — JPQL is portable and type-safe
6. **Don't add business logic in repositories** — filtering, transforming belongs in service
7. **Don't load full entities for reports** — use constructor expressions or projections
8. **Don't mix paginated and non-paginated methods with the same name** — name variants clearly
9. **Don't skip `@Modifying`** on UPDATE/DELETE queries — Spring will throw an exception
10. **Don't put `@Transactional` on repository methods** unless absolutely necessary — let the service layer own the transaction boundary

---

## Related Skills

- **Entity Design**: `skills/spring/entity_design.md`
- **DTO Design**: `skills/spring/dto_design.md`
- **Query Optimization** *(JOIN FETCH, EntityGraph, N+1)*: `skills/spring/query_optimization.md`
- **Error Handling**: `skills/spring/error_handling.md`

---

**Last Updated:** 2025-02-13
**Status:** ✅ Production-ready (generic, reusable across projects)