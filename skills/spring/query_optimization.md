# Skill: Query Optimization - Spring Data JPA Performance

## TL;DR - Quick Reference

### Critical Rules
1. **Always Avoid N+1**: Use `JOIN FETCH` or `@EntityGraph` for relations.
2. **Lazy Loading**: Set all `@ManyToOne` and `@OneToOne` to `FetchType.LAZY`.
3. **Projections**: Use Constructor Expressions (`new DTO(...)`) for read-only reports.
4. **Batching**: Use `@BatchSize` on collections to optimize IN-clause loading.
5. **Read-Only**: Use `@Transactional(readOnly = true)` for search/get operations.

---

## 1. N+1 Problem & Solutions

### The Problem
Loading 10 entities with a lazy relation causes 10 additional queries to fetch those relations.

### Solution: JOIN FETCH

```java
// Bad: Loading relations lazily in a loop causes N+1 queries
@Query("SELECT o FROM Order o WHERE o.status = :status")
List<Order> findAllByStatus(@Param("status") String status);

// Good: Using JOIN FETCH to load relations in a single query
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
List<Order> findAllWithItems(@Param("status") String status);
```

### Solution: @EntityGraph

```java
// Bad: Default fetch will cause multiple queries for lazy fields
List<Order> findAllByStatus(String status);

// Good: EntityGraph specifies which fields to fetch eagerly
@EntityGraph(attributePaths = {"items", "customer"})
List<Order> findAllByStatus(String status);
```

---

## 2. Projections for Reporting
Avoid loading full Entities when you only need a few fields.

```java
// Bad: Loading full Order and Customer entities for a simple report
@Query("SELECT o FROM Order o JOIN o.customer c")
List<Order> findAllOrders();

// Good: Loading only required fields into a DTO
@Query("SELECT new com.example.dto.OrderSummary(o.id, o.orderNumber, c.name) " +
       "FROM Order o JOIN o.customer c")
List<OrderSummary> findSummaries();
```

---

## 3. Batch Loading
Optimize collection loading when `JOIN FETCH` isn't suitable (e.g., multiple collections).

```java
// Bad: Default loading causes N additional queries for the items collection
@OneToMany(mappedBy = "order")
private List<Item> items;

// Good: BatchSize allows loading multiple collections in a single IN-clause query
@OneToMany(mappedBy = "order")
@BatchSize(size = 20)
private List<Item> items;
```

---

## Related Skills
- **Entity Design**: `skills/spring/entity_design.md`
- **Repository Design**: `skills/spring/repository_design.md`
