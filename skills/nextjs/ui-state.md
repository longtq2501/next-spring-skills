# UI State Management

Manage complex UI logic (modals, global themes, sidebars) without prop-drilling or messy context.

## Recommended: Zustand

Zustand is lightweight, fast, and does not require a Provider.

Install: `npm install zustand`

### Creating a Store

```ts
// store/ui-store.ts
import { create } from 'zustand'

interface UIState {
  isSidebarOpen: boolean
  toggleSidebar: () => void
}

export const useUIStore = create<UIState>((set) => ({
  isSidebarOpen: false,
  toggleSidebar: () => set((state) => ({ isSidebarOpen: !state.isSidebarOpen })),
}))
```

### Usage in Components

```tsx
'use client'
import { useUIStore } from '@/store/ui-store'

export function SidebarTrigger() {
  const toggle = useUIStore((state) => state.toggleSidebar)
  return <button onClick={toggle}>Toggle</button>
}
```

## When to use what?

| Type of State | Recommended Tool |
| :--- | :--- |
| **Server Data** | React Query / Server Components |
| **Form State** | React Hook Form |
| **Simple UI (Local)** | `useState` |
| **Complex UI (Global)** | **Zustand** |
| **URL Parameters** | `useSearchParams` / `nuqs` |

## Best Practices

1. **Selectors**: Always use selectors to prevent unnecessary re-renders.
   - `const isOpen = useUIStore(s => s.isOpen)` vs `const { isOpen } = useUIStore()`
2. **Atomic Stores**: Split stores by feature (e.g., `useAuthStore`, `useCartStore`) rather than one giant store.
3. **Immutability**: Zustand handles it, but be careful with nested objects (use Immer if needed).
4. **Hydration**: If you need to persist state to `localStorage`, use the `persist` middleware.

```ts
import { persist } from 'zustand/middleware'

export const useAppStore = create(
  persist(
    (set) => ({ ... }),
    { name: 'app-storage' }
  )
)
```

## Interaction Patterns

- **URL as Truth**: For filters, pagination, and tabs, use **URL Search Params** instead of global state so the state is shareable and survives page refresh.
- **Feedback**: Combine Zustand with a Toast library (like `sonner`) for global notifications.
