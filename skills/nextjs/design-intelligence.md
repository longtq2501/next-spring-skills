# Skill: Design Intelligence (Next.js 15 + UI/UX Pro Max)

Bridge the gap between pure design logic and high-performance Next.js implementation.

## 1. The Design Workflow

When building a new feature or page, follow this hierarchical retrieval pattern:

1.  **Analyze**: Extract product type and style keywords from the request.
2.  **Generate**: Run the design system generator (use `--design-system`).
3.  **Integrate**: Map the generated variables to shadcn/ui or Tailwind tokens.

### Generator Command
```bash
python3 skills/ui-ux-pro-max/scripts/search.py "<product_type> <keywords>" --design-system
```

---

## 2. Integrated Implementation Patterns

### Mapping to shadcn/ui
Generated design system variables should be mapped to `globals.css` to respect the high-performance theming engine of shadcn.

// Bad: Hardcoding values in components
<div style={{ backgroundColor: '#0080FF' }}>...</div>

// Good: Map to CSS variables
// globals.css
:root {
  --primary: 221.2 83.2% 53.3%; /* From generated palette */
  --radius: 0.75rem; /* From generated style */
}

### Performance-Aware Effects
High-end effects must use Next.js optimized patterns to maintain 60FPS.

| Style | Next.js Implementation Detail |
|-------|-------------------------------|
| **Glassmorphism** | Use `backdrop-blur` sparingly. Prefer `bg-white/80` in light mode for contrast. |
| **Parallax** | Use `will-change: transform` and Framer Motion's `useScroll`. |
| **Dark Mode** | Use `next-themes` to prevent hydration mismatch. |

---

## 3. Tooling & Automation

### Automated Design System (Hierarchical)
Always use the `--persist` flag when starting a project to create a `MASTER.md` file.

```bash
python3 skills/ui-ux-pro-max/scripts/search.py "fintech dashboard glassmorphism" --design-system --persist -p "BankPro"
```

1.  Read `design-system/MASTER.md` for global rules.
2.  Check for page-specific overrides in `design-system/pages/<page_name>.md`.

---

## 4. Quick Trigger Prompt (Universal)

To trigger the full power of this integration for ANY task or project, use this concise prompt:

> **"Triển khai [Task] theo chuẩn Design Intelligence + Next.js Performance."**

**What this triggers in me:**
1.  **Search**: I will automatically query `skills/ui-ux-pro-max/` for the best design system.
2.  **Plan**: I will create a `task.md` with integrated design & performance phases.
3.  **Execute**: I will use CSS variables for theming and Next.js Best Practices for code.

---

## Related Skills
- **UI Libraries**: `skills/nextjs/ui-libraries.md` (Integration with shadcn/ui)
- **Performance**: `skills/nextjs/performance.md` (Visual Performance rules)
- **Design Database**: `skills/ui-ux-pro-max/SKILL.md` (Original CSV data)
