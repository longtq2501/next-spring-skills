# Skill: Agent Workflow & Interaction Protocol

## TL;DR - Quick Reference

### Session Protocol
1. **Context Check**: Read `CONTINUITY.md` and `ISSUES.md` at start.
2. **Task Alignment**: Update `CONTINUITY.md` for context and `task.md` for high-level tracking.
3. **Plan First**: For complex work, create an `implementation_plan.md` and get approval.

### ⚡ Tactical Execution (The ISSUES Cycle)
- List issues in `ISSUES.md` (root cause, target, metrics).
- Propose solutions in `ISSUES.md`.
- Implement and archive to "Completed Work" with performance/impact markers.

---

## 1. The Ledger System

This project uses a coordinated ledger system to maintain state and high performance.

| Artifact | Level | Purpose |
|----------|-------|---------|
| `CONTINUITY.md`| Contextual | The primary bridge between sessions. Explains **why** things are happening and where we are in the big picture. |
| `ISSUES.md` | Tactical | The active workplace. Lists bugs, performance optimizations, and technical debt with specific metrics. |
| `task.md` | Systems | High-level checklist for initial system setup and task breakdown. |
| `walkthrough.md` | Verification| Final proof of work and verification results after a task is finished. |

---

## 2. ISSUES.md Template

Every feature/module should have an `ISSUES.md` following this structure:

```markdown
# [Module Name] - Issues & Optimization

## Performance Issues
- [ ] [P0-Critical] Issue description
    - Root cause: ...
    - Target: ...
    - Metrics: ...
- [ ] [P1-High] Issue description

## UX Issues
- [ ] [P1-High] Issue description
- [ ] [P2-Medium] Issue description

## UI Issues
- [ ] [P2-Medium] Issue description
- [ ] [P3-Low] Issue description

## Technical Debt (Optional)
- [ ] Code smell 1
- [ ] Refactor needed 2

---

## Completed Work (Archive)
- [x] [P0-Critical] Fixed issue
    - Solution: ...
    - Performance impact: X → Y
    - Tested: ✅
```

---

## 3. Session Protocols

### Session Start
**MANDATORY FIRST STEPS:**
1. **Read CONTINUITY.md**: Gain contextual situational awareness.
2. **Analyze ISSUES.md**: Identify the highest priority tactical items (P0/P1).
3. **Sync task.md**: Ensure the high-level roadmap matches current tactical needs.

### Session End
1. **Archive Completed Issues**: Move resolved items in `ISSUES.md` to the archive with impact notes.
2. **Update CONTINUITY.md**: Summarize the current state and next steps for the next agent.
3. **Generate Walkthrough**: Create the `walkthrough.md` as final proof of work.
