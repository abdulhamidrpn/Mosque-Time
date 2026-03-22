# FIX SUMMARY: Step-by-Step Actions

## Your Errors
1. ❌ Slide upload: "new row violates row-level security policy"
2. ❌ Mosque image upload: "bucket not found"

---

## IMMEDIATE ACTION (Do This Now)

### Action 1: Create Storage Buckets (2 min)
**Go to**: https://app.supabase.com

1. Click **Storage** (left sidebar)
2. Click **Create New Bucket** → Name: `mosque-slides` → **Uncheck Private** → Create
3. Click **Create New Bucket** → Name: `mosque-images` → **Uncheck Private** → Create

✅ **Done**: You now have 2 public buckets

### Action 2: Fix RLS Policies (1 min)
**Go to**: Supabase SQL Editor

1. Click **SQL Editor** (left sidebar)
2. Click **New Query**
3. Copy ALL text from this file: [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)
4. Paste into the editor
5. Click **Run** button

✅ **Done**: RLS policies are now correct

### Action 3: Add Storage RLS Policies (2 min)
**Go to**: Supabase Storage → Policies tab

**For mosque-slides bucket:**
- Click New Policy → SELECT → Expression: `true` → Save
- Click New Policy → INSERT → Expression: `auth.role() = 'authenticated'` → Save
- Click New Policy → DELETE → Expression: `auth.role() = 'authenticated'` → Save

**For mosque-images bucket:**
- Do the same 3 policies as above

✅ **Done**: Storage policies are configured

### Action 4: Verify Your Mosque (30 sec)
**Go to**: Supabase SQL Editor

Run this query:
```sql
SELECT 
  (SELECT auth.uid()) as your_id,
  (SELECT owner_uid FROM mosques LIMIT 1) as mosque_owner,
  COUNT(*) as mosque_count
FROM mosques;
```

**Check**:
- `mosque_count` should be ≥ 1 (you have at least 1 mosque)
- If `your_id` = `mosque_owner`: ✅ Perfect, all set
- If `your_id` ≠ `mosque_owner`: 
  - Go to **Profile** page in your app
  - Edit mosque name or city
  - Save
  - This updates the owner

### Action 5: Test Upload (1 min)
1. Open your app
2. Go to **Digital Signage** page
3. Click **Upload Slides**
4. Select 1-2 images
5. Check console (F12) for success logs

✅ **Success**: Images upload and appear in the list!

---

## Expected Results After Each Step

| Step | Expected Result |
|------|-----------------|
| Step 1 | Supabase Storage shows 2 buckets: mosque-slides, mosque-images |
| Step 2 | SQL query runs with "RLS Policies Created Successfully" message |
| Step 3 | Each bucket has 3 policies listed in the Policies tab |
| Step 4 | mosque_count ≥ 1, and your_id = mosque_owner (or you update it) |
| Step 5 | Console shows "Database insert successful" message, image appears in list |

---

## If Something Fails

### "RLS Policy" error still appears
→ Run this query to check:
```sql
SELECT policyname FROM pg_policies 
WHERE tablename = 'mosque_slides';
```
Should show 4 policies. If not, re-do Step 2.

### "bucket not found" still appears
→ Go to Supabase → Storage
→ Check that mosque-slides and mosque-images buckets are listed
→ If not, re-do Step 1

### "401 Unauthorized" error
→ Go to Storage → Policies
→ Check that each bucket has the 3 policies listed
→ If not, re-do Step 3

### Image uploads but doesn't appear
→ Refresh the page (F5)
→ Check console (F12 → Console tab) for "Database insert successful"

---

## Timeline
- **Step 1**: 2 minutes
- **Step 2**: 1 minute
- **Step 3**: 2 minutes
- **Step 4**: 30 seconds
- **Step 5**: 1 minute

**Total**: ~6-7 minutes for complete fix

---

## Reference Files
- [QUICK_FIX_5MIN.md](QUICK_FIX_5MIN.md) - Condensed version
- [COMPLETE_FIX_GUIDE.md](COMPLETE_FIX_GUIDE.md) - Detailed explanations
- [UPLOAD_ERRORS_EXPLAINED.md](UPLOAD_ERRORS_EXPLAINED.md) - Why these errors happen
- [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql) - SQL to run

---

## Success = When You See This
After Step 5, when you upload an image, you should see:
```
✓ Upload started - Mosque ID: abc-123-xyz, Max order: 0
✓ Processing file 1/1: photo.jpg, Size: 2456789, Type: image/jpeg
✓ Storage upload path: abc-123-xyz/1708245678-0.jpg
✓ Starting storage upload...
✓ Storage upload successful
✓ Public URL obtained: https://storage.supabase.co/object/public/mosque-slides/...
✓ Inserting into database...
✓ Database insert successful: {id: ..., mosque_id: ..., image_url: ...}
✓ Cache updated with new slides
```

Then the image appears in the **Digital Signage** grid immediately. ✅

**That's it! Both errors are fixed!**
