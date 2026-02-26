# Skill: Performance Optimization - Frontend

Guidelines for creating snappy, high-performance Next.js applications.

## TL;DR - Quick Reference

### Critical Rules
1. **Avoid Unnecessary Re-renders**: Use `useMemo` and `useCallback` for expensive calculations or callback props.
2. **Standardize Data Fetching**: Use React Query for caching, automatic revalidation, and SWR.
3. **URL as State**: Prefer search params over global state for filters to keep them shareable.
4. **Asset Optimization**: Use `next/image` and `next/font` for zero layout shift and optimized delivery.

---

## 1. React Render Optimization

### useMemo & useCallback
// Bad: Creating new objects/functions on every render
const values = items.filter(i => i.active);
const onClick = () => console.log(id);

// Good: Memoize to prevent child re-renders
const values = useMemo(() => items.filter(i => i.active), [items]);
const onClick = useCallback(() => console.log(id), [id]);

---

## 2. Data Fetching & Caching

### React Query / SWR
// Bad: Fetching inside useEffect without caching
useEffect(() => {
  fetch('/api/data').then(res => setData(res.json()));
}, []);

// Good: Automatic caching, background cleanup, and loading states
const { data, isLoading } = useQuery({ 
  queryKey: ['users'], 
  queryFn: getUsers 
});

---

## 3. Network & Payload

### Payload Reduction
- **Pagination**: Never fetch more than 50-100 items at a time.
- **Selective Fields**: Request only the DTO fields you need (see backend Query Optimization).

### Response Compression
Ensure the server or CDN uses GZIP/Brotli to compress JSON responses.

---

## 4. Visual Performance (Design Intelligence)

Design choices impact technical performance. Balance aesthetics with the 60FPS target:

1.  **Glassmorphism**: Limit `backdrop-blur` usage. Multiple layers can cause heavy CPU/GPU load on lower-end devices.
2.  **Animations**: Use `will-change: transform` for hardware acceleration. Avoid animating `width`, `height`, or `margin`.
3.  **SVGs vs Emojis**: Never use emojis for UI icons. SVGs offer superior performance (vector), accessibility (aria-label), and brand consistency.

---

## Related Skills
- **Design Intelligence**: `skills/nextjs/design-intelligence.md`
- **Interactivity & Animation**: `skills/nextjs/interactivity.md`
- **UI State Management**: `skills/nextjs/ui-state.md`
