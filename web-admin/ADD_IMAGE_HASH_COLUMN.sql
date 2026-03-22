-- ADD ADDRESS COLUMN AND REMOVE CITY/COUNTRY
-- This replaces city and country with a single address field

-- Add address column if it doesn't exist
ALTER TABLE public.mosques 
ADD COLUMN IF NOT EXISTS address TEXT;

-- Migrate data from city/country to address (if both exist)
UPDATE public.mosques 
SET address = CONCAT_WS(', ', city, country)
WHERE address IS NULL 
AND (city IS NOT NULL OR country IS NOT NULL);

-- Drop old columns (optional - comment out if you want to keep them temporarily)
-- ALTER TABLE public.mosques DROP COLUMN IF EXISTS city;
-- ALTER TABLE public.mosques DROP COLUMN IF EXISTS country;

-- ADD IMAGE HASH COLUMN FOR DEDUPLICATION
-- This column stores SHA-256 hash of uploaded images to detect duplicates
ALTER TABLE public.mosque_slides 
ADD COLUMN IF NOT EXISTS image_hash TEXT;

-- Create index on image_hash for faster duplicate checking
CREATE INDEX IF NOT EXISTS idx_mosque_slides_image_hash 
ON public.mosque_slides(mosque_id, image_hash);

-- Summary
SELECT 'Migration completed: address field added' as status;
