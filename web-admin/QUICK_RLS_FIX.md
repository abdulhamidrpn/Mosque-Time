# 🚀 QUICK FIX - Copy & Paste to Fix Upload

## ❌ Error You're Getting
```
Storage Upload Failed
background_mosque.jpeg: new row violates row-level security policy
```

## ✅ Solution (30 Seconds)

### Go Here:
```
https://app.supabase.com/project/lllnnuqdcoockevelthu/sql/new
```

### Copy & Paste This (entire block):

```sql
-- Drop old policies
DROP POLICY IF EXISTS "Users can insert slides for their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can view slides from their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can update slides in their mosques" ON public.mosque_slides;
DROP POLICY IF EXISTS "Users can delete slides from their mosques" ON public.mosque_slides;

-- Ensure RLS enabled
ALTER TABLE public.mosque_slides ENABLE ROW LEVEL SECURITY;

-- Create new policies
CREATE POLICY "view_own_mosque_slides" ON public.mosque_slides
FOR SELECT TO authenticated
USING (auth.uid() IN (
  SELECT owner_uid FROM public.mosques WHERE id = mosque_id
));

CREATE POLICY "insert_own_mosque_slides" ON public.mosque_slides
FOR INSERT TO authenticated
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);

CREATE POLICY "update_own_mosque_slides" ON public.mosque_slides
FOR UPDATE TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
)
WITH CHECK (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);

CREATE POLICY "delete_own_mosque_slides" ON public.mosque_slides
FOR DELETE TO authenticated
USING (
  mosque_id IN (
    SELECT id FROM public.mosques WHERE owner_uid = auth.uid()
  )
);
```

### Click: **Execute** (or Ctrl+Enter)

### Done! ✅

Now try uploading the image again.

---

## What This Does

1. ✅ Removes old problematic policies
2. ✅ Enables RLS on table
3. ✅ Creates 4 new policies:
   - SELECT (view)
   - INSERT (upload) ← **This fixes your error**
   - UPDATE (reorder)
   - DELETE (remove)

---

## Test Upload

1. Go back to **Slides** page
2. Click **"Upload Slides"**
3. Select **background_mosque.jpeg** (the one that failed)
4. Should upload successfully ✅

---

## If Still Not Working

Check these 3 things:

### 1. Are you logged in?
```
In your app, top right - see your email?
If not → Log in
```

### 2. Did you complete onboarding?
```
In your app, did you fill out mosque name?
If not → Complete onboarding
```

### 3. Did SQL execute successfully?
```
After running SQL above, did you see:
"Query executed successfully" message?
If not → Check for errors in red text
```

---

## Still Having Issues?

Open **RLS_POLICY_FIX.md** for detailed explanation and debugging.
