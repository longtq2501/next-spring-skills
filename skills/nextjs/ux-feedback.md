# UX Feedback Patterns

Ensuring the application feels responsive and communicates clearly with the user. A premium app never leaves the user wondering "Is it working?".

## 1. Skeleton Screens

Always prefer skeletons over generic spinners for page transitions. Skeletons should precisely match the layout of the final content.

- **Pulse Animation**: Use a subtle pulse (`animate-pulse`) instead of a fast blink.
- **Shape Matching**: If it's a circle avatar, the skeleton must be a circle. If it's a 16px title, the skeleton must be 16x100px.

```tsx
/* Simple Stat Card Skeleton */
<div className="p-4 rounded-xl border border-border animate-pulse">
  <div className="h-4 w-24 bg-muted rounded mb-2" />
  <div className="h-8 w-16 bg-muted-foreground/20 rounded" />
</div>
```

## 2. Optimistic UI

For actions that are likely to succeed (like toggling a like button or marking a task as done), update the UI *before* the server responds.

- **Immediate Feedback**: The user sees the change instantly.
- **Rollback Logic**: If the request fails, revert the UI and show a toast.

> [!NOTE]
> React's `useOptimistic` hook is the standard for implementing this in Next.js.

## 3. Non-Intrusive Toasts 

Use toasts for feedback on background actions (Save, Delete, Update).

- **Position**: Usually `bottom-right` or `top-center`.
- **Duration**: `3-5 seconds` is enough for most messages.
- **Interaction**: Allow users to swipe or click to dismiss early.
- **Icons**: Always include a small icon (Checkmark for success, Alert for error).

## 4. Empty & Error States

Never show a blank white screen when there's an error or no data.

- **Empty State**: Use an illustration or a clear message + a "Call to Action" (e.g., "No items found. Create your first one?").
- **Error State**: Provide a clear explanation and a "Retry" button.

## 5. Layout Transitions (Exit/Enter)

Use `<AnimatePresence>` to prevent elements from simply "disappearing" or "popping in."

- **Exit**: Fade out + slide down.
- **Enter**: Fade in + slide up.

## Best Practices
1. **Consistency**: Use the same animation durations (e.g., 200ms or 300ms) for all similar transitions.
2. **Avoid Flash of Unstyled Content (FOUC)**: Use server-side state or Suspense to ensure some content is ready before rendering.
3. **Progressive Disclosure**: Only show the most important information first; reveal details on interaction to keep the UI clean.
