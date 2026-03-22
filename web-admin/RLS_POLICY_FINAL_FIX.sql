-- SUPABASE SQL: Complete RLS Policy Fix for mosque_slides Table
-- Copy-paste ALL of this into Supabase SQL Editor and click Run
-- This will fix the "new row violates row-level security policy" error

-- Step 1: Drop old policies (if they exist)
DROP POLICY IF EXISTS "Users can select their mosque slides" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can insert slides for their mosque" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can update their mosque slides" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can delete their mosque slides" ON public.mosque_slides;

-- Step 2: Enable RLS (if not already enabled)
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- Step 3: Create CORRECTED RLS Policies (The fix: using WITH CHECK for INSERT/UPDATE)

-- Policy 1: SELECT - Users can view slides from their own mosque
CREATE POLICY "Users can select their mosque slides" 
ON public.mosque_slides 
FOR SELECT 
TO authenticated 
USING (
  auth.uid() IN (
    SELECT owner_uid FROM public.mosques WHERE id = mosque_slides.mosque_id
  )
);

-- Policy 2: INSERT - Users can upload slides to their own mosque
-- KEY FIX: Uses WITH CHECK (not USING) for INSERT operations
CREATE POLICY "Users can insert slides for their mosque" 
ON public.mosque_slides 
FOR INSERT 
TO authenticated 
WITH CHECK (
  auth.uid() IN (
    SELECT owner_uid FROM public.mosques WHERE id = mosque_slides.mosque_id
  )
);

-- Policy 3: UPDATE - Users can update slides in their own mosque
CREATE POLICY "Users can update their mosque slides" 
ON public.mosque_slides 
FOR UPDATE 
TO authenticated 
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

-- Policy 4: DELETE - Users can delete slides from their own mosque
CREATE POLICY "Users can delete their mosque slides" 
ON public.mosque_slides 
FOR DELETE 
TO authenticated 
USING (
  auth.uid() IN (
    SELECT owner_uid FROM public.mosques WHERE id = mosque_slides.mosque_id
  )
);

-- Verification: Run these to confirm setup
SELECT 'RLS Policies Created Successfully' as status;
