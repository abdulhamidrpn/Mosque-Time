# Authentication & Owner ID Integration

## Overview
The system has been updated to seamlessly integrate user authentication with mosque data ownership. Users now automatically own the mosques they create, and mosque data is automatically fetched based on the logged-in user's ID.

## Changes Made

### 1. **[src/hooks/use-auth.tsx](src/hooks/use-auth.tsx)** - Auth State Management
**What Changed:**
- Added `useQueryClient` from React Query to AuthProvider
- When auth state changes (sign in/sign out), the mosque cache is automatically invalidated
- This ensures that when a user logs in or out, their mosque data is refetched based on the new user ID

**Key Improvement:**
```tsx
queryClient.invalidateQueries({ queryKey: ["mosque"] });
```
This line triggers a cache refresh whenever authentication state changes, ensuring each user sees their own mosque data.

---

### 2. **[src/hooks/use-mosque-data.ts](src/hooks/use-mosque-data.ts)** - Data Layer Updates

#### `useMosque()` Query
**What Changed:**
- Now filters mosques by `owner_uid` matching the current user's ID
- Includes `user?.id` in the query key for proper cache separation
- Query only runs when user is authenticated (`enabled: !!user?.id`)

**Before:**
```tsx
.select("*")
.limit(1)
.maybeSingle();
```

**After:**
```tsx
.eq("owner_uid", user!.id)
.limit(1)
.maybeSingle();
```

**Benefit:** Each user only sees their own mosque data, automatically enforced at the query level.

---

#### `useCreateMosque()` Mutation
**What Changed:**
- Automatically assigns the logged-in user's ID as the `owner_uid`
- No longer requires `owner_uid` to be passed from the caller
- Validates that user is authenticated before creating

**Before:**
```tsx
mutationFn: async (mosque: TablesInsert<"mosques">) => {
  // Required owner_uid in input
}
```

**After:**
```tsx
mutationFn: async (mosque: Omit<TablesInsert<"mosques">, "owner_uid">) => {
  if (!user?.id) throw new Error("User not authenticated");
  const { data, error } = await supabase
    .from("mosques")
    .insert({
      ...mosque,
      owner_uid: user.id,  // Automatic
    })
    // ...
}
```

**Benefit:** Impossible to accidentally create a mosque with the wrong owner ID.

---

### 3. **[src/pages/OnboardingPage.tsx](src/pages/OnboardingPage.tsx)** - Simplified Creation
**What Changed:**
- Removed manual `owner_uid` parameter from mosque creation call
- Now only passes: `name`, `city`, `country`, `bottom_message`

**Before:**
```tsx
await createMosque.mutateAsync({
  owner_uid: user.id,  // Manual
  name,
  city: city || null,
  country: country || null,
  bottom_message: bottomMessage,
});
```

**After:**
```tsx
await createMosque.mutateAsync({
  name,
  city: city || null,
  country: country || null,
  bottom_message: bottomMessage,
});
```

**Benefit:** Cleaner code, no risk of passing wrong owner ID.

---

## How It Works

### User Sign In Flow
1. User enters credentials on AuthPage
2. `useAuth().signIn()` authenticates with Supabase
3. AuthProvider detects auth state change
4. **Mosque cache is invalidated** (triggers refetch)
5. `useMosque()` query runs with new user ID
6. User's mosque data loads automatically
7. User is routed to dashboard or onboarding

### Mosque Creation Flow
1. User fills onboarding form
2. Form submits to `useCreateMosque().mutateAsync()`
3. Hook automatically adds `owner_uid: user.id`
4. Record created in database with correct owner
5. Cache invalidated, mosque query refetches
6. User is navigated to dashboard

### User Sign Out Flow
1. User clicks sign out
2. `useAuth().signOut()` clears session
3. AuthProvider detects auth state change
4. **Mosque cache is invalidated**
5. User is redirected to auth page
6. Next login will fetch their mosque data

---

## Database-Level Security

The Supabase RLS (Row Level Security) should enforce:
- Users can only read their own mosques (`owner_uid = auth.uid()`)
- Users can only update their own mosques
- Users can only delete their own mosques

Example RLS policy:
```sql
CREATE POLICY "Users can see their own mosques"
ON mosques
FOR SELECT
USING (owner_uid = auth.uid());
```

---

## Benefits

✅ **Data Isolation** - Each user automatically sees only their mosque  
✅ **Automatic Ownership** - No manual owner_uid assignment needed  
✅ **Sign-In/Out Sync** - Cache refresh ensures correct data on auth changes  
✅ **Type Safety** - TypeScript prevents passing owner_uid unnecessarily  
✅ **Security** - Impossible to create mosque with wrong owner  
✅ **User Experience** - Seamless multi-account support  

---

## Testing Checklist

- [ ] Sign up a new user → Create mosque → Verify owner_uid matches user ID
- [ ] Sign in user A → See mosque A
- [ ] Sign out → Sign in as user B → See mosque B (not mosque A)
- [ ] Sign in user A again → Mosque A data loads automatically
- [ ] Create mosque → Verify cache invalidation triggers refetch
- [ ] Update mosque → Verify changes appear immediately

---

## Notes

- The system now requires a user to be authenticated before fetching mosque data
- Empty state (no mosque) properly routes to onboarding page
- Query cache keys include user ID for proper multi-user support
