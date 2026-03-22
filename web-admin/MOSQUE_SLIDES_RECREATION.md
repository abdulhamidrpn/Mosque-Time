# Mosque Slides Table Recreation - Complete Guide

## Problem
The `mosque_slides` table was deleted. We need to recreate it with proper RLS policies that won't block uploads.

## Solution: Complete SQL for Supabase

Copy and paste ALL of the following SQL into your Supabase SQL Editor and execute:

```sql
-- 1. DROP existing RLS policies (if any remain)
DROP POLICY IF EXISTS "Users can select their mosque slides" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can insert slides for their mosque" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can update their mosque slides" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can delete their mosque slides" ON public.mosque_slides;

-- 2. DROP table if it exists
DROP TABLE IF EXISTS public.mosque_slides CASCADE;

-- 3. CREATE the table with proper schema
CREATE TABLE public.mosque_slides (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  mosque_id UUID NOT NULL REFERENCES public.mosques(id) ON DELETE CASCADE,
  image_url TEXT NOT NULL,
  display_order INTEGER NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 4. CREATE indexes for performance
CREATE INDEX idx_mosque_slides_mosque_id ON public.mosque_slides(mosque_id);
CREATE INDEX idx_mosque_slides_display_order ON public.mosque_slides(mosque_id, display_order);

-- 5. ENABLE RLS on the table
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- 6. CREATE RLS POLICIES - These are the CORRECTED policies that won't block uploads

-- Policy 1: SELECT - Users can view slides from their own mosque
CREATE POLICY "Users can select their mosque slides" 
ON public.mosque_slides 
FOR SELECT 
TO authenticated 
USING (
  EXISTS (
    SELECT 1 FROM public.mosques 
    WHERE mosques.id = mosque_slides.mosque_id 
    AND mosques.owner_uid = auth.uid()
  )
);

-- Policy 2: INSERT - Users can upload slides to their own mosque
CREATE POLICY "Users can insert slides for their mosque" 
ON public.mosque_slides 
FOR INSERT 
TO authenticated 
WITH CHECK (
  EXISTS (
    SELECT 1 FROM public.mosques 
    WHERE mosques.id = mosque_slides.mosque_id 
    AND mosques.owner_uid = auth.uid()
  )
);

-- Policy 3: UPDATE - Users can update slides in their own mosque
CREATE POLICY "Users can update their mosque slides" 
ON public.mosque_slides 
FOR UPDATE 
TO authenticated 
USING (
  EXISTS (
    SELECT 1 FROM public.mosques 
    WHERE mosques.id = mosque_slides.mosque_id 
    AND mosques.owner_uid = auth.uid()
  )
)
WITH CHECK (
  EXISTS (
    SELECT 1 FROM public.mosques 
    WHERE mosques.id = mosque_slides.mosque_id 
    AND mosques.owner_uid = auth.uid()
  )
);

-- Policy 4: DELETE - Users can delete slides from their own mosque
CREATE POLICY "Users can delete their mosque slides" 
ON public.mosque_slides 
FOR DELETE 
TO authenticated 
USING (
  EXISTS (
    SELECT 1 FROM public.mosques 
    WHERE mosques.id = mosque_slides.mosque_id 
    AND mosques.owner_uid = auth.uid()
  )
);
```

## Step-by-Step Instructions

### 1. Open Supabase SQL Editor
1. Go to your Supabase project dashboard: https://app.supabase.com
2. Navigate to **SQL Editor** (left sidebar)
3. Click **New Query**

### 2. Copy the SQL
Copy the entire SQL block above (including all comments and the 6 numbered sections)

### 3. Paste into Supabase
Paste into the SQL Editor

### 4. Execute
Click the **Run** button (or press `Ctrl+Enter`)

### 5. Verify Success
You should see output like:
```
CREATE TABLE
CREATE INDEX
CREATE INDEX
ALTER TABLE
CREATE POLICY
CREATE POLICY
CREATE POLICY
CREATE POLICY
```

All 8 operations should complete without errors.

## Verification Queries

Run these queries to verify everything is working:

### Check table exists:
```sql
SELECT column_name, data_type FROM information_schema.columns 
WHERE table_name = 'mosque_slides' 
ORDER BY ordinal_position;
```

Expected columns:
- id (uuid)
- mosque_id (uuid)
- image_url (text)
- display_order (integer)
- is_active (boolean)
- created_at (timestamp with time zone)
- updated_at (timestamp with time zone)

### Check RLS is enabled:
```sql
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE tablename = 'mosque_slides';
```

Expected output:
```
mosque_slides | t
```
(t = true, meaning RLS is enabled)

### Check RLS policies exist:
```sql
SELECT policyname, roles, qual, with_check 
FROM pg_policies 
WHERE tablename = 'mosque_slides' 
ORDER BY policyname;
```

Expected: 4 policies listed

### Check indexes exist:
```sql
SELECT indexname FROM pg_indexes 
WHERE tablename = 'mosque_slides';
```

Expected: 3 indexes (2 we created + 1 automatic on primary key)

## Why These Policies Work

The corrected RLS policies use `WITH CHECK` for INSERT and UPDATE, which is the critical fix:

❌ **Old Problem**:
```sql
CREATE POLICY "insert slides" ON mosque_slides 
FOR INSERT TO authenticated 
USING (EXISTS(...))  -- WRONG! USING doesn't apply to INSERT
```

✅ **New Solution**:
```sql
CREATE POLICY "insert slides" ON mosque_slides 
FOR INSERT TO authenticated 
WITH CHECK (EXISTS(...))  -- CORRECT! WITH CHECK applies to INSERT
```

**Key difference**: 
- `USING` applies to SELECT/UPDATE/DELETE existing rows
- `WITH CHECK` applies to INSERT/UPDATE new row values

## Testing the Upload Flow

After recreating the table, test your upload:

1. Go to Slides page in your dashboard
2. Select an image file
3. Click Upload
4. Check console for logs (should see mosque_id, file validation, storage upload, database insert)
5. Image should appear in the list immediately
6. Try dragging to reorder
7. Try deleting

All operations should work without RLS errors.

## Common Issues & Solutions

### Issue: "permission denied for schema public"
**Solution**: Make sure you're logged in to Supabase as the project owner/admin

### Issue: "relation doesn't exist"
**Solution**: The `mosques` table must exist first. Check it exists:
```sql
SELECT COUNT(*) FROM public.mosques;
```

### Issue: "violates foreign key constraint"
**Solution**: Make sure you're uploading for a mosque ID that exists:
```sql
SELECT id FROM public.mosques LIMIT 1;
```

### Issue: "new row violates row-level security policy"
**Solution**: Make sure:
1. You're authenticated (logged in with email)
2. Your user ID matches the `owner_uid` in the mosques table
3. You're using the correct `mosque_id`

Check your user ID:
```sql
SELECT auth.uid();
```

Check mosque owner:
```sql
SELECT id, owner_uid FROM public.mosques WHERE id = 'YOUR_MOSQUE_ID';
```

## Next Steps

1. Execute the SQL above
2. Run verification queries
3. Test upload from your app
4. If any errors, check the Common Issues section
5. Upload should now work correctly

## Backup Reference

If needed later, here's what was recreated:
- Table: `public.mosque_slides`
- Columns: 7 (id, mosque_id, image_url, display_order, is_active, created_at, updated_at)
- Indexes: 2 (mosque_id, mosque_id + display_order)
- RLS Policies: 4 (SELECT, INSERT, UPDATE, DELETE)
- Foreign Key: mosque_id → mosques.id (ON DELETE CASCADE)
