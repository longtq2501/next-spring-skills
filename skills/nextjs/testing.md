# Advanced Frontend Testing - Vitest

When building frontend features, **testing logic is as important as building the UI**. This skill ensures that every piece of business logic (hooks, utils, state) is verified automatically.

## 1. Logic-First Testing Strategy

Always separate business logic from UI components. This makes testing easier and more reliable.

### Testing Utility Functions
Test pure functions that transform data or perform calculations.

```typescript
// utils/price.test.ts
import { describe, it, expect } from 'vitest'
import { formatCurrency } from './price'

describe('formatCurrency', () => {
  it('should format numbers to VND correctly', () => {
    expect(formatCurrency(100000)).toBe('100.000 ₫')
  })
})
```

### Testing Custom Hooks
Use `renderHook` to test hooks that contain state or side effects.

```typescript
// hooks/useCounter.test.ts
import { renderHook, act } from '@testing-library/react'
import { useCounter } from './useCounter'

it('should increment correctly', () => {
  const { result } = renderHook(() => useCounter())
  act(() => result.current.increment())
  expect(result.current.count).toBe(1)
})
```

## 2. Integration Testing (Components + Logic)

Test how the UI interacts with the underlying logic and services.

- **Mock Service Worker (MSW)**: Always intercept network calls. Never hit the live API during tests.
- **User Actions**: Use `@testing-library/user-event` for more realistic interaction simulation than `fireEvent`.

```tsx
// components/LoginForm.test.tsx
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LoginForm } from './LoginForm'

it('should show error when login fails', async () => {
  const user = userEvent.setup()
  render(<LoginForm />)
  
  await user.type(screen.getByLabelText(/email/i), 'wrong@example.com')
  await user.click(screen.getByRole('button', { name: /login/i }))
  
  expect(await screen.findByText(/invalid credentials/i)).toBeInTheDocument()
})
```

## 3. Mocking Next.js Patterns

Next.js 15+ patterns require specific mocking strategies. Admin - Menu Management

- **Navigation**: Mock `useRouter` and `usePathname`.
- **Server Actions**: Wrap calls in `vi.fn()` to track submissions.

```typescript
// Setup file or top of test
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: vi.fn() }),
  usePathname: () => '/home',
}))
```

## 4. Automated Testing Protocol for Agents

When requested to build a feature, the agent should:
1.  **Draft the Logic**: Write the function/hook.
2.  **Generate the Test**: Immediately create a `.test.ts(x)` file covering edge cases.
3.  **Run Vitest**: Use `npm test` to verify logic before even building the UI.
4.  **Connect to UI**: Build the component and add integration tests.

## Best Practices
1. **Coverage over UI**: prioritize testing complex logic over simple "it renders" tests.
2. **No implementation details**: Don't test internal state variables; test the output visible to the user.
3. **Environment**: Ensure `jsdom` is configured in `vitest.config.ts`.
4. **Clean up**: Use `afterEach(() => cleanup())` to prevent state leakage between tests.
