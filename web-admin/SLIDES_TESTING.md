# Slides Upload - Complete Testing & Verification Guide

## ✅ Fixed Issues

### Issue 1: Images Not Showing After Upload
**Root Cause:** React Query cache invalidation was using wrong query key
```tsx
// ❌ BEFORE (Missing mosqueId)
qc.invalidateQueries({ queryKey: ["mosque-slides"] });

// ✅ AFTER (Includes mosqueId)
qc.setQueryData(["mosque-slides", mosque.id], updatedSlides);
await qc.invalidateQueries({ queryKey: ["mosque-slides", mosque.id] });
```

### Issue 2: Capacity Indicator Not Updating
**Fixed By:** Proper cache updates now trigger component re-render

---

## 🧪 Complete Testing Steps

### Test 1: Single Image Upload

1. Go to **Slides** page
2. Click **Upload Slides** button
3. Select **1 image** from your device
4. **Verify:**
   - ✅ Progress bar appears and goes to 100%
   - ✅ "Uploading..." changes to "Upload Slides"
   - ✅ Image appears in grid immediately
   - ✅ Image has number (#1) label
   - ✅ Capacity shows "1/10"

### Test 2: Multiple Images Upload (3 images)

1. Click **Upload Slides** button
2. Select **3 images** at once
3. **Verify:**
   - ✅ Progress bar shows sequential progress
   - ✅ All 3 images appear in grid
   - ✅ Numbers show correctly (#1, #2, #3)
   - ✅ Capacity shows "3/10"
   - ✅ All images load (may take a moment)

### Test 3: Capacity Limit (10+ images)

1. Upload images until you have 9 slides
2. Click **Upload Slides** button
3. Try to select **5 more images**
4. **Verify:**
   - ✅ Toast error appears: "Too Many Slides"
   - ✅ Explains: "You can add 1 more slide(s)"
   - ✅ Upload is prevented
   - ✅ Upload button disabled when at capacity

### Test 4: Drag & Reorder

1. Upload at least 3 images
2. **Drag image #2** to position #1
3. **Verify:**
   - ✅ Image moves smoothly
   - ✅ Numbers re-order immediately (#2 becomes #1, etc.)
   - ✅ Hover grip handle (⋮⋮) shows cursor changes to grab
   - ✅ Toast shows "Order Updated"

### Test 5: Delete Functionality

1. Hover over any image
2. **Click trash icon** that appears
3. **Verify:**
   - ✅ Image disappears from grid immediately
   - ✅ Toast shows "Slide Deleted"
   - ✅ Capacity updates (e.g., "2/10" → "1/10")
   - ✅ Images reorder numbers correctly
   - ✅ Image deleted from Supabase storage

### Test 6: File Type Validation

1. Click **Upload Slides** button
2. Try to select a **non-image file** (.txt, .pdf, etc.)
3. **Verify:**
   - ✅ Toast error: "Invalid File"
   - ✅ File is skipped
   - ✅ Other valid images still upload

### Test 7: Browser Refresh

1. Upload 3 images
2. **Refresh the page** (F5)
3. **Verify:**
   - ✅ All 3 images still appear
   - ✅ Capacity still shows "3/10"
   - ✅ Order is preserved

---

## 🔍 Debug Checklist

If uploads still don't show, check these in order:

### 1. Browser Console (F12)
- Open **DevTools** → **Console** tab
- Try upload and look for error messages
- Common errors:
  - `"mosque-slides" bucket does not exist` → Create bucket
  - `permission denied` → Check RLS policies
  - `network error` → Check internet/Supabase status

### 2. Network Tab (F12)
- Open **DevTools** → **Network** tab
- Click upload
- Look for requests with status **200**
- If 403/401 errors → RLS policy issue
- If 404 error → Bucket doesn't exist

### 3. Supabase Dashboard
- Go to https://app.supabase.com
- Select project `lllnnuqdcoockevelthu`
- Check **Storage** → **mosque-slides** bucket exists
- Check **SQL Editor** → `SELECT * FROM mosque_slides;` returns data

### 4. React Query DevTools (Optional)
- Install: `npm install @tanstack/react-query-devtools`
- See cache state in real-time
- Verify cache keys and data

---

## 📊 Data Flow Diagram

```
User clicks Upload Slides
           ↓
Select images from device
           ↓
handleUpload function triggers
           ↓
For each image:
  ├─ Validate file type
  ├─ Upload to Supabase storage
  ├─ Get public URL
  ├─ Insert into mosque_slides table
  └─ Add to uploadedSlides array
           ↓
setQueryData with new slides (optimistic)
           ↓
invalidateQueries to refetch fresh data
           ↓
React component re-renders with new slides
           ↓
Capacity indicator updates automatically
           ↓
Show success toast
```

---

## 🎯 Key Implementation Details

### Why Optimistic Update?
```tsx
// Set immediately so UI updates right away
qc.setQueryData(["mosque-slides", mosque.id], updatedSlides);

// Then refetch to ensure server data is correct
await qc.invalidateQueries({ queryKey: ["mosque-slides", mosque.id] });
```

### How Capacity Updates?
```tsx
const currentSlideCount = slides?.length || 0;  // Recalculates on data change
const canAddMore = currentSlideCount < 10;      // Button state depends on this
```

When slides data updates, component re-renders and capacity automatically changes.

### Delete Flow
```tsx
const deleteSlide = useDeleteSlide();  // Uses custom hook
handleDelete(slide) {
  await deleteSlide.mutateAsync({ id, imageUrl });
  // Hook auto-invalidates cache
  // Component re-renders
  // Capacity updates
}
```

---

## 🚀 Performance Notes

- **Sequential Upload:** Images upload one at a time (not parallel)
  - Reason: Better error tracking per image
  - Ensures proper display_order calculation
  
- **Optimistic Updates:** Instant UI feedback
  - Images appear immediately
  - Capacity updates right away
  - Better UX

- **Progress Tracking:** Percentage calculation
  - Shows (current file + 1) / total files * 100

---

## 💾 Success Indicators

After implementing these fixes, you should see:

1. **Immediate Image Display** ✅
   - Images show in grid as soon as upload completes
   
2. **Capacity Updates** ✅
   - Counter changes immediately (e.g., "0/10" → "3/10")
   - Progress bar fills proportionally

3. **Drag & Delete Works** ✅
   - Can reorder and delete without refresh

4. **Page Refresh Persistence** ✅
   - Refresh page - images still there

5. **Error Handling** ✅
   - Invalid files skipped
   - Capacity limits enforced
   - Clear error messages

---

## 🐛 Still Not Working?

Follow these steps:

1. **Clear browser cache**
   - DevTools → Application → Storage → Clear All

2. **Restart dev server**
   - Stop: `Ctrl+C`
   - Start: `npm run dev`

3. **Check Supabase**
   - Storage → mosque-slides bucket exists?
   - Public? (can see icons)
   - RLS policies set? (check SQL)

4. **Check database**
   - SQL Editor:
   ```sql
   SELECT * FROM mosque_slides LIMIT 10;
   ```
   - See your uploaded slides?

5. **Check logs**
   - Terminal where `npm run dev` runs
   - Any error messages?

---

## 📞 Need Help?

- Check console: `F12` → `Console`
- Check network: `F12` → `Network`
- Check storage: `F12` → `Application` → `Storage`
- Restart everything and try again
