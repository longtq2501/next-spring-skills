## TL;DR - Quick Reference

### Standard Controller Setup
```java
@RestController
@RequestMapping("/api/resources") // prefix /api + plural snake_case
@RequiredArgsConstructor
@Slf4j
public class MyController { ... }
```

### Critical Rules
1. **Always use `/api` prefix** and kebab-case URLs.
2. **Never return entities** directly — always map to DTOs.
3. **Return `201 Created`** for successful POST requests.
4. **Use `@Valid`** for all request body inputs.
5. **Standardize response format** (Wrapped `ApiResponse` vs Direct).

### 📄 Templates
- [Standard Controller Template](./templates/ControllerTemplate.java)

---

## Core Principles

### 1. Controller Structure

**Standard Annotations:**
```java
@RestController
@RequestMapping("/api/{resource-name}")  // Always /api prefix, kebab-case
@RequiredArgsConstructor                 // Lombok for DI
@Slf4j                                   // Logging via Lombok
```

// Good: Correct prefix and constructor injection
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService service;
}

// Bad: Missing /api prefix and using field injection
@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService service;
}
```

**Rationale:**
- `/api` prefix clearly separates API endpoints from other routes (web pages, static resources)
- Kebab-case for URL readability (`product-categories` not `productCategories`)
- `@RequiredArgsConstructor` cleaner than `@Autowired` (immutable dependencies, easier to test)
- `@Slf4j` standardized logging across all controllers

---

### 2. Response Wrapping Pattern

**DECISION: Choose one approach for your project**

#### **Option A: Wrapped Response (Recommended for consistency)**

Create a generic wrapper:
```java
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

Usage:
```java
// Success
return ResponseEntity.ok(ApiResponse.success(data));

// Success with message
return ResponseEntity.ok(ApiResponse.success("Operation successful", data));

// Error
return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed"));
```

Response format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

**Pros:**
- ✅ Consistent structure across ALL endpoints
- ✅ Frontend always knows contract (`success` + `data` + `message`)
- ✅ Easy to add metadata (timestamp, requestId, etc.)

**Cons:**
- ⚠️ Extra nesting in response
- ⚠️ More verbose

---

#### **Option B: Direct Response (Spring standard)**

Return data directly:
```java
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(service.findById(id));
}
```

Response format:
```json
{
  "id": 1,
  "name": "Product A",
  "price": 100
}
```

**Pros:**
- ✅ Less verbose
- ✅ RESTful purist approach
- ✅ Follows Spring conventions

**Cons:**
- ⚠️ No standard way to send metadata
- ⚠️ Success/error distinction only via HTTP status

---

**RECOMMENDATION:** 
- Use **Option A (wrapped)** if:
  - Building client-facing API
  - Need consistent error format
  - Frontend team wants predictable structure
  
- Use **Option B (direct)** if:
  - Building internal microservices
  - Following strict REST standards
  - API consumers prefer minimal payload

**⚠️ CRITICAL: Pick ONE approach for entire project. Don't mix.**

---

### 3. HTTP Method Mapping

| Method | Purpose | Return Type | Status Code | Example |
|--------|---------|-------------|-------------|---------|
| `GET` | Retrieve single resource | `T` or `ApiResponse<T>` | 200 OK | `GET /api/products/1` |
| `GET` | Retrieve collection | `List<T>` or `ApiResponse<List<T>>` | 200 OK | `GET /api/products` |
| `POST` | Create resource | `T` or `ApiResponse<T>` | **201 CREATED** | `POST /api/products` |
| `PUT` | Full update | `T` or `ApiResponse<T>` | 200 OK | `PUT /api/products/1` |
| `PATCH` | Partial update | `T` or `ApiResponse<T>` | 200 OK | `PATCH /api/products/1` |
| `DELETE` | Remove resource | `Void` or `ApiResponse<Void>` | 200 OK or 204 NO CONTENT | `DELETE /api/products/1` |

**Standard CRUD Pattern:**
```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService service;
    
    // GET - Single resource
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }
    
    // GET - Collection
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.findAll()));
    }
    
    // POST - Create
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)  // 201, not 200
                .body(ApiResponse.success("Resource created successfully", response));
    }
    
    // PUT - Update (full replacement)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Resource updated successfully", response));
    }
    
    // PATCH - Partial update (optional)
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        ProductResponse response = service.partialUpdate(id, updates);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // DELETE - Remove
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Resource deleted successfully", null));
        // Or: return ResponseEntity.noContent().build(); (204 No Content)
    }
}
```

**Key Points:**
- ✅ POST returns **201 CREATED** (not 200 OK)
- ✅ DELETE can return 200 with message OR 204 No Content (pick one, be consistent)
- ✅ Update operations return the full updated resource
- ✅ PUT = full replacement, PATCH = partial update

---

### 4. Path Variables vs Query Parameters

**Path Variables (Resource Identifiers):**
```java
// ✅ DO: Use for resource IDs
@GetMapping("/{id}")
public ResponseEntity<T> getById(@PathVariable Long id) { }

@GetMapping("/users/{userId}/orders/{orderId}")
public ResponseEntity<T> getUserOrder(
    @PathVariable Long userId,
    @PathVariable Long orderId
) { }
```

**Query Parameters (Filters, Pagination, Options):**
```java
// ✅ DO: Use for filters and options
@GetMapping
public ResponseEntity<List<T>> getAll(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
) { }
```

**Examples:**
```
GET /api/products/123                    // Path variable for ID
GET /api/products?category=electronics   // Query param for filter
GET /api/products?page=0&size=20         // Query params for pagination
GET /api/users/5/orders?status=pending   // Combination
```

**Rule of Thumb:**
- Path variable = **What** resource (noun, identifier)
- Query parameter = **How** to filter/sort/paginate (adjective, option)

---

### 5. Validation Pattern

**Always validate request bodies with `@Valid`:**
```java
@PostMapping
public ResponseEntity<T> create(@Valid @RequestBody ProductRequest request) { }

@PutMapping("/{id}")
public ResponseEntity<T> update(
        @PathVariable Long id,
        @Valid @RequestBody ProductRequest request
) { }
```

**In Request DTOs:**
```java
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;
    
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String phone;
    
    // Custom validation
    @ValidCategory  // Custom annotation
    private String category;
}
```

**Validation Error Handling:**
- Let `GlobalExceptionHandler` handle `MethodArgumentNotValidException`
- Return 400 BAD REQUEST with field-level errors
- See `skills/spring/error_handling.md`

---

### 6. Security Patterns

**Method-level security with `@PreAuthorize`:**
```java
// Public endpoint (no annotation)
@GetMapping("/public")
public ResponseEntity<T> getPublicData() { }

// Authenticated users only
@PreAuthorize("isAuthenticated()")
@GetMapping("/profile")
public ResponseEntity<T> getProfile() { }

// Single role
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) { }

// Multiple roles (OR)
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@GetMapping
public ResponseEntity<List<T>> getAll() { }

// Multiple roles (AND) - rare
@PreAuthorize("hasRole('ADMIN') and hasRole('SUPER_USER')")
@PostMapping("/critical-action")
public ResponseEntity<T> criticalAction() { }

// Custom SpEL expression
@PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
@GetMapping("/users/{userId}/private-data")
public ResponseEntity<T> getPrivateData(@PathVariable Long userId) { }
```

**Class-level security (applies to all methods):**
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // All methods require ADMIN
public class AdminController {
    
    @GetMapping("/dashboard")
    public ResponseEntity<T> getDashboard() { }  // Requires ADMIN
    
    @PreAuthorize("hasRole('SUPER_ADMIN')")  // Override: now requires SUPER_ADMIN
    @DeleteMapping("/critical")
    public ResponseEntity<T> criticalAction() { }
}
```

**Accessing authenticated user:**
```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@PostMapping
public ResponseEntity<T> create(
        @Valid @RequestBody Request request,
        @AuthenticationPrincipal UserDetails user  // Or your custom User class
) {
    log.info("Action initiated by: {}", user.getUsername());
    // Access: user.getUsername(), user.getAuthorities(), etc.
}
```

**Note:** No `ROLE_` prefix needed in `hasRole()` - Spring adds it automatically.

---

### 7. Logging Patterns

**Standard logging levels:**
```java
@Slf4j
public class ProductController {
    
    @PostMapping
    public ResponseEntity<T> create(@RequestBody Request request) {
        log.info("Creating new product: {}", request.getName());  // Normal operations
        // ...
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable Long id, @RequestBody Request request) {
        log.debug("Update request for product {}: {}", id, request);  // Detailed info
        // ...
    }
    
    private void validateBusinessRule(Product product) {
        if (someCondition) {
            log.warn("Business rule violation for product {}: {}", product.getId(), reason);  // Warnings
        }
    }
    
    // Errors logged in service layer or exception handler
}
```

**Logging best practices:**
- `log.info()` - Important business operations (create, update, delete)
- `log.debug()` - Detailed information for troubleshooting
- `log.warn()` - Business rule violations, recoverable issues
- `log.error()` - Exceptions, critical failures (usually in exception handler)

**⚠️ Security considerations:**
- DON'T log sensitive data (passwords, tokens, credit cards)
- Use parameterized logging (`{}`) for performance
- Include correlation IDs for tracing (in production)

---

### 8. Pagination Pattern

**Using Spring Data `Pageable`:**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@GetMapping
public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
        Pageable pageable
) {
    Page<ProductResponse> products = service.findAll(pageable);
    return ResponseEntity.ok(ApiResponse.success(products));
}
```

**How clients call:**
```
GET /api/products?page=0&size=20&sort=name,asc
GET /api/products?page=1&size=50&sort=price,desc
GET /api/products?page=0&size=10&sort=createdAt,desc&sort=name,asc  // Multi-sort
```

**Response format (Spring's Page object):**
```json
{
  "content": [...],           // Array of items
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { ... }
  },
  "totalElements": 150,       // Total items in DB
  "totalPages": 8,            // Total pages
  "last": false,              // Is this the last page?
  "first": true,              // Is this the first page?
  "numberOfElements": 20      // Items in current page
}
```

**With filters:**
```java
@GetMapping
public ResponseEntity<Page<ProductResponse>> getAll(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String status,
        Pageable pageable
) {
    Page<ProductResponse> products = service.findAll(category, status, pageable);
    return ResponseEntity.ok(products);
}
```

**When NOT to paginate:**
- Small collections (< 100 items guaranteed)
- Lookup data (categories, countries, statuses)
- Dashboard stats

---

### 9. Endpoint Naming Conventions

**Standard patterns:**

| Pattern | Example | Purpose |
|---------|---------|---------|
| `GET /{resource}` | `GET /api/products` | List all |
| `GET /{resource}/{id}` | `GET /api/products/1` | Get by ID |
| `GET /{resource}?filter=value` | `GET /api/products?category=electronics` | Filtered list |
| `POST /{resource}` | `POST /api/products` | Create |
| `PUT /{resource}/{id}` | `PUT /api/products/1` | Full update |
| `PATCH /{resource}/{id}` | `PATCH /api/products/1` | Partial update |
| `DELETE /{resource}/{id}` | `DELETE /api/products/1` | Delete |

**Nested resources:**
```
GET    /api/users/{userId}/orders           // User's orders
GET    /api/users/{userId}/orders/{orderId} // Specific order
POST   /api/users/{userId}/orders           // Create order for user
```

**Custom actions (non-CRUD):**
```
PUT    /api/products/{id}/activate          // State change
PUT    /api/products/{id}/deactivate        // State change
POST   /api/products/{id}/publish           // Action
POST   /api/orders/{id}/cancel              // Action
POST   /api/invoices/{id}/send-email        // Action
```

**Batch operations:**
```
POST   /api/products/bulk-create            // Batch create
PUT    /api/products/bulk-update            // Batch update
DELETE /api/products/bulk-delete            // Batch delete
```

**Search/query operations:**
```
GET    /api/products/search?q=laptop        // Simple search
POST   /api/products/search                 // Advanced search (complex body)
POST   /api/reports/generate                // Generate report
```

**Naming rules:**
- ✅ Use kebab-case: `/product-categories`, `/bulk-update`
- ✅ Use plural nouns: `/products`, `/orders`, `/users`
- ✅ Use verbs for actions: `/activate`, `/send-email`, `/generate`
- ❌ Avoid verbs in resource URLs: `/getProducts`, `/createOrder`

---

### 10. Request Body Patterns

**Option A: Typed DTO (Recommended)**
```java
@PostMapping
public ResponseEntity<T> create(@Valid @RequestBody ProductRequest request) { }
```

**Pros:**
- ✅ Type-safe
- ✅ Auto-validation with `@Valid`
- ✅ Self-documenting (clear contract)
- ✅ IDE autocomplete

**Cons:**
- ⚠️ Need to create DTO class
- ⚠️ Less flexible

---

**Option B: Map (For flexible/dynamic input)**
```java
@PostMapping("/dynamic")
public ResponseEntity<T> create(@RequestBody Map<String, Object> request) {
    String name = (String) request.get("name");
    Integer quantity = (Integer) request.get("quantity");
    
    // Manual validation
    if (name == null || name.isBlank()) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Name is required"));
    }
    // ...
}
```

**Pros:**
- ✅ Flexible (accept any fields)
- ✅ No DTO needed

**Cons:**
- ⚠️ No type safety (casting required)
- ⚠️ No auto-validation
- ⚠️ Manual null checks
- ⚠️ Less clear contract

---

**RECOMMENDATION:**
- Use **DTO** for:
  - Public APIs
  - Complex validation
  - Type safety important
  - Long-term maintenance
  
- Use **Map** for:
  - Internal/admin endpoints
  - Quick prototyping
  - Truly dynamic data (user preferences, metadata)

---

### 11. Response Patterns

**For single resource:**
```java
return ResponseEntity.ok(ApiResponse.success(productResponse));
```

**For collection:**
```java
return ResponseEntity.ok(ApiResponse.success(List.of(product1, product2)));
```

**For paginated data:**
```java
return ResponseEntity.ok(ApiResponse.success(pageOfProducts));
```

**For statistics/metrics (use Map):**
```java
Map<String, Object> stats = new HashMap<>();
stats.put("totalProducts", 150);
stats.put("activeProducts", 120);
stats.put("revenue", 50000.00);
return ResponseEntity.ok(ApiResponse.success(stats));
```

**For void operations (DELETE):**
```java
// Option 1: 200 OK with message
return ResponseEntity.ok(ApiResponse.success("Resource deleted successfully", null));

// Option 2: 204 No Content
return ResponseEntity.noContent().build();
```

---

### 12. Status Codes

**Success codes:**
- `200 OK` - Successful GET, PUT, PATCH, DELETE (with body)
- `201 CREATED` - Successful POST (resource created)
- `204 NO CONTENT` - Successful DELETE (no body)

**Client error codes:**
- `400 BAD REQUEST` - Validation error, malformed request
- `401 UNAUTHORIZED` - Not authenticated (missing/invalid token)
- `403 FORBIDDEN` - Authenticated but not authorized (insufficient permissions)
- `404 NOT FOUND` - Resource not found
- `409 CONFLICT` - Duplicate resource, business rule violation

**Server error codes:**
- `500 INTERNAL SERVER ERROR` - Unexpected errors
- `503 SERVICE UNAVAILABLE` - System maintenance, database down

**Example usage:**
```java
// 201 Created
return ResponseEntity.status(HttpStatus.CREATED).body(response);

// 400 Bad Request
return ResponseEntity.badRequest().body(ApiResponse.error("Invalid input"));

// 404 Not Found (throw exception in service, handled by GlobalExceptionHandler)
throw new ResourceNotFoundException("Product not found with id: " + id);

// 409 Conflict
throw new DuplicateResourceException("Product with this SKU already exists");
```

---

## Common Patterns Summary

### ✅ DO's:

1. **Use consistent response format** across all endpoints (pick wrapped or direct)
2. **Always validate** request bodies with `@Valid`
3. **Log important actions** (create, update, delete) at INFO level
4. **Use `@PreAuthorize`** for security at method level
5. **Return 201 CREATED** for POST operations
6. **Use kebab-case** for URLs (`/product-categories`)
7. **Use plural nouns** for resources (`/products`, `/orders`)
8. **Use `/api` prefix** for REST endpoints
9. **Use DTOs** for requests/responses (not entities directly)
10. **Use `Pageable`** for large collections
11. **Use `@AuthenticationPrincipal`** to access current user
12. **Document with JavaDoc** for complex endpoints

### ❌ DON'Ts:

1. **Don't return entities** directly (use DTOs/Response classes)
2. **Don't use `@Autowired`** (use constructor injection with `@RequiredArgsConstructor`)
3. **Don't mix response formats** (wrapped vs direct) in same project
4. **Don't forget security** annotations on sensitive endpoints
5. **Don't return 200 for POST** (use 201 CREATED)
6. **Don't use verbs in resource URLs** (`/getProducts` ❌)
7. **Don't skip validation** on input
8. **Don't log sensitive data** (passwords, tokens)
9. **Don't mix camelCase and kebab-case** in URLs
10. **Don't hardcode messages** in controllers (externalize or use service layer)

---

## Quick Reference Template

See [ControllerTemplate.java](./templates/ControllerTemplate.java) for a complete REST controller boilerplate.

---

## Related Skills

- **Error Handling**: `skills/spring/error_handling.md`
- **Security Configuration**: `skills/spring/security_config.md`
- **Service Layer Design**: `skills/spring/service_layer.md`
- **DTO Patterns**: `skills/spring/dto_design.md`
- **JPA Best Practices**: `skills/spring/jpa_optimization.md`

---

## Templates

See `.agent/templates/spring/controller.template` for copy-paste boilerplate.

---

**Last Updated:** 2025-02-13  
**Status:** ✅ Production-ready (generic, reusable across projects)