# Accessibility (a11y)

Crafting a premium UI means ensuring it works for everyone, including those using screen readers or keyboard navigation.

## Core Principles

1. **Semantic HTML**: Use `<button>` for actions, `<a>` for navigation, `<nav>`, `<main>`, `<header>`, `<footer>`.
2. **Focus Management**: Ensure the focus indicator is always visible (`outline-none` is a sin unless you provide a custom style).
3. **Contrast**: Text should have at least 4.5:1 contrast ratio against the background.

## ARIA Attributes

Use ARIA only when semantic HTML isn't enough.

```tsx
<button
  aria-expanded={isOpen}
  aria-controls="dropdown-menu"
  aria-label="Toggle menu"
>
  <HamburgerIcon />
</button>

<div id="dropdown-menu" hidden={!isOpen} role="menu">
  {/* items */}
</div>
```

## Shadcn/UI & Radix UI (Recommended)

Radix UI components (used in Shadcn) come with high-quality a11y out of the box:
- Keyboard navigation (Arrow keys, Enter, Esc).
- Focus trapping in modals.
- Proper ARIA roles.

## Image Accessibility

Always provide `alt` text for `next/image`.

- **Informative images**: `alt="Mô tả nội dung hình ảnh"`
- **Decorative images**: `alt=""` (Empty string tells screen readers to skip it).

## Skip Links

Useful for keyboard users to skip navigation and jump to content.

```tsx
// components/SkipLink.tsx
export const SkipLink = () => (
  <a href="#main-content" className="sr-only focus:not-sr-only">
    Skip to content
  </a>
)
```

## Testing a11y

1. **Keyboard Only**: Try navigating your site using only `Tab` and `Shift + Tab`.
2. **Screen Readers**: Use VoiceOver (macOS) or NVDA (Windows).
3. **Automated Tools**:
   - `axe-core`
   - Lighthouse (Accessibility score)
   - `eslint-plugin-jsx-a11y` (Already in most Next.js setups)

## Best Practices

- **Button Labels**: Buttons with only icons MUST have an `aria-label`.
- **Forms**: Labels MUST be associated with inputs using `htmlFor`.
- **Modals**: Always use a "Dialog" pattern that prevents background scrolling and traps focus.
