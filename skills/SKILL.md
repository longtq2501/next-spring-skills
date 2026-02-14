# Development Skills

This directory contains standardized best practices and code templates for the project.

## Next.js (Frontend)

Apply these rules when writing or reviewing Next.js code.

### File Conventions
See [file-conventions.md](./nextjs/file-conventions.md) for project structure, special files, and route segments.

### RSC Boundaries
See [rsc-boundaries.md](./nextjs/rsc-boundaries.md) for valid server/client component patterns.

### Async Patterns
See [async-patterns.md](./nextjs/async-patterns.md) for Next.js 15+ async API changes.

### Runtime Selection
See [runtime-selection.md](./nextjs/runtime-selection.md) for Node.js vs Edge runtime usage.

### Directives
See [directives.md](./nextjs/directives.md) for `use client`, `use server`, and `use cache`.

### Functions
See [functions.md](./nextjs/functions.md) for navigation hooks and server functions.

### Error Handling
See [error-handling.md](./nextjs/error-handling.md) for error boundaries and redirects.

### Data Patterns
See [data-patterns.md](./nextjs/data-patterns.md) for fetching patterns and avoiding waterfalls.

### Route Handlers
See [route-handlers.md](./nextjs/route-handlers.md) for API routes basics and environment behavior.

### Metadata & OG Images
See [metadata.md](./nextjs/metadata.md) for static/dynamic metadata and OG images.

### Image Optimization
See [image.md](./nextjs/image.md) for `next/image` best practices.

### Font Optimization
See [font.md](./nextjs/font.md) for `next/font` setup and Tailwind integration.

### Bundling
See [bundling.md](./nextjs/bundling.md) for server-incompatible packages and bundle analysis.

### Scripts
See [scripts.md](./nextjs/scripts.md) for `next/script` strategies.

### Hydration Errors
See [hydration-error.md](./nextjs/hydration-error.md) for causes and fixes.

### Suspense Boundaries
See [suspense-boundaries.md](./nextjs/suspense-boundaries.md) for CSR bailout and hooks requiring Suspense.

### Interactivity & Animation
See [interactivity.md](./nextjs/interactivity.md) for Framer Motion basics and AnimatePresence.

### UI State Management
See [ui-state.md](./nextjs/ui-state.md) for Zustand store patterns and URL parameters.

### Three.js & 3D
See [threejs.md](./nextjs/threejs.md) for high-performance 3D development and R3F.

### WebRTC & P2P
See [webrtc.md](./nextjs/webrtc.md) for real-time media and signaling.

### Quality Assurance
See [testing.md](./nextjs/testing.md) for Vitest and React Testing Library patterns.

### Architecture & Monorepo
See [monorepo.md](./nextjs/monorepo.md) for Turborepo and code-sharing strategies.

### Accessibility (a11y)
See [accessibility.md](./nextjs/accessibility.md) for semantic HTML and ARIA.

### UI Libraries
See [ui-libraries.md](./nextjs/ui-libraries.md) for shadcn/ui and Radix UI patterns.

### Parallel & Intercepting Routes
See [parallel-routes.md](./nextjs/parallel-routes.md) for modal patterns and interceptors.

### Self-Hosting
See [self-hosting.md](./nextjs/self-hosting.md) for Docker and ISR cache handlers.

### Debug Tricks
See [debug-tricks.md](./nextjs/debug-tricks.md) for AI-assisted debugging and build paths.

### Performance
See [performance.md](./nextjs/performance.md) for React optimization and data fetching strategies.

---

## AI Agent Workflow

### Interaction Protocol
See [agent-workflow.md](./agent-workflow.md) for session start protocols, stuck protocols, and automated ledger systems.

---

## Spring Boot (Backend)

High-performance, stateless REST API patterns using Spring Boot, JPA, and JWT.

### Core Architecture
- [Entity Design](./spring/entity_design.md)
- [DTO Design](./spring/dto_design.md)
- [Repository Design](./spring/repository_design.md)
- [Service Design](./spring/service_design.md)
- [REST API Design](./spring/rest_api_design.md)
- [Error Handling](./spring/error_handling_design.md)

### Security & Identity
- [JWT Service](./spring/jwt_service_design.md)
- [Security Config](./spring/security_config.md)
- [Enum Design](./spring/enum_design.md)

### Performance
- [Query Optimization](./spring/query_optimization.md)
- [Performance Optimization](./spring/performance_optimization.md)

### Real-time
- [WebSocket & STOMP](./spring/websocket.md)
- [Server-Sent Events (SSE)](./spring/sse.md)

### Quality Assurance
- [Backend Testing](./spring/testing.md)

---

**Templates:** See the [spring/templates/](./spring/templates/) directory for production-ready boilerplates.
