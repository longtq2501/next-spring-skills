## TL;DR - Quick Reference

### Standard JWT Service Setup
```java
@Service
public class JwtService {
    public String generateToken(UserDetails user) { ... }
    public boolean isTokenValid(String token, UserDetails user) { ... }
}
```

### Critical Rules
1. **Secret Key**: Store in environment variables, never hardcode.
2. **Access Token TTL**: Short-lived (15m - 24h).
3. **Refresh Token**: Use UPSERT (one per user) to avoid race conditions.
4. **Claims**: Embed roles/IDs to avoid DB lookups, but keep it small.
5. **Security**: Cast `Principal` directly after login to save a DB hit.

### Templates
- [JWT Service Template](./templates/JwtServiceTemplate.java)

**Dependencies:**
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

**Configuration (`application.yml`):**
```yaml
jwt:
  secret: your-256-bit-base64-encoded-secret-key-here
  expiration: 86400000        # 24 hours in ms
  refresh-expiration: 604800000  # 7 days in ms
```

---

## Architecture Overview

```
Client → [POST /api/auth/login]
            → AuthenticationService.login()
                → authenticationManager.authenticate()     # validates credentials
                → jwtService.generateToken(user)           # creates access token
                → refreshTokenService.createRefreshToken() # creates refresh token
            ← AuthResponse { accessToken, refreshToken }

Client → [Any protected endpoint] with "Authorization: Bearer <accessToken>"
            → JwtAuthenticationFilter
                → jwtService.extractUsername(token)
                → userDetailsService.loadUserByUsername()
                → jwtService.isTokenValid(token, userDetails)
                → SecurityContextHolder.setAuthentication()
            → Controller proceeds normally

Client → [POST /api/auth/refresh] with refreshToken
            → AuthenticationService.refreshToken()
                → refreshTokenService.verifyExpiration()
                → jwtService.generateToken(user)           # new access token
            ← AuthResponse { new accessToken, same refreshToken }
```

---

## Core Components

### 1. JwtService

**Responsibilities:** Generate, parse, and validate JWT access tokens.

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // ─── Extract ───────────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ─── Generate ──────────────────────────────────────────────────────────

    // Overload 1: UserDetails only (no extra claims)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Overload 2: Custom entity with domain-specific claims
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("fullName", user.getFullName());

        // Only include optional fields if present
        if (user.getStudentId() != null) {
            extraClaims.put("studentId", user.getStudentId());
        }

        return generateToken(extraClaims, user);
    }

    // Overload 3: Base method used by all overloads above
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    // ─── Validate ──────────────────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ─── Internal ──────────────────────────────────────────────────────────

    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

**Rationale for method overloading:**

| Overload | Input | Use Case |
|---|---|---|
| `generateToken(UserDetails)` | Generic Spring type | Simple projects, no extra claims |
| `generateToken(User)` | Domain entity | Embed role, name, custom fields in token |
| `generateToken(Map, UserDetails)` | Manual claims | Full control, advanced use |

---

### 2. Custom Claims Pattern

**Embed domain data into the token to avoid extra DB lookups:**
```java
// ✅ Put role in token → no DB lookup needed to check permissions
extraClaims.put("role", user.getRole().name());

// ✅ Extract role from token in filter/controller
public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
}

// ✅ Extract any nullable field safely
public Long extractStudentId(String token) {
    Object studentId = extractAllClaims(token).get("studentId");
    return studentId != null ? ((Number) studentId).longValue() : null;
}
```

**What to put in claims:**

| ✅ Good candidates | ❌ Never include |
|---|---|
| Role / permissions | Password (even hashed) |
| Display name | Credit card / sensitive PII |
| Non-sensitive user ID | Large data (bloats token size) |
| Feature flags | Frequently-changing data |

---

### 3. CustomUserDetailsService

**Loads user from DB for Spring Security's authentication pipeline:**
```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }
}
```

**Optional: cache to reduce DB hits on every request:**
```java
@Override
@Cacheable(value = "users", key = "#email")
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with email: " + email));
}
```

**⚠️ Cache considerations:**

| Scenario | Action needed |
|---|---|
| User password changes | Evict cache: `@CacheEvict(value = "users", key = "#email")` |
| User is disabled/locked | Evict cache immediately |
| No caching infra available | Skip `@Cacheable` — direct DB is fine |

**The `User` entity must implement `UserDetails`:**
```java
@Entity
public class User implements UserDetails {

    // Spring Security fields
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return accountNonLocked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
```

---

### 4. RefreshTokenService

**Manages refresh token lifecycle with UPSERT pattern:**
```java
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // ✅ UPSERT: one token per user, atomically create or replace
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(refreshTokenDurationMs);

        refreshTokenRepository.upsert(userId, token, expiryDate);

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Failed to create refresh token"));
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Validate and return token, delete if expired
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token expired. Please sign in again.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }
}
```

**Refresh token storage — `RefreshToken` entity:**
```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
}
```

**UPSERT query (one refresh token per user):**
```java
// JPA Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);

    // Native UPSERT — prevents duplicate tokens per user
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
}
```

**⚠️ Why UPSERT instead of delete + insert?**
```java
// BAD - race condition: two requests can create two refresh tokens
refreshTokenRepository.deleteByUser(user);           // delete old
refreshTokenRepository.save(new RefreshToken(...));  // insert new ← race condition here

// GOOD - atomic, one operation
refreshTokenRepository.upsert(userId, token, expiryDate);
```

---

### 5. AuthenticationService

**Ties everything together — register, login, refresh, logout:**
```java
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(user);
        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        return buildAuthResponse(savedUser, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // authenticate() calls loadUserByUsername() internally
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // ✅ Cast principal directly — avoids second DB lookup
        User user = (User) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtService.generateToken(user);
                    return buildAuthResponse(user, newAccessToken, refreshTokenStr);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
}
```

**Key patterns in `AuthenticationService`:**

| Pattern | Why |
|---|---|
| Cast `authentication.getPrincipal()` directly after login | Avoids a second DB lookup — user was already loaded by `authenticate()` |
| `AlreadyExistsException` on duplicate email | Maps to 409 CONFLICT (see error_handling skill) |
| `@Transactional` on write methods | Rolls back if token creation fails after user save |
| Separate `buildAuthResponse()` helper | Keeps register/login/refresh DRY |

---

### 6. AuthResponse DTO

**Standard response structure for all auth endpoints:**
```java
@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;    // always "Bearer"
    private Long expiresIn;      // ms, from jwt.expiration config
    private UserInfo user;       // optional: include user info on login/register

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String fullName;
        private String role;
    }
}
```

---

## Token Lifecycle

```
Register/Login
    │
    ├─► accessToken  (short-lived: 15min – 24h)   → sent with every API request
    └─► refreshToken (long-lived: 7 – 30 days)     → stored by client, used to renew access token
            │
            ├─► accessToken expires
            │       └─► POST /api/auth/refresh → new accessToken (same refreshToken)
            │
            └─► refreshToken expires
                    └─► Force re-login
```

**Token expiration strategy:**

| Token | Recommended TTL | Rationale |
|---|---|---|
| Access token | 15min – 24h | Short = less damage if stolen |
| Refresh token | 7 – 30 days | Long = better UX, fewer re-logins |
| Remember me | 90 days | Explicit user opt-in only |

---

## Security Considerations

### Secret Key Generation

**⚠️ Never use a weak or hardcoded secret:**
```bash
# Generate a strong 256-bit Base64 key (run once, store in env variable)
openssl rand -base64 32
```

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET}   # ✅ From environment variable, never hardcoded

# application-local.yml (gitignored)
jwt:
  secret: your-local-dev-secret-here
```

**❌ Never do this:**
```yaml
jwt:
  secret: mySecretKey     # ← too short, predictable, committed to git
```

### Token Invalidation

JWT tokens are **stateless** — you cannot invalidate them server-side after issuing.
Mitigations:

| Strategy | How |
|---|---|
| Short access token TTL | Damage window is limited even if stolen |
| Refresh token rotation | Issue new refresh token on each use |
| Token blacklist (Redis) | Store revoked JTIs — adds statefulness |
| Logout deletes refresh token | Client must re-login; old access token still valid until expiry |

---

## Common Patterns Summary

### ✅ DO's:

1. **Use method overloading** for `generateToken()` — generic + domain-specific versions
2. **Embed role in claims** — avoids DB lookup on every request
3. **UPSERT refresh tokens** — one per user, atomic, no race condition
4. **Cast `authentication.getPrincipal()`** after `authenticate()` — avoids second DB lookup
5. **Store JWT secret in environment variables**, never hardcode in code or `application.yml`
6. **Use `@Transactional`** on methods that write both user + token
7. **Delete refresh token on logout** — invalidates the session
8. **Validate both username match AND expiry** in `isTokenValid()`
9. **Use `UUID.randomUUID()`** for refresh tokens — unpredictable, no pattern to guess
10. **Use `Instant` (not `Date`) for refresh token expiry** — timezone-safe

### ❌ DON'Ts:

1. **Don't put sensitive data in JWT claims** — token is base64, not encrypted
2. **Don't use a weak secret** — minimum 256-bit, generated randomly
3. **Don't do delete + insert for refresh token** — race condition, use UPSERT
4. **Don't do a second DB lookup after `authenticate()`** — cast the principal directly
5. **Don't use long-lived access tokens** — 24h max, prefer 15–60min
6. **Don't skip `@Transactional`** on register/login — partial failure leaves dirty state
7. **Don't return stack traces in auth errors** — generic messages only
8. **Don't log tokens** — even partially (first N chars is still a security risk)
9. **Don't cache user indefinitely** — evict cache on password change or account lock
10. **Don't hardcode `jwt.expiration`** — keep in `application.yml`, configurable per environment

---

## Quick Reference Template

See [JwtServiceTemplate.java](./templates/JwtServiceTemplate.java) for the minimal service to handle JWT logic.

---

## Related Skills

- **Security Configuration**: `skills/spring/security_config.md`
- **Error Handling**: `skills/spring/error_handling.md`
- **Validation Patterns**: `skills/spring/validation.md`
- **Logging Best Practices**: `skills/spring/logging.md`

---

## Examples

See `examples/spring/jwt_auth/` for complete working example with all 4 services, entities, repositories, and controller.

---

**Last Updated:** 2025-02-13
**Status:** ✅ Production-ready (generic, reusable across projects)