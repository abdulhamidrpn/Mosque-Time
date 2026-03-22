# Quick Setup: Image Optimization (2 Minutes)

## What You Get

✅ **Automatic image resizing** - Large images scaled to 1920x1080
✅ **Deduplication** - Same image uploaded twice? Only stored once
✅ **File size reduction** - 90%+ smaller (2MB → 150KB)
✅ **Quality preserved** - 85% JPEG quality (imperceptible loss)

---

## Setup (Just 1 Step!)

### Run SQL to Add Hash Column

1. Go to: https://app.supabase.com
2. Your project → **SQL Editor** → **New Query**
3. Copy this:

```sql
ALTER TABLE public.mosque_slides 
ADD COLUMN IF NOT EXISTS image_hash TEXT;

CREATE INDEX IF NOT EXISTS idx_mosque_slides_image_hash 
ON public.mosque_slides(mosque_id, image_hash);
```

4. Click **Run**

✅ **Done!** That's it!

---

## Test It

1. Open your app
2. Go to **Digital Signage** page
3. Upload an image (2MB+)
4. Open console (F12) and see:
   - Hash generated ✅
   - Image resized ✅
   - Optimized size ✅

5. Upload the SAME image again
6. See: "Duplicate Image - Using existing slide" ✅

---

## What Happens

```
Your image (5 MB)
    ↓
Hash generated (deduplication)
    ↓
Check for duplicates
    ↓
Resize to 1920x1080 (85% JPEG)
    ↓
Optimized image (150 KB)
    ↓
Upload ✅
```

---

## Console Logs to Expect

```
✓ Image hash generated: a1b2c3d4e5f6...
✓ Checking for duplicate images...
✓ Resizing image...
✓ Image resized. Original: 5 MB → New: 150 KB
✓ Storage upload successful
✓ Database insert successful
```

---

## Size Examples

| Before | After | Savings |
|--------|-------|---------|
| 5 MB | 150 KB | 97% ↓ |
| 2.1 MB | 85 KB | 96% ↓ |
| 3.5 MB | 120 KB | 97% ↓ |

---

## That's It! 🚀

Your images are now:
- ✅ Optimized (90% smaller)
- ✅ Deduplicated (no double uploads)
- ✅ Quality preserved (85% JPEG)

**No other setup needed!**

---

## More Info

Read [IMAGE_OPTIMIZATION_GUIDE.md](IMAGE_OPTIMIZATION_GUIDE.md) for detailed documentation.
