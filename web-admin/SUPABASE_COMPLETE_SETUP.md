# Complete Supabase Setup Guide for Mosque Admin Dashboard

**Project:** salatetime@gmail.com's Project  
**Project ID:** lllnnuqdcoockevelthu  
**URL:** https://lllnnuqdcoockevelthu.supabase.co

---

## 🚨 CRITICAL: Must Complete These Steps

### Step 1: Create Storage Bucket

**Via UI (Easiest):**
1. Go to https://app.supabase.com/project/lllnnuqdcoockevelthu/storage/buckets
2. Click **"New Bucket"** button
3. **Bucket name:** `mosque-slides` (exactly this)
4. **Public bucket:** Toggle **ON** (green)
5. Click **Create Bucket**

**Verify it worked:**
- You should see `mosque-slides` in the buckets list
- It should have a green "Public" badge

---

### Step 2: Enable Row Level Security (RLS) for Storage

1. Still in Storage, click the **mosque-slides** bucket
2. Click the **"Policies"** tab
3. Click **"New Policy"** 

**Create these 3 policies:**

#### Policy 1: Allow Public Read (Anyone can view images)
```sql
CREATE POLICY "Allow public read access to mosque-slides"
ON storage.objects 
FOR SELECT 
TO public 
USING (bucket_id = 'mosque-slides');
```

**In UI:**
- Policy name: `Allow public read`
- Target roles: `Public`
- Permission: SELECT
- Click "Create policy"

#### Policy 2: Allow Authenticated Users to Upload
```sql
CREATE POLICY "Allow authenticated users to upload slides"
ON storage.objects 
FOR INSERT 
TO authenticated 
WITH CHECK (
  bucket_id = 'mosque-slides' AND
  (auth.uid())::text = (storage.foldername(name))[1]
);
```

**In UI:**
- Policy name: `Allow authenticated upload`
- Target roles: `authenticated`
- Permission: INSERT
- Click "Create policy"

#### Policy 3: Allow Users to Delete Their Own Slides
```sql
CREATE POLICY "Allow users to delete their own slides"
ON storage.objects 
FOR DELETE 
TO authenticated 
USING (
  bucket_id = 'mosque-slides' AND
  (auth.uid())::text = (storage.foldername(name))[1]
);
```

**In UI:**
- Policy name: `Allow users delete own`
- Target roles: `authenticated`
- Permission: DELETE
- Click "Create policy"

---

### Step 3: Configure Database Table RLS

1. Go to https://app.supabase.com/project/lllnnuqdcoockevelthu/sql/new
2. Run these SQL queries one by one:

#### Query 1: Enable RLS on mosque_slides table
```sql
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;
```

#### Query 2: Allow users to select slides from their mosques
```sql
CREATE POLICY "Users can view slides from their mosques"
ON public.mosque_slides FOR SELECT
TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

#### Query 3: Allow users to insert slides for their mosques
```sql
CREATE POLICY "Users can insert slides for their mosques"
ON public.mosque_slides FOR INSERT
TO authenticated
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

#### Query 4: Allow users to update slides in their mosques
```sql
CREATE POLICY "Users can update slides in their mosques"
ON public.mosque_slides FOR UPDATE
TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

#### Query 5: Allow users to delete slides from their mosques
```sql
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

### Step 4: Verify Database Tables Exist

Run this query to check if `mosque_slides` table exists:

```sql
SELECT * FROM information_schema.tables 
WHERE table_schema = 'public' AND table_name = 'mosque_slides';
```

If it exists, run this to see its structure:

```sql
SELECT * FROM public.mosque_slides LIMIT 1;
```

If table doesn't exist, create it:

```sql
CREATE TABLE public.mosque_slides (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  mosque_id UUID NOT NULL REFERENCES public.mosques(id) ON DELETE CASCADE,
  image_url TEXT NOT NULL,
  display_order INTEGER NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Create index for faster queries
CREATE INDEX idx_mosque_slides_mosque_id ON public.mosque_slides(mosque_id);
CREATE INDEX idx_mosque_slides_display_order ON public.mosque_slides(display_order);
```

---

## 🧪 Testing Upload

### Before Testing:
1. ✅ Bucket `mosque-slides` exists and is public
2. ✅ RLS policies created on storage
3. ✅ RLS policies created on database table
4. ✅ User is authenticated
5. ✅ User has a mosque profile

### Test Steps:

1. **Open Browser Console:**
   - Press `F12`
   - Click **Console** tab
   - Keep it open

2. **Go to Slides page:**
   - Navigate to app's Slides page
   - You should see "Digital Signage" heading

3. **Upload a test image:**
   - Click "Upload Slides" button
   - Select 1 small image (JPG/PNG, <1MB)
   - Watch console for detailed logs

4. **Expected Console Output:**
```
Upload started - Mosque ID: [UUID] Max order: 0
Processing file 1/1: image.jpg Size: 123456 Type: image/jpeg
Storage upload path: [UUID]/1739886000000-0.jpg
Starting storage upload...
Storage upload successful, file: {path: ..., id: ...}
Public URL obtained: https://lllnnuqdcoockevelthu.supabase.co/storage/v1/object/public/mosque-slides/[path]
Inserting into database...
Database insert successful: {id: ..., mosque_id: ..., image_url: ..., display_order: 0, ...}
All files processed. Successful uploads: 1
Cache updated with new slides
```

5. **Verify Image Appears:**
   - Image should appear in grid below
   - Number should show: #1
   - Capacity should update: 1/10

---

## 🐛 Common Issues & Fixes

### Issue: "bucket not found" Error

**Cause:** `mosque-slides` bucket doesn't exist

**Fix:**
1. Go to https://app.supabase.com/project/lllnnuqdcoockevelthu/storage/buckets
2. Create bucket named `mosque-slides`
3. Make it **Public**

---

### Issue: "Permission denied" Error

**Cause:** RLS policies not set up correctly

**Fix:**
1. Check Storage → mosque-slides → Policies
2. Verify these 3 policies exist:
   - `Allow public read`
   - `Allow authenticated upload`
   - `Allow users delete own`
3. If missing, create them using SQL Editor

---

### Issue: Image Uploads but Doesn't Show

**Cause 1:** Database table RLS policies missing

**Fix:** Run the 5 SQL queries in "Step 3" above

**Cause 2:** Image URL wrong format

**Fix:** Check public URL format:
```
https://lllnnuqdcoockevelthu.supabase.co/storage/v1/object/public/mosque-slides/[path]
```

---

### Issue: Can Upload but Can't Delete

**Cause:** DELETE RLS policy not set up

**Fix:** Check database DELETE policy exists:
```sql
SELECT * FROM pg_policies WHERE tablename = 'mosque_slides' AND policyname LIKE '%delete%';
```

If empty, run:
```sql
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

## 📊 Verification Checklist

Use this checklist to ensure everything is set up:

```
Storage Setup:
☐ Bucket "mosque-slides" exists
☐ Bucket is Public
☐ RLS Policy: "Allow public read" exists
☐ RLS Policy: "Allow authenticated upload" exists
☐ RLS Policy: "Allow users delete own" exists

Database Setup:
☐ Table "mosque_slides" exists
☐ Table has columns: id, mosque_id, image_url, display_order, is_active, created_at
☐ RLS is enabled on "mosque_slides"
☐ RLS Policy: SELECT exists
☐ RLS Policy: INSERT exists
☐ RLS Policy: UPDATE exists
☐ RLS Policy: DELETE exists

Application Setup:
☐ .env has correct VITE_SUPABASE_URL
☐ .env has correct VITE_SUPABASE_PUBLISHABLE_KEY
☐ SlidesPage.tsx has upload handler with logging
☐ use-mosque-data.ts has useDeleteSlide hook
```

---

## 🔗 Quick Links

- **Supabase Dashboard:** https://app.supabase.com/project/lllnnuqdcoockevelthu
- **Storage Buckets:** https://app.supabase.com/project/lllnnuqdcoockevelthu/storage/buckets
- **SQL Editor:** https://app.supabase.com/project/lllnnuqdcoockevelthu/sql/new
- **Table Editor:** https://app.supabase.com/project/lllnnuqdcoockevelthu/editor/

---

## 💡 How It Works

1. **User selects images** from device
2. **Browser validates** file types (images only)
3. **Checks capacity** (max 10 slides)
4. **Uploads to storage** → `mosque-slides/[mosque-id]/[timestamp].jpg`
5. **Gets public URL** → stored in database
6. **Inserts record** in `mosque_slides` table
7. **Updates React Query cache** → images appear immediately
8. **User can drag to reorder** → updates `display_order`
9. **User can delete** → removes from storage and database

---

## 📞 If Still Not Working

1. **Check browser console (F12):**
   - Look for error messages
   - Share the exact error

2. **Check Network tab (F12):**
   - Look for failed requests (red X)
   - Check status codes (403, 404, 500, etc.)

3. **Check Supabase status:**
   - Go to https://status.supabase.com
   - Any service outages?

4. **Verify credentials in .env:**
   ```
   VITE_SUPABASE_URL="https://lllnnuqdcoockevelthu.supabase.co"
   VITE_SUPABASE_PUBLISHABLE_KEY="sb_publishable_WvtOpUuXcNmx8rr9HurMUg_Kb_rtKHf"
   ```

5. **Restart dev server:**
   ```bash
   # Kill current server (Ctrl+C)
   # Restart:
   npm run dev
   ```

---

**You're all set! Images should now upload, appear immediately, and be deletable.** ✅
