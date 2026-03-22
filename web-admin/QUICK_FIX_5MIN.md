# Quick Start: Fix Both Upload Errors (5 Minutes)

## Problem
- ❌ "new row violates row-level security policy" when uploading slides
- ❌ "bucket not found" when uploading mosque images

## Solution (5-Step Fix)

### STEP 1: Create Storage Buckets (2 minutes)
1. Go to https://app.supabase.com → your project
2. Click **Storage** (left sidebar)
3. Click **Create New Bucket**
   - Name: `mosque-slides` → Uncheck "Private bucket" → Create
   - Name: `mosque-images` → Uncheck "Private bucket" → Create
4. Now you have 2 public buckets ✅

### STEP 2: Fix RLS Database Policies (1 minute)
1. Click **SQL Editor** (left sidebar)
2. Click **New Query**
3. Copy ALL SQL from this file: [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)
4. Paste into Supabase SQL Editor
5. Click **Run** button
6. Wait for success message ✅

### STEP 3: Add Storage RLS Policies (2 minutes)
1. Click **Storage** → **Policies** tab
2. For **mosque-slides** bucket, add 3 policies:
   - **SELECT** policy: Expression = `true`
   - **INSERT** policy: Expression = `auth.role() = 'authenticated'`
   - **DELETE** policy: Expression = `auth.role() = 'authenticated'`
3. For **mosque-images** bucket, add the same 3 policies
4. Done ✅

### STEP 4: Verify Data (30 seconds)
Run this in SQL Editor to confirm your mosque has the correct owner:

```sql
SELECT auth.uid() as your_user_id, 
       (SELECT owner_uid FROM public.mosques LIMIT 1) as mosque_owner,
       (SELECT auth.uid()) = (SELECT owner_uid FROM public.mosques LIMIT 1) as is_owner;
```

**Expected**: `is_owner` = `true`

If `is_owner` = `false`:
- You haven't created a mosque yet
- Go to **Profile** page and update mosque info
- OR go through **Onboarding** to create a new mosque

### STEP 5: Test Upload (1 minute)
1. Refresh your app (F5)
2. Go to **Digital Signage** page
3. Click **Upload Slides**
4. Select 1-2 images
5. Wait for success toast ✅

**Done!** Both errors should be fixed.

---

## Troubleshooting

### Still Getting "RLS Policy" Error?
1. Run this query to check your user owns the mosque:
```sql
SELECT COUNT(*) FROM public.mosques 
WHERE owner_uid = auth.uid();
```
- If result = `0`: Go to Profile page and update mosque info
- If result ≥ `1`: Policies are correctly set

### Still Getting "Bucket Not Found"?
1. Check buckets exist:
```sql
SELECT name, public FROM storage.buckets 
WHERE name IN ('mosque-slides', 'mosque-images');
```
- If no results: Go back to Step 1 and create buckets
- If results show `public = false`: Go to Storage, click bucket → Settings → Toggle public ON

### Images Upload but Don't Appear?
1. Check browser console (F12 → Console tab)
2. Look for "Database insert successful" log
3. If you see it: Just refresh the page (F5)

---

## File References

| File | Purpose |
|------|---------|
| [COMPLETE_FIX_GUIDE.md](COMPLETE_FIX_GUIDE.md) | Detailed 7-step guide with explanations |
| [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql) | Copy-paste SQL for RLS policies |
| [FINAL_TABLE_SETUP.sql](FINAL_TABLE_SETUP.sql) | If you need to recreate the table |

---

## Success Checklist
- [ ] Created mosque-slides bucket (public)
- [ ] Created mosque-images bucket (public)
- [ ] Ran RLS policy SQL successfully
- [ ] Added 3 storage policies to each bucket
- [ ] Verified user owns mosque (is_owner = true)
- [ ] Tested image upload works
- [ ] Images appear in list after upload

All done when all boxes are checked! ✅
