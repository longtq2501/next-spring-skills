# Skill: Security Configuration - Spring Boot Best Practices

## Context
This skill defines the standard security configuration pattern for Spring Boot REST APIs using **Spring Security + JWT**.
Set up once per project — provides authentication, authorization, CORS, and security headers out of the box.

**When to use:** New Spring Boot project requiring JWT-based stateless authentication, role-based access control, or CORS configuration for frontend integration.

**Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

---

## Core Principles

### 1. Class-Level Annotations

**Always use these three annotations together:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // ← enables @PreAuthorize, @Secured on methods
@RequiredArgsConstructor
public class SecurityConfiguration {
    // ...
}
```

**Rationale:**
- `@EnableWebSecurity` — activates Spring Security's web support
- `@EnableMethodSecurity` — enables method-level security (`@PreAuthorize("hasRole('ADMIN')")`)
- `@RequiredArgsConstructor` — Lombok injects `final` dependencies via constructor (no `@Autowired`)

---

### 2. SecurityFilterChain — The Core Bean

**Full production-ready configuration:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            // Security headers
            .headers(headers -> headers
                    .httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000))
                    .xssProtection(HeadersConfigurer.XXssConfig::disable) // handled by frontend frameworks
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            )
            // Disable CSRF (stateless JWT doesn't need it)
            .csrf(AbstractHttpConfigurer::disable)
            // CORS config (defined separately)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .anyRequest().authenticated()
            )
            // Stateless session (JWT)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

**Key decisions explained:**

| Configuration | Reason |
|---|---|
| `csrf.disable()` | JWT is stateless — CSRF tokens are only needed for session-based auth |
| `SessionCreationPolicy.STATELESS` | No server-side sessions; each request carries JWT |
| `addFilterBefore(jwtFilter, ...)` | JWT filter runs before Spring's default auth filter |
| `OPTIONS` always permitted | Browsers send preflight OPTIONS before CORS requests |
| `/error` permitted | Spring's error redirect endpoint must be accessible |

---

### 3. Route Authorization Rules

**Order matters — most specific first:**
```java
.authorizeHttpRequests(auth -> auth
        // ✅ Public endpoints first (most specific)
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/public/**").permitAll()

        // ✅ Role-based access
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/tutor/**").hasAnyRole("ADMIN", "TUTOR")

        // ✅ HTTP method-level control
        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/courses/**").hasRole("ADMIN")

        // ✅ Infrastructure / tooling
        .requestMatchers("/actuator/**").permitAll()
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        .requestMatchers("/error").permitAll()

        // ✅ Catch-all: require auth for everything else
        .anyRequest().authenticated()
)
```

**❌ DON'T put `anyRequest().authenticated()` in the middle — it blocks everything below it:**
```java
// BAD
.requestMatchers("/api/auth/**").permitAll()
.anyRequest().authenticated()          // ← This blocks everything below
.requestMatchers("/api/public/**").permitAll()  // ← Never reached
```

---

### 4. CORS Configuration

**Explicit origins only — never use wildcard with credentials:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // ✅ List specific origins (wildcard * breaks with allowCredentials=true)
    configuration.setAllowedOrigins(Arrays.asList(
            "https://your-app.vercel.app",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
    ));

    configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
    ));

    configuration.setAllowedHeaders(List.of("*"));

    // Expose headers the frontend needs to read
    configuration.setExposedHeaders(Arrays.asList(
            "Content-Disposition",
            "Content-Type",
            "Authorization"
    ));

    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L); // Cache preflight for 1 hour

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**⚠️ Critical CORS rules:**

| Scenario | Correct Config |
|---|---|
| Frontend sends cookies/JWT | `setAllowCredentials(true)` + explicit origins |
| `allowCredentials=true` | **CANNOT** use `setAllowedOrigins(List.of("*"))` |
| File downloads | Must expose `Content-Disposition` header |
| Preflight caching | Set `setMaxAge(3600L)` to reduce OPTIONS requests |

**❌ BAD — wildcard + credentials will fail:**
```java
configuration.setAllowedOrigins(List.of("*"));   // ← Breaks CORS with credentials
configuration.setAllowCredentials(true);          // ← These two conflict
```

---

### 5. Password Encoding

**Always use BCrypt — tune the strength for your environment:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10); // strength 10 = production default
}
```

**Strength guide:**

| Strength | Hashing time | Use case |
|---|---|---|
| 4 | ~1ms | Tests only |
| 8 | ~40ms | Development / lower-spec servers |
| 10 | ~100ms | **Production default** |
| 12 | ~400ms | High-security (banking, healthcare) |

**⚠️ Don't use low strength in production — it makes brute-force attacks cheap.**

---

### 6. AuthenticationProvider & AuthenticationManager

**Standard DaoAuthenticationProvider setup:**
```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);  // load user from DB
    authProvider.setPasswordEncoder(passwordEncoder());      // verify BCrypt hash
    return authProvider;
}

@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
    return config.getAuthenticationManager();
}
```

**Why expose `AuthenticationManager` as a bean?**
The login endpoint (typically in `AuthService`) needs to call `authenticationManager.authenticate(...)` manually.
Without this bean, you can't inject `AuthenticationManager` in services.

---

### 7. Security Headers

**Configure HTTP security headers to harden the API:**
```java
.headers(headers -> headers
        // Force HTTPS for 1 year, including subdomains
        .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000))    // 1 year

        // Disable Spring's XSS header — modern browsers & React/Vue handle this
        .xssProtection(HeadersConfigurer.XXssConfig::disable)

        // Prevent clickjacking (no iframes allowed)
        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
)
```

**Header reference:**

| Header | Value | Purpose |
|---|---|---|
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | Enforce HTTPS |
| `X-Frame-Options` | `DENY` | Block clickjacking |
| `X-XSS-Protection` | disabled | Let browser/framework handle |

**⚠️ Note on XSS header:** `X-XSS-Protection` is deprecated in modern browsers. React/Vue/Angular already escape output. Disable it to avoid false positives.

---

### 8. JWT Filter Registration

**Register JWT filter BEFORE Spring's default auth filter:**
```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```

**Typical `JwtAuthenticationFilter` structure:**
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip if no Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);

        // Only authenticate if not already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

### 9. Method-Level Security (Optional but Recommended)

**After enabling `@EnableMethodSecurity`, use on controllers or services:**
```java
// Controller
@GetMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<UserResponse>> getAllUsers() { ... }

// Service
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
public UserResponse getUserById(Long userId) { ... }

// Common expressions
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
@PreAuthorize("isAuthenticated()")
@PreAuthorize("#email == authentication.principal.username")  // owns the resource
```

**Rationale:** Method security complements URL-level rules — use URL rules for coarse-grained access, method-level for fine-grained ownership checks.

---

## Common Patterns Summary

### ✅ DO's:

1. **Use `@EnableMethodSecurity`** for fine-grained `@PreAuthorize` support
2. **Disable CSRF** for stateless JWT APIs
3. **Use `SessionCreationPolicy.STATELESS`** — no sessions with JWT
4. **List specific CORS origins** — never wildcard with `allowCredentials=true`
5. **Set BCrypt strength 10** for production
6. **Expose `AuthenticationManager` as a bean** (needed in auth service)
7. **Always permit `OPTIONS /**`** to support CORS preflight
8. **Always permit `/error`** to avoid auth loops on Spring error redirects
9. **Register JWT filter before** `UsernamePasswordAuthenticationFilter`
10. **Order URL rules** from most specific to most generic

### ❌ DON'Ts:

1. **Don't use `setAllowedOrigins("*")` with `setAllowCredentials(true)`** — CORS will break
2. **Don't store session** — JWT is stateless, sessions waste memory
3. **Don't use BCrypt strength < 10 in production** — too easy to brute-force
4. **Don't put `anyRequest().authenticated()` in the middle** of rules
5. **Don't skip `OPTIONS` permit** — preflight requests will fail
6. **Don't forget `@EnableMethodSecurity`** if using `@PreAuthorize`
7. **Don't hardcode CORS origins** — move to `application.yml`
8. **Don't expose Swagger in production** without IP restriction or auth
9. **Don't catch `Throwable`** in security filters — catch `Exception`
10. **Don't log JWT tokens or passwords** in any log statement

---

## Quick Reference Template

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .xssProtection(HeadersConfigurer.XXssConfig::disable)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "https://your-frontend.vercel.app",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList(
                "Content-Disposition", "Content-Type", "Authorization"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## Configuration via `application.yml` (Recommended)

**Move environment-specific values out of code:**
```yaml
# application.yml
app:
  cors:
    allowed-origins:
      - https://your-frontend.vercel.app
      - http://localhost:3000
  jwt:
    secret-key: your-256-bit-secret-key
    expiration: 86400000       # 24 hours in ms
    refresh-expiration: 604800000  # 7 days in ms
  security:
    public-paths:
      - /api/auth/**
      - /api/public/**
      - /actuator/**
```

---

## Related Skills

- **Error Handling**: `skills/spring/error_handling.md`
- **JWT Service**: `skills/spring/jwt_service.md`
- **Validation Patterns**: `skills/spring/validation.md`
- **Logging Best Practices**: `skills/spring/logging.md`

---

## Examples

See `examples/spring/security_config/` for complete working example with JWT filter, UserDetailsService, and role-based access.

---

**Last Updated:** 2025-02-13
**Status:** ✅ Production-ready (generic, reusable across projects)