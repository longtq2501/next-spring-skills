# Skill: Debug & Fix (Pro Max Standards)

Systematic workflow for identifying root causes and applying high-performance fixes across Next.js and Spring Boot.

## 1. The Pro Max Debugging Workflow

Follow these 5 steps for ANY issue (Bug, Performance, or UX):

### Step 1: Observation & Data Collection
- **Frontend**: Check Browser Console, Network Tab, and `next-development.log`.
- **Backend**: Check Spring Boot logs (`log.info/error`) and Hibernate SQL output.
- **Tools**: Use `curl -X POST /_next/mcp` to get real-time dev server errors.

### Step 2: Hypothesis Generation
Based on the data, identify the most likely layer of failure:
- **UI/UX**: Component state, Tailwind classes, or React lifecycle.
- **Logic**: Frontend hooks or Backend Service implementation.
- **Performance**: DB Query (N+1), heavy JS bundle, or inefficient re-renders.

### Step 3: Isolation & Reproduction
- **Bisect**: Comment out components/logic to find the exact line causing the crash.
- **Repro**: Define the exact sequence of user actions that triggers the issue.
- **Mock**: Use `MockMVC` (Backend) or `Vitest` (Frontend) to isolate the unit.

### Step 4: Resolution (Clean Fix)
Apply the fix according to project standards:
- **Frontend**: Use `useMemo`, `useCallback`, or `next/image`.
- **Backend**: Use proper Enum comparison (`==`), custom exceptions, and DB-level filtering.
- **Refactor**: If the fix introduces complexity, refactor surrounding code to maintain readability.

### Step 5: Verification & Documentation
- **Verify**: Test the fix across different viewports and states.
- **Update Ledger**: Update `ISSUES.md` (Archive) with root cause and impact.
- **Regression**: Ensure no other features were broken.

---

## 2. Standard Issue Categories

| Category | Priority | Lead Indicator | Standard Action |
|----------|----------|----------------|-----------------|
| **Performance** | P0/P1 | LCP > 2.5s / API > 2s | `query_optimization.md` / `performance.md` |
| **Logic/Bug** | P0/P1 | 500 Error / Crash | `error_handling_design.md` / `debug-tricks.md` |
| **UX/UI** | P2/P3 | Visual glitch / Delay | `ux-feedback.md` / `design-intelligence.md` |

---

## 3. Universal Trigger Prompt

To trigger this workflow for a specific issue, use:

> **"Sửa lỗi [Mô tả lỗi] theo chuẩn Debug & Fix Pro Max."**

**What this triggers in me:**
1. **Log Analysis**: I will proactively look for logs/errors in the workspace.
2. **Systematic Plan**: I will update `ISSUES.md` and propose a scoped fix.
3. **Verified Execution**: I will implement the fix and verify it before delivery.

---

## Related Skills
- **Agent Workflow**: `skills/agent-workflow.md` (Ledger management)
- **Debug Tricks**: `skills/nextjs/debug-tricks.md` (Next.js specific tools)
- **Performance**: `skills/nextjs/performance.md` & `skills/spring/performance_optimization.md`
