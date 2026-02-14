# Skill: Frontend Testing - Vitest & RTL

Guidelines for testing React components and logic using Vitest and React Testing Library (RTL).

## TL;DR - Quick Reference

### Critical Rules
1. **User-Centric Testing**: Test how the user interacts with the app (e.g., clicking buttons), not implementation details (e.g., state).
2. **Vitest**: Use Vitest for speed and compatibility with Vite/Next.js.
3. **Screen Queries**: Prefer `getByRole`, `getByLabelText`, and `getByText` over `test-id`.
4. **Mocking**: Use `vi.mock()` for modules and **MSW (Mock Service Worker)** for API calls.
5. **Async Handling**: Use `findBy...` or `waitFor` to handle elements that appear after an async operation.

---

## 1. Utility & Logic Testing (Vitest)
Test pure JavaScript/TypeScript functions and logic.

// Good: Testing a utility function
import { describe, it, expect } from 'vitest';
import { calculateTotal } from './math';

describe('calculateTotal', () => {
  it('should sum items correctly with tax', () => {
    const items = [{ price: 100 }, { price: 200 }];
    const result = calculateTotal(items, 0.1);
    expect(result).toBe(330);
  });
});

---

## 2. Component Testing (React Testing Library)
Test UI behavior and interaction.

// Good: Testing a Button component
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from './Button';

it('should call onClick when clicked', () => {
  const handleClick = vi.fn();
  render(<Button onClick={handleClick}>Click Me</Button>);
  
  fireEvent.click(screen.getByText(/click me/i));
  
  expect(handleClick).toHaveBeenCalledTimes(1);
});

---

## 3. Async & API Testing (MSW)
Using Mock Service Worker to intercept network requests.

// Good: Testing a component that fetches data
it('should display products from API', async () => {
  render(<ProductList />);
  
  // findBy queries are async and will wait
  const item = await screen.findByText('Laptop');
  expect(item).toBeInTheDocument();
});

---

## 4. Hooks Testing
Use `@testing-library/react-hooks` or the built-in `renderHook` from RTL.

// Good: Testing a custom hook
import { renderHook, act } from '@testing-library/react';
import { useCounter } from './useCounter';

it('should increment counter', () => {
  const { result } = renderHook(() => useCounter());
  
  act(() => {
    result.current.increment();
  });

  expect(result.current.count).toBe(1);
});

---

## Related Skills
- **Interactivity & Animation**: `skills/nextjs/interactivity.md`
- **Performance**: `skills/nextjs/performance.md`
