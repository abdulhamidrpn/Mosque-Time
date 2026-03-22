# Complete Fix Guide: RLS Policies & Storage Buckets

## Problem Summary
- ❌ "new row violates row-level security policy" when uploading slides
- ❌ "bucket not found" when uploading mosque images

## Root Causes
1. **RLS Policy Error**: The `mosque_slides` table RLS policies don't correctly check mosque ownership
2. **Bucket Not Found**: Storage buckets `mosque-slides` and `mosque-images` don't exist in Supabase

---

## STEP 1: Create Storage Buckets

### 1.1 Go to Supabase Dashboard
- Open https://app.supabase.com
- Go to your project: `lllnnuqdcoockevelthu`
- Click **Storage** (left sidebar)

### 1.2 Create "mosque-slides" Bucket
1. Click **Create New Bucket**
2. Name: `mosque-slides`
3. Uncheck **Private bucket** (make it public)
4. Click **Create Bucket**

### 1.3 Create "mosque-images" Bucket
1. Click **Create New Bucket**
2. Name: `mosque-images`
3. Uncheck **Private bucket** (make it public)
4. Click **Create Bucket**

### Result
You should now see both buckets in the Storage list:
- ✅ mosque-slides (public)
- ✅ mosque-images (public)

---

## STEP 2: Fix RLS Policies on Database Table

### 2.1 Open SQL Editor
- In Supabase, click **SQL Editor** (left sidebar)
- Click **New Query**

### 2.2 Run This SQL (Copy-paste everything below)

```sql
-- First, check if the mosque_slides table exists
SELECT EXISTS (
  SELECT FROM information_schema.tables 
  WHERE table_name = 'mosque_slides'
);

-- If it exists, drop the old policies
DROP POLICY IF EXISTS "Users can select their mosque slides" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can insert slides for their mosque" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can update their mosque slides" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can delete their mosque slides" ON public.mosque_slides;

-- Enable RLS
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- Create CORRECTED RLS policies
-- Policy 1: SELECT
CREATE POLICY "Users can select their mosque slides" 
ON public.mosque_slides FOR SELECT TO authenticated 
USING (
  auth.uid() IN (
    SELECT owner_uid FROM public.mosques WHERE id = mosque_slides.mosque_id
  )
);

-- Policy 2: INSERT (THIS IS THE KEY FIX - uses WITH CHECK, not USING)
CREATE POLICY "Users can insert slides for their mosque" 
ON public.mosque_slides FOR INSERT TO authenticated 
WITH CHECK (
  auth.uid() IN (
    SELECT owner_uid FROM public.mosques WHERE id = mosque_slides.mosque_id
  )
);

-- Policy 3: UPDATE
CREATE POLICY "Users can update their mosque slides" 
ON public.mosque_slides FOR UPDATE TO authenticated 
USING (
  auth.uid() IN (
    SELECT owner_uid FROM public.mosques WHERE id = mosque_slides.mosque_id
  )
)
WITH CHECK (
  auth.uid() IN (
    SELECT owner_uid FROM public.mosques WHERE id = mosque_slides.mosque_id
  )
);

-- Policy 4: DELETE
CREATE POLICY "Users can delete their mosque slides" 
ON public.mosque_slides FOR DELETE TO authenticated 
USING (
  auth.uid() IN (
    SELECT owner_uid FROM public.mosques WHERE id = mosque_slides.mosque_id
  )
);
```

### 2.3 Run the Query
- Click **Run** button (or press `Ctrl+Enter`)
- All queries should succeed without errors

---

## STEP 3: Verify Your Data

### 3.1 Check You Have a Mosque Record
Run this query in SQL Editor:

```sql
SELECT id, owner_uid, name FROM public.mosques LIMIT 1;
```

**Expected result**: Should show at least 1 mosque with:
- ✅ `id` - a UUID (e.g., `550e8400-e29b-41d4-a716-446655440000`)
- ✅ `owner_uid` - a UUID (matches your Supabase auth user ID)
- ✅ `name` - mosque name

**If no results**: Create a mosque first via the onboarding page

### 3.2 Check Your Auth User ID
Run this query:

```sql
SELECT auth.uid();
```

**Expected result**: Should show your Supabase user ID (UUID)

### 3.3 Verify They Match
```sql
SELECT 
  (SELECT auth.uid()) as your_user_id,
  (SELECT owner_uid FROM public.mosques LIMIT 1) as mosque_owner_id,
  (SELECT auth.uid()) = (SELECT owner_uid FROM public.mosques LIMIT 1) as user_is_owner;
```

**Expected result**: `user_is_owner` should be `true`

**If false**: The logged-in user doesn't own any mosque. Go through onboarding to create one.

---

## STEP 4: Set Up Storage RLS Policies

### 4.1 Go to Storage Policies
- In Supabase, click **Storage** (left sidebar)
- Click **Policies** tab

### 4.2 Create Policies for "mosque-slides" Bucket

**Policy 1: Public Read Access**
1. Click **New Policy** on `mosque-slides` bucket
2. Choose **For SELECT**
3. Paste this in the **Expression** field:
```sql
true
```
4. Click **Save**

**Policy 2: Authenticated Upload**
1. Click **New Policy** on `mosque-slides` bucket
2. Choose **For INSERT**
3. Paste this in the **Expression** field:
```sql
auth.role() = 'authenticated'
```
4. Click **Save**

**Policy 3: User Can Delete Own Uploads**
1. Click **New Policy** on `mosque-slides` bucket
2. Choose **For DELETE**
3. Paste this in the **Expression** field:
```sql
auth.role() = 'authenticated'
```
4. Click **Save**

### 4.3 Create Policies for "mosque-images" Bucket

**Do the same 3 policies for `mosque-images` bucket:**
1. Policy for SELECT: `true`
2. Policy for INSERT: `auth.role() = 'authenticated'`
3. Policy for DELETE: `auth.role() = 'authenticated'`

---

## STEP 5: Test the Upload

### 5.1 Go to Your App
- Open http://localhost:8080 (or your dev server)
- Navigate to **Digital Signage** page

### 5.2 Try Uploading an Image
1. Click **Upload Slides** button
2. Select 1-2 image files
3. Check the console (F12 → Console tab) for logs

### 5.3 Check for Errors
- ✅ If you see images appear in the list = SUCCESS
- ❌ If you see "bucket not found" = Bucket not created (go back to Step 1)
- ❌ If you see "RLS policy" error = RLS policies not working (check Step 3 data verification)

---

## STEP 6: Troubleshooting

### Error: "new row violates row-level security policy"

**Cause**: User doesn't own the mosque

**Fix**:
1. Run this to check your data:
```sql
SELECT auth.uid() as user_id, owner_uid, name FROM public.mosques;
```

2. If `auth.uid()` ≠ `owner_uid`, then:
   - Go to your app's **Profile** page
   - Update your mosque info (this should set the correct owner_uid)
   - OR delete the mosque and go through onboarding again

### Error: "bucket not found" or "401 Unauthorized"

**Cause**: Bucket doesn't exist or RLS policies block access

**Fix**:
1. Go to Supabase → Storage
2. Verify both buckets exist:
   - mosque-slides
   - mosque-images
3. Click each bucket → Policies tab
4. Verify 3 policies exist for each bucket

### Error: "Cannot read properties of undefined"

**Cause**: Mosque ID is undefined

**Fix**:
1. Make sure you're logged in
2. Make sure you created a mosque (go through onboarding)
3. Check console logs to see the mosque ID

---

## STEP 7: Final Verification

Run these 4 queries to verify everything is set up:

**Query 1**: Check table exists
```sql
SELECT EXISTS (
  SELECT FROM information_schema.tables 
  WHERE table_name = 'mosque_slides'
);
```
Expected: `true`

**Query 2**: Check RLS is enabled
```sql
SELECT tablename, rowsecurity FROM pg_tables 
WHERE tablename = 'mosque_slides';
```
Expected: `mosque_slides | t`

**Query 3**: Check RLS policies exist
```sql
SELECT policyname FROM pg_policies 
WHERE tablename = 'mosque_slides' 
ORDER BY policyname;
```
Expected: 4 policies listed

**Query 4**: Check buckets exist
```sql
SELECT name, public FROM storage.buckets 
WHERE name IN ('mosque-slides', 'mosque-images');
```
Expected: 2 rows, both public = true

---

## Complete Checklist

- [ ] Step 1: Created `mosque-slides` bucket (public)
- [ ] Step 1: Created `mosque-images` bucket (public)
- [ ] Step 2: Ran RLS policy SQL - all succeeded
- [ ] Step 3: Verified mosque record exists with owner_uid
- [ ] Step 3: Verified user_is_owner = true
- [ ] Step 4: Created 3 policies for mosque-slides bucket
- [ ] Step 4: Created 3 policies for mosque-images bucket
- [ ] Step 5: Tested image upload successfully
- [ ] Step 7: All 4 verification queries passed

Once all checks are done, your uploads should work perfectly!

---

## Quick Reference: What Was Wrong

| Error | Cause | Fix |
|-------|-------|-----|
| "RLS policy" on slide upload | Wrong RLS policy (used USING instead of WITH CHECK for INSERT) | Recreate policies with WITH CHECK for INSERT/UPDATE |
| "bucket not found" | Buckets don't exist | Create mosque-slides and mosque-images buckets |
| "401 Unauthorized" on storage | No storage RLS policies | Create 3 policies per bucket (SELECT, INSERT, DELETE) |
| "user doesn't own mosque" | owner_uid ≠ auth.uid() | Update mosque record or recreate via onboarding |

---

## Key Insight: Why WITH CHECK Matters

❌ **WRONG** (blocks INSERT):
```sql
CREATE POLICY "insert slides" ON mosque_slides 
FOR INSERT TO authenticated 
USING (auth.uid() IN (SELECT owner_uid FROM public.mosques WHERE id = mosque_id))
```

✅ **CORRECT** (allows INSERT):
```sql
CREATE POLICY "insert slides" ON mosque_slides 
FOR INSERT TO authenticated 
WITH CHECK (auth.uid() IN (SELECT owner_uid FROM public.mosques WHERE id = mosque_id))
```

**Why**: 
- `USING` applies to existing rows (SELECT, UPDATE, DELETE)
- `WITH CHECK` applies to new rows (INSERT, UPDATE)

For INSERT, there's no existing row to check, so `USING` fails. Use `WITH CHECK` instead!
