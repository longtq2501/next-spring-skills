# Premium Inputs & Buttons

The most interactive elements of your application deserve the most attention. Standardizing these prevents "clunky" or "ugly" user interfaces.

## 1. Button Archetypes

Every button should have a clear purpose and a distinct visual style.

| Variant | Usage | Style |
|:---|:---|:---|
| **Primary** | Main Action (Save, Submit) | Solid brand color, high contrast. |
| **Secondary** | Secondary Action (Cancel, Edit) | Outline or subtle background. |
| **Ghost** | Tertiary Action (Navigation) | No background/border until hover. |
| **Subtle** | Low-priority utility | Very low contrast background. |

### Interactive States (The "Vibe")
Always add micro-interactions to buttons to make them feel responsive.

```tsx
<button className="
  px-4 py-2 rounded-lg font-medium transition-all
  hover:scale-[1.02] active:scale-[0.98]
  hover:brightness-110 active:brightness-90
  focus-visible:ring-2 focus-visible:ring-primary/50
">
  Click Me
</button>
```

## 2. Input Polish

Modern inputs should feel deep and clean.

- **Height**: Minimum `40px` (h-10) for readability and touch targets.
- **Padding**: Horizontal `12px` (px-3), Vertical `8px` (py-2).
- **Focus State**: Use a subtle inner shadow or a distinct (but not jarring) border glow.

```tsx
<input className="
  w-full bg-background border border-input rounded-md px-3 py-2
  placeholder:text-muted-foreground
  focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary
  transition-shadow
" />
```

## 3. Shared Field Patterns

- **Label Placement**: Place labels 4px-8px above the input. Use `text-sm font-medium`.
- **Help/Error Text**: Use `text-[12px]` (text-xs) and ensure `Destructive` color for errors.
- **Required Indicator**: Use a subtle dot or `*` in the brand color, not just plain red.

## 4. Loading States

Never leave a user guessing. If a button triggers an async action, it *must* show a loading state.

- **Spinner**: Small, centered, matching the text color.
- **Text change**: Optionally change "Submit" to "Submitting...".
- **Disabling**: Disable the button to prevent double-submissions.

## Best Practices
1. **Touch Targets**: Ensure all clickable elements are at least `44x44px` on mobile.
2. **Clear Affordance**: Buttons should look like buttons (radius, slight shadow, or distinct color).
3. **Contrast**: Ensure text inside buttons is highly readable (Primary foreground vs Primary background).
4. **No Placeholders as Labels**: Never use `placeholder` as a substitute for a real `<label>`.
