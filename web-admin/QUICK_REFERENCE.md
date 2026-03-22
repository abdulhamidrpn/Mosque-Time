# 🎯 QUICK REFERENCE: Upload Errors - Instant Solutions

## Your Errors

```
❌ Error 1: new row violates row-level security policy
❌ Error 2: bucket not found
```

## The Fix (6 Steps = 6 Minutes)

### Step 1: Buckets (2 min)
Go to Supabase → Storage → Create 2 buckets:
- ✅ `mosque-slides` (PUBLIC)
- ✅ `mosque-images` (PUBLIC)

### Step 2: RLS Policies (1 min)
Go to Supabase → SQL Editor → Copy-paste and run:
[RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)

### Step 3: Storage Policies (2 min)
Go to Storage → Policies → Add 3 policies per bucket:
- ✅ SELECT: `true`
- ✅ INSERT: `auth.role() = 'authenticated'`
- ✅ DELETE: `auth.role() = 'authenticated'`

### Step 4: Verify Data (30 sec)
Run query in SQL Editor:
```sql
SELECT auth.uid() as id, (SELECT owner_uid FROM mosques LIMIT 1) as owner;
```
Expected: Both should be the same ✅

### Step 5: Test (1 min)
Open app → Upload image → Works? ✅

## Success = Done!

---

## Need More Help?

| Need | Read This |
|------|-----------|
| Exact steps | [EXACT_STEPS.md](EXACT_STEPS.md) |
| Checklist | [MASTER_CHECKLIST.md](MASTER_CHECKLIST.md) |
| Understanding | [UPLOAD_ERRORS_EXPLAINED.md](UPLOAD_ERRORS_EXPLAINED.md) |
| Code review | [CODE_REVIEW_UPLOAD.md](CODE_REVIEW_UPLOAD.md) |
| Diagrams | [VISUAL_ARCHITECTURE.md](VISUAL_ARCHITECTURE.md) |
| All docs | [START_HERE.md](START_HERE.md) |

## Quick Links

- 📋 Full Checklist: [MASTER_CHECKLIST.md](MASTER_CHECKLIST.md)
- 📝 Detailed Steps: [EXACT_STEPS.md](EXACT_STEPS.md)
- 📊 SQL Script: [RLS_POLICY_FINAL_FIX.sql](RLS_POLICY_FINAL_FIX.sql)
- 🎓 Learn Why: [UPLOAD_ERRORS_EXPLAINED.md](UPLOAD_ERRORS_EXPLAINED.md)
- 🎨 Diagrams: [VISUAL_ARCHITECTURE.md](VISUAL_ARCHITECTURE.md)

---

**👉 Start with [EXACT_STEPS.md](EXACT_STEPS.md) or [MASTER_CHECKLIST.md](MASTER_CHECKLIST.md)**
