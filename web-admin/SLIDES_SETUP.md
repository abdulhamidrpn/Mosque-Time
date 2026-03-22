# Slides Upload Feature - Setup & Troubleshooting Guide

## ✅ What's Implemented

The **Slides Upload Feature** now includes:

1. **Multi-Image Upload** - Upload 0-10 images sequentially
2. **Drag-and-Drop Reordering** - Reorder slides by dragging
3. **Delete Functionality** - Remove individual slides
4. **Upload Progress** - Visual progress bar during uploads
5. **Capacity Tracking** - Shows current/max slides (e.g., "5/10")

## 🔧 Required Supabase Setup

### 1. Create Storage Bucket

You MUST create a storage bucket called `mosque-slides` in your Supabase dashboard:

1. Go to https://app.supabase.com
2. Select your project: `lllnnuqdcoockevelthu`
3. Click **Storage** in the sidebar
4. Click **New Bucket**
5. Name it: `mosque-slides` (exactly this)
6. Make it **Public** (so images are accessible)
7. Click **Create Bucket**

### 2. Enable Bucket Access (RLS Policies)

In the bucket settings, you need RLS policies:

#### For Uploads (INSERT):
```sql
CREATE POLICY "Allow authenticated users to upload slides"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (
  bucket_id = 'mosque-slides' AND
  auth.uid()::text = (storage.foldername(name))[1]
);
```

#### For Public Read (SELECT):
```sql
CREATE POLICY "Allow public read access to mosque-slides"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'mosque-slides');
```

#### For Deletes (DELETE):
```sql
CREATE POLICY "Allow authenticated users to delete their slides"
ON storage.objects FOR DELETE
TO authenticated
USING (
  bucket_id = 'mosque-slides' AND
  auth.uid()::text = (storage.foldername(name))[1]
);
```

### 3. Database RLS Policies

Verify these policies exist for `mosque_slides` table:

#### SELECT Policy:
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

#### INSERT Policy:
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

#### UPDATE Policy:
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

#### DELETE Policy:
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

## 🚀 How It Works

### Upload Flow:
1. Click **Upload Slides** button
2. Select 1 or multiple images from your device
3. Images upload sequentially
4. Each image appears in the grid with a number (#1, #2, etc.)
5. Progress bar shows upload status

### Reorder Flow:
1. Drag any slide by the **grip handle** (⋮⋮ icon)
2. Drop it in the desired position
3. Order updates automatically

### Delete Flow:
1. Hover over a slide
2. Click the **trash icon** that appears
3. Slide is deleted from database and storage

## 📁 File Structure

```
src/
├── pages/
│   └── SlidesPage.tsx          # Main upload UI component
├── hooks/
│   └── use-mosque-data.ts      # Data hooks including useDeleteSlide()
└── integrations/
    └── supabase/
        ├── client.ts           # Supabase client config
        └── types.ts            # Auto-generated types
```

## 🧪 Testing Steps

1. **Start the app:**
   ```bash
   npm run dev
   ```

2. **Navigate to Slides page** in the dashboard

3. **Upload images:**
   - Click "Upload Slides" button
   - Select 1-3 images
   - See progress bar
   - Images appear in grid

4. **Reorder slides:**
   - Hover over a slide
   - See the grip handle (⋮⋮)
   - Drag to reorder
   - See order update (numbers change)

5. **Delete slides:**
   - Hover over a slide
   - Click red trash icon
   - Slide disappears

## 🐛 Troubleshooting

### "Upload Slides button doesn't work"
- ✅ Fixed! Button now has proper `onClick={() => fileInputRef.current?.click()}`

### "Images don't appear after upload"
**Check:**
1. Supabase storage bucket `mosque-slides` exists and is **PUBLIC**
2. Database has mosque_slides table (migrations run)
3. RLS policies allow your user to insert/select

### "Images upload but don't show in grid"
**Check:**
1. Open browser DevTools Console (F12)
2. Look for error messages
3. Check if images are loading (Network tab)
4. Verify `publicUrl` is correct in database

### "Can't delete images"
**Check:**
1. RLS delete policy exists on `mosque_slides` table
2. RLS delete policy exists on `storage.objects` for bucket
3. Your user owns the mosque (owner_uid matches)

### "Progress bar stuck at 0%"
- Upload may be in progress. Wait or check console for errors.

## 🔑 Key Code References

### Upload Handler (SlidesPage.tsx):
```tsx
const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
  // Validates max 10 slides
  // Uploads sequentially
  // Tracks progress
  // Handles errors per file
};
```

### Delete Hook (use-mosque-data.ts):
```tsx
export function useDeleteSlide() {
  return useMutation({
    mutationFn: async ({ id, imageUrl }) => {
      // Deletes from storage
      // Deletes from database
      // Invalidates cache
    },
  });
}
```

### Drag Reorder (SlidesPage.tsx):
```tsx
const handleDragEnd = async (event: DragEndEvent) => {
  // Uses dnd-kit library
  // Updates display_order in database
  // Shows optimistic UI update
};
```

## 🎯 Feature Limits

- **Max Slides:** 10 per mosque
- **File Types:** Images only (PNG, JPG, WebP, etc.)
- **Max Size:** Depends on Supabase plan (usually 1GB per bucket)

## 💡 Pro Tips

- Drag slides to reorder **while uploading** for efficient workflow
- Delete slides you don't need to stay under 10-slide limit
- Use sequential numbering to track slide order
- Upload smaller images for faster upload

---

**Need help?** Check browser console (F12) for detailed error messages!
