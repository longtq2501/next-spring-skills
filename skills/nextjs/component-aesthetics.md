# Component Aesthetics

Standardizing the "feel" of components like cards, buttons, and sections ensures a premium, cohesive user experience.

## 1. The 8px Spacing System

A consistent spacing system is the foundation of professional-looking UI. Always use multiples of 8px (or 4px for fine-tuning).

- **Nano**: `4px` (`p-1`, `m-1`) - Micro-spacing for icons/text.
- **Small**: `8px` (`p-2`, `m-2`) - Default gap for small items.
- **Base**: `16px` (`p-4`, `m-4`) - Standard padding for cards/modals.
- **Large**: `24px` (`p-6`, `m-6`) - Generous padding for landing sections.
- **Huge**: `32px` (`p-8`, `m-8`) - Container margins.

> [!TIP]
> Use `gap-{n}` instead of margins whenever possible to keep layout logic centralized in the container.

## 2. Border Radius Scale

Consistent rounded corners make the UI feel modern and approachable. Avoid "sharp" 0px corners or "clipping" 2px corners.

- **Soft (md)**: `6px` / `0.375rem` - Checkboxes, small inputs.
- **Standard (lg)**: `8px` / `0.5rem` - Default button radius.
- **Premium (xl)**: `12px` / `0.75rem` - Cards, dropdown menus.
- **Super (2xl)**: `16px` / `1rem` - Large modals, image containers.
- **Full**: `9999px` - Pills, circular avatars.

## 3. Shadow & Depth (Elevation)

Shadows should be subtle and multi-layered. Avoid single, dark, blurry shadows.

```css
/* Custom utility for Premium Shadow */
.shadow-premium {
  box-shadow: 
    0 1px 2px rgba(0, 0, 0, 0.05),
    0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

/* Glassmorphism Border (Adds depth without heavy shadows) */
.border-premium {
  border: 1px solid hsl(var(--border) / 0.5);
  background-clip: padding-box;
}
```

## 4. Visual Hierarchy Techniques

- **Surface Contrast**: In dark mode, nested components should be *lighter* than their parents to simulate light reflection.
  - Body Background: `hsl(240 10% 3.9%)`
  - Card/Overlay: `hsl(240 10% 7.5%)`
- **Ghost Dividers**: Instead of solid lines, use `border-muted/30` or just whitespace (`gap`) to separate content.

## Best Practices
1. **Consistency is King**: If one card has `rounded-xl`, all equivalent cards must have `rounded-xl`.
2. **Inner Radius Logic**: If a parent has `rounded-xl` (12px), the child should have `rounded-lg` (8px) to maintain a concentric look.
3. **Subtle Borders**: Never use `#eee` or `black` for borders. Use `hsl(var(--border))` with varying opacities.
