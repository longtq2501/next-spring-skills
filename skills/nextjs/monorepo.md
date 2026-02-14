# Skill: Monorepo Management - Turborepo (Production Blueprint)

Guidelines for building a scalable, high-performance monorepo for enterprise-level multi-platform projects.

## TL;DR - Quick Reference

### Critical Rules
1. **Orchestration**: Use Turborepo for build pipeline and remote caching.
2. **Modular Packages**: Extract logic to `packages/` to ensure 95%+ code reusability.
3. **Internal Linking**: Use `workspace:*` for local package dependencies.
4. **Consistency**: Share ESLint, Prettier, Tailwind, and TS configs across all apps.
5. **Types First**: Shared DTOs in `packages/types` are the single source of truth for FE/BE sync.

---

## 1. Enterprise Directory Structure

```text
/my-monorepo/
├── apps/                           # Deployable Applications
│   ├── web/                        # Next.js 14 App Router
│   ├── admin/                      # Admin Dashboard
│   ├── mobile/                     # React Native (Expo)
│   ├── desktop/                    # Electron
│   └── api/                        # Spring Boot (Backend)
├── packages/                       # Shared Domain Logic
│   ├── ui/                         # Core UI (Shadcn/React)
│   ├── types/                      # Shared Interfaces & DTOs
│   ├── validators/                 # Zod/Validation Logic
│   ├── utils/                      # Shared Helpers (Date, String)
│   ├── api-client/                 # Axios/Fetch Wrapper
│   └── database/                   # Prisma/DB Client
├── infra/                          # Shared Configuration
│   ├── tailwind-config/
│   ├── eslint-config/
│   └── typescript-config/
├── turbo.json                      # Build Pipeline Config
└── package.json                    # Workspace Definition
```

---

## 2. Core Configurations

### turbo.json (The Brain)
Defines task dependencies and caching outputs.

```json
{
  "$schema": "https://turbo.build/schema.json",
  "pipeline": {
    "build": {
      "dependsOn": ["^build"],
      "outputs": [".next/**", "dist/**", "out/**"]
    },
    "lint": {},
    "test": {
      "cache": true
    },
    "dev": {
      "cache": false,
      "persistent": true
    }
  }
}
```

### package.json (The Workspace)
Must include `workspaces` (npm/yarn) or `pnpm-workspace.yaml`.

```json
{
  "private": true,
  "workspaces": ["apps/*", "packages/*"],
  "scripts": {
    "dev": "turbo dev",
    "build": "turbo build",
    "test": "turbo test"
  }
}
```

---

## 3. Shared Packages Pattern

### How to share a package (ex: Types)
1. Create `packages/types/package.json` with a scoped name like `@repo/types`.
2. Export your types/interfaces from `index.ts`.
3. In `apps/web/package.json`, add `"@repo/types": "workspace:*"`.

// Good: Shared DTO Example
export interface OrderDTO {
  id: string;
  status: 'PENDING' | 'DONE';
  items: string[];
}

---

## 4. Scaling & Efficiency

### Remote Caching
Enable `turbo login` and `turbo link` in CI/CD to share build artifacts across the team, reducing build times from minutes to seconds.

### Monolith to Monorepo Migration
- **Step 1**: Move your monolithic `src/` to `apps/main-app/`.
- **Step 2**: Identify shared logic (utils, constants) and move them to `packages/`.
- **Step 3**: Introduce internal packages and replace local imports with `@repo/...`.

---

## Related Skills
- **Frontend Testing**: `skills/nextjs/testing.md`
- **Performance**: `skills/nextjs/performance.md`
- **Agent Workflow**: `skills/agent-workflow.md`
