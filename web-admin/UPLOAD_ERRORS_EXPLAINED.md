# Upload Errors: Root Causes & Fixes

## Error 1: "new row violates row-level security policy"

### Where It Happens
- When uploading slide images in the **Digital Signage** page
- Database insert is blocked by RLS policy

### Root Cause
The RLS policy on `mosque_slides` table uses the wrong clause for INSERT:
- ❌ Uses `USING` clause (which only works for SELECT/UPDATE/DELETE)
- ✅ Should use `WITH CHECK` clause (which works for INSERT/UPDATE)

### Technical Detail
```sql
-- WRONG (blocks INSERT)
CREATE POLICY "insert slides" ON mosque_slides 
FOR INSERT TO authenticated 
USING (auth.uid() IN (SELECT owner_uid FROM mosques WHERE id = mosque_id))
-- ^ USING doesn't apply to INSERT, so all INSERTs are blocked

-- CORRECT (allows INSERT)
CREATE POLICY "insert slides" ON mosque_slides 
FOR INSERT TO authenticated 
WITH CHECK (auth.uid() IN (SELECT owner_uid FROM mosques WHERE id = mosque_id))
-- ^ WITH CHECK applies to INSERT, so INSERTs are allowed if condition is true
```

### How to Fix
1. Open Supabase SQL Editor
2. Copy all SQL from: [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)
3. Paste into SQL Editor and click Run
4. This will:
   - Drop the old incorrect policies
   - Create 4 new policies using WITH CHECK for INSERT/UPDATE

### Verification
After fixing, run this:
```sql
SELECT policyname, pol_as FROM pg_policies 
WHERE tablename = 'mosque_slides' ORDER BY policyname;
```
You should see 4 policies with `as` columns containing your expressions.

---

## Error 2: "bucket not found"

### Where It Happens
- When uploading slide images (from SlidesPage.tsx)
- When uploading profile image (from ProfilePage.tsx)
- Storage upload tries to access bucket that doesn't exist

### Root Cause
The Supabase Storage buckets don't exist:
- `mosque-slides` bucket NOT created
- `mosque-images` bucket NOT created

### How to Fix
1. Go to Supabase Dashboard → **Storage**
2. Click **Create New Bucket**
3. Create `mosque-slides`:
   - Name: `mosque-slides`
   - **Uncheck** "Private bucket" (make it PUBLIC)
   - Click Create
4. Create `mosque-images`:
   - Name: `mosque-images`
   - **Uncheck** "Private bucket" (make it PUBLIC)
   - Click Create

### Verification
```sql
SELECT name, public FROM storage.buckets 
WHERE name IN ('mosque-slides', 'mosque-images');
```
Should return 2 rows, both with `public = true`

---

## Error 3: "401 Unauthorized" on Storage Upload (If You See This)

### Root Cause
Storage buckets exist but don't have RLS policies allowing upload.

### How to Fix
1. Go to Supabase → **Storage** → **Policies** tab
2. For each bucket (mosque-slides, mosque-images), add 3 policies:

**Policy 1: SELECT (Allow everyone to read)**
- Click "New Policy" → SELECT
- Expression: `true`
- Save

**Policy 2: INSERT (Allow authenticated users to upload)**
- Click "New Policy" → INSERT
- Expression: `auth.role() = 'authenticated'`
- Save

**Policy 3: DELETE (Allow authenticated users to delete)**
- Click "New Policy" → DELETE
- Expression: `auth.role() = 'authenticated'`
- Save

Do this for both `mosque-slides` and `mosque-images` buckets.

---

## Error 4: "user doesn't own mosque" (If You Still Get RLS Error)

### Root Cause
The database RLS policy requires:
```sql
auth.uid() IN (SELECT owner_uid FROM mosques WHERE id = mosque_id)
```
This means your user's ID doesn't match any mosque's `owner_uid`.

### How to Fix
1. Make sure you're logged in as the mosque owner
2. Check your mosque record:
```sql
SELECT id, owner_uid, name FROM public.mosques LIMIT 1;
```
3. Check your auth user ID:
```sql
SELECT auth.uid();
```
4. If they don't match:
   - Go to **Profile** page in your app
   - Update any mosque field (e.g., add a city name, change name)
   - Save
   - This will update the `owner_uid` to your current auth.uid()
   
OR

- Go through **Onboarding** to create a brand new mosque from scratch

---

## Complete Flow: What Happens on Upload

### Slide Upload Flow (SlidesPage.tsx)
```
1. User clicks "Upload Slides"
2. Selects image files
3. For each file:
   a. Upload to "mosque-slides" bucket
      ↓
      (Storage RLS Policy 1: Checks auth.role() = 'authenticated')
      ↓
      File stored at: mosque-slides/{mosque_id}/{timestamp}-{i}.{ext}
   
   b. Get public URL: https://storage.supabase.co/object/public/mosque-slides/{path}
   
   c. Insert record into mosque_slides table
      ↓
      (Database RLS Policy 2 [INSERT]: Checks auth.uid() owns mosque)
      ↓
      Record inserted: {id: ..., mosque_id: ..., image_url: ..., display_order: ...}
   
   d. Update React Query cache
   
   e. Show success toast
4. Images appear in grid
```

### Profile Image Upload Flow (ProfilePage.tsx)
```
1. User clicks "Upload Image"
2. Selects image file
3. Upload to "mosque-images" bucket
   ↓
   (Storage RLS Policy 1: Checks auth.role() = 'authenticated')
   ↓
   File stored at: mosque-images/{mosque_id}/profile.{ext}
4. Get public URL
5. Update mosque record with image URL
   ↓
   (Database RLS Policy 3 [UPDATE]: Checks auth.uid() owns mosque)
6. Show success toast
```

---

## Security Model

```
┌─────────────────────────────────────────────────┐
│ User Authentication (Supabase Auth)             │
│ - User logs in with email                       │
│ - Gets auth.uid() (UUID)                        │
└──────────────────────┬──────────────────────────┘
                       │
        ┌──────────────┴───────────────┐
        ↓                              ↓
┌───────────────────┐        ┌────────────────────┐
│ Database Layer    │        │ Storage Layer      │
│ (mosque_slides)   │        │ (buckets)          │
├───────────────────┤        ├────────────────────┤
│ RLS Policy:       │        │ RLS Policy:        │
│ auth.uid() IN     │        │ auth.role() =      │
│ (SELECT          │        │ 'authenticated'    │
│  owner_uid FROM  │        │                    │
│  mosques WHERE   │        │ → All authenticated│
│  id = mosque_id) │        │   users can upload │
│                   │        │                    │
│ → Only mosque     │        │ But need correct   │
│   owner can       │        │ bucket to exist    │
│   upload to DB    │        │                    │
└───────────────────┘        └────────────────────┘
```

---

## Summary: What You Need to Do

| Error | Fix | Files |
|-------|-----|-------|
| "RLS policy" error on slide upload | Run RLS policy SQL | [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql) |
| "bucket not found" on any upload | Create 2 public buckets | Manual steps in Supabase UI |
| "401 unauthorized" on storage upload | Add 3 storage policies per bucket | Manual steps in Supabase UI |
| User doesn't own mosque | Update mosque record or redo onboarding | Via Profile page or Onboarding |

---

## Files in This Project

- **[QUICK_FIX_5MIN.md](QUICK_FIX_5MIN.md)** - 5-minute quick start (READ THIS FIRST)
- **[COMPLETE_FIX_GUIDE.md](COMPLETE_FIX_GUIDE.md)** - Detailed 7-step guide with explanations
- **[RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)** - Copy-paste SQL for RLS policies
- **[FINAL_TABLE_SETUP.sql](FINAL_TABLE_SETUP.sql)** - If you need to recreate table from scratch
- **UPLOAD_ERRORS_EXPLAINED.md** - (This file) Technical explanation of errors

**Next Step**: Read [QUICK_FIX_5MIN.md](QUICK_FIX_5MIN.md) and follow the 5 steps!
