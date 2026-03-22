# MASTER CHECKLIST: Fix Both Upload Errors

## Current Errors
```
❌ Error 1: "new row violates row-level security policy" 
   → When uploading slides to Digital Signage

❌ Error 2: "bucket not found" 
   → When uploading mosque images to Profile
```

## Root Causes
```
❌ Missing: Storage buckets don't exist
   - mosque-slides bucket not created
   - mosque-images bucket not created

❌ Wrong: RLS database policies use incorrect syntax
   - Using USING clause for INSERT (should use WITH CHECK)
   
❌ Missing: Storage RLS policies don't exist
   - mosque-slides bucket policies not created
   - mosque-images bucket policies not created

❌ Possibly Wrong: Mosque owner_uid may not match auth.uid()
```

---

# STEP-BY-STEP FIX (6 MINUTES)

## ☐ STEP 1: Create Storage Buckets (2 min)

**URL**: https://app.supabase.com → Your Project → Storage

### ☐ 1.1 Create "mosque-slides" Bucket
- [ ] Click "Create New Bucket"
- [ ] Name: `mosque-slides`
- [ ] **Uncheck** "Private bucket"
- [ ] Click "Create Bucket"

### ☐ 1.2 Create "mosque-images" Bucket
- [ ] Click "Create New Bucket"
- [ ] Name: `mosque-images`
- [ ] **Uncheck** "Private bucket"
- [ ] Click "Create Bucket"

**Expected Result**: public)

---

## ☐ STEP 2: Fix Database RLS Policies (1 min)

**URL**: https://app.supabase.com → Your Project → SQL Editor

### ☐ 2.1 Open SQL Editor
- [ ] Click "SQL Editor" (left sidebar)
- [ ] Click "New Query"

### ☐ 2.2 Copy & Paste SQL
- [ ] Open file: [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)
- [ ] Copy all content
- [ ] Paste into SQL Editor
- [ ] Click "Run" button

**Expected Result**: No errors, message says "RLS Policies Created Successfully"

**Expected Output**:
```
CREATE POLICY
CREATE POLICY
CREATE POLICY
CREATE POLICY
SELECT
```

---

## ☐ STEP 3: Create Storage RLS Policies (2 min)

**URL**: https://app.supabase.com → Your Project → Storage → Policies

### ☐ 3.1 For "mosque-slides" Bucket

#### Policy 1: SELECT (Allow everyone to view)
- [ ] Click "New Policy" on mosque-slides
- [ ] Choose: **SELECT**
- [ ] Expression: `true`
- [ ] Click "Save"

#### Policy 2: INSERT (Allow authenticated upload)
- [ ] Click "New Policy" on mosque-slides
- [ ] Choose: **INSERT**
- [ ] Expression: `auth.role() = 'authenticated'`
- [ ] Click "Save"

#### Policy 3: DELETE (Allow authenticated delete)
- [ ] Click "New Policy" on mosque-slides
- [ ] Choose: **DELETE**
- [ ] Expression: `auth.role() = 'authenticated'`
- [ ] Click "Save"

### ☐ 3.2 For "mosque-images" Bucket

Repeat the same 3 policies for mosque-images:
- [ ] Policy 1: SELECT with `true`
- [ ] Policy 2: INSERT with `auth.role() = 'authenticated'`
- [ ] Policy 3: DELETE with `auth.role() = 'authenticated'`

**Expected Result**: Each bucket shows 3 policies in the Policies tab

---

## ☐ STEP 4: Verify Your Mosque Data (30 sec)

**URL**: https://app.supabase.com → Your Project → SQL Editor

### ☐ 4.1 Run Verification Query
Copy and paste this:
```sql
SELECT 
  (SELECT auth.uid()) as your_user_id,
  (SELECT owner_uid FROM public.mosques LIMIT 1) as mosque_owner,
  (SELECT COUNT(*) FROM public.mosques) as mosque_count,
  (SELECT auth.uid()) = (SELECT owner_uid FROM public.mosques LIMIT 1) as user_is_owner;
```

Click "Run"

### ☐ 4.2 Check Results

**Expected**:
```
your_user_id: abc-123-def (your Supabase user ID)
mosque_owner: abc-123-def (same as above!)
mosque_count: 1 or more
user_is_owner: true ✅
```

**If user_is_owner = false**:
- [ ] Go to your app → **Profile** page
- [ ] Edit any field (e.g., add city name)
- [ ] Click Save
- [ ] This updates the owner_uid to your current user ID
- [ ] Re-run the query to verify

**If mosque_count = 0**:
- [ ] You haven't created a mosque yet
- [ ] Go through **Onboarding** in your app to create one
- [ ] Then re-run the query

---

## ☐ STEP 5: Test Upload (1 min)

### ☐ 5.1 Open Your App
- [ ] Open http://localhost:8080 (or your dev URL)
- [ ] Log in if needed
- [ ] Navigate to **Digital Signage** page (or any page with slides)

### ☐ 5.2 Upload an Image
- [ ] Click "Upload Slides" button
- [ ] Select 1-2 image files (jpg, png, etc.)
- [ ] Wait for upload to complete

### ☐ 5.3 Check Success
- [ ] Open browser console (F12 → Console tab)
- [ ] Look for logs like:
  ```
  ✓ Upload started - Mosque ID: xyz
  ✓ Storage upload successful
  ✓ Database insert successful
  ✓ Cache updated
  ```
- [ ] Images appear in the Digital Signage grid
- [ ] Toast notification says "Slides Uploaded"

**Expected Result**: ✅ Images upload without errors and appear in the list

---

## ✅ COMPLETION CHECKLIST

All steps done = All errors fixed!

- [ ] Step 1.1: mosque-slides bucket created (public)
- [ ] Step 1.2: mosque-images bucket created (public)
- [ ] Step 2.1: SQL Editor opened
- [ ] Step 2.2: RLS policy SQL ran successfully
- [ ] Step 3.1: 3 policies created for mosque-slides
- [ ] Step 3.2: 3 policies created for mosque-images
- [ ] Step 4.1: Verification query ran
- [ ] Step 4.2: user_is_owner = true (or updated in Profile)
- [ ] Step 5.1: App opened
- [ ] Step 5.2: Selected image files
- [ ] Step 5.3: Images uploaded successfully

**When all boxes are checked**: ✅ Both errors are FIXED!

---

## TROUBLESHOOTING

### ❌ Step 2: SQL Query Failed
**Problem**: Got error when running RLS policy SQL
**Solution**:
- [ ] Check that `public.mosque_slides` table exists:
  ```sql
  SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'mosque_slides');
  ```
- [ ] If result = false: Table doesn't exist
  - Run [FINAL_TABLE_SETUP.sql](FINAL_TABLE_SETUP.sql) first to recreate it
- [ ] If result = true: Table exists
  - Re-run [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)

### ❌ Step 3: Policy Creation Failed
**Problem**: Error when creating storage policies
**Solution**:
- [ ] Make sure bucket exists:
  ```sql
  SELECT name FROM storage.buckets WHERE name IN ('mosque-slides', 'mosque-images');
  ```
- [ ] If no results: Go back to Step 1
- [ ] If buckets exist: Try creating policies again

### ❌ Step 4: user_is_owner = false
**Problem**: Your user doesn't own any mosque
**Solution**:
- [ ] Option A: Create mosque via onboarding
  - Go to app → Onboarding page
  - Fill in mosque details
  - Submit
- [ ] Option B: Update existing mosque
  - Go to app → Profile page
  - Edit any field
  - Save
  - This updates owner_uid to your auth.uid()

### ❌ Step 5: "bucket not found" Error
**Problem**: Still getting bucket not found error
**Solution**:
- [ ] Check buckets exist:
  ```sql
  SELECT name, public FROM storage.buckets 
  WHERE name IN ('mosque-slides', 'mosque-images');
  ```
- [ ] If no results: Go back to Step 1
- [ ] If results show public = false: 
  - Go to Storage → mosque-slides → Settings
  - Toggle "Make bucket public" ON
  - Do same for mosque-images

### ❌ Step 5: "RLS policy" Error Still Appears
**Problem**: Still getting RLS policy error
**Solution**:
- [ ] Run this to verify policies exist:
  ```sql
  SELECT policyname FROM pg_policies 
  WHERE tablename = 'mosque_slides' 
  ORDER BY policyname;
  ```
- [ ] Should show 4 policies
- [ ] If fewer than 4: Re-run [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)
- [ ] If 4 policies but still error: Check Step 4 (user_is_owner)

### ❌ Step 5: Image Uploads but Doesn't Appear
**Problem**: Upload succeeds but image not in list
**Solution**:
- [ ] Check console for "Database insert successful" log
- [ ] If you see it: Just refresh page (F5)
- [ ] If you don't see it: Check for database error in console

---

## REFERENCE FILES

| File | Purpose | When to Use |
|------|---------|-----------|
| [START_HERE_FIX_GUIDE.md](START_HERE_FIX_GUIDE.md) | This checklist but shorter | Quick reference |
| [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql) | SQL for Step 2 | Copy-paste into SQL Editor |
| [QUICK_FIX_5MIN.md](QUICK_FIX_5MIN.md) | 5-minute condensed version | Super quick version |
| [COMPLETE_FIX_GUIDE.md](COMPLETE_FIX_GUIDE.md) | Detailed 7-step guide | In-depth explanations |
| [UPLOAD_ERRORS_EXPLAINED.md](UPLOAD_ERRORS_EXPLAINED.md) | Why errors happen | Understanding root causes |
| [CODE_REVIEW_UPLOAD.md](CODE_REVIEW_UPLOAD.md) | Your code is correct | Verification that code is ready |
| [FINAL_TABLE_SETUP.sql](FINAL_TABLE_SETUP.sql) | Recreate table from scratch | If table needs to be recreated |

---

## SUCCESS INDICATORS

✅ **You're Done When You See**:

1. **In Supabase Dashboard**:
   - 2 public buckets: mosque-slides, mosque-images
   - 4 RLS policies on mosque_slides table
   - 3 policies on each storage bucket

2. **In Your Database**:
   - user_is_owner = true (verification query)

3. **In Your App**:
   - Console shows "Database insert successful"
   - Image appears in Digital Signage grid
   - Toast says "Slides Uploaded"

4. **No Errors**:
   - ❌ No "RLS policy" errors
   - ❌ No "bucket not found" errors
   - ❌ No "401 Unauthorized" errors

---

## Timeline
- Step 1: 2 minutes
- Step 2: 1 minute
- Step 3: 2 minutes
- Step 4: 30 seconds
- Step 5: 1 minute
- **Total: 6-7 minutes**

---

**Ready to fix? Start with STEP 1 above! ⬆️**
