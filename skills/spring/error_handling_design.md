## TL;DR - Quick Reference

### Global Setup
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler { ... }
```

### Critical Rules
1. **Never handle exceptions in Controllers** — let them bubble up to `RestControllerAdvice`.
2. **Specifics before Generics** — Spring matches exception handlers top-to-bottom.
3. **Standard Error Format** — Always return the same `ApiResponse` (success=false).
4. **Log Levels** — INFO/WARN for 4xx errors, ERROR + Stacktrace for 500 errors.
5. **Never expose stacktraces** or internal database details to the client.

### 📄 Templates
- [Global Exception Handler Template](./templates/GlobalExceptionHandlerTemplate.java)

---

## Core Principles

### 1. Centralized Exception Handling

**Use `@RestControllerAdvice` for global exception handling:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(SpecificException.class)
    public ResponseEntity<ApiResponse<Void>> handleSpecificException(SpecificException ex) {
        return ResponseEntity.status(HttpStatus.XXX)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
```

**Rationale:**
- ✅ Single source of truth for error responses
- ✅ Consistent error format across entire API
- ✅ Controllers stay clean (just throw exceptions)
- ✅ Easy to maintain and test

// Bad: Handling exceptions manually in controllers leads to duplication
@GetMapping("/{id}")
public ResponseEntity<?> getById(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(service.findById(id));
    } catch (NotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
}

// Good: Controller stays clean, Exception handled by GlobalExceptionHandler
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
}
```

---

## 2. Custom Exception Classes

**Create domain-specific exceptions:**
```java
// Base exception (optional)
public class BaseException extends RuntimeException {
    public BaseException(String message) {
        super(message);
    }
}

// Specific exceptions
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
```

**Usage in service layer:**
```java
@Service
public class ProductService {
    
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found with id: " + id
                ));
    }
    
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new AlreadyExistsException(
                "Product with name '" + request.getName() + "' already exists"
            );
        }
        // ... create logic
    }
}
```

**Rationale:**
- ✅ Clear intent (exception name explains what went wrong)
- ✅ Type-safe (can handle different exceptions differently)
- ✅ Searchable codebase (find all places throwing `ResourceNotFoundException`)

---

## 3. Exception-to-HTTP Status Mapping

**Standard mappings:**

| Exception Type | HTTP Status | Code | Use Case |
|----------------|-------------|------|----------|
| `ResourceNotFoundException` | NOT_FOUND | 404 | Resource doesn't exist |
| `AlreadyExistsException` | CONFLICT | 409 | Duplicate resource |
| `InvalidInputException` | BAD_REQUEST | 400 | Business validation failed |
| `BadCredentialsException` | UNAUTHORIZED | 401 | Wrong credentials |
| `AccessDeniedException` | FORBIDDEN | 403 | Insufficient permissions |
| `ExpiredJwtException` | UNAUTHORIZED | 401 | Token expired |
| `MethodArgumentNotValidException` | BAD_REQUEST | 400 | Request validation failed |
| `TooManyRequestsException` | TOO_MANY_REQUESTS | 429 | Rate limit exceeded |
| `RuntimeException` | BAD_REQUEST | 400 | Generic runtime error |
| `Exception` | INTERNAL_SERVER_ERROR | 500 | Unexpected error |

**Implementation:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 404 - Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    // 409 - Duplicate/Conflict
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyExistsException(
            AlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    // 401 - Authentication failed
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Email or password is incorrect"));
    }
    
    // 403 - Authorization failed
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You do not have permission to access this resource"));
    }
    
    // 429 - Rate limit
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyRequestsException(
            TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    // 500 - Unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        // Log full stack trace for debugging
        log.error("Unexpected error occurred", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
    }
}
```

**Key Points:**
- More specific exceptions first, generic last (Spring matches top-down)
- Always return consistent `ApiResponse` structure
- Log errors for debugging (especially 500s)

---

## 4. Validation Error Handling

**Handle `@Valid` validation errors:**
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
        MethodArgumentNotValidException ex) {
    
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
    });
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("Validation failed")
                    .data(errors)
                    .build());
}
```

**Example request with validation errors:**
```java
public class ProductRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;
}
```

**Response format:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "name": "Name is required",
    "price": "Price must be non-negative"
  }
}
```

**Rationale:**
- Field-level errors help frontend highlight specific inputs
- Clear, actionable error messages
- Standard format across all validation errors

---

## 5. Security-Related Exceptions

**JWT & Authentication errors:**
```java
// Invalid JWT signature
@ExceptionHandler(SignatureException.class)
public ResponseEntity<ApiResponse<Void>> handleSignatureException(SignatureException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Invalid JWT signature"));
}

// Expired JWT token
@ExceptionHandler(ExpiredJwtException.class)
public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("JWT token has expired"));
}

// Token refresh failure
@ExceptionHandler(TokenRefreshException.class)
public ResponseEntity<ApiResponse<Void>> handleTokenRefreshException(TokenRefreshException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getMessage()));
}

// Account locked/disabled
@ExceptionHandler(AccountStatusException.class)
public ResponseEntity<ApiResponse<Void>> handleAccountStatusException(AccountStatusException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Account is locked or disabled"));
}

// User not found (during authentication)
@ExceptionHandler(UsernameNotFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(
        UsernameNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
}
```

**Pattern observations:**
- JWT errors → 401 UNAUTHORIZED
- Account issues → 403 FORBIDDEN
- User not found → 404 NOT_FOUND (or 401 to avoid user enumeration)

**⚠️ Security consideration:**
```java
// BAD - reveals whether user exists
@ExceptionHandler(UsernameNotFoundException.class)
public ResponseEntity<?> handle(UsernameNotFoundException ex) {
    return ResponseEntity.status(404).body("User 'john@example.com' not found");
}

// GOOD - generic message
@ExceptionHandler(UsernameNotFoundException.class)
public ResponseEntity<?> handle(UsernameNotFoundException ex) {
    return ResponseEntity.status(401).body("Invalid credentials");
}
```

---

## 6. File Upload Exceptions

**Handle file upload errors:**
```java
// File too large
@ExceptionHandler(MaxUploadSizeExceededException.class)
public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
        MaxUploadSizeExceededException ex) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiResponse.error("File size exceeds maximum allowed size (50MB)"));
}

// Missing file part
@ExceptionHandler(MissingServletRequestPartException.class)
public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestPartException(
        MissingServletRequestPartException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Required part '" + ex.getRequestPartName() + "' is not present"));
}

// Unsupported media type
@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ApiResponse.error(ex.getMessage()));
}
```

**Configuration (in `application.yml`):**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

---

## 7. Domain-Specific Exceptions

**Create exceptions for your business domain:**
```java
// Example: Online session management
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}

public class RoomAccessDeniedException extends RuntimeException {
    public RoomAccessDeniedException(String message) {
        super(message);
    }
}

public class RoomAlreadyEndedException extends RuntimeException {
    public RoomAlreadyEndedException(String message) {
        super(message);
    }
}

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
```

**Handle in GlobalExceptionHandler:**
```java
@ExceptionHandler(RoomNotFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleRoomNotFoundException(RoomNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
}

@ExceptionHandler(RoomAccessDeniedException.class)
public ResponseEntity<ApiResponse<Void>> handleRoomAccessDeniedException(
        RoomAccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getMessage()));
}

@ExceptionHandler(RoomAlreadyEndedException.class)
public ResponseEntity<ApiResponse<Void>> handleRoomAlreadyEndedException(
        RoomAlreadyEndedException ex) {
    return ResponseEntity.status(HttpStatus.GONE)  // 410 GONE
            .body(ApiResponse.error(ex.getMessage()));
}

@ExceptionHandler(InvalidTokenException.class)
public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(InvalidTokenException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.getMessage()));
}
```

**Note:** Use `410 GONE` for resources that existed but are no longer available (ended sessions, expired links, etc.)

---

## 8. Catch-All Exception Handlers

**Order matters - most specific to most generic:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 1. Specific domain exceptions (highest priority)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(...) { }
    
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<?> handleAlreadyExists(...) { }
    
    // 2. Framework exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(...) { }
    
    // 3. Security exceptions
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(...) { }
    
    // 4. Generic runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.warn("Runtime exception occurred", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    // 5. Catch-all (lowest priority)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred", ex);  // Log full stack trace
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
    }
}
```

**Key Points:**
- Spring matches handlers **top to bottom**
- More specific exceptions should be declared first
- `RuntimeException` handler catches unchecked exceptions
- `Exception` handler is the ultimate fallback

**⚠️ Warning:**
```java
// BAD - too broad, catches everything
@ExceptionHandler(Throwable.class)
public ResponseEntity<?> handle(Throwable ex) { }

// GOOD - catch Exception, not Throwable
@ExceptionHandler(Exception.class)
public ResponseEntity<?> handle(Exception ex) { }
```

---

## 9. Logging Best Practices

**Log errors appropriately:**
```java
@RestControllerAdvice
@Slf4j  // Lombok
public class GlobalExceptionHandler {
    
    // Client errors (4xx) - log at WARN or INFO
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());  // WARN, not ERROR
        return ResponseEntity.status(404).body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        log.info("Failed login attempt");  // INFO (expected behavior)
        return ResponseEntity.status(401).body(ApiResponse.error("Invalid credentials"));
    }
    
    // Server errors (5xx) - log at ERROR with full stack trace
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobal(Exception ex) {
        log.error("Unexpected error occurred", ex);  // Full stack trace
        return ResponseEntity.status(500).body(ApiResponse.error("Internal server error"));
    }
}
```

**Logging levels:**
- `log.info()` - Expected errors (failed login, validation)
- `log.warn()` - Business rule violations (not found, already exists)
- `log.error()` - Unexpected errors (500s, database failures)

**⚠️ Don't log sensitive data:**
```java
// BAD
log.error("Login failed for user {} with password {}", username, password);

// GOOD
log.warn("Login failed for user {}", username);
```

---

## 10. Error Response Format

**Standard error response structure:**
```java
// Using ApiResponse wrapper
{
  "success": false,
  "message": "Resource not found with id: 123",
  "data": null
}

// Validation errors (with field details)
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email is required",
    "age": "Age must be at least 18"
  }
}
```

**Alternative: Detailed error response (optional)**
```java
@Data
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}

@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex,
        HttpServletRequest request) {
    
    ErrorResponse error = new ErrorResponse(
        LocalDateTime.now(),
        404,
        "Not Found",
        ex.getMessage(),
        request.getRequestURI()
    );
    
    return ResponseEntity.status(404).body(error);
}
```

Response:
```json
{
  "timestamp": "2025-02-13T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 123",
  "path": "/api/products/123"
}
```

**Choose one format and use consistently across entire project.**

---

## 11. Testing Exception Handling

**Unit test exception handlers:**
```java
@WebMvcTest(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldReturn404WhenResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/products/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }
    
    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"price\":-10}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.data.name").exists())
            .andExpect(jsonPath("$.data.price").exists());
    }
}
```

---

## Common Patterns Summary

### ✅ DO's:

1. **Use `@RestControllerAdvice`** for centralized exception handling
2. **Create custom exceptions** for domain-specific errors
3. **Map exceptions to appropriate HTTP status codes** (404, 409, 401, etc.)
4. **Return consistent error format** (`ApiResponse` or `ErrorResponse`)
5. **Handle validation errors** with field-level details
6. **Log errors appropriately** (WARN for 4xx, ERROR for 5xx)
7. **Include helpful error messages** for debugging
8. **Order handlers** from specific to generic
9. **Use `@Slf4j`** for logging
10. **Test exception handlers** with unit tests

### ❌ DON'Ts:

1. **Don't handle exceptions in controllers** (use global handler)
2. **Don't expose stack traces** to clients (security risk)
3. **Don't log sensitive data** (passwords, tokens, personal info)
4. **Don't use generic Exception everywhere** (create specific exceptions)
5. **Don't return different error formats** across endpoints
6. **Don't forget to log 500 errors** (need stack trace for debugging)
7. **Don't use wrong HTTP status codes** (409 for duplicates, not 400)
8. **Don't catch `Throwable`** (catch `Exception` instead)
9. **Don't reveal implementation details** in error messages
10. **Don't return 200 OK for errors** (use appropriate 4xx/5xx)

---

## Quick Reference Template

See [GlobalExceptionHandlerTemplate.java](./templates/GlobalExceptionHandlerTemplate.java) for the complete production-ready handler.

---

## Related Skills

- **REST API Design**: `skills/spring/rest_api_design.md`
- **Security Configuration**: `skills/spring/security_config.md`
- **Validation Patterns**: `skills/spring/validation.md`
- **Logging Best Practices**: `skills/spring/logging.md`

---

## Examples

See `examples/spring/global_exception_handler/` for complete working example.

---

**Last Updated:** 2025-02-13  
**Status:** ✅ Production-ready (generic, reusable across projects)