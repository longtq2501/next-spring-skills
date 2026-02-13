# 🌍 Universal Global Rules for AI Agents

**Version:** 2.0  
**Last Updated:** February 2026  
**Applies To:** Full-stack projects with NextJS Frontend + Spring Boot Backend

---

## 📋 Table of Contents

1. [Overview & Philosophy](#overview--philosophy)
2. [Architecture Foundation](#architecture-foundation)
3. [Technology Stack Standards](#technology-stack-standards)
4. [Core Development Principles](#core-development-principles)
5. [Code Organization Patterns](#code-organization-patterns)
6. [Universal Code Standards](#universal-code-standards)
7. [Workflow Rules](#workflow-rules)
8. [Documentation Standards](#documentation-standards)
9. [Security Rules](#security-rules)
10. [Performance Guidelines](#performance-guidelines)
11. [Error Handling](#error-handling)
12. [Testing & Quality Assurance](#testing--quality-assurance)
13. [Dependency Management](#dependency-management)
14. [AI Agent Specific Rules](#ai-agent-specific-rules)
15. [Ledger System](#ledger-system)
16. [File Restrictions](#file-restrictions)
17. [Checklists](#checklists)
18. [Customization Guide](#customization-guide)

---

## 🎯 Overview & Philosophy

### Purpose
This document establishes **universal rules** for AI agents working on full-stack applications using:
- **Frontend:** NextJS with Feature-based Architecture
- **Backend:** Spring Boot with Modular Monolith Architecture  
- **Database:** MySQL or similar relational databases

### Three Pillars of Excellence

```
┌─────────────────────────────────────────────────┐
│                                                 │
│    READABILITY → MAINTAINABILITY → PERFORMANCE  │
│                                                 │
│    Clear Code      Future-proof      Optimized │
│    Easy to read    Easy to change    When needed│
│                                                 │
└─────────────────────────────────────────────────┘
```

### Core Values
- ✅ **Consistency** over cleverness
- ✅ **Simplicity** over complexity  
- ✅ **Explicitness** over implicitness
- ✅ **Standards** over personal preference

---

## 🏗️ Architecture Foundation

### Frontend: Feature-based Architecture

**Why:** Organizes code by business features, not technical layers. Improves discoverability, reduces coupling, scales with team growth.

```
src/
├── app/                    # NextJS App Router
│   ├── (auth)/            # Route groups
│   ├── (dashboard)/
│   └── api/               # API routes
│
├── features/              # 🎯 Core: Business domains
│   ├── auth/
│   │   ├── components/    # Feature-specific UI
│   │   ├── hooks/         # Feature-specific logic
│   │   ├── types/         # TypeScript definitions
│   │   ├── utils/         # Feature utilities
│   │   └── api/           # API client functions
│   │
│   ├── student/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── types/
│   │   └── utils/
│   │
│   └── payment/
│       └── ...
│
├── shared/                # Cross-cutting concerns
│   ├── components/        # Reusable UI components
│   ├── hooks/             # Common React hooks
│   ├── lib/               # Third-party configs
│   ├── types/             # Global types
│   └── utils/             # Global utilities
│
└── styles/                # Global styles
```

**Key Rules:**
- ✅ Keep features **independent** - minimal cross-feature imports
- ✅ Shared code goes in `shared/`, not copied across features
- ✅ Each feature is a **mini-application** with its own concerns
- ❌ Don't create "helpers" or "utils" folders outside features/shared

---

### Backend: Modular Monolith Architecture

**Why:** Combines monolith simplicity with microservices modularity. Enables future migration to microservices if needed.

```
src/main/java/com/project/
├── config/                # Global Spring configurations
│   ├── SecurityConfig.java
│   ├── JpaConfig.java
│   └── WebConfig.java
│
├── shared/                # Cross-module utilities
│   ├── exception/
│   ├── security/
│   └── util/
│
└── modules/               # 🎯 Core: Business modules
    ├── auth/
    │   ├── domain/        # Entities, Value Objects
    │   │   ├── User.java
    │   │   └── Role.java
    │   ├── application/   # Business logic
    │   │   ├── dto/
    │   │   └── service/
    │   ├── infrastructure/# External concerns
    │   │   └── repository/
    │   └── web/           # API layer
    │       └── controller/
    │
    ├── student/
    │   ├── domain/
    │   ├── application/
    │   ├── infrastructure/
    │   └── web/
    │
    └── payment/
        └── ...
```

**Key Rules:**
- ✅ Each module is **independently deployable** (in theory)
- ✅ Modules communicate through **well-defined interfaces**
- ✅ Domain layer has **zero external dependencies**
- ❌ No direct repository access from controllers
- ❌ No cross-module entity references (use DTOs/IDs)

---

### Database Architecture

**Connection Management:**
```java
// ✅ GOOD: Connection pooling configured
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

// ⚠️ CRITICAL: Always disable OSIV
spring.jpa.open-in-view=false
```

**Why OSIV must be disabled:**
- Prevents N+1 query problems hidden during development
- Forces explicit transaction boundaries
- Avoids lazy loading exceptions in views
- Improves performance and resource management

**Pagination Standards:**
```java
// ✅ GOOD: Always paginate large datasets
@GetMapping("/students")
public Page<StudentDTO> getStudents(
    @PageableDefault(size = 20, sort = "createdAt", direction = DESC) 
    Pageable pageable
) {
    return studentService.findAll(pageable);
}

// ❌ BAD: Fetching all records
List<Student> students = studentRepository.findAll();
```

**Resource Management Rules:**
- ✅ Use pagination for lists (default max: 100 items)
- ✅ Index frequently queried columns
- ✅ Use DTOs for API responses (never expose entities)
- ✅ Close resources in try-with-resources or @Transactional
- ❌ Never use `SELECT *` in production queries

---

## 💻 Technology Stack Standards

### Frontend Stack

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| **NextJS** | 14+ | React framework | Server components, App Router, built-in optimization |
| **React** | 18+ | UI library | Functional components only, hooks |
| **TypeScript** | 5+ | Type safety | Catch errors at compile time |
| **Tailwind CSS** | 3+ | Styling | Utility-first, consistent design |
| **Shadcn/UI** | Latest | Component library | Accessible, customizable, copy-paste |
| **React Hook Form** | Latest | Form management | Performance, validation |
| **Zod** | Latest | Schema validation | Type-safe validation |

**Coding Standards:**
```typescript
// ✅ GOOD: Functional component with TypeScript
interface StudentCardProps {
  student: Student;
  onEdit?: (id: string) => void;
}

export function StudentCard({ student, onEdit }: StudentCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{student.name}</CardTitle>
      </CardHeader>
    </Card>
  );
}

// ❌ BAD: Class components (deprecated in this stack)
class StudentCard extends React.Component { }
```

---

### Backend Stack

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| **Spring Boot** | 3.2+ | Application framework | Production-ready, comprehensive |
| **Java** | 17+ | Programming language | LTS, modern features |
| **Maven** | 3.9+ | Build tool | Dependency management |
| **Spring Data JPA** | Latest | ORM abstraction | Simplifies database access |
| **Hibernate** | 6+ | JPA implementation | Mature, feature-rich ORM |
| **Spring Security** | Latest | Authentication/Authorization | Industry standard |
| **JWT** | Latest | Token-based auth | Stateless, scalable |
| **Lombok** | Latest | Boilerplate reduction | Cleaner code |

**Critical Configuration:**
```yaml
# application.yml - MUST HAVE settings
spring:
  jpa:
    open-in-view: false          # ⚠️ CRITICAL: Disable OSIV
    hibernate:
      ddl-auto: validate         # Never use 'create' in production
    properties:
      hibernate:
        format_sql: true         # Dev only
        show_sql: false          # Never in production
        
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      
logging:
  level:
    org.hibernate.SQL: DEBUG     # Dev only
    org.hibernate.type: TRACE    # Dev only
```

---

## 🧭 Core Development Principles

### 1. Clean Code Fundamentals

**Boy Scout Rule:**
> "Leave code cleaner than you found it."

```java
// Before (found existing code)
public List<Student> getStudents() {
    return studentRepository.findAll();
}

// After (improved)
/**
 * Retrieves all active students with pagination
 * @param pageable Pagination parameters
 * @return Page of student DTOs
 */
public Page<StudentDTO> getActiveStudents(Pageable pageable) {
    return studentRepository.findByStatusActive(pageable)
        .map(studentMapper::toDTO);
}
```

### 2. YAGNI (You Aren't Gonna Need It)

**When to apply:**
- ✅ Don't build features for "future needs"
- ✅ Don't create abstractions prematurely
- ✅ Don't add dependencies "just in case"

```java
// ❌ BAD: Over-engineering for未来 needs
public interface PaymentStrategy { }
public class CreditCardStrategy implements PaymentStrategy { }
public class PayPalStrategy implements PaymentStrategy { }
// ...when you only support credit cards now

// ✅ GOOD: Simple, current needs only
@Service
public class PaymentService {
    public Payment processCreditCard(PaymentRequest request) {
        // Direct implementation
    }
}
```

### 3. Consistency Over Convention

**Apply project patterns everywhere:**
```typescript
// ✅ GOOD: Consistent naming across features
features/
  auth/
    components/LoginForm.tsx
    hooks/useAuth.ts
    types/auth.types.ts
    
  student/
    components/StudentForm.tsx
    hooks/useStudent.ts
    types/student.types.ts

// ❌ BAD: Inconsistent naming
features/
  auth/
    components/login-form.tsx
    hooks/authHook.ts
    types/AuthTypes.ts
```

---

## 📦 Code Organization Patterns

### Backend Patterns

#### 1. Entity (Domain Layer)
```java
@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Business logic methods (domain-driven design)
    public void activate() {
        this.status = StudentStatus.ACTIVE;
    }
    
    public boolean isActive() {
        return this.status == StudentStatus.ACTIVE;
    }
}
```

#### 2. DTO (Application Layer)
```java
// Request DTO
@Data
@Builder
public class CreateStudentRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;
    
    @Email(message = "Invalid email format")
    @NotBlank
    private String email;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
    private String phoneNumber;
}

// Response DTO
@Data
@Builder
public class StudentDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private StudentStatus status;
    private LocalDateTime createdAt;
}
```

#### 3. Repository (Infrastructure Layer)
```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // ✅ GOOD: Query methods with clear names
    Page<Student> findByStatus(StudentStatus status, Pageable pageable);
    
    Optional<Student> findByEmail(String email);
    
    @Query("SELECT s FROM Student s WHERE s.fullName LIKE %:keyword% " +
           "OR s.email LIKE %:keyword%")
    Page<Student> searchByKeyword(@Param("keyword") String keyword, 
                                   Pageable pageable);
    
    // ❌ BAD: Returning entities directly
    // List<Student> findAll();  // Use pagination instead!
}
```

#### 4. Service (Application Layer)
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default read-only
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    
    public Page<StudentDTO> getActiveStudents(Pageable pageable) {
        return studentRepository.findByStatus(StudentStatus.ACTIVE, pageable)
            .map(studentMapper::toDTO);
    }
    
    @Transactional  // Write operation
    public StudentDTO createStudent(CreateStudentRequest request) {
        // Validate business rules
        validateUniqueEmail(request.getEmail());
        
        // Map and save
        Student student = studentMapper.toEntity(request);
        student.setStatus(StudentStatus.PENDING);
        
        Student saved = studentRepository.save(student);
        return studentMapper.toDTO(saved);
    }
    
    private void validateUniqueEmail(String email) {
        if (studentRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email already exists");
        }
    }
}
```

#### 5. Controller (Web Layer)
```java
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Validated
public class StudentController {
    
    private final StudentService studentService;
    
    @GetMapping
    public ResponseEntity<Page<StudentDTO>> getStudents(
        @PageableDefault(size = 20, sort = "createdAt", direction = DESC)
        Pageable pageable
    ) {
        Page<StudentDTO> students = studentService.getActiveStudents(pageable);
        return ResponseEntity.ok(students);
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(
        @Valid @RequestBody CreateStudentRequest request
    ) {
        StudentDTO student = studentService.createStudent(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(student, "Student created successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudent(@PathVariable Long id) {
        return studentService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

#### 6. Enum Pattern
```java
@Getter
@RequiredArgsConstructor
public enum StudentStatus {
    PENDING("Pending Approval", "yellow"),
    ACTIVE("Active", "green"),
    SUSPENDED("Suspended", "red"),
    GRADUATED("Graduated", "blue");
    
    private final String displayName;
    private final String color;
    
    public boolean isActive() {
        return this == ACTIVE;
    }
}
```

#### 7. Exception Handling
```java
// Custom exceptions
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id));
    }
}

// Global exception handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
        BusinessException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
        ResourceNotFoundException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", errors));
    }
}
```

#### 8. JWT Authentication Pattern
```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

---

### Frontend Patterns

#### 1. Feature Folder Structure
```
features/student/
├── components/
│   ├── StudentCard.tsx
│   ├── StudentForm.tsx
│   └── StudentList.tsx
├── hooks/
│   ├── useStudent.ts
│   ├── useStudents.ts
│   └── useStudentForm.ts
├── types/
│   └── student.types.ts
├── utils/
│   └── studentHelpers.ts
└── api/
    └── studentApi.ts
```

#### 2. Component Pattern
```typescript
// student.types.ts
export interface Student {
  id: string;
  fullName: string;
  email: string;
  status: StudentStatus;
}

export enum StudentStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED'
}

// StudentCard.tsx
interface StudentCardProps {
  student: Student;
  onEdit?: (id: string) => void;
  onDelete?: (id: string) => void;
}

export function StudentCard({ student, onEdit, onDelete }: StudentCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{student.fullName}</CardTitle>
        <CardDescription>{student.email}</CardDescription>
      </CardHeader>
      <CardContent>
        <Badge variant={getStatusVariant(student.status)}>
          {student.status}
        </Badge>
      </CardContent>
      <CardFooter className="gap-2">
        {onEdit && (
          <Button onClick={() => onEdit(student.id)} variant="outline">
            Edit
          </Button>
        )}
        {onDelete && (
          <Button onClick={() => onDelete(student.id)} variant="destructive">
            Delete
          </Button>
        )}
      </CardFooter>
    </Card>
  );
}
```

#### 3. Custom Hook Pattern
```typescript
// useStudents.ts
export function useStudents() {
  const [students, setStudents] = useState<Student[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchStudents = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const data = await studentApi.getAll();
      setStudents(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch students');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents();
  }, []);

  return {
    students,
    isLoading,
    error,
    refetch: fetchStudents
  };
}
```

#### 4. API Client Pattern
```typescript
// studentApi.ts
import { apiClient } from '@/shared/lib/apiClient';

export const studentApi = {
  getAll: async (): Promise<Student[]> => {
    const response = await apiClient.get('/students');
    return response.data;
  },
  
  getById: async (id: string): Promise<Student> => {
    const response = await apiClient.get(`/students/${id}`);
    return response.data;
  },
  
  create: async (data: CreateStudentRequest): Promise<Student> => {
    const response = await apiClient.post('/students', data);
    return response.data;
  },
  
  update: async (id: string, data: UpdateStudentRequest): Promise<Student> => {
    const response = await apiClient.put(`/students/${id}`, data);
    return response.data;
  },
  
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/students/${id}`);
  }
};
```

#### 5. App Router Pattern
```typescript
// app/(dashboard)/students/page.tsx
import { StudentList } from '@/features/student/components/StudentList';

export default function StudentsPage() {
  return (
    <div className="container mx-auto py-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Students</h1>
        <Button asChild>
          <Link href="/students/new">Add Student</Link>
        </Button>
      </div>
      <StudentList />
    </div>
  );
}

// app/(dashboard)/students/[id]/page.tsx
interface StudentDetailPageProps {
  params: { id: string };
}

export default async function StudentDetailPage({ 
  params 
}: StudentDetailPageProps) {
  const student = await studentApi.getById(params.id);
  
  return (
    <div className="container mx-auto py-6">
      <StudentCard student={student} />
    </div>
  );
}
```

---

## 📏 Universal Code Standards

### Naming Conventions

| Type | Convention | Example | Why |
|------|-----------|---------|-----|
| **Variables** | camelCase | `studentList`, `isActive` | JavaScript/TypeScript standard |
| **Constants** | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `API_BASE_URL` | Clearly indicates immutability |
| **Functions** | camelCase | `getStudents()`, `validateEmail()` | Verb-based, action-oriented |
| **Classes/Interfaces** | PascalCase | `StudentService`, `UserRepository` | Noun-based, distinguishes types |
| **Components** | PascalCase | `StudentCard`, `LoginForm` | React convention |
| **Files (TS/TSX)** | PascalCase | `StudentCard.tsx`, `UserService.ts` | Matches export name |
| **Files (Java)** | PascalCase | `StudentService.java`, `User.java` | Matches class name |
| **Folders** | kebab-case | `student-management/`, `api-client/` | Lowercase, URL-friendly |
| **Enums** | PascalCase | `StudentStatus`, `PaymentMethod` | Type-like naming |
| **Enum Values** | UPPER_SNAKE_CASE | `ACTIVE`, `PAYMENT_PENDING` | Constant-like values |

### Function Design Principles

**Single Responsibility:**
```typescript
// ❌ BAD: Function doing too much
function handleStudentSubmit(data: StudentFormData) {
  // Validates
  if (!data.email.includes('@')) throw new Error('Invalid email');
  
  // Transforms
  const student = { ...data, createdAt: new Date() };
  
  // Makes API call
  fetch('/api/students', { method: 'POST', body: JSON.stringify(student) });
  
  // Updates UI
  setStudents(prev => [...prev, student]);
  showToast('Student created');
}

// ✅ GOOD: Separated concerns
function validateStudentData(data: StudentFormData): void {
  if (!data.email.includes('@')) {
    throw new ValidationError('Invalid email');
  }
}

function createStudent(data: StudentFormData): Promise<Student> {
  return studentApi.create(data);
}

async function handleStudentSubmit(data: StudentFormData) {
  validateStudentData(data);
  const student = await createStudent(data);
  setStudents(prev => [...prev, student]);
  showSuccessToast('Student created');
}
```

**Function Length Guidelines:**
- ✅ **Ideal:** 10-20 lines
- ⚠️ **Warning:** 20-50 lines (consider refactoring)
- ❌ **Bad:** 50+ lines (definitely refactor)

**Parameter Limits:**
```typescript
// ❌ BAD: Too many parameters
function createStudent(
  name: string,
  email: string,
  phone: string,
  address: string,
  city: string,
  country: string,
  zipCode: string
) { }

// ✅ GOOD: Object parameter
interface CreateStudentParams {
  name: string;
  email: string;
  phone: string;
  address: Address;
}

function createStudent(params: CreateStudentParams) { }
```

### Comments & Documentation

**When to Comment:**
- ✅ **Complex algorithms** - Explain the "why", not the "what"
- ✅ **Business rules** - Document domain-specific logic
- ✅ **Non-obvious decisions** - Explain trade-offs
- ✅ **TODOs** - Track technical debt
- ✅ **Public APIs** - JSDoc/Javadoc for all public methods

**When NOT to Comment:**
```typescript
// ❌ BAD: Obvious comment
// Set student to active
student.status = StudentStatus.ACTIVE;

// ✅ GOOD: Self-documenting code
student.activate();
```

**Good Comment Examples:**
```typescript
// ✅ GOOD: Explains "why"
// We use debounce here because the search API rate limits to 10 req/sec
const debouncedSearch = useDebouncedCallback(handleSearch, 300);

// ✅ GOOD: Documents business rule
// Students must be 18+ to enroll in adult programs per policy #2024-03
if (student.age < 18 && program.category === 'ADULT') {
  throw new BusinessRuleViolation('Student must be 18 or older');
}

// ✅ GOOD: Technical debt tracking
// TODO: Migrate to react-query for better caching (2024-Q2)
const [data, setData] = useState<Student[]>([]);
```

---

## 🔄 Workflow Rules

### Before Writing Any Code

**MANDATORY CHECKLIST:**

- [ ] Read existing patterns in the codebase
- [ ] Check if similar functionality already exists
- [ ] Understand the full requirement before coding
- [ ] Identify which module/feature this belongs to
- [ ] Review relevant documentation (README, architecture docs)

**Questions to Ask:**
1. Does this feature already exist somewhere?
2. Which layer(s) does this change affect?
3. Are there existing utilities I can reuse?
4. What's the expected performance impact?
5. How will this be tested?

### During Development

**Test-First Approach:**
```java
// 1. Write test first
@Test
void shouldCreateStudent() {
    CreateStudentRequest request = new CreateStudentRequest();
    request.setFullName("John Doe");
    request.setEmail("john@example.com");
    
    StudentDTO result = studentService.createStudent(request);
    
    assertNotNull(result.getId());
    assertEquals("John Doe", result.getFullName());
}

// 2. Then implement
public StudentDTO createStudent(CreateStudentRequest request) {
    // Implementation
}

// 3. Run test to verify
```

**Commit Frequency:**
- ✅ Commit **logical units of work** (not random save points)
- ✅ One feature/fix per commit
- ✅ Commit when tests pass
- ❌ Don't commit broken code
- ❌ Don't commit "WIP" or "temp" commits to main

**Live Documentation:**
```typescript
// Update types when changing data structures
export interface Student {
  id: string;
  fullName: string;
  email: string;
  // Added 2024-02-13: Support for multiple phone numbers
  phoneNumbers: string[];  // Changed from phoneNumber: string
}
```

### Before Committing

**PRE-COMMIT CHECKLIST:**

```bash
# 1. Run linter
npm run lint        # Frontend
./mvnw checkstyle   # Backend

# 2. Run tests
npm run test        # Frontend
./mvnw test         # Backend

# 3. Check for debug code
grep -r "console.log" src/       # Remove console.logs
grep -r "System.out" src/        # Remove System.out.println

# 4. Update documentation
# - Update CONTINUITY.md if needed
# - Update component/function docs
# - Update README if public API changed

# 5. Security check
# - No hardcoded secrets
# - No API keys in code
# - No passwords in comments

# 6. Responsive design check (Frontend)
# - Test on mobile viewport
# - Test on tablet viewport
# - Verify accessibility
```

### Commit Message Format

**Structure:**
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

**Examples:**
```bash
# ✅ GOOD
feat(student): add pagination to student list

Implemented server-side pagination with 20 items per page.
Added page navigation controls to UI.

Closes #123

# ✅ GOOD
fix(auth): prevent token expiration edge case

Users were being logged out 1 second before token actually expired
due to clock skew. Added 30-second buffer.

Fixes #456

# ❌ BAD
update stuff

# ❌ BAD
fix bug
```

---

## 📚 Documentation Standards

### What to Document

**MUST DOCUMENT:**
- [ ] Public API methods (all parameters, return types, exceptions)
- [ ] Complex algorithms or business logic
- [ ] Non-obvious design decisions
- [ ] Module/package purposes
- [ ] Environment variables and configuration
- [ ] Setup/installation steps
- [ ] Deployment procedures

**DON'T DOCUMENT:**
- ❌ Self-explanatory code
- ❌ Private helper methods (unless complex)
- ❌ Getters/setters
- ❌ Obvious variable names

### JSDoc Template (Frontend)

```typescript
/**
 * Retrieves paginated list of students with optional filtering
 * 
 * @param {Object} params - Query parameters
 * @param {number} params.page - Page number (0-indexed)
 * @param {number} params.size - Items per page (max: 100)
 * @param {StudentStatus} [params.status] - Optional status filter
 * @param {string} [params.search] - Optional search keyword
 * 
 * @returns {Promise<PageResponse<Student>>} Paginated student list
 * 
 * @throws {ValidationError} If page size exceeds maximum
 * @throws {ApiError} If server request fails
 * 
 * @example
 * const students = await getStudents({ 
 *   page: 0, 
 *   size: 20, 
 *   status: StudentStatus.ACTIVE 
 * });
 */
export async function getStudents(params: GetStudentsParams): Promise<PageResponse<Student>> {
  // Implementation
}
```

### Javadoc Template (Backend)

```java
/**
 * Service for managing student-related business operations.
 * 
 * <p>Handles CRUD operations, validation, and business rules for students.
 * All methods use pagination for list operations to prevent performance issues.
 * 
 * @author AI Agent
 * @since 1.0
 * @see StudentRepository
 * @see StudentDTO
 */
@Service
@RequiredArgsConstructor
public class StudentService {
    
    /**
     * Creates a new student in the system.
     * 
     * <p>Validates email uniqueness and applies business rules before creation.
     * New students start in PENDING status and require admin approval.
     * 
     * @param request the student creation request containing required fields
     * @return the created student as DTO with generated ID
     * @throws BusinessException if email already exists
     * @throws ValidationException if request data is invalid
     */
    @Transactional
    public StudentDTO createStudent(CreateStudentRequest request) {
        // Implementation
    }
}
```

### README Updates

**When to update README:**
- ✅ New environment variable added
- ✅ New dependency required
- ✅ Setup steps changed
- ✅ New feature affects public API
- ✅ Deployment process changed

**README Structure:**
```markdown
# Project Name

## Overview
Brief description of the project

## Tech Stack
- Frontend: NextJS 14, React 18, TypeScript, Tailwind
- Backend: Spring Boot 3.2, Java 17, MySQL 8

## Prerequisites
- Node.js 18+
- Java 17+
- MySQL 8+

## Setup
### Backend
1. Clone repository
2. Configure database in `application-dev.yml`
3. Run `./mvnw spring-boot:run`

### Frontend
1. Install dependencies: `npm install`
2. Configure environment: `cp .env.example .env.local`
3. Run dev server: `npm run dev`

## Environment Variables
| Variable | Description | Example |
|----------|-------------|---------|
| DATABASE_URL | MySQL connection string | jdbc:mysql://localhost:3306/db |
| JWT_SECRET | Secret for JWT signing | your-secret-key |

## Project Structure
See [ARCHITECTURE.md](./docs/ARCHITECTURE.md)

## Contributing
See [CONTRIBUTING.md](./docs/CONTRIBUTING.md)
```

---

## 🔒 Security Rules

### Never Commit These

**ABSOLUTE PROHIBITIONS:**

```bash
# ❌ NEVER COMMIT
API_KEY=sk-1234567890abcdef
DATABASE_PASSWORD=mySecretPassword123
JWT_SECRET=super-secret-key
STRIPE_SECRET_KEY=sk_live_...

# ❌ NEVER COMMIT
private_key.pem
credentials.json
.env.local (if contains real secrets)

# ❌ NEVER COMMIT
# Hardcoded in code
const API_KEY = "sk-1234567890abcdef";
const PASSWORD = "admin123";
```

**Use Instead:**
```bash
# ✅ GOOD: Environment variables
API_KEY=${API_KEY}
DATABASE_PASSWORD=${DB_PASSWORD}

# ✅ GOOD: Environment-specific files (gitignored)
.env.local
.env.production.local

# ✅ GOOD: Secret management service
AWS Secrets Manager
Azure Key Vault
HashiCorp Vault
```

### Input Validation

**Always Validate:**
```java
// ✅ GOOD: Backend validation
@PostMapping("/students")
public ResponseEntity<StudentDTO> createStudent(
    @Valid @RequestBody CreateStudentRequest request  // @Valid triggers validation
) {
    // Additional business validation
    if (request.getAge() < 0) {
        throw new ValidationException("Age cannot be negative");
    }
    
    return ResponseEntity.ok(studentService.createStudent(request));
}

// DTO with validation annotations
@Data
public class CreateStudentRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String fullName;
    
    @Email(message = "Invalid email format")
    @NotBlank
    private String email;
    
    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 150, message = "Age seems unrealistic")
    private Integer age;
}
```

```typescript
// ✅ GOOD: Frontend validation with Zod
const studentSchema = z.object({
  fullName: z.string()
    .min(2, 'Name must be at least 2 characters')
    .max(100, 'Name too long'),
  email: z.string().email('Invalid email format'),
  age: z.number()
    .min(0, 'Age cannot be negative')
    .max(150, 'Age seems unrealistic')
});

type StudentFormData = z.infer<typeof studentSchema>;

function StudentForm() {
  const form = useForm<StudentFormData>({
    resolver: zodResolver(studentSchema)
  });
  
  // Zod validates before submission
}
```

### Authentication & Authorization

**JWT Best Practices:**
```java
// ✅ GOOD: Secure JWT configuration
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

// ✅ GOOD: Token expiration
private static final long JWT_EXPIRATION = 15 * 60 * 1000; // 15 minutes
private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days
```

### Rate Limiting

```java
// ✅ GOOD: Rate limiting for sensitive endpoints
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    @RateLimit(maxRequests = 5, windowSeconds = 300) // 5 attempts per 5 minutes
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

### SQL Injection Prevention

```java
// ✅ GOOD: Parameterized queries (JPA does this automatically)
@Query("SELECT s FROM Student s WHERE s.email = :email")
Optional<Student> findByEmail(@Param("email") String email);

// ❌ BAD: String concatenation (NEVER DO THIS)
String query = "SELECT * FROM students WHERE email = '" + email + "'";
```

### XSS Prevention

```typescript
// ✅ GOOD: React escapes by default
<div>{student.fullName}</div>  // Automatically escaped

// ⚠️ DANGEROUS: Only when you control the content
<div dangerouslySetInnerHTML={{ __html: sanitizedHtml }} />

// ✅ GOOD: Sanitize before using dangerouslySetInnerHTML
import DOMPurify from 'dompurify';

const sanitized = DOMPurify.sanitize(userContent);
<div dangerouslySetInnerHTML={{ __html: sanitized }} />
```

---

## ⚡ Performance Guidelines

### When to Optimize

**OPTIMIZE NOW:**
- ✅ Database queries (N+1 problems, missing indexes)
- ✅ Large file uploads/downloads
- ✅ Heavy computations in loops
- ✅ Memory leaks
- ✅ API response times > 2 seconds

**OPTIMIZE LATER:**
- ⏳ Micro-optimizations (few milliseconds)
- ⏳ Premature abstractions
- ⏳ Code that runs once (initialization)
- ⏳ Perfect caching strategies (start simple)

### Database Performance

**N+1 Query Problem:**
```java
// ❌ BAD: N+1 queries
List<Student> students = studentRepository.findAll();
for (Student student : students) {
    // This triggers a separate query for EACH student!
    List<Enrollment> enrollments = student.getEnrollments();
}

// ✅ GOOD: Fetch join
@Query("SELECT s FROM Student s LEFT JOIN FETCH s.enrollments")
List<Student> findAllWithEnrollments();
```

**Pagination:**
```java
// ❌ BAD: Fetching all records
@GetMapping("/students")
public List<StudentDTO> getStudents() {
    return studentService.findAll();  // Could be 10,000+ records!
}

// ✅ GOOD: Paginated
@GetMapping("/students")
public Page<StudentDTO> getStudents(
    @PageableDefault(size = 20) Pageable pageable
) {
    return studentService.findAll(pageable);
}
```

**Indexing:**
```sql
-- ✅ GOOD: Index frequently queried columns
CREATE INDEX idx_student_email ON students(email);
CREATE INDEX idx_student_status ON students(status);
CREATE INDEX idx_enrollment_student_id ON enrollments(student_id);
```

### Frontend Performance

**Code Splitting:**
```typescript
// ✅ GOOD: Dynamic imports for large components
const HeavyChart = dynamic(() => import('@/components/HeavyChart'), {
  loading: () => <Skeleton />,
  ssr: false
});

// ✅ GOOD: Route-based code splitting (NextJS does this automatically)
app/
  dashboard/
    page.tsx      // Separate bundle
  students/
    page.tsx      // Separate bundle
```

**Memoization:**
```typescript
// ✅ GOOD: Memoize expensive computations
const expensiveValue = useMemo(() => {
  return students
    .filter(s => s.status === 'ACTIVE')
    .map(s => ({ ...s, fullName: s.firstName + ' ' + s.lastName }))
    .sort((a, b) => a.fullName.localeCompare(b.fullName));
}, [students]);

// ✅ GOOD: Memoize callbacks
const handleStudentClick = useCallback((id: string) => {
  router.push(`/students/${id}`);
}, [router]);
```

**Image Optimization:**
```typescript
// ✅ GOOD: NextJS Image component
import Image from 'next/image';

<Image
  src="/student-photo.jpg"
  alt="Student photo"
  width={200}
  height={200}
  loading="lazy"
  placeholder="blur"
/>

// ❌ BAD: Regular img tag
<img src="/student-photo.jpg" />
```

### API Response Size

```java
// ❌ BAD: Sending entire entity
@GetMapping("/students")
public List<Student> getStudents() {
    return studentRepository.findAll();  // Includes ALL fields, relations!
}

// ✅ GOOD: DTO with only needed fields
@GetMapping("/students")
public Page<StudentListDTO> getStudents(Pageable pageable) {
    return studentService.findAll(pageable)
        .map(student -> new StudentListDTO(
            student.getId(),
            student.getFullName(),
            student.getEmail(),
            student.getStatus()
        ));
}
```

---

## 🚨 Error Handling

### Error Hierarchy

```
Errors
├── Operational Errors (Expected, handle gracefully)
│   ├── ValidationError (400)
│   ├── UnauthorizedError (401)
│   ├── ForbiddenError (403)
│   ├── NotFoundError (404)
│   ├── ConflictError (409)
│   └── BusinessError (400)
│
└── Programming Errors (Unexpected, fix immediately)
    ├── NullPointerException
    ├── TypeError
    ├── DatabaseConnectionError
    └── OutOfMemoryError
```

### Backend Error Handling

```java
// Custom exception hierarchy
public class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    
    public BaseException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }
}

public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
}

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resource, Object id) {
        super(
            String.format("%s not found with id: %s", resource, id),
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND"
        );
    }
}

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION");
    }
}

// Global error response structure
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> errors;
}

// Global exception handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
        BaseException ex,
        HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
        return ResponseEntity.status(ex.getStatus()).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(
        Exception ex,
        HttpServletRequest request
    ) {
        // Log the full stack trace
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred. Please try again later.")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Frontend Error Handling

```typescript
// API error types
export class ApiError extends Error {
  constructor(
    message: string,
    public statusCode: number,
    public code: string,
    public errors?: Record<string, string>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// API client with error handling
export const apiClient = {
  async request<T>(url: string, options?: RequestInit): Promise<T> {
    try {
      const response = await fetch(url, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          ...options?.headers,
        },
      });

      if (!response.ok) {
        const error = await response.json();
        throw new ApiError(
          error.message,
          response.status,
          error.code,
          error.errors
        );
      }

      return response.json();
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }
      
      // Network or other errors
      throw new ApiError(
        'Network error. Please check your connection.',
        0,
        'NETWORK_ERROR'
      );
    }
  },
};

// Component error handling
export function StudentForm() {
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (data: StudentFormData) => {
    try {
      setError(null);
      setFieldErrors({});
      
      await studentApi.create(data);
      toast.success('Student created successfully');
      router.push('/students');
      
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.errors) {
          // Validation errors for specific fields
          setFieldErrors(err.errors);
        } else {
          // General error message
          setError(err.message);
        }
      } else {
        setError('An unexpected error occurred');
      }
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}
      
      <Input
        {...register('fullName')}
        error={fieldErrors.fullName}
      />
      {/* ... */}
    </form>
  );
}
```

---

## 🧪 Testing & Quality Assurance

### Backend Testing

**Test Structure:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class StudentServiceTest {
    
    @Autowired
    private StudentService studentService;
    
    @MockBean
    private StudentRepository studentRepository;
    
    @Test
    @DisplayName("Should create student successfully")
    void shouldCreateStudent() {
        // Given (Arrange)
        CreateStudentRequest request = CreateStudentRequest.builder()
            .fullName("John Doe")
            .email("john@example.com")
            .build();
            
        Student savedStudent = new Student();
        savedStudent.setId(1L);
        savedStudent.setFullName("John Doe");
        savedStudent.setEmail("john@example.com");
        
        when(studentRepository.save(any(Student.class)))
            .thenReturn(savedStudent);
        
        // When (Act)
        StudentDTO result = studentService.createStudent(request);
        
        // Then (Assert)
        assertNotNull(result.getId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        verify(studentRepository, times(1)).save(any(Student.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        CreateStudentRequest request = CreateStudentRequest.builder()
            .email("existing@example.com")
            .build();
            
        when(studentRepository.findByEmail("existing@example.com"))
            .thenReturn(Optional.of(new Student()));
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            studentService.createStudent(request);
        });
    }
}
```

**Integration Tests:**
```java
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class StudentIntegrationTest {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Test
    @DisplayName("Should save and retrieve student from database")
    void shouldSaveAndRetrieveStudent() {
        // Given
        CreateStudentRequest request = CreateStudentRequest.builder()
            .fullName("Jane Doe")
            .email("jane@example.com")
            .build();
        
        // When
        StudentDTO created = studentService.createStudent(request);
        Student retrieved = studentRepository.findById(created.getId()).orElseThrow();
        
        // Then
        assertEquals("Jane Doe", retrieved.getFullName());
        assertEquals("jane@example.com", retrieved.getEmail());
    }
}
```

### Frontend Testing (Prepared for Jest/Vitest)

**Component Test Template:**
```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { StudentCard } from './StudentCard';

describe('StudentCard', () => {
  const mockStudent = {
    id: '1',
    fullName: 'John Doe',
    email: 'john@example.com',
    status: StudentStatus.ACTIVE
  };

  it('renders student information correctly', () => {
    render(<StudentCard student={mockStudent} />);
    
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
  });

  it('calls onEdit when edit button is clicked', () => {
    const handleEdit = jest.fn();
    render(<StudentCard student={mockStudent} onEdit={handleEdit} />);
    
    fireEvent.click(screen.getByText('Edit'));
    
    expect(handleEdit).toHaveBeenCalledWith('1');
  });
});
```

**Hook Test Template:**
```typescript
import { renderHook, waitFor } from '@testing-library/react';
import { useStudents } from './useStudents';

describe('useStudents', () => {
  it('fetches students on mount', async () => {
    const { result } = renderHook(() => useStudents());
    
    expect(result.current.isLoading).toBe(true);
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.students).toHaveLength(3);
  });
});
```

### Test Coverage Goals

| Layer | Minimum Coverage | Target Coverage |
|-------|-----------------|-----------------|
| Service Layer | 80% | 90%+ |
| Repository Layer | 70% | 80%+ |
| Controllers | 60% | 70%+ |
| Components | 70% | 80%+ |
| Utilities | 90% | 95%+ |

---

## 📦 Dependency Management

### Before Adding a Dependency

**ASK THESE QUESTIONS:**

1. ❓ Can I solve this with existing dependencies?
2. ❓ Is this library actively maintained? (Last commit < 6 months)
3. ❓ Does it have good documentation?
4. ❓ What's the bundle size impact? (Frontend)
5. ❓ Are there known security vulnerabilities?
6. ❓ Is this a well-known, trusted library?
7. ❓ Will this create version conflicts?

### Version Pinning

**Backend (pom.xml):**
```xml
<!-- ✅ GOOD: Specific version -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- ❌ BAD: Range or latest -->
<version>LATEST</version>
<version>[3.0,4.0)</version>
```

**Frontend (package.json):**
```json
{
  "dependencies": {
    // ✅ GOOD: Pinned version
    "next": "14.0.4",
    "react": "18.2.0",
    
    // ⚠️ ACCEPTABLE: Patch updates only
    "react-hook-form": "^7.49.0",
    
    // ❌ BAD: Major version ranges
    "some-package": "*",
    "another-package": ">=1.0.0"
  }
}
```

### Dependency Update Strategy

```bash
# Monthly dependency check
npm outdated              # Frontend
./mvnw versions:display-dependency-updates  # Backend

# Update strategy:
# 1. Patch updates (7.49.0 → 7.49.1): Auto-update if tests pass
# 2. Minor updates (7.49.0 → 7.50.0): Review changelog, update if safe
# 3. Major updates (7.49.0 → 8.0.0): Plan migration, test thoroughly
```

---

## 🤖 AI Agent Specific Rules

### Session Start Protocol

**MANDATORY FIRST STEPS:**

1. **Read Project Continuity:**
   ```bash
   # Always read these files first
   CONTINUITY.md
   ISSUES.md
   README.md
   ```

2. **Understand Current State:**
   - What was the last completed task?
   - Are there any open issues or blockers?
   - What's the current sprint/milestone?

3. **Check for Dependencies:**
   - Are there pending PRs affecting this work?
   - Any merge conflicts to resolve?
   - Required environment setup?

### During Development

**Communication Standards:**

1. **Explain Your Approach:**
   ```markdown
   I'm going to implement student pagination with this approach:
   
   1. Backend:
      - Update StudentRepository with Pageable parameter
      - Modify StudentService to return Page<StudentDTO>
      - Update Controller endpoint
   
   2. Frontend:
      - Add pagination controls to StudentList component
      - Update useStudents hook to handle page state
      - Implement page navigation logic
   
   3. Testing:
      - Unit tests for service pagination
      - Integration test for full flow
   ```

2. **Show Trade-offs:**
   ```markdown
   For the search feature, I see two options:
   
   Option A: Client-side filtering
   ✅ Pros: Faster for small datasets, no extra API calls
   ❌ Cons: Won't work with pagination, performance issues at scale
   
   Option B: Server-side filtering
   ✅ Pros: Scalable, works with pagination, consistent performance
   ❌ Cons: Extra API calls, slightly more complex
   
   Recommendation: Option B, because we're using pagination and 
   expecting the dataset to grow significantly.
   ```

3. **Document Decisions:**
   ```markdown
   Decision: Disabled OSIV (Open Session In View)
   
   Why: 
   - Prevents hidden N+1 query problems
   - Forces explicit transaction boundaries
   - Better performance in production
   
   Impact:
   - Must use fetch joins for lazy-loaded relations
   - Need to convert entities to DTOs in service layer
   
   References:
   - https://vladmihalcea.com/the-open-session-in-view-anti-pattern/
   ```

### When Stuck

**Problem-Solving Protocol:**

1. **State the Problem Clearly:**
   ```markdown
   Problem: Students API returns 500 error when fetching list
   
   What I've Tried:
   1. Checked logs - NullPointerException in StudentService line 45
   2. Verified database connection - working fine
   3. Tested with Postman - same error
   
   Current Hypothesis:
   The mapper might be failing on a null field in the database
   ```

2. **Show Debugging Attempts:**
   ```java
   // Added debug logging
   log.debug("Fetching students with pageable: {}", pageable);
   Page<Student> students = studentRepository.findAll(pageable);
   log.debug("Found {} students", students.getTotalElements());
   
   // Added null checks
   if (student.getPhoneNumber() == null) {
       log.warn("Student {} has null phone number", student.getId());
   }
   ```

3. **Ask Specific Questions:**
   ❌ BAD: "It's not working, what should I do?"
   
   ✅ GOOD: "The mapper fails when phoneNumber is null. Should I:
   1. Add @JsonProperty(defaultValue = "") to DTO?
   2. Add null check in mapper?
   3. Add database constraint to prevent null?
   
   I'm leaning toward option 2 for backward compatibility."

### Code Review Checklist

Before marking a task as complete:

- [ ] Code follows project architecture patterns
- [ ] All tests pass (unit + integration)
- [ ] No console.log or System.out.println remaining
- [ ] Error handling implemented
- [ ] Input validation added
- [ ] Documentation updated (if public API changed)
- [ ] CONTINUITY.md updated
- [ ] No hardcoded secrets or credentials
- [ ] Responsive design verified (Frontend)
- [ ] Database migrations created (if schema changed)
- [ ] Performance impact assessed
- [ ] Security implications considered

---

## 📊 Ledger System

### CONTINUITY.md

**Purpose:** Track project-level progress, decisions, and context across sessions.

**Structure:**
```markdown
# Project Continuity Log

## Current Status
- Sprint: Sprint 5 - Student Management
- Last Updated: 2024-02-13
- Active Branch: feature/student-pagination
- Blockers: None

## Recent Completions
### 2024-02-13: Student Pagination
- ✅ Added pagination to backend API
- ✅ Updated frontend StudentList component
- ✅ Added tests for pagination logic
- Commit: abc123f

### 2024-02-12: JWT Authentication
- ✅ Implemented JWT token generation
- ✅ Added refresh token endpoint
- ✅ Secured all /api routes
- Commit: def456a

## In Progress
### Student Search Feature
- Status: 70% complete
- Remaining:
  - [ ] Implement fuzzy search
  - [ ] Add search debouncing
  - [ ] Write integration tests
- Assignee: AI Agent
- Target: 2024-02-15

## Upcoming
1. Email notifications for student enrollment
2. Export student list to Excel
3. Student import from CSV

## Important Decisions
### 2024-02-10: Disabled OSIV
- Decision: Set spring.jpa.open-in-view=false
- Rationale: Prevent lazy loading issues, improve performance
- Impact: Must use DTOs for all API responses
- Reference: docs/adr/002-disable-osiv.md

### 2024-02-08: Chosen Shadcn/UI
- Decision: Use Shadcn/UI for component library
- Rationale: Customizable, accessible, copy-paste approach
- Alternatives Considered: Material-UI, Chakra UI
- Reference: docs/adr/001-component-library.md

## Environment
- Node: 18.19.0
- Java: 17.0.9
- MySQL: 8.0.35
- NextJS: 14.0.4
- Spring Boot: 3.2.0

## Notes
- Remember to run migrations before deploying to staging
- Student email uniqueness constraint added in migration 003
```

**Update Frequency:**
- ✅ After completing each task
- ✅ When making architectural decisions
- ✅ When encountering blockers
- ✅ At end of each work session

---

### ISSUES.md

**Purpose:** Track technical debt, bugs, and module-level issues.

**Structure:**
```markdown
# Issues & Technical Debt

## Critical Issues (Fix ASAP)
### [CRITICAL] Student search timeout on large datasets
- **Module:** Student Management
- **Impact:** Users experience 30+ second waits
- **Root Cause:** Missing database index on student.full_name
- **Solution:** Add index in next migration
- **Priority:** P0
- **Created:** 2024-02-13
- **Assignee:** AI Agent

## High Priority
### [HIGH] Inconsistent error messages between frontend/backend
- **Module:** Shared/Error Handling
- **Impact:** Poor user experience
- **Solution:** Create shared error code enum
- **Priority:** P1
- **Created:** 2024-02-12

## Technical Debt
### [DEBT] StudentService has 400+ lines
- **Module:** Student Management
- **Impact:** Hard to maintain, violates SRP
- **Solution:** Split into StudentService, StudentValidationService, StudentEnrollmentService
- **Estimated Effort:** 4 hours
- **Priority:** P2
- **Created:** 2024-02-10

### [DEBT] No integration tests for payment flow
- **Module:** Payment
- **Impact:** Risk of production bugs
- **Solution:** Add end-to-end payment tests
- **Estimated Effort:** 6 hours
- **Priority:** P2
- **Created:** 2024-02-09

## Known Limitations
### Limited file upload size (5MB max)
- **Module:** File Upload
- **Reason:** Hosting provider limitation
- **Workaround:** Document in user guide
- **Potential Solution:** Move to cloud storage (future)

## Resolved Issues
### ~~[FIXED] JWT tokens expire too quickly~~
- **Fixed:** 2024-02-11
- **Solution:** Increased expiration from 5 min to 15 min
- **Commit:** xyz789b

### ~~[FIXED] Student list not responsive on mobile~~
- **Fixed:** 2024-02-10
- **Solution:** Added responsive grid layout
- **Commit:** uvw456c
```

**Update Frequency:**
- ✅ When discovering new issues
- ✅ When issue priority changes
- ✅ When issues are resolved
- ✅ During code reviews

---

## 🚫 File Restrictions

### DO NOT MODIFY (Without explicit permission)

**Build Configuration:**
```bash
# Backend
pom.xml                    # Maven configuration
mvnw, mvnw.cmd            # Maven wrapper

# Frontend
package.json              # Dependencies
package-lock.json         # Dependency lock
next.config.ts            # NextJS configuration
tsconfig.json             # TypeScript configuration
tailwind.config.ts        # Tailwind configuration
```

**Why:** These files affect the entire project. Changes can break builds, introduce version conflicts, or cause deployment issues.

**If you must modify:**
1. Document the reason in CONTINUITY.md
2. Test thoroughly on local environment
3. Get approval from team lead
4. Create backup before modifying

### Database Migrations
```bash
# Never modify existing migrations
src/main/resources/db/migration/
  V001__initial_schema.sql     # ❌ DON'T MODIFY
  V002__add_student_table.sql  # ❌ DON'T MODIFY
  V003__current_migration.sql  # ⚠️ ONLY if not in production
```

**Rules:**
- ❌ Never modify migrations that have been applied to production
- ❌ Never change migration version numbers
- ✅ Create new migration for schema changes
- ✅ Test migrations on clean database

### CAREFUL WITH

**Project Documentation:**
```bash
CONTINUITY.md    # Update, don't replace
ISSUES.md        # Append, don't delete resolved issues
README.md        # Update relevant sections only
```

**Environment Files:**
```bash
.env.example     # Template - update when adding new variables
.env.local       # Local only - never commit
.env.production  # Production secrets - never commit
```

---

## ✅ Checklists

### Pre-Development Checklist

- [ ] Read CONTINUITY.md and ISSUES.md
- [ ] Understand the requirement completely
- [ ] Check if similar functionality exists
- [ ] Identify affected modules/features
- [ ] Review relevant documentation
- [ ] Set up local environment
- [ ] Create feature branch from main
- [ ] Pull latest changes

### During Development Checklist

- [ ] Follow existing patterns and conventions
- [ ] Write tests alongside code (TDD)
- [ ] Use meaningful variable/function names
- [ ] Add comments for complex logic
- [ ] Handle errors appropriately
- [ ] Validate all inputs
- [ ] Consider performance implications
- [ ] Check for security vulnerabilities
- [ ] Commit logical units of work
- [ ] Push commits regularly

### Pre-Commit Checklist

- [ ] All tests pass (unit + integration)
- [ ] Linter passes with no errors
- [ ] No console.log or debug code remains
- [ ] No commented-out code
- [ ] No hardcoded secrets or credentials
- [ ] Documentation updated (if needed)
- [ ] CONTINUITY.md updated
- [ ] ISSUES.md updated (if relevant)
- [ ] Responsive design verified (Frontend)
- [ ] Database indexes added (if new queries)
- [ ] Migration tested (if schema changed)
- [ ] Performance tested (if data-intensive)
- [ ] Security reviewed (if auth/sensitive data)
- [ ] Accessibility checked (Frontend)

### Code Review Checklist

**Functionality:**
- [ ] Code does what it's supposed to do
- [ ] Edge cases are handled
- [ ] Error handling is comprehensive
- [ ] Input validation is thorough

**Code Quality:**
- [ ] Follows project architecture patterns
- [ ] Adheres to naming conventions
- [ ] Functions are single-responsibility
- [ ] No code duplication (DRY principle)
- [ ] Complexity is reasonable (no deeply nested logic)

**Testing:**
- [ ] Adequate test coverage
- [ ] Tests are meaningful (not just for coverage)
- [ ] Integration tests for critical flows
- [ ] Edge cases are tested

**Performance:**
- [ ] No N+1 query problems
- [ ] Pagination used for large datasets
- [ ] Database queries are optimized
- [ ] No unnecessary computations in loops
- [ ] Images/assets are optimized (Frontend)

**Security:**
- [ ] No hardcoded secrets
- [ ] Input validation on all user inputs
- [ ] SQL injection prevention (parameterized queries)
- [ ] XSS prevention (React escaping used correctly)
- [ ] Authentication/authorization properly implemented
- [ ] Sensitive data properly encrypted

**Documentation:**
- [ ] Public APIs are documented
- [ ] Complex logic is explained
- [ ] README updated if needed
- [ ] Architecture diagrams updated if needed

### Production Readiness Checklist

**Backend:**
- [ ] All environment variables documented
- [ ] Database migrations tested
- [ ] Error logging configured
- [ ] Monitoring/alerts set up
- [ ] Rate limiting configured
- [ ] CORS configured correctly
- [ ] Security headers configured
- [ ] Connection pooling optimized
- [ ] Caching strategy implemented
- [ ] Health check endpoint exists
- [ ] OSIV is disabled
- [ ] Production profile configured

**Frontend:**
- [ ] Environment variables set
- [ ] Build succeeds without warnings
- [ ] Bundle size is reasonable
- [ ] Images are optimized
- [ ] SEO meta tags added
- [ ] Analytics configured
- [ ] Error tracking configured (Sentry, etc.)
- [ ] Loading states implemented
- [ ] Responsive on all screen sizes
- [ ] Accessibility (WCAG) compliance
- [ ] Browser compatibility tested

**Database:**
- [ ] Indexes on frequently queried columns
- [ ] Backup strategy in place
- [ ] Migration rollback plan exists
- [ ] Connection limits configured
- [ ] Query performance tested

**DevOps:**
- [ ] CI/CD pipeline configured
- [ ] Automated tests in pipeline
- [ ] Deployment process documented
- [ ] Rollback procedure documented
- [ ] Monitoring dashboard created
- [ ] Log aggregation configured
- [ ] Secrets management in place

---

## 🎨 Customization Guide

### Adapting These Rules

**This document is designed to be:**
- ✅ **80% universal** - Core principles apply to most projects
- ✅ **20% customizable** - Adapt to specific project needs

### Adaptation Matrix

| Project Type | Keep As-Is | Customize | Skip |
|-------------|-----------|-----------|------|
| **E-commerce Platform** | Architecture, Security, Testing | Payment patterns, checkout flow | N/A |
| **SaaS Application** | All core sections | Multi-tenancy patterns, billing | N/A |
| **Internal Tool** | Code standards, workflow | Simplified security, basic UI | Production checklist (if not deployed) |
| **API-only Backend** | Backend patterns, security | Remove frontend sections | Frontend sections |
| **Mobile App (React Native)** | Code standards, testing | Mobile-specific patterns | Web-specific sections |

### Project-Specific Rules Template

**Create a PROJECT_RULES.md file:**

```markdown
# Project-Specific Rules
> Extends UNIVERSAL_GLOBAL_RULES.md

## Project Context
- **Name:** Student Management System
- **Type:** SaaS Platform
- **Team Size:** 3 developers
- **Timeline:** 6 months

## Architecture Overrides

### Additional Backend Modules
```
modules/
  notification/    # Email/SMS notifications
  reporting/       # Analytics and reports
  billing/         # Subscription management
```

### Frontend Features
```
features/
  dashboard/       # Admin dashboard
  analytics/       # Reporting interface
  billing/         # Subscription UI
```

## Technology Additions
- **Notifications:** SendGrid for email, Twilio for SMS
- **Storage:** AWS S3 for file uploads
- **Caching:** Redis for session management
- **Queue:** RabbitMQ for background jobs

## Custom Naming Conventions
- **Jobs/Tasks:** `XxxJob.java` (e.g., `SendEmailJob.java`)
- **Events:** `XxxEvent.java` (e.g., `StudentEnrolledEvent.java`)
- **Listeners:** `XxxListener.java` (e.g., `EnrollmentListener.java`)

## Business Rules
- Students must be 18+ to enroll
- Maximum 3 courses per student per semester
- Payment due 7 days before course start
- Refund available within 14 days

## Deployment-Specific
- **Staging:** Auto-deploy on merge to `develop`
- **Production:** Manual approval required
- **Database:** Backup before each migration
- **Rollback:** Keep 3 previous versions

## Custom Checklists

### Before Sending Notification
- [ ] User has opted in
- [ ] Rate limit not exceeded
- [ ] Template variables validated
- [ ] Fallback mechanism exists

### Before Processing Payment
- [ ] Amount validated
- [ ] User authenticated
- [ ] Idempotency key generated
- [ ] Webhook handler configured
```

### When to Create Project Rules

**Create PROJECT_RULES.md when you have:**
- Domain-specific business rules
- Industry-specific compliance requirements
- Custom technology stack additions
- Unique deployment procedures
- Team-specific workflows
- Project-specific naming conventions

### Relationship Between Documents

```
UNIVERSAL_GLOBAL_RULES.md (This document)
    ↓ (Extends)
PROJECT_RULES.md (Project-specific additions)
    ↓ (References)
CONTINUITY.md (Current state)
    ↓ (Tracks)
ISSUES.md (Technical debt)
```

**Reading Order for New Team Members:**
1. UNIVERSAL_GLOBAL_RULES.md (Learn the foundation)
2. PROJECT_RULES.md (Learn project specifics)
3. README.md (Learn setup process)
4. CONTINUITY.md (Understand current state)
5. ISSUES.md (Know what to fix)

---

## 📝 Conclusion

### Key Takeaways

1. **Consistency beats cleverness** - Follow patterns, even if you know a "better" way
2. **Test before commit** - Broken code should never reach the repository
3. **Document decisions** - Future you (or another developer) will thank you
4. **Security first** - Never compromise on security for convenience
5. **Communicate clearly** - Explain your reasoning, show trade-offs

### Living Document

This document should evolve with the project:
- Update when discovering better patterns
- Refine when rules prove ineffective
- Add examples from real situations
- Remove outdated practices

**Version Control:**
- Tag major changes with version numbers
- Document why rules changed
- Maintain backward compatibility where possible

### Getting Help

**When stuck:**
1. Check CONTINUITY.md for context
2. Review relevant sections in this document
3. Search ISSUES.md for similar problems
4. Check project README and docs
5. Ask specific questions with context

**When proposing changes:**
1. State the problem clearly
2. Show why current rule doesn't work
3. Propose alternative with pros/cons
4. Get team consensus
5. Update documentation

---

**Remember:** These rules exist to make development faster, safer, and more predictable. They're guidelines, not dogma. When in doubt, prioritize: **Readability → Maintainability → Performance**

---

*Document Version: 2.0*  
*Last Updated: February 13, 2026*  
*Maintained by: AI Agent Team*