# Interactivity & Animation

Use Framer Motion for smooth, high-performance UI interactions in Next.js.

## Framer Motion Basics

Install: `npm install motion` (previously `framer-motion`)

```tsx
'use client'
import { motion } from 'framer-motion'

export function FadeIn() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      Content fades in smoothly
    </motion.div>
  )
}
```

## Layout Animations

Framer Motion automatically handles layout changes with the `layout` prop.

```tsx
<motion.div layout className="card">
  {/* Position or size changes inside will animate automatically */}
</motion.div>
```

## AnimatePresence (For Modals/Tabs)

Essential for animating components that are removed from the DOM.

```tsx
import { AnimatePresence, motion } from 'framer-motion'

export function Modal({ isOpen }) {
  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          Modal content
        </motion.div>
      )}
    </AnimatePresence>
  )
}
```

## Viewport Animations (Scroll)

Trigger animations when an element enters the screen.

```tsx
<motion.div
  initial={{ opacity: 0 }}
  whileInView={{ opacity: 1 }}
  viewport={{ once: true, margin: "-100px" }}
>
  Appears when scrolled into view
</motion.div>
```

## Best Practices

- **'use client'**: Always required for `motion` components.
- **Micro-interactions**: Use `whileHover={{ scale: 1.05 }}` and `whileTap={{ scale: 0.95 }}` for buttons.
- **Shared Layouts**: Use `layoutId` to animate elements moving between different components (e.g., active tab indicator).
- **Reduced Motion**: Respect user OS settings for reduced motion.

```tsx
import { useReducedMotion } from 'framer-motion'

const shouldReduceMotion = useReducedMotion()
// Use to disable animations if necessary
```

## Performance Tips

- Prefer `transform` and `opacity` animations over `width`, `height`, or `top/left`.
- Use `motion.div` instead of `motion(div)`.
- For simple hover/focus effects, use **Vanilla CSS** or **Tailwind** transitions instead of JS-based animations to keep the bundle size small.
