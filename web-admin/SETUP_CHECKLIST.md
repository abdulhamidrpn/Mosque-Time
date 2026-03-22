# 📋 Image Upload Setup - Visual Checklist

## 🎯 3-Step Setup (5 minutes)

### Step 1: Create Storage Bucket ✅

```
https://app.supabase.com/project/lllnnuqdcoockevelthu/storage/buckets
                            ↓
                    Click "New Bucket"
                            ↓
                    Name: mosque-slides
                    Public: ✓ (toggle ON)
                            ↓
                    Click "Create Bucket"
                            ↓
        ✅ Should see "mosque-slides" in list
```

### Step 2: Create Storage RLS Policies ✅

```
Storage → mosque-slides → Policies
                            ↓
        Click "New Policy" (do this 3 times)
                            ↓
    Policy 1: Allow public read
    Policy 2: Allow authenticated upload
    Policy 3: Allow users delete own
                            ↓
        ✅ Should see 3 policies listed
```

**SQL for Step 2** (if using SQL Editor instead):
```sql
-- Copy & run in SQL Editor one at a time:

-- Policy 1
CREATE POLICY "Allow public read access to mosque-slides"
ON storage.objects FOR SELECT TO public 
USING (bucket_id = 'mosque-slides');

-- Policy 2
CREATE POLICY "Allow authenticated users to upload slides"
ON storage.objects FOR INSERT TO authenticated 
WITH CHECK (bucket_id = 'mosque-slides' AND (auth.uid())::text = (storage.foldername(name))[1]);

-- Policy 3
CREATE POLICY "Allow users to delete their own slides"
ON storage.objects FOR DELETE TO authenticated 
USING (bucket_id = 'mosque-slides' AND (auth.uid())::text = (storage.foldername(name))[1]);
```

### Step 3: Create Database RLS Policies ✅

```
SQL Editor → New Query
                            ↓
        Copy & run each query (5 total)
                            ↓
    Query 1: Enable RLS on table
    Query 2: SELECT policy
    Query 3: INSERT policy
    Query 4: UPDATE policy
    Query 5: DELETE policy
                            ↓
        ✅ Should see 5 policies on table
```

**SQL for Step 3** (Copy & run in SQL Editor):
```sql
-- Query 1: Enable RLS
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- Query 2: SELECT policy
CREATE POLICY "Users can view slides from their mosques"
ON public.mosque_slides FOR SELECT TO authenticated
USING (mosque_id IN (SELECT id FROM public.mosques WHERE owner_uid = auth.uid()));

-- Query 3: INSERT policy
CREATE POLICY "Users can insert slides for their mosques"
ON public.mosque_slides FOR INSERT TO authenticated
WITH CHECK (mosque_id IN (SELECT id FROM public.mosques WHERE owner_uid = auth.uid()));

-- Query 4: UPDATE policy
CREATE POLICY "Users can update slides in their mosques"
ON public.mosque_slides FOR UPDATE TO authenticated
USING (mosque_id IN (SELECT id FROM public.mosques WHERE owner_uid = auth.uid()));

-- Query 5: DELETE policy
CREATE POLICY "Users can delete slides from their mosques"
ON public.mosque_slides FOR DELETE TO authenticated
USING (mosque_id IN (SELECT id FROM public.mosques WHERE owner_uid = auth.uid()));
```

---

## 🧪 Test Your Setup

```
1. Open DevTools: F12
2. Click "Console" tab
3. Go to Slides page
4. Click "Upload Slides" button
5. Select 1 small image
6. Watch console

EXPECTED OUTPUT:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Upload started - Mosque ID: xxxxxxxx-xxxx... Max order: 0
Processing file 1/1: image.jpg Size: 123456 Type: image/jpeg
Storage upload path: xxxxxxxx-xxxx.../1739886000000-0.jpg
Starting storage upload...
Storage upload successful, file: {path: ..., id: ...}
Public URL obtained: https://lllnnuqdcoockevelthu.supabase.co/storage/...
Inserting into database...
Database insert successful: {id: ..., mosque_id: ..., image_url: ..., display_order: 0}
All files processed. Successful uploads: 1
Cache updated with new slides
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

THEN:
✅ Green toast appears: "Slides Uploaded - 1 slide(s) uploaded successfully."
✅ Image appears in grid
✅ Image number shows: #1
✅ Capacity updates: 1/10
```

---

## ✅ Verification Checklist

### After Completing All 3 Steps, Verify:

```
STORAGE BUCKET:
☐ Bucket "mosque-slides" exists
☐ Bucket is Public (green badge)
☐ Bucket visible in Storage Buckets list

STORAGE POLICIES:
☐ "Allow public read" exists
☐ "Allow authenticated upload" exists  
☐ "Allow users delete own" exists
☐ All 3 visible in Policies tab

DATABASE TABLE:
☐ Table "mosque_slides" exists
☐ Table visible in Table Editor
☐ Has columns: id, mosque_id, image_url, display_order, is_active, created_at

DATABASE POLICIES:
☐ SELECT policy exists
☐ INSERT policy exists
☐ UPDATE policy exists
☐ DELETE policy exists
☐ All 5 visible in Table → Policies tab

APPLICATION:
☐ .env has correct VITE_SUPABASE_URL
☐ .env has correct VITE_SUPABASE_PUBLISHABLE_KEY
☐ User is logged in
☐ User has mosque profile (completed onboarding)
```

---

## 🚀 After Successful Setup

### You Can Now:

```
✅ Upload images
   → Click "Upload Slides"
   → Select images
   → Watch progress bar
   → Images appear immediately

✅ Reorder images
   → Hover over image
   → Grab grip handle (⋮⋮)
   → Drag to new position
   → Order saves automatically

✅ Delete images
   → Hover over image
   → Click trash icon (🗑️)
   → Image deleted from storage & database
   → List refreshes immediately

✅ Check capacity
   → See progress bar: "3/10"
   → Upload button disabled at 10 slides
   → Can delete to make room for more

✅ Persist data
   → Refresh page (F5)
   → Images still there
   → Order preserved
```

---

## 🐛 Troubleshooting Map

```
IF YOU SEE THIS ERROR          PROBLEM                   SOLUTION
─────────────────────────────  ─────────────────────────  ──────────────────────
"bucket not found"             Bucket doesn't exist      → Create "mosque-slides"
                                                         → Make it Public

"permission denied" (upload)   Storage policies missing  → Create 3 storage policies

"Insert failed" (after upload) DB policies missing       → Create 5 database policies

"Table doesn't exist"          Table not created         → Run CREATE TABLE query

Image uploads but doesn't show Cache/render issue        → Refresh page
                                                         → Check SQL if DB updated

Slow uploads                   Large files               → Use smaller images
                                                         → Check internet speed
```

---

## 📱 Upload Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│ User Clicks "Upload Slides" Button                          │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────v────────────────────────────────────────┐
│ Browser Opens File Picker                                   │
│ User Selects 1-10 Images                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────v────────────────────────────────────────┐
│ App Validates:                                              │
│ ✓ File types (images only)                                 │
│ ✓ Count (max 10)                                           │
│ ✓ User logged in                                           │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────v────────────────────────────────────────┐
│ For Each Image:                                             │
│                                                              │
│ 1. Upload to Storage                                        │
│    → mosque-slides/[mosque-id]/[timestamp].jpg             │
│    → RLS policy: "Allow authenticated upload"              │
│                                                              │
│ 2. Get Public URL                                           │
│    → https://lllnnuqdcoockevelthu.supabase.co/...          │
│                                                              │
│ 3. Insert into Database                                     │
│    → Table: public.mosque_slides                            │
│    → RLS policy: "Allow insert for own mosque"             │
│    → Store: id, mosque_id, image_url, display_order        │
│                                                              │
│ 4. Update Progress                                          │
│    → Show: 50% (if 2 images)                               │
│                                                              │
│ 5. Track Success                                            │
│    → Add to uploadedSlides array                            │
│                                                              │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────v────────────────────────────────────────┐
│ Update React Query Cache                                    │
│ setQueryData: new slides + uploaded slides                  │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────v────────────────────────────────────────┐
│ Component Re-Renders                                        │
│ ✓ Images appear in grid                                     │
│ ✓ Numbers show: #1, #2, #3                                │
│ ✓ Capacity updates: 3/10                                   │
│ ✓ Toast: "3 slides uploaded successfully"                 │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────v────────────────────────────────────────┐
│ Invalidate Cache (Sync with Server)                         │
│ Refetch latest slides from database                         │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────v────────────────────────────────────────┐
│ Done! User Can Now:                                         │
│ • Drag to reorder                                           │
│ • Delete individual slides                                  │
│ • Upload more images                                        │
│ • Refresh page and data persists                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 📞 Quick Help

**Something not working?**

1. **First:** Check all ✅ boxes in Verification Checklist above
2. **Second:** Read QUICK_FIX.md for your specific error
3. **Third:** Follow DEBUGGING_GUIDE.md step-by-step
4. **Fourth:** Check console (F12) for exact error message

**Still stuck?** Open SUPABASE_COMPLETE_SETUP.md and follow every single step carefully.

---

## 📊 Success Indicators

After setup is complete, you should see:

| Indicator | What It Means |
|-----------|--------------|
| 3 storage policies | ✅ Upload/delete working |
| 5 database policies | ✅ Database access controlled |
| Console logging | ✅ Can track upload progress |
| Image in grid | ✅ Upload + insert working |
| Capacity updating | ✅ React Query cache working |
| Drag reordering | ✅ Database updates working |
| Delete removing image | ✅ Storage + DB delete working |
| Data persists on refresh | ✅ Database queries working |

---

**Ready? Let's go! Complete the 3 steps above and you're done.** 🚀
