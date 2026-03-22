# Image Upload - Complete Debugging Checklist

## 🎯 What Was Fixed

### Code Improvements:
1. ✅ **Enhanced Error Logging** - Console now shows detailed upload flow
2. ✅ **Better Error Messages** - User gets clear feedback on failures
3. ✅ **Proper Cache Updates** - Images appear immediately after upload
4. ✅ **Database Integration** - Records properly inserted with all fields
5. ✅ **File Validation** - Type checking before upload

### Error Messages Now Tracked:
- Storage upload errors (bucket not found, permission denied, etc.)
- Database insert errors (RLS violations, constraint failures)
- File type validation errors
- Network errors

---

## 📋 Complete Debugging Flow

### Before You Start
```
✅ Confirm these are EXACTLY correct in .env:
   VITE_SUPABASE_URL="https://lllnnuqdcoockevelthu.supabase.co"
   VITE_SUPABASE_PUBLISHABLE_KEY="sb_publishable_WvtOpUuXcNmx8rr9HurMUg_Kb_rtKHf"

✅ User is authenticated (logged in to app)

✅ User has completed onboarding (has mosque profile)
```

### Step 1: Verify Supabase Bucket

**Via Browser Console:**
```javascript
// Run in browser console (F12):
import { supabase } from '@/integrations/supabase/client';
await supabase.storage.listBuckets();
// Should show 'mosque-slides' bucket
```

**Via Supabase Dashboard:**
```
1. Go to: https://app.supabase.com/project/lllnnuqdcoockevelthu/storage/buckets
2. Look for "mosque-slides" bucket
3. Verify it has "Public" badge (green)
4. If NOT there → Create it immediately!
```

---

### Step 2: Test Storage Upload

**Via SQL Editor (Simple Test):**
```
1. Go to SQL Editor
2. Check if bucket is accessible:
   
   SELECT * FROM storage.buckets WHERE id = 'mosque-slides';
   
   Result should show:
   - id: mosque-slides
   - name: mosque-slides
   - public: true
```

---

### Step 3: Verify RLS Policies

**Storage Policies (3 Required):**
```
Go to: Storage → mosque-slides → Policies

Check these exist:
☐ Allow public read (SELECT)
☐ Allow authenticated upload (INSERT)
☐ Allow users delete own (DELETE)

Missing any? Run in SQL Editor:

-- Policy 1: Public Read
CREATE POLICY "Allow public read access to mosque-slides"
ON storage.objects FOR SELECT TO public 
USING (bucket_id = 'mosque-slides');

-- Policy 2: Authenticated Upload
CREATE POLICY "Allow authenticated users to upload slides"
ON storage.objects FOR INSERT TO authenticated 
WITH CHECK (
  bucket_id = 'mosque-slides' AND
  (auth.uid())::text = (storage.foldername(name))[1]
);

-- Policy 3: User Delete Own
CREATE POLICY "Allow users to delete their own slides"
ON storage.objects FOR DELETE TO authenticated 
USING (
  bucket_id = 'mosque-slides' AND
  (auth.uid())::text = (storage.foldername(name))[1]
);
```

**Database Policies (5 Required):**
```
Go to: Table Editor → mosque_slides → Policies

Check these exist:
☐ SELECT policy
☐ INSERT policy
☐ UPDATE policy
☐ DELETE policy

View current policies in SQL Editor:
SELECT tablename, policyname FROM pg_policies 
WHERE tablename = 'mosque_slides';

Missing any? Run in SQL Editor:

-- Enable RLS
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- SELECT policy
CREATE POLICY "Users can view slides from their mosques"
ON public.mosque_slides FOR SELECT
TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);

-- INSERT policy
CREATE POLICY "Users can insert slides for their mosques"
ON public.mosque_slides FOR INSERT
TO authenticated
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);

-- UPDATE policy
CREATE POLICY "Users can update slides in their mosques"
ON public.mosque_slides FOR UPDATE
TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);

-- DELETE policy
CREATE POLICY "Users can delete slides from their mosques"
ON public.mosque_slides FOR DELETE
TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

---

### Step 4: Verify Database Table

**Check if table exists:**
```sql
SELECT * FROM information_schema.tables 
WHERE table_schema = 'public' AND table_name = 'mosque_slides';
```

**If NOT found, create it:**
```sql
CREATE TABLE public.mosque_slides (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  mosque_id UUID NOT NULL REFERENCES public.mosques(id) ON DELETE CASCADE,
  image_url TEXT NOT NULL,
  display_order INTEGER NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Indexes for performance
CREATE INDEX idx_mosque_slides_mosque_id ON public.mosque_slides(mosque_id);
CREATE INDEX idx_mosque_slides_display_order ON public.mosque_slides(display_order);

-- Enable RLS
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;
```

---

### Step 5: Test Upload with Console Logging

**In Your App:**
```
1. Press F12 (Open DevTools)
2. Click "Console" tab
3. Filter to show only messages from your app
4. Go to Slides page
5. Click "Upload Slides"
6. Select 1 test image (small, JPG/PNG)
7. Watch console closely

EXPECTED OUTPUT (in order):
✅ Upload started - Mosque ID: [UUID] Max order: 0
✅ Processing file 1/1: image.jpg Size: [bytes] Type: image/jpeg
✅ Storage upload path: [UUID]/[timestamp]-0.jpg
✅ Starting storage upload...
✅ Storage upload successful, file: {path: ..., id: ...}
✅ Public URL obtained: https://lllnnuqdcoockevelthu.supabase.co/storage/...
✅ Inserting into database...
✅ Database insert successful: {id: ..., mosque_id: ..., ...}
✅ All files processed. Successful uploads: 1
✅ Cache updated with new slides

AFTER THIS:
✅ Green toast: "Slides Uploaded - 1 slide(s) uploaded successfully."
✅ Image appears in grid below
✅ Image numbered #1
✅ Capacity shows "1/10"
```

---

## 🚨 If You See Errors

### Error: "bucket not found"
```
Root cause: mosque-slides bucket doesn't exist

Fix:
1. Go to Storage Buckets
2. Create bucket: "mosque-slides"
3. Make it PUBLIC
4. Try again
```

### Error: "permission denied" (on upload)
```
Root cause: Storage RLS policies not set

Fix:
1. Go to Storage → mosque-slides → Policies
2. Create the 3 policies (see Step 3)
3. Try again
```

### Error: "Insert failed" or "permission denied" (after upload)
```
Root cause: Database RLS policies not set

Fix:
1. Go to SQL Editor
2. Run the 5 database policies (see Step 3)
3. Try again
```

### Error: "Network error"
```
Root cause: Connection issue

Fix:
1. Check internet connection
2. Verify Supabase status: https://status.supabase.com
3. Restart dev server: Ctrl+C, then npm run dev
4. Try again
```

### Image uploads but doesn't appear
```
Root cause: Could be several things

Debug steps:
1. Check console - any errors?
2. Check Network tab - all requests successful?
3. Go to SQL Editor, run:
   SELECT * FROM public.mosque_slides ORDER BY created_at DESC LIMIT 5;
   Do you see your uploaded image?
4. If yes, but not showing in app:
   - Refresh page (F5)
   - Clear cache (F12 → Application → Clear)
5. If no, the database insert failed - check console error
```

---

## ✅ Success Checklist

When everything works, you should be able to:

```
✅ Click "Upload Slides" button
✅ Select 1 or more images (up to 10 total)
✅ See progress bar during upload
✅ Image appears in grid immediately
✅ Image has number label (#1, #2, etc.)
✅ Capacity updates (e.g., "3/10")
✅ Hover over image → trash icon appears
✅ Click trash → image deletes
✅ Drag image by grip handle → reorder
✅ Refresh page → images still there
✅ Can select and upload more images
```

---

## 📊 Data Flow Diagram

```
User selects images
        ↓
App validates: file type, count (max 10)
        ↓
For each image:
    ├─ Upload to: https://lllnnuqdcoockevelthu.supabase.co/storage/v1/object/public/mosque-slides/[path]
    ├─ Get public URL
    └─ Insert record in: public.mosque_slides table
        ├─ id: UUID (auto-generated)
        ├─ mosque_id: [user's mosque]
        ├─ image_url: [public URL]
        ├─ display_order: [sequential]
        └─ is_active: true
        ↓
React Query cache updated
        ↓
UI re-renders → images appear in grid
        ↓
User can reorder, delete, or add more
```

---

## 🔑 Key Code References

**Upload Handler:** [src/pages/SlidesPage.tsx](src/pages/SlidesPage.tsx#L100-L175)
- Line 100-175: handleUpload function
- Includes detailed console logging
- Full error handling
- Optimistic cache updates

**Delete Hook:** [src/hooks/use-mosque-data.ts](src/hooks/use-mosque-data.ts#L175-L184)
- Deletes from both storage and database
- Auto-invalidates cache

**Query Hook:** [src/hooks/use-mosque-data.ts](src/hooks/use-mosque-data.ts#L151-L166)
- useMosqueSlides fetches all slides
- Filtered by mosque_id
- Ordered by display_order

---

## 🎓 Understanding the Architecture

**Why Storage + Database?**
- **Storage:** Holds actual image files (fast, scalable)
- **Database:** Holds metadata (URLs, order, relationships)

**Why RLS?**
- Prevents unauthorized access
- Users can only access/modify their own mosque's slides
- Security best practice

**Why Cache?**
- Instant UI updates (optimistic)
- Prevents unnecessary refetches
- Better user experience

**Why Console Logging?**
- Tracks every step of upload
- Helps identify exactly where problems occur
- Makes debugging much faster

---

## 📞 Getting Help

**Provide this info when asking for help:**

1. **Exact error message** from console (F12)
2. **Screenshot** of error
3. **Step that fails** (upload, insert, display, etc.)
4. **What you've checked:**
   - [ ] Bucket exists?
   - [ ] Bucket is public?
   - [ ] Storage policies created?
   - [ ] Database table exists?
   - [ ] Database policies created?
5. **Credentials confirmed:**
   - [ ] VITE_SUPABASE_URL correct
   - [ ] VITE_SUPABASE_PUBLISHABLE_KEY correct

---

**Good luck! You've got this!** 🚀
