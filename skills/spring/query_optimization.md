# Skill: Query Optimization - Database Performance

Advanced patterns for scaling and optimizing database access in high-traffic applications.

## TL;DR - Quick Reference

### Critical Rules
1. **DTO Projection**: Reduce payload by 60-80% using constructor expressions.
2. **JOIN FETCH / @EntityGraph**: Fix N+1 problems by fetching relations eagerly.
3. **Composite Indexing**: Query under 10ms by indexing multiple columns used in WHERE/JOIN.
4. **Leftmost Prefix**: Lead with the most selective column in composite indexes.
5. **Slow Query Guards**: Use `@QueryHints` to set timeouts and prevent resource starvation.

---

## 1. Database Query Optimization

### DTO Projection
// Bad: Loading full entities just for a few fields
@Query("SELECT s FROM Student s")
List<Student> findAll();

// Good: Loading only required fields into a DTO (Giảm 60-80% payload)
@Query("SELECT new com.example.dto.StudentDTO(s.id, s.name, s.email) FROM Student s")
List<StudentDTO> findAllProjected();

### JOIN FETCH vs @EntityGraph
// Bad: Causes N+1 problem
List<Order> orders = repo.findAll(); // 1 query
orders.forEach(o -> o.getItems().size()); // N queries

// Good: Single query with JOIN FETCH
@Query("SELECT o FROM Order o JOIN FETCH o.items")
List<Order> findAllWithItems();

// Good: Declarative eager loading
@EntityGraph(attributePaths = {"items", "customer"})
List<Order> findAllWithGraph();

### Indexing Standards
- **Composite Indexing**: Standard for complex queries.
- **Index Column Ordering**: Always lead with the column used most frequently or with highest selectivity (Leftmost prefix rule).

| Rule | Description |
|---|---|
| Use `EXPLAIN` | Always verify index usage for slow queries (>100ms) |
| Composite Order | Most selective column goes first |
| Functional Index | Use for queries on modified columns (e.g., `LOWER(email)`) |

---

## 2. Advanced Scaling Patterns

### Database Sharding & Partitioning
Scale horizontally when a single database reaches its limit.
- **Partitioning**: Split table by date/id within the same DB (e.g., `sessions_2024`).
- **Sharding**: Distribute data across multiple physical servers.

### Read Replicas
Scale read operations independently of writes.
- **Master**: Handles all `INSERT/UPDATE/DELETE`.
- **Replica**: Handles all `SELECT` (Search/Get).
- **Pro Tip**: Use `@Transactional(readOnly = true)` to route traffic to replicas.

---

## 3. Bulk & Streaming Operations

### Bulk Operations (JDBC Template)
JPA `saveAll()` is slow for thousands of records. Use JDBC batching for high performance.
// Good: JDBC batch update (10x faster than JPA)
jdbcTemplate.batchUpdate(
    "INSERT INTO students VALUES (?, ?)",
    students,
    100, // batch size
    (ps, student) -> { ... }
);

### Response Streaming
Use for large exports (50k+ records) to keep memory usage low.
// Good: Stream large datasets
@GetMapping(produces = "application/x-ndjson")
public Flux<StudentDTO> streamAll() { ... }

---

## Related Skills
- **Entity Design**: `skills/spring/entity_design.md`
- **Performance Optimization**: `skills/spring/performance_optimization.md`
