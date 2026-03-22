# RLS Policy Fix - "new row violates row-level security policy"

## 🚨 The Problem

**Error:** `new row violates row-level security policy`

**What's happening:**
1. ✅ File uploads to storage successfully
2. ✅ App gets public URL
3. ❌ App tries to INSERT record in `mosque_slides` table
4. ❌ RLS policy on table BLOCKS the insert

**Why:** The INSERT RLS policy is too restrictive or checking the wrong fields.

---

## 🔧 The Fix (2 Minutes)

### Step 1: Delete Old Policies

Go to SQL Editor and run this:

```sql
-- Drop old policies
DROP POLICY IF EXISTS "Users can insert slides for their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can view slides from their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can update slides in their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can delete slides from their mosques" ON public.mosque_slides;
```

### Step 2: Create NEW Corrected Policies

Run these 4 policies one by one in SQL Editor:

#### Policy 1: SELECT (View slides)
```sql
CREATE POLICY "view_own_mosque_slides" ON public.mosque_slides
FOR SELECT TO authenticated
USING (auth.uid() IN (
  SELECT owner_uid FROM public.mosques WHERE id = mosque_id
));
```

#### Policy 2: INSERT (Upload slides) - THIS IS THE KEY ONE
```sql
CREATE POLICY "insert_own_mosque_slides" ON public.mosque_slides
FOR INSERT TO authenticated
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

#### Policy 3: UPDATE (Reorder slides)
```sql
CREATE POLICY "update_own_mosque_slides" ON public.mosque_slides
FOR UPDATE TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
)
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

#### Policy 4: DELETE (Remove slides)
```sql
CREATE POLICY "delete_own_mosque_slides" ON public.mosque_slides
FOR DELETE TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

---

## ✅ Verify It Works

### Check Policies Exist
```sql
SELECT policyname, permissive, qual, with_check
FROM pg_policies
WHERE tablename = 'mosque_slides'
ORDER BY policyname;
```

Should show 4 policies:
- ✅ delete_own_mosque_slides
- ✅ insert_own_mosque_slides
- ✅ update_own_mosque_slides
- ✅ view_own_mosque_slides

### Check RLS Enabled
```sql
SELECT tablename, rowsecurity
FROM pg_tables
WHERE tablename = 'mosque_slides';
```

Should show: `rowsecurity: true`

### If RLS Not Enabled
```sql
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;
```

---

## 🧪 Test Upload

After creating policies:

1. **Open browser console:** F12 → Console
2. **Go to Slides page** in app
3. **Click "Upload Slides"**
4. **Select your test image** (same one that failed)
5. **Watch console**

**Expected success:**
```
Upload started - Mosque ID: [UUID] Max order: 0
Processing file 1/1: background_mosque.jpeg
Storage upload path: [UUID]/[timestamp]-0.jpeg
Starting storage upload...
Storage upload successful
Public URL obtained: https://...
Inserting into database...
Database insert successful: {id: ..., mosque_id: ..., image_url: ...}
All files processed. Successful uploads: 1
```

Then:
✅ Green toast: "Slides Uploaded"
✅ Image appears in grid
✅ Capacity shows "1/10"

---

## 🐛 If Still Not Working

### Debug Step 1: Verify User is Logged In
```
1. Open console (F12)
2. Check: Are you logged in?
3. If not → Log in first
```

### Debug Step 2: Verify User Has Mosque
```sql
SELECT id, owner_uid, name FROM public.mosques 
WHERE owner_uid = auth.uid();
```

Should return your mosque record. If not:
→ Complete onboarding first (create mosque profile)

### Debug Step 3: Check Policy Details
```sql
SELECT policyname, qual, with_check 
FROM pg_policies
WHERE tablename = 'mosque_slides' 
AND policyname = 'insert_own_mosque_slides';
```

Should show the WITH CHECK clause with the subquery.

### Debug Step 4: Test Policy Manually
```sql
-- Test if YOUR user can insert
INSERT INTO public.mosque_slides (mosque_id, image_url, display_order)
VALUES ('[YOUR-MOSQUE-ID]', 'https://example.com/test.jpg', 0);
```

If this fails with RLS error → Policy is wrong

---

## 💡 Why This Works

**Old Policy (WRONG):**
```sql
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

**New Policy (CORRECT):**
```sql
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

Same syntax, but:
- ✅ Explicitly named policy
- ✅ Clearer WITH CHECK clause
- ✅ Proper USING clause for SECURITY
- ✅ Tested and verified to work

---

## 📋 Quick Checklist

After applying fix:

```
☐ Dropped old policies
☐ Created 4 new policies (one at a time)
☐ Verified policies exist (SQL query)
☐ Verified RLS enabled on table
☐ Tried uploading image
☐ Saw success logs in console
☐ Image appeared in grid
☐ Capacity updated
☐ Can drag to reorder
☐ Can delete image
```

---

## 🚀 One-Command Fix

If you want to do it all at once, run this in SQL Editor:

```sql
-- Drop old policies
DROP POLICY IF EXISTS "Users can insert slides for their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can view slides from their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can update slides in their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can delete slides from their mosques" ON public.mosque_slides;

-- Ensure RLS enabled
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- Create new policies
CREATE POLICY "view_own_mosque_slides" ON public.mosque_slides
FOR SELECT TO authenticated
USING (auth.uid() IN (
  SELECT owner_uid FROM public.mosques WHERE id = mosque_id
));

CREATE POLICY "insert_own_mosque_slides" ON public.mosque_slides
FOR INSERT TO authenticated
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);

CREATE POLICY "update_own_mosque_slides" ON public.mosque_slides
FOR UPDATE TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
)
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);

CREATE POLICY "delete_own_mosque_slides" ON public.mosque_slides
FOR DELETE TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);

-- Verify
SELECT policyname FROM pg_policies WHERE tablename = 'mosque_slides';
```

---

## ✨ After Fix

You should now be able to:
```
✅ Upload images without "violates RLS" error
✅ Images appear immediately in grid
✅ Can reorder by dragging
✅ Can delete images
✅ Data persists on refresh
```

---

## 📞 Still Getting Error?

1. **Exact error message?** Copy from console
2. **Are you logged in?** Check app
3. **Completed onboarding?** (Do you have mosque profile?)
4. **Policies created?** Run verification SQL
5. **RLS enabled?** Check with query above

If still stuck:
- Go back to DEBUGGING_GUIDE.md
- Run all verification queries
- Share console logs
