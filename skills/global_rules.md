# Universal Global Rules (Lean Manifest)

**Version:** 2.1 (Optimized)
**Applies To:** Full-stack projects with NextJS Frontend + Spring Boot Backend

---

## Architecture Foundation

### Frontend: Feature-based Architecture
Organizes code by business features, not technical layers.
- **Rules**: Keep features independent. Shared code in `shared/`.
- **See Implementation**: [file-conventions.md](./nextjs/file-conventions.md)

### Backend: Modular Monolith
Combines monolith simplicity with microservices modularity.
- **Rules**: Zero cross-module entity references (use DTOs/IDs). Domain layer has zero external dependencies.
- **See Implementation**: [service_design.md](./spring/service_design.md)

### Database & Performance
- **Critical**: Always disable OSIV (`spring.jpa.open-in-view=false`).
- **Standard**: Always paginate large datasets. Use `JOIN FETCH` for N+1 issues.
- **See Details**: [query_optimization.md](./spring/query_optimization.md) & [performance_optimization.md](./spring/performance_optimization.md)

---

## Technical Standards

| Layer | Standard | Implementation Details |
|------|----------|------------------------|
| **Frontend** | Next.js 15, TS, Tailwind, shadcn/ui | [Next.js Skills Directory](./nextjs/) |
| **Backend** | Spring Boot 3, Java 17, JPA, JWT | [Spring Skills Directory](./spring/) |
| **Naming** | Language-standard (camelCase, PascalCase) | Follow standard IDE/Language defaults |

---

## Workflow & AI Protocol

### Agent Protocol
All AI agents must follow the **Interaction Protocol** focusing on `CONTINUITY.md` and `ISSUES.md`.
- **Manifest**: [agent-workflow.md](./agent-workflow.md)
- **Tracking**: `ISSUES.md` (Tactical) + `CONTINUITY.md` (Contextual) + `task.md` (Systems).

### Documentation
- Document "Why" not "What".
- Public APIs must have JSDoc/Javadoc.
- Keep `SKILL.md` as the source of truth for all patterns.

---

## Security Rules
- **Never Commit Secrets**: Check list in [Security Config](./spring/security_config.md).
- **Validation**: Strict Zod (Frontend) and @Valid (Backend) validation on all inputs.
- **Authorization**: Stateless JWT with role-based access control.

---

## Production Readiness Checklist
See comprehensive checklist in: [agent-workflow.md](./agent-workflow.md#code-review-checklist)

---

**Note**: This is a lean manifest. For any technical implementation detail, refer to the [Development Skills Index](./SKILL.md).