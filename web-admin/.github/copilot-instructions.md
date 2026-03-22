# Copilot Instructions - Mosque Admin Dashboard

## Project Overview
This is a **Mosque Administration Dashboard** built with Vite + React + TypeScript, styled with Tailwind CSS and shadcn/ui components. The app enables mosque managers to administer prayer times, slides, and profile information. Architecture uses Supabase for authentication and database, React Router for navigation, TanStack React Query for data fetching, and react-hook-form for form handling.

## Architecture & Key Patterns

### Auth & Route Protection
- **Auth Hook** ([src/hooks/use-auth.tsx](src/hooks/use-auth.tsx)): `AuthContext` + `useAuth()` manages user sessions. Supabase handles auth with auto-refresh.
- **Route Guards** ([src/App.tsx](src/App.tsx#L19-L35)): `ProtectedRoute` checks authentication; `MosqueGuard` ensures mosque data exists before showing dashboard.
- **Guard Order**: Auth → Mosque → DashboardLayout (see routing in [src/App.tsx](src/App.tsx#L39-L47)).

### Data Layer Architecture
- **React Query Integration** ([src/hooks/use-mosque-data.ts](src/hooks/use-mosque-data.ts)): All queries use TanStack React Query with cache keys like `["mosque"]`, `["prayer-times-month", mosqueId, year, month]`.
- **Mutation Pattern**: Mutations auto-invalidate related queries (e.g., `useUpdateMosque()` invalidates `["mosque"]` on success).
- **Query Keys**: Follow pattern `[resourceType, ...filters]` for automatic cache invalidation.

### UI Library & Styling
- **shadcn/ui Components**: Pre-built, unstyled Radix components in [src/components/ui/](src/components/ui/). Import via barrel exports (`import { Button } from "@/components/ui/button"`).
- **Tailwind CSS**: All styling uses Tailwind; custom colors defined in `tailwind.config.ts`. Use `cn()` from [src/lib/utils.ts](src/lib/utils.ts) for conditional classes.
- **Typography**: Font family `font-heading` for titles, default sans for body.

### Component Structure
- **DashboardLayout** ([src/components/DashboardLayout.tsx](src/components/DashboardLayout.tsx)): Responsive sidebar + main content. Mobile drawer handled with `mobileOpen` state. Nav items array at top of file.
- **Page Components**: Located in [src/pages/](src/pages/) — each page imports its own hooks and handles form submission with error toast feedback.

### Form Handling Pattern
- Example: [src/pages/OnboardingPage.tsx](src/pages/OnboardingPage.tsx#L30-L35) shows standard pattern:
  - `useState` for form fields → `handleSubmit` calls mutation → toast on success/error → navigate on completion.

### Database Entities
- **Mosques**: Owner profile (name, city, country, bottom_message, preferences).
- **Prayer Times**: Per-mosque daily prayer times (date, fajr, sunrise, dhuhr, asr, maghrib, isha).
- **Mosque Slides**: Display order carousel images (image_url, display_order, is_active).
- Types auto-generated in [src/integrations/supabase/types.ts](src/integrations/supabase/types.ts) from Supabase schema.

## Developer Workflows

### Running the Project
```bash
npm install           # Install dependencies (uses bun.lockb for lock file)
npm run dev          # Start dev server on port 8080 with HMR
npm run build        # Production build with Vite
npm run test         # Run vitest once
npm run test:watch   # Watch mode for tests
npm lint             # Run ESLint
```

### Adding New Pages
1. Create file in [src/pages/](src/pages/) following pattern: import hooks → component → export default.
2. Add route in [src/App.tsx](src/App.tsx) Routes section (wrap in ProtectedRoute/MosqueGuard if needed).
3. Add nav item to `navItems` array in [src/DashboardLayout.tsx](src/components/DashboardLayout.tsx#L17-L20) if needed.

### Adding Data Hooks
1. Define query/mutation in [src/hooks/use-mosque-data.ts](src/hooks/use-mosque-data.ts).
2. Follow naming: `use[ResourceName]()` for queries, `use[Action][ResourceName]()` for mutations.
3. Always include `queryKey` for caching and `invalidateQueries()` for mutations.
4. Export types: `export type ResourceName = Tables<"table_name">`.

### Supabase Integration
- Client in [src/integrations/supabase/client.ts](src/integrations/supabase/client.ts) — auto-configured from env vars.
- Env vars required: `VITE_SUPABASE_URL`, `VITE_SUPABASE_PUBLISHABLE_KEY`.
- Migrations stored in [supabase/migrations/](supabase/migrations/) — apply via Supabase CLI.

## Code Conventions

### Imports
- **Absolute paths**: Use `@/` alias (configured in `vite.config.ts`) instead of relative paths.
- **UI Components**: `import { Button } from "@/components/ui/button"` (lowercase, hyphens).
- **Hooks**: `import { useAuth } from "@/hooks/use-auth"` (lowercase, hyphens).

### Naming
- **Files**: kebab-case for components/hooks (`use-auth.tsx`, `dashboard-layout.tsx`), PascalCase for page components ([src/pages/](src/pages/) folder).
- **React Components**: PascalCase, export default.
- **Functions**: camelCase.
- **Constants**: UPPER_SNAKE_CASE (e.g., `prayerFields` for const arrays/objects in component scope).

### Error Handling
- All mutations catch errors and display via `toast()` hook: `toast({ title: "Error", description: err.message, variant: "destructive" })`.
- Never silent-fail; always show user feedback for failed operations.

### Type Safety
- Use TypeScript strict mode; no `any` except for error objects (`catch (err: any)`).
- Export types from data hooks (e.g., `export type Mosque = Tables<"mosques">`).
- Use auto-generated Supabase types from [src/integrations/supabase/types.ts](src/integrations/supabase/types.ts#L1).

## Common Patterns

### Query + Mutation Pattern (Prayer Times Example)
See [src/pages/PrayerTimesPage.tsx](src/pages/PrayerTimesPage.tsx#L10-L27):
```tsx
const { data: mosque } = useMosque();
const { data: monthTimes, isLoading } = usePrayerTimesForMonth(mosque?.id, year, month);
const upsertPT = useUpsertPrayerTime();

const handleSave = async (date: string, times: Record<string, string>) => {
  try {
    await upsertPT.mutateAsync({ date, mosque_id: mosque.id, ...times });
    toast({ title: "Saved" });
  } catch (err: any) {
    toast({ title: "Error", description: err.message, variant: "destructive" });
  }
};
```

### Conditional Rendering with Loading States
```tsx
if (isLoading) return <Skeleton />;
if (!data) return <Navigate to="/fallback" replace />;
return <div>{/* render data */}</div>;
```

### Responsive Sidebar Toggle
[src/components/DashboardLayout.tsx](src/components/DashboardLayout.tsx#L29) uses `mobileOpen` state + `cn()` for conditional classes:
```tsx
const [mobileOpen, setMobileOpen] = useState(false);
<aside className={cn("fixed lg:translate-x-0", mobileOpen ? "translate-x-0" : "-translate-x-full")}>
```

## Testing
- Tests in [src/test/](src/test/) using Vitest.
- Example setup in [src/test/setup.ts](src/test/setup.ts).
- Run `npm run test:watch` during development; commit test fixes alongside feature changes.
