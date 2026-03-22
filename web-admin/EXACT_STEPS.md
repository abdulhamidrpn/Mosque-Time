# Action Plan: Exact Steps to Take Right Now

## Error Messages You're Getting
```
Error 1: new row violates row-level security policy
  ↓ Happens when uploading slides

Error 2: bucket not found
  ↓ Happens when uploading mosque images
```

---

## STEP-BY-STEP SOLUTION (6 Minutes Total)

### STEP 1️⃣: CREATE STORAGE BUCKETS (2 minutes)

**Action**: Create 2 public buckets in Supabase

1. Go to: https://app.supabase.com
2. Log in with your Supabase account
3. Select your project: `lllnnuqdcoockevelthu`
4. Click **Storage** (left sidebar)
5. Click **Create New Bucket**
   - Type name: `mosque-slides`
   - **UNCHECK** the box "Private bucket"
   - Click **Create Bucket**
6. Click **Create New Bucket** again
   - Type name: `mosque-images`
   - **UNCHECK** the box "Private bucket"
   - Click **Create Bucket**

**Result**: You see 2 buckets in the list: mosque-slides, mosque-images ✅

---

### STEP 2️⃣: FIX RLS POLICIES (1 minute)

**Action**: Run SQL to fix database policies

1. In Supabase, click **SQL Editor** (left sidebar)
2. Click **New Query**
3. Open this file: [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)
4. Copy ALL the content (select all, Ctrl+A, Ctrl+C)
5. Go back to Supabase SQL Editor
6. Paste the SQL (Ctrl+V)
7. Click **Run** button (top right)

**Result**: You see success messages, no errors ✅

---

### STEP 3️⃣: ADD STORAGE POLICIES (2 minutes)

**Action**: Create RLS policies for each bucket

**For mosque-slides bucket:**

1. Click **Storage** → **Policies** tab
2. Find "mosque-slides" bucket row
3. Click the three dots → **New Policy**
4. Choose **SELECT**
5. In the Expression field, paste: `true`
6. Click **Save**
7. Repeat: Click **New Policy** → **INSERT** → Expression: `auth.role() = 'authenticated'` → Save
8. Repeat: Click **New Policy** → **DELETE** → Expression: `auth.role() = 'authenticated'` → Save

**For mosque-images bucket:**

Do the same 3 policies again:
1. **SELECT**: `true`
2. **INSERT**: `auth.role() = 'authenticated'`
3. **DELETE**: `auth.role() = 'authenticated'`

**Result**: Each bucket has 3 policies ✅

---

### STEP 4️⃣: VERIFY YOUR DATA (30 seconds)

**Action**: Check that you own the mosque

1. Click **SQL Editor** (left sidebar)
2. Click **New Query**
3. Paste this:
```sql
SELECT 
  (SELECT auth.uid()) as your_id,
  (SELECT owner_uid FROM mosques LIMIT 1) as mosque_owner,
  (SELECT COUNT(*) FROM mosques) as mosque_count;
```
4. Click **Run**

**Expected Result**:
- `mosque_count` = 1 or more
- `your_id` = `mosque_owner` (they're the same!) ✅

**If they're NOT the same**:
- Open your app
- Go to **Profile** page
- Edit the mosque name or city (change something)
- Click **Save**
- This updates the owner
- Re-run the query to verify

---

### STEP 5️⃣: TEST UPLOAD (1 minute)

**Action**: Upload an image and verify it works

1. Open your app (http://localhost:8080 or your dev URL)
2. Go to **Digital Signage** page
3. Click **Upload Slides** button
4. Select 1-2 image files (jpg, png, etc.)
5. Wait for upload to complete

**Expected**:
- ✅ Console shows "Database insert successful" (F12 to open console)
- ✅ Image appears in the grid
- ✅ Toast notification says "Slides Uploaded"
- ✅ No error messages

**If it works**: ✅ **YOU'RE DONE!** Both errors are fixed!

---

## 🎯 Summary: What You Just Did

| Step | What You Did | Why |
|------|--------------|-----|
| 1 | Created 2 public buckets | Fixes "bucket not found" error |
| 2 | Fixed RLS database policies | Fixes "RLS policy" error on database insert |
| 3 | Added storage RLS policies | Allows authenticated users to upload |
| 4 | Verified you own the mosque | Ensures RLS policies let YOUR uploads through |
| 5 | Tested upload | Confirms everything works |

---

## ✅ You're Done When...

You should be able to:
- ✅ Click "Upload Slides"
- ✅ Select image file
- ✅ See "Uploading..." progress
- ✅ See image appear in grid
- ✅ See "Slides Uploaded" toast
- ✅ No error messages

---

## ❌ If Something Goes Wrong

### Error Still Says "bucket not found"
→ Go back to Step 1
→ Make sure buckets are in the list
→ Make sure both are marked as PUBLIC (not private)

### Error Still Says "RLS policy"
→ Go back to Step 4
→ Run the verification query
→ If `your_id` ≠ `mosque_owner`:
   - Go to Profile page in your app
   - Update mosque info
   - Save
   - Re-run query

### "401 Unauthorized" error
→ Go back to Step 3
→ Make sure each bucket has 3 policies
→ Make sure INSERT policy uses `auth.role() = 'authenticated'`

### Upload succeeds but image doesn't appear
→ Refresh page (F5)
→ Check console (F12) for errors

---

## 📞 If You Get Stuck

Read one of these in order:

1. **[QUICK_FIX_5MIN.md](QUICK_FIX_5MIN.md)** - Another perspective
2. **[COMPLETE_FIX_GUIDE.md](COMPLETE_FIX_GUIDE.md)** - Detailed guide
3. **[MASTER_CHECKLIST.md](MASTER_CHECKLIST.md)** - With troubleshooting

---

## 🎓 If You Want to Understand What You Did

Read these (30 minutes):
1. [UPLOAD_ERRORS_EXPLAINED.md](UPLOAD_ERRORS_EXPLAINED.md) - Why errors happen
2. [VISUAL_ARCHITECTURE.md](VISUAL_ARCHITECTURE.md) - How it all connects
3. [CODE_REVIEW_UPLOAD.md](CODE_REVIEW_UPLOAD.md) - Your code is correct!

---

## 🚀 That's It!

You now have:
- ✅ 2 storage buckets created
- ✅ RLS database policies fixed
- ✅ Storage RLS policies created
- ✅ Data verified
- ✅ Upload tested

**Both errors should be gone!** 🎉

If you encounter any issues, the documentation has comprehensive troubleshooting guides!
