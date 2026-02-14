# UI Libraries (shadcn/ui & Radix UI)

Modern Next.js development favors "headless" components that provide logic and accessibility while leaving the styling to you.

## shadcn/ui (The Gold Standard)

shadcn/ui is not a dependency but a collection of reusable components that you copy and paste into your project.

### Why use it?
- **Full Control**: Since the code is in your project, you can change anything.
- **Accessibility**: Built on top of Radix UI.
- **Tailwind-first**: Easy to theme with CSS variables.

### Installation & Usage

```bash
npx shadcn-ui@latest init
npx shadcn-ui@latest add button
```

```tsx
import { Button } from "@/components/ui/button"

export default function Home() {
  return <Button variant="outline">Click me</Button>
}
```

## Radix UI (The Engine)

Many high-quality libraries (including shadcn) use Radix UI primitives. Use Radix directly when you need to build custom complex components (like multi-selects or specialized modals).

```tsx
import * as Dialog from '@radix-ui/react-dialog';

export const MyDialog = () => (
  <Dialog.Root>
    <Dialog.Trigger>Open</Dialog.Trigger>
    <Dialog.Portal>
      <Dialog.Overlay className="bg-black/50 fixed inset-0" />
      <Dialog.Content className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white p-4">
        {/* Content */}
      </Dialog.Content>
    </Dialog.Portal>
  </Dialog.Root>
);
```

## Best Practices

### 1. Don't Over-customize Early
Use the default shadcn styles first. Only modify the component code once you are sure about the design system change.

### 2. Composition Pattern
Always support `className` and `asChild` (via `@radix-ui/react-slot`) for maximum flexibility.

```tsx
// Example supporting asChild
import { Slot } from "@radix-ui/react-slot"

const MyComponent = ({ asChild, ...props }) => {
  const Comp = asChild ? Slot : "div"
  return <Comp {...props} />
}
```

### 3. Theming
Use CSS variables in `globals.css` for theming instead of hardcoding colors in Tailwind.

```css
:root {
  --primary: 222.2 47.4% 11.2%;
  /* ... */
}
```

## Common Libraries in Next.js

- **shadcn/ui**: All-purpose, clean, professional.
- **Radix UI**: Foundational primitives.
- **Headless UI**: Good for Tailwind + simple components.
- **Magic UI / Aceternity UI**: For high-end marketing and background effects (often uses Framer Motion).
