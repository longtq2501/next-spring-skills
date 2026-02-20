# Responsive Design

Ensuring the application is usable and beautiful on every screen size, from mobile phones to ultra-wide monitors. We follow a **Mobile-First** approach.

## 1. Mobile-First Workflow

Always start with mobile styles (default) and use Tailwind breakpoints to "add" complexity for larger screens.

- **Mobile (Default)**: Single column, full-width buttons, condensed headers.
- **Tablet (`md:`)**: Two columns, sidebar reveals, increased padding.
- **Desktop (`lg:`)**: Multi-column layouts, full navigation menus, expanded content.

```tsx
/* Responsive Container Example */
<div className="
  grid grid-cols-1 gap-4 
  md:grid-cols-2 lg:grid-cols-3
  p-4 md:p-8
">
  {/* Content adapts to screen size */}
</div>
```

## 2. Standard Breakpoints

Stick to Tailwind's default breakpoints for consistency:

| Breakpoint | Prefix | Width | Usage |
|:---|:---|:---|:---|
| **Small** | `sm:` | 640px | Small tablets / Large phones |
| **Medium** | `md:` | 768px | Tablets (Portrait) |
| **Large** | `lg:` | 1024px | Laptops / Tablets (Landscape) |
| **Extra Large**| `xl:` | 1280px | Desktops |

## 3. Fluid Typography & Spacing

Instead of hardcoding sizes, use relative units or clamp functions for elements that need to scale smoothly.

- **Prefer**: `text-base md:text-lg` over a static size.
- **Stacking**: Use `flex-col md:flex-row` for components that should stack on mobile but be side-by-side on desktop.

## 4. Touch-Friendly UI (Mobile)

- **Hit Targets**: Buttons and links must be at least `44x44px`.
- **No Hover for Logic**: Never hide critical functionality behind a hover state on mobile (mobile has no hover).
- **Navigation**: Use "Hamburger" menus or bottom bars for mobile navigation.

## 5. Responsive Media

- **Images**: Use `next/image` with `sizes` attribute to prevent loading massive images on small screens.
- **Aspect Ratio**: Use `aspect-video` or `aspect-square` to maintain proportions during resizing.

## Best Practices
1. **Test in Browser**: Always verify by resizing the browser or using Device Mode in DevTools.
2. **Avoid "Hiding" Content**: Try to reorganize content rather than using `hidden` to remove logic for mobile users.
3. **Overflow Handling**: Always use `overflow-x-hidden` on the main container to prevent accidental horizontal scrolling.
