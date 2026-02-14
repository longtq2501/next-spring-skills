# Skill: Performance Optimization - Backend Infrastructure

Guidelines for tuning backend performance, from connection pools to asynchronous processing.

## TL;DR - Quick Reference

### Critical Rules
1. **Connection Pool Tuning**: Prevent timeouts under load by configuring HikariCP properly.
2. **Transaction Mode**: Use `@Transactional(readOnly = true)` for search to bypass dirty checking (saves 50% RAM).
3. **Async Processing**: Use `@Async` for heavy tasks (email, PDF) to unblock the request thread.
4. **Caching Strategy**: Use Caffeine for local speed and Redis for distributed consistency.
5. **Rate Limiting**: Protect APIs from abuse with Bucket4j or Spring Rate Limiter.

---

## 1. Connection Pool & Transactions

### Connection Pool Tuning (HikariCP)
// Good: Configure pool size based on concurrent user load
spring.datasource.hikari:
  maximum-pool-size: 20
  minimum-idle: 5
  connection-timeout: 30000
  validation-timeout: 3000

### Transaction Management
// Bad: Holding connections longer than necessary during expensive processing
@Transactional
public List<DTO> process() {
    List<Entity> data = repo.findAll();
    return expensiveMapping(data); // Connection held during mapping
}

// Good: Release connection back to pool after data fetch
public List<DTO> process() {
    List<Entity> data = fetchData(); // Local @Transactional(readOnly=true)
    return expensiveMapping(data); // Connection released here
}

---

## 2. Asynchronous & Batch Processing

### Async Processing (@Async)
Use for tasks that don't need to block the user (PDF generation, Emails).
// Good: Return CompletableFuture for async tasks
@Async
public CompletableFuture<Report> generateReport() { ... }

### Batch Insert
// Good: Use JDBC Template for massive inserts (1000 inserts in 2s vs 30s)
jdbcTemplate.batchUpdate(sql, list, 100, ps -> { ... });

---

## 3. Caching Standards

### Caffeine (In-Memory)
Best for static data or dashboard stats that change rarely.
- **Speed**: Reduces latency from 500ms to 5ms.
- **Use case**: One-instance apps or non-critical shared data.

### Redis (Distributed)
Required when running multiple instances to share cache state.
- **Use case**: Shared sessions, user-specific data across nodes.

---

## 4. API Resilience

### Rate Limiting
Always protect public or heavy endpoints.
// Good: Limit requests per duration
@RateLimit(limit = 100, duration = 1, unit = MINUTES)

### Response Compression
Enable GZIP to reduce JSON payload size (500KB -> 50KB).
// Good: YAML config
server.compression:
  enabled: true
  mime-types: application/json
  min-response-size: 1024

---

## Related Skills
- **Query Optimization**: `skills/spring/query_optimization.md`
- **REST API Design**: `skills/spring/rest_api_design.md`
