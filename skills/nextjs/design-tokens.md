# Modern Design Tokens

Design tokens are the visual atoms of your design system. Standardizing these ensures professional color harmony, readable typography, and consistent aesthetics across the application.

## 1. Color Harmony (HSL)

Always use HSL (Hue, Saturation, Lightness) for colors. It is intuitive and allows for programmatically generating shades.

### Core Palette
Avoid generic "pure" colors. Use curated HSL values.

```css
:root {
  /* Neutral Palette - Slate/Zinc */
  --background: 240 10% 3.9%;
  --foreground: 0 0% 98%;
  --muted: 240 3.7% 15.9%;
  --muted-foreground: 240 5% 64.9%;

  /* Primary - Deep Indigo/Blue */
  --primary: 263.4 70% 50.4%;
  --primary-foreground: 210 20% 98%;

  /* Accent - Soft Violet */
  --accent: 240 3.7% 15.9%;
  --accent-foreground: 0 0% 98%;
}
```

### Semantic Colors
Define clear colors for status to improve UX.

- **Success**: Emerald (`142.1 70.6% 45.3%`)
- **Warning**: Amber (`37.9 92.1% 50.2%`)
- **Destructive**: Rose (`346.8 77.2% 49.8%`)

## 2. Typography Excellence

Premium UIs rely on high-quality font stacks and proper spacing.

### Font Stacks
Use modern, highly legible fonts.

```css
/* Tailwind Config */
fontFamily: {
  sans: ['var(--font-geist-sans)', 'Inter', 'system-ui', 'sans-serif'],
  display: ['Outfit', 'var(--font-geist-sans)'],
}
```

### Hierarchy & Leading
- **Body Text**: `tracking-tight` and `leading-relaxed` (1.625).
- **Headings**: `tracking-tighter` and `leading-tight` (1.2) for a modern, compact look.
- **Micro-copy**: `uppercase`, `tracking-widest`, `text-xs`, `font-semibold`.

## 3. Dark Mode Strategy

Modern apps must prioritize dark mode. Use "Soft Dark" instead of "Pure Black" (`#000000`) for backgrounds to reduce eye strain.

- **Background**: `hsl(240 10% 3.9%)`
- **Surface/Card**: `hsl(240 10% 6%)`
- **Border**: `hsl(240 5% 15%)`

## Best Practices
1. **Never use Hex/RGB for primary tokens**: Use HSL to allow for opacity modifiers (e.g., `bg-primary/20`).
2. **Limit Colors**: Stick to 1 Primary, 1 Neutral, and the 3 Semantic colors.
3. **Contrast First**: Always verify your foreground/background contrast using APCA or WCAG 2.1 standards.
