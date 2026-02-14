## TL;DR - Quick Reference

### Standard DTO Setup
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MyRequest/Response { ... }
```

### Critical Rules
1. **Request DTOs** are for input validation (use `@Valid`).
2. **Response DTOs** are for UI formatting (flat, clean, no secrets).
3. **Never reuse** the same DTO for both request and response.
4. **Jackson compatibility** — Always include `@NoArgsConstructor`.
5. **Package separation** — Keep them in `dto.request` and `dto.response`.

### Templates
- [Request DTO Template](./templates/RequestDTOTemplate.java)
- [Response DTO Template](./templates/ResponseDTOTemplate.java)

---

## Core Principles

### 1. Package Structure

**Separate request and response into sub-packages:**
```
modules/
└── finance/
    └── dto/
        ├── request/
        │   ├── InvoiceRequest.java
        │   └── SessionRequest.java
        └── response/
            ├── InvoiceResponse.java
            └── SessionResponse.java
```

**Rationale:**
- Request and response DTOs have different concerns — don't mix them
- Easier to find when the module grows
- Clear at a glance which classes are inbound vs outbound

---

### 2. Lombok Annotations

**Standard annotation set for DTOs:**
```java
// Request DTO — needs mutability for @Valid binding
@Data               // generates getters, setters, equals, hashCode, toString
@Builder            // fluent construction in tests
@NoArgsConstructor  // required for JSON deserialization (Jackson)
@AllArgsConstructor // required alongside @Builder
public class InvoiceRequest { }

// Response DTO — same set
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse { }
```

**Why `@Data` is fine on DTOs (unlike entities):**

| Class | `@Data` safe? | Reason |
|---|---|---|
| Entity | No | Has lazy-loaded relations → infinite loop in `equals/hashCode` |
| DTO | Yes | Plain fields only, no JPA proxies or circular refs |

**⚠️ Exception — avoid `@Data` on Response DTOs with nested lists if you override `equals`:**
```java
// If you need custom equals/hashCode, use @Getter @Setter @Builder explicitly
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse { }
```

---

### 3. Request DTO Design

**Design around the use case — not the entity:**
```java
/**
 * Request payload for generating an invoice.
 * Supports single student, multiple students, or all students in a billing month.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {

    /**
     * ID of the student the invoice is for.
     * Nullable when generating for multiple or all students.
     */
    private Long studentId;

    /** Billing month in YYYY-MM format. */
    private String month;

    /** Specific session record IDs to include in this invoice. */
    private List<Long> sessionRecordIds;

    /** Set to true to generate a summary invoice for all students in the month. */
    private Boolean allStudents;

    /** Set to true to filter for a subset of students. */
    private Boolean multipleStudents;

    /**
     * Target student IDs when {@link #multipleStudents} is true.
     */
    private List<Long> selectedStudentIds;
}
```

**Key design decisions:**

| Decision | Reason |
|---|---|
| Nullable `studentId` | Supports 3 modes: single / multiple / all |
| `Boolean` (wrapper) vs `boolean` (primitive) | Wrapper allows null → "not provided" vs false |
| `List<Long>` for IDs | Prefer IDs over full objects in request payloads |
| Javadoc on non-obvious fields | Documents conditional logic and relationships between fields |

---

### 4. Response DTO Design

**Return only what the client needs — no internal fields:**
```java
/**
 * Response payload for invoice details, intended for export or display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    /** Unique identifier for the invoice (e.g., INV-2024-12-001). */
    private String invoiceNumber;

    private String studentName;

    /** Human-readable month representation (e.g., Tháng 12/2024). */
    private String month;

    private Integer totalSessions;
    private Double totalHours;
    private Long totalAmount;

    /** Detailed breakdown of sessions included in the invoice. */
    private List<InvoiceItem> items;

    private BankInfo bankInfo;

    /** URL for generating the VietQR payment QR code. */
    private String qrCodeUrl;

    private String createdDate;
}
```

**What to include vs exclude:**

| Include in Response DTO | Exclude from Response DTO |
|---|---|
| Fields the UI directly renders | Internal DB IDs (unless needed for navigation) |
| Human-readable formatted values | Raw storage paths (`filePath` → `fileUrl`) |
| Aggregated/computed values | Sensitive data (`password`, `secretKey`) |
| Nested DTOs for related data | Full entity objects |
| Formatted strings (dates, amounts) | `version`, `createdBy`, audit fields (unless needed) |

**Transform at the DTO boundary — don't expose raw storage:**
```java
// Entity field
private String filePath;   // "cloudinary://bucket/file123.pdf"

// Response DTO field — expose public URL instead
private String fileUrl;    // "https://cdn.example.com/file123.pdf"

// Service mapping
response.setFileUrl(cloudinaryService.buildUrl(entity.getFilePath()));
```

---

### 5. Nested DTOs

**Use inner static classes or separate files for nested response structures:**

```java
// Option A: Inner static class (good for small, tightly-coupled structures)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private List<InvoiceItem> items;
    private BankInfo bankInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItem {
        private String subject;
        private LocalDate sessionDate;
        private Double hours;
        private Long pricePerHour;
        private Long amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankInfo {
        private String bankName;
        private String accountNumber;
        private String accountHolder;
    }
}

// Usage
InvoiceResponse.InvoiceItem item = InvoiceResponse.InvoiceItem.builder()
        .subject("Mathematics")
        .hours(1.5)
        .build();
```

```java
// Option B: Separate file (good for reuse across multiple response DTOs)
// InvoiceItem.java — reused in InvoiceResponse and InvoiceSummaryResponse
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
    private String subject;
    private LocalDate sessionDate;
    private Double hours;
    private Long pricePerHour;
    private Long amount;
}
```

**Which to use:**

| Scenario | Approach |
|---|---|
| Nested object used only in one DTO | Inner static class |
| Nested object reused across 2+ DTOs | Separate file |
| Nested object is complex (10+ fields) | Separate file |

---

### 6. Validation on Request DTOs

**Add `@Valid` constraints on request DTOs — not on entities:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Month is required")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Month must be in YYYY-MM format")
    private String month;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotNull(message = "Hours is required")
    @DecimalMin(value = "0.5", message = "Minimum session duration is 0.5 hours")
    @DecimalMax(value = "8.0", message = "Maximum session duration is 8 hours")
    private Double hours;

    @NotNull(message = "Price per hour is required")
    @Min(value = 0, message = "Price must be non-negative")
    private Long pricePerHour;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}
```

**Activate validation in the controller:**
```java
@PostMapping
public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
        @RequestBody @Valid InvoiceRequest request) {   // ← @Valid triggers validation
    return ResponseEntity.ok(ApiResponse.success(service.create(request)));
}
```

**Common validation annotations:**

| Annotation | Use Case |
|---|---|
| `@NotNull` | Field must be present (any type) |
| `@NotBlank` | String must be non-null and non-empty |
| `@NotEmpty` | Collection/String must not be empty |
| `@Min` / `@Max` | Numeric range |
| `@DecimalMin` / `@DecimalMax` | Decimal numeric range |
| `@Size(min, max)` | String length or collection size |
| `@Pattern(regexp)` | Regex validation |
| `@Email` | Email format |
| `@Past` / `@Future` | Date constraints |
| `@Valid` | Cascade validation to nested DTO |

**Cascade validation to nested objects:**
```java
public class OrderRequest {

    @Valid                       // ← triggers validation on the nested DTO
    @NotNull
    private AddressRequest shippingAddress;
}
```

---

### 7. Conditional Validation Pattern

**For DTOs with mutually exclusive modes (like `InvoiceRequest`):**
```java
// Option A: Validate in service layer (simple, explicit)
public InvoiceResponse generate(InvoiceRequest request) {
    if (Boolean.TRUE.equals(request.getMultipleStudents())) {
        if (request.getSelectedStudentIds() == null || request.getSelectedStudentIds().isEmpty()) {
            throw new InvalidInputException("selectedStudentIds required when multipleStudents is true");
        }
    } else if (!Boolean.TRUE.equals(request.getAllStudents())) {
        if (request.getStudentId() == null) {
            throw new InvalidInputException("studentId required for single student invoice");
        }
    }
    // proceed...
}
```

```java
// Option B: Custom validator annotation (cleaner, reusable)
@Constraint(validatedBy = InvoiceRequestValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInvoiceRequest {
    String message() default "Invalid invoice request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@ValidInvoiceRequest  // ← class-level annotation
public class InvoiceRequest { ... }
```

**Which to use:**

| Scenario | Approach |
|---|---|
| Simple, one-off validation | Service layer check |
| Same rule reused across multiple DTOs | Custom `@Constraint` |
| Complex cross-field logic | Custom `@Constraint` |

---

### 8. Naming Conventions

**Consistent names across all DTOs:**

| Pattern | Example |
|---|---|
| Request DTO | `{Feature}Request` | `InvoiceRequest`, `LoginRequest` |
| Response DTO | `{Feature}Response` | `InvoiceResponse`, `UserResponse` |
| Create request | `Create{Feature}Request` | `CreateSessionRequest` |
| Update request | `Update{Feature}Request` | `UpdateSessionRequest` |
| List response | `{Feature}ListResponse` or `List<{Feature}Response>` | `List<InvoiceResponse>` |
| Nested item | `{Feature}Item` or `{Feature}Info` | `InvoiceItem`, `BankInfo` |
| Paginated response | `PageResponse<T>` | `PageResponse<InvoiceResponse>` |

**Field naming — match what the client expects:**
```java
// ✅ Client-friendly names
private String studentName;      // not "stdNm" or "student_name"
private String invoiceNumber;    // not "invNo"
private LocalDate sessionDate;   // not "sessDate"

// ✅ Human-readable format fields — name them accordingly
private String month;            // "Tháng 12/2024" — not raw "2024-12"
private String createdDate;      // formatted string, not LocalDateTime
```

---

### 9. Javadoc on DTOs

**Document the DTO purpose and non-obvious fields:**
```java
/**
 * Request payload for generating an invoice.
 * Supports three modes:
 * - Single student: set {@link #studentId}
 * - Multiple students: set {@link #multipleStudents} = true + {@link #selectedStudentIds}
 * - All students: set {@link #allStudents} = true
 */
@Data
public class InvoiceRequest {

    /**
     * Nullable when generating for multiple or all students.
     */
    private Long studentId;

    /**
     * Target student IDs when {@link #multipleStudents} is true.
     */
    private List<Long> selectedStudentIds;
}
```

**What to document on DTOs:**

| Document | Skip |
|---|---|
| Class-level: overall purpose and modes | Self-explanatory field names |
| Conditional/nullable fields | `totalAmount`, `studentName`, `month` |
| Fields with format constraints | Boolean flags when name is self-explanatory |
| Cross-field relationships | Simple required fields |

---

### 10. Mapping Entity → Response DTO

**Map in the service layer — keep controllers and repositories clean:**
```java
@Service
@RequiredArgsConstructor
public class InvoiceService {

    // Option A: Manual mapping method (transparent, easy to debug)
    private InvoiceResponse toResponse(SessionRecord record) {
        return InvoiceResponse.builder()
                .invoiceNumber(generateInvoiceNumber(record))
                .studentName(record.getStudent().getFullName())
                .month(formatMonth(record.getMonth()))
                .totalSessions(record.getSessions())
                .totalHours(record.getHours())
                .totalAmount(record.getTotalAmount())
                .createdDate(LocalDate.now().toString())
                .build();
    }

    // Option B: MapStruct (recommended for large projects)
    // See: skills/spring/mapstruct.md
}
```

**Mapping approach comparison:**

| Approach | Pros | Cons | Use When |
|---|---|---|---|
| Manual (`builder`) | Simple, explicit, easy to debug | Verbose for many fields | Small–medium projects |
| MapStruct | Zero boilerplate, compile-time safe | Setup overhead | Large projects, many entities |
| ModelMapper | Easy setup | Runtime reflection, error-prone | ❌ Avoid |

---

## Full DTO Templates

See [RequestDTOTemplate.java](./templates/RequestDTOTemplate.java) and [ResponseDTOTemplate.java](./templates/ResponseDTOTemplate.java) for clean starter classes.

---

## Common Patterns Summary

### ✅ DO's:

1. **Separate `request/` and `response/` packages** inside each module's `dto/` folder
2. **Use `@Data @Builder @NoArgsConstructor @AllArgsConstructor`** on all DTOs
3. **Design request DTOs around the use case** — not around the entity schema
4. **Use `Boolean` (wrapper)** for optional boolean flags — allows null = "not provided"
5. **Add `@Valid` constraints on request DTOs** — not on entities
6. **Use inner static classes** for nested structures used only in one DTO
7. **Use separate files** for nested structures reused across multiple DTOs
8. **Transform at the DTO boundary** — never expose internal paths, raw enums, or DB IDs when not needed
9. **Document the class purpose and conditional fields** with Javadoc
10. **Map entity → DTO in the service layer** — not in controllers or repositories

### ❌ DON'Ts:

1. **Don't reuse the same DTO for both request and response** — different concerns, different fields
2. **Don't return entities from controllers** — always map to response DTOs first
3. **Don't put business logic in DTOs** — DTOs are dumb data containers
4. **Don't use `boolean` primitive for optional flags** — use `Boolean` wrapper to distinguish null vs false
5. **Don't skip `@NoArgsConstructor`** — Jackson needs it to deserialize JSON
6. **Don't expose sensitive fields** (`password`, `secretKey`, internal paths) in response DTOs
7. **Don't validate in the entity** — `@NotBlank`, `@Min` etc. belong in request DTOs
8. **Don't mix request and response in one class** — creates coupling and security risks
9. **Don't use `@Data` on entities** (reminder) — but it's fine on DTOs
10. **Don't duplicate nested classes** across DTOs — extract to a shared file if used in 2+ places

---

## Related Skills

- **Entity Design**: `skills/spring/entity_design.md`
- **Error Handling**: `skills/spring/error_handling.md`
- **Validation Patterns**: `skills/spring/validation.md`
- **Security Configuration**: `skills/spring/security_config.md`

---

**Last Updated:** 2025-02-13
**Status:** ✅ Production-ready (generic, reusable across projects)