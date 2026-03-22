-- QUICK COPY-PASTE: Recreate mosque_slides table with proper RLS
-- Paste all of this into Supabase SQL Editor and click Run

-- Drop old policies and table
DROP POLICY IF EXISTS "Users can select their mosque slides" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can insert slides for their mosque" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can update their mosque slides" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can delete their mosque slides" ON public.mosque_slides;
DROP TABLE IF EXISTS public.mosque_slides CASCADE;

-- Create table
CREATE TABLE public.mosque_slides (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  mosque_id UUID NOT NULL REFERENCES public.mosques(id) ON DELETE CASCADE,
  image_url TEXT NOT NULL,
  display_order INTEGER NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_mosque_slides_mosque_id ON public.mosque_slides(mosque_id);
CREATE INDEX idx_mosque_slides_display_order ON public.mosque_slides(mosque_id, display_order);

-- Enable RLS
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY "Users can select their mosque slides" 
ON public.mosque_slides FOR SELECT TO authenticated 
USING (
  EXISTS (
    SELECT 1 FROM public.mosques 
    WHERE mosques.id = mosque_slides.mosque_id 
    AND mosques.owner_uid = auth.uid()
  )
);

CREATE POLICY "Users can insert slides for their mosque" 
ON public.mosque_slides FOR INSERT TO authenticated 
WITH CHECK (
  EXISTS (
    SELECT 1 FROM public.mosques 
    WHERE mosques.id = mosque_slides.mosque_id 
    AND mosques.owner_uid = auth.uid()
  )
);

CREATE POLICY "Users can update their mosque slides" 
ON public.mosque_slides FOR UPDATE TO authenticated 
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

CREATE POLICY "Users can delete their mosque slides" 
ON public.mosque_slides FOR DELETE TO authenticated 
USING (
  EXISTS (
    SELECT 1 FROM public.mosques 
    WHERE mosques.id = mosque_slides.mosque_id 
    AND mosques.owner_uid = auth.uid()
  )
);

-- DONE! All 8 operations should complete successfully.
-- Table recreated with proper RLS policies that won't block uploads.
