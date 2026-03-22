# Quick Troubleshooting: Image Upload Not Working

## 🚀 First Steps (Do These Now)

### 1. Check Bucket Exists
```
Go to: https://app.supabase.com/project/lllnnuqdcoockevelthu/storage/buckets
Look for: "mosque-slides" bucket
Status: Should say "Public" (green badge)

If NOT there:
→ Click "New Bucket"
→ Name: mosque-slides
→ Make it Public
→ Create
```

### 2. Check Console for Errors
```
In your app:
1. Press F12 (Open DevTools)
2. Click "Console" tab
3. Try uploading an image
4. Look for error messages starting with:
   - "bucket not found"
   - "permission denied"
   - "Network error"
   - etc.

Copy the exact error message!
```

### 3. Check Network Tab
```
1. Press F12 (DevTools)
2. Click "Network" tab
3. Try uploading
4. Look for requests to "supabase.co"
5. Check if any have status 403, 404, or 500
6. Click on failed request to see details
```

---

## 📋 Step-by-Step Verification

### Step 1: Bucket Exists & Is Public
```
❌ FAILED: "bucket not found" error
→ Go to Storage Buckets
→ Create "mosque-slides"
→ Set to Public
→ Click Create

✅ PASSED: Bucket visible in list
→ Continue to Step 2
```

### Step 2: RLS Policies Exist
```
1. Go to Storage → mosque-slides
2. Click "Policies" tab
3. Should see these:
   ✅ Allow public read
   ✅ Allow authenticated upload
   ✅ Allow users delete own

❌ MISSING ANY?
→ Go to SQL Editor
→ Copy/paste policies from SUPABASE_COMPLETE_SETUP.md
→ Run each one
```

### Step 3: Database Table Exists
```
1. Go to SQL Editor
2. Run:
   SELECT * FROM public.mosque_slides LIMIT 1;

✅ RESULT: See table with columns
→ Continue to Step 4

❌ ERROR: Table doesn't exist
→ Copy SQL from SUPABASE_COMPLETE_SETUP.md Step 4
→ Run entire CREATE TABLE query
```

### Step 4: Database RLS Policies
```
1. Go to Table Editor → mosque_slides
2. Click "Policies" tab
3. Should see 5 policies:
   ✅ SELECT policy
   ✅ INSERT policy
   ✅ UPDATE policy
   ✅ DELETE policy

❌ MISSING ANY?
→ Go to SQL Editor
→ Run the missing policies from Step 3 in guide
```

---

## 🧪 Upload Test

```
1. Open Console (F12 → Console)
2. Keep it visible
3. Go to Slides page
4. Click "Upload Slides"
5. Select 1 small image

WATCH CONSOLE - You should see:
✅ "Upload started - Mosque ID: ..."
✅ "Processing file 1/1: imagename.jpg"
✅ "Storage upload path: ..."
✅ "Starting storage upload..."
✅ "Storage upload successful"
✅ "Public URL obtained: https://..."
✅ "Inserting into database..."
✅ "Database insert successful"
✅ "All files processed. Successful uploads: 1"

IF YOU DON'T SEE THESE:
→ Copy the error message
→ Go to SUPABASE_COMPLETE_SETUP.md
→ Find "Common Issues" section
→ Match your error
→ Follow the fix
```

---

## 🔍 Common Error Messages & Fixes

### Error: "bucket not found"
```
Problem: mosque-slides bucket doesn't exist
Solution: 
  1. Go to Storage Buckets
  2. Create bucket "mosque-slides"
  3. Make it Public
  4. Try upload again
```

### Error: "permission denied" or "403"
```
Problem: RLS policies not set up
Solution:
  1. Go to Storage → mosque-slides → Policies
  2. Check if these exist:
     - "Allow public read"
     - "Allow authenticated upload"
  3. If missing, create them
  4. Try upload again
```

### Error: "Table doesn't exist"
```
Problem: mosque_slides table not created
Solution:
  1. Go to SQL Editor
  2. Copy CREATE TABLE query from guide
  3. Run it
  4. Try upload again
```

### Error: "Insert failed" or "permission denied" on database
```
Problem: RLS policies on table not set
Solution:
  1. Go to SQL Editor
  2. Run all 5 RLS policies from Step 3
  3. Go to Table Editor → mosque_slides → Policies
  4. Verify all 5 policies exist
  5. Try upload again
```

### Image Uploads but Doesn't Show
```
Problem: Database updated but cache not refreshed
Solution:
  1. Refresh page (F5)
  2. Image should appear
  3. If not, check:
     - Is database actually updated? (SQL Editor)
     - Is public URL correct format?
     - Is user logged in?
```

---

## ✅ Success Indicators

After completing all steps, you should see:

1. **Console output** showing successful upload
2. **Image appears** in grid immediately
3. **Capacity updates** (e.g., "1/10")
4. **Can reorder** by dragging
5. **Can delete** by clicking trash icon
6. **Page refresh** shows image still there

---

## 🆘 Still Not Working?

**Collect this information:**

1. **Exact error message** (from console)
2. **Screenshot** of console error
3. **Supabase credentials confirmed:**
   - URL: https://lllnnuqdcoockevelthu.supabase.co ✓
   - Key: sb_publishable_WvtOpUuXcNmx8rr9HurMUg_Kb_rtKHf ✓
4. **Bucket status:**
   - [ ] Bucket "mosque-slides" exists
   - [ ] Bucket is Public
   - [ ] Can see in Storage Buckets
5. **Table status:**
   - [ ] Table "mosque_slides" exists
   - [ ] Visible in Table Editor
   - [ ] Has required columns
6. **Policies status:**
   - [ ] Storage policies exist (3 total)
   - [ ] Database policies exist (5 total)

**Share this info for faster debugging!**

---

## 📚 Full Documentation

See **SUPABASE_COMPLETE_SETUP.md** for:
- Complete setup instructions with UI screenshots
- All SQL queries
- Policy details
- How upload flow works
- Performance notes
