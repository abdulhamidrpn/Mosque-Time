# Visual Architecture: Upload Flow & Configuration

## Current Error Flow

```
User clicks "Upload Image"
    ↓
App code tries to upload
    ↓
┌─────────────────────────────────┐
│ Storage Upload                  │
│ mosque-slides bucket            │
│                                 │
│ ❌ ERROR: bucket not found      │
│    (bucket doesn't exist)       │
└─────────────────────────────────┘
    ↓
Upload FAILS ❌
```

## Fixed Flow

```
User clicks "Upload Image"
    ↓
App code uploads image
    ↓
┌─────────────────────────────────┐
│ Storage Upload                  │
│ mosque-slides bucket (PUBLIC)   │
│                                 │
│ ✅ Storage RLS Policy 1:        │
│    auth.role() = 'authenticated'│
│    → Allows upload              │
│                                 │
│ ✅ File uploaded to storage     │
│    Path: {mosque_id}/file.jpg   │
└─────────────────────────────────┘
    ↓
Get Public URL
    ↓
┌─────────────────────────────────┐
│ Database Insert                 │
│ mosque_slides table             │
│                                 │
│ ✅ Database RLS Policy 2:       │
│    auth.uid() IN (              │
│      SELECT owner_uid FROM      │
│      mosques WHERE id =         │
│      mosque_id                  │
│    )                            │
│    WITH CHECK (user owns mosque)│
│                                 │
│ ✅ Record inserted to DB        │
│    {mosque_id, image_url,       │
│     display_order}              │
└─────────────────────────────────┘
    ↓
Update React Query Cache
    ↓
Image appears in UI ✅
```

---

## Component Architecture

```
┌─────────────────────────────────────────────────┐
│ Supabase Project                                │
│ lllnnuqdcoockevelthu                            │
│                                                 │
│  ┌──────────────────────────────────────────┐  │
│  │ Authentication (Supabase Auth)           │  │
│  │                                          │  │
│  │ User Email: xxx@example.com              │  │
│  │ User ID (auth.uid()): abc-123-def        │  │
│  └──────────────────────────────────────────┘  │
│              ↓              ↓                   │
│      ┌───────────────┐  ┌────────────────┐     │
│      │ Database      │  │ Storage        │     │
│      │ (PostgreSQL)  │  │ (S3-compatible)│     │
│      │               │  │                │     │
│      │ mosques table │  │ Buckets:       │     │
│      │ ├─ id         │  │ ├─ mosque-     │     │
│      │ ├─ owner_uid  │  │ │  slides      │     │
│      │ └─ ...        │  │ │  (public)    │     │
│      │               │  │ └─ mosque-     │     │
│      │ mosque_slides │  │    images      │     │
│      │ ├─ id         │  │    (public)    │     │
│      │ ├─ mosque_id  │  │                │     │
│      │ ├─ image_url  │  │ RLS Policies:  │     │
│      │ └─ ...        │  │ ├─ SELECT: ✅  │     │
│      │               │  │ ├─ INSERT: ✅  │     │
│      │ RLS Enabled   │  │ └─ DELETE: ✅  │     │
│      │ ├─ SELECT: ✅ │  │                │     │
│      │ ├─ INSERT: ✅ │  │ Path Structure:│     │
│      │ ├─ UPDATE: ✅ │  │ {mosque_id}/   │     │
│      │ └─ DELETE: ✅ │  │ {file_name}    │     │
│      └───────────────┘  └────────────────┘     │
│              ↓              ↓                   │
│  ┌──────────────────────────────────────────┐  │
│  │ Row Level Security (RLS)                 │  │
│  │                                          │  │
│  │ Database RLS checks:                     │  │
│  │ auth.uid() ∈ (SELECT owner_uid FROM     │  │
│  │              mosques WHERE id = mosque_id)  │
│  │                                          │  │
│  │ Storage RLS checks:                      │  │
│  │ auth.role() = 'authenticated'            │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
              ↑
              │ HTTPS
              │
┌─────────────────────────────────────────────────┐
│ Client (Your React App)                         │
│                                                 │
│ ┌──────────────────────────────────────────┐   │
│ │ SlidesPage.tsx                           │   │
│ │ ├─ useMosque()                           │   │
│ │ ├─ useMosqueSlides(mosque.id)            │   │
│ │ ├─ handleUpload()                        │   │
│ │ │  ├─ supabase.storage.upload(path)      │   │
│ │ │  ├─ supabase.storage.getPublicUrl()    │   │
│ │ │  └─ supabase.from("mosque_slides")     │   │
│ │ │     .insert({...})                     │   │
│ │ └─ updateQueryClient                     │   │
│ └──────────────────────────────────────────┘   │
│                                                 │
│ ┌──────────────────────────────────────────┐   │
│ │ ProfilePage.tsx                          │   │
│ │ ├─ useMosque()                           │   │
│ │ ├─ handleImageUpload()                   │   │
│ │ │  ├─ supabase.storage.upload(path)      │   │
│ │ │  ├─ supabase.storage.getPublicUrl()    │   │
│ │ │  └─ supabase.from("mosques")           │   │
│ │ │     .update({image: url})              │   │
│ │ └─ useUpdateMosque()                     │   │
│ └──────────────────────────────────────────┘   │
│                                                 │
│ ┌──────────────────────────────────────────┐   │
│ │ React Query Cache                        │   │
│ │ ├─ ["mosque"] → mosque data              │   │
│ │ ├─ ["mosque-slides", mosqueId]           │   │
│ │ │  └─ [slide1, slide2, ...]              │   │
│ │ └─ ["prayer-times-*"]                    │   │
│ └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

---

## Data Flow: Slide Upload

```
┌────────────────────────────────────────────────────────────────┐
│ 1. User selects file from disk                                 │
│    File: photo.jpg (2MB)                                       │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│ 2. App validates file                                           │
│    ✅ Type: image/jpeg                                         │
│    ✅ Size: < 100MB (assumed)                                  │
│    ✅ Mosque ID exists                                         │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│ 3. Upload to Storage                                            │
│                                                                │
│    supabase.storage.from("mosque-slides").upload(path, file)   │
│                                                                │
│    Path: {mosque-id}/1708245678-0.jpg                          │
│    Storage bucket: mosque-slides                               │
│    ✅ Checks RLS: auth.role() = 'authenticated'                │
│    ✅ File stored in object storage                            │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│ 4. Get Public URL                                               │
│                                                                │
│    supabase.storage.getPublicUrl(path)                         │
│                                                                │
│    Returns:                                                    │
│    https://storage.supabase.co/object/public/mosque-slides/... │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│ 5. Insert Database Record                                       │
│                                                                │
│    supabase.from("mosque_slides").insert({                     │
│      mosque_id: "abc-123",                                     │
│      image_url: "https://...",                                 │
│      display_order: 0                                          │
│    })                                                          │
│                                                                │
│    ✅ Checks RLS: auth.uid() ∈ (                               │
│         SELECT owner_uid FROM mosques                          │
│         WHERE id = mosque_id                                   │
│       )                                                        │
│    ✅ Record inserted into mosque_slides table                 │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│ 6. Update React Query Cache                                     │
│                                                                │
│    queryClient.setQueryData(["mosque-slides", mosque.id],      │
│      [...oldSlides, newSlide]                                  │
│    )                                                           │
│                                                                │
│    ✅ UI updates immediately (optimistic)                      │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│ 7. Show Success to User                                         │
│                                                                │
│    ✅ Toast: "Slides Uploaded"                                 │
│    ✅ Image appears in grid                                    │
│    ✅ Display order shows position                             │
└────────────────────────────────────────────────────────────────┘
```

---

## RLS Policy Check Points

### Database RLS (mosque_slides table)

```
INSERT INTO mosque_slides (mosque_id, image_url, display_order)
VALUES ('abc-123', 'https://...', 0)

RLS Policy Evaluation:
┌────────────────────────────────────────┐
│ Logged-in user: auth.uid() = xyz-456   │
│ Mosque owner: owner_uid = xyz-456      │
│                                        │
│ Policy check:                          │
│ auth.uid() ∈ (                         │
│   SELECT owner_uid FROM mosques        │
│   WHERE id = 'abc-123'                 │
│ )                                      │
│                                        │
│ Result:                                │
│ xyz-456 ∈ (xyz-456)  → TRUE ✅         │
│                                        │
│ INSERT allowed! ✅                     │
└────────────────────────────────────────┘
```

### Storage RLS (mosque-slides bucket)

```
PUT /object/public/mosque-slides/{path}

RLS Policy Evaluation:
┌────────────────────────────────────────┐
│ Logged-in user: auth.role() = 'authenticated'
│                                        │
│ Policy check:                          │
│ auth.role() = 'authenticated'          │
│                                        │
│ Result:                                │
│ 'authenticated' = 'authenticated' ✅   │
│                                        │
│ UPLOAD allowed! ✅                     │
└────────────────────────────────────────┘
```

---

## Configuration Checklist Visual

```
┌─ Supabase Configuration ──────────────────────────────────┐
│                                                           │
│  Storage Buckets:                                         │
│  ☐ mosque-slides (public)                                │
│  ☐ mosque-images (public)                                │
│                                                           │
│  Database Table:                                          │
│  ✅ mosque_slides exists                                 │
│  ├─ Columns: id, mosque_id, image_url, display_order     │
│  └─ RLS: ENABLED                                         │
│                                                           │
│  Database RLS Policies:                                   │
│  ☐ SELECT policy                                         │
│  ☐ INSERT policy (WITH CHECK)                            │
│  ☐ UPDATE policy                                         │
│  ☐ DELETE policy                                         │
│                                                           │
│  Storage RLS Policies (mosque-slides):                    │
│  ☐ SELECT: true                                          │
│  ☐ INSERT: auth.role() = 'authenticated'                 │
│  ☐ DELETE: auth.role() = 'authenticated'                 │
│                                                           │
│  Storage RLS Policies (mosque-images):                    │
│  ☐ SELECT: true                                          │
│  ☐ INSERT: auth.role() = 'authenticated'                 │
│  ☐ DELETE: auth.role() = 'authenticated'                 │
│                                                           │
│  Data Verification:                                       │
│  ☐ auth.uid() exists (user logged in)                    │
│  ☐ Mosque record exists                                  │
│  ☐ owner_uid matches auth.uid()                          │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

---

## Error Resolution Paths

```
Error: "bucket not found"
  ↓
Check: Does bucket exist?
  ├─ NO → Create bucket (Step 1)
  └─ YES → Check: Is bucket public?
           ├─ NO → Make public (Settings)
           └─ YES → Check RLS policies (Step 3)

Error: "RLS policy" error
  ↓
Check: User owns mosque?
  ├─ NO → Update profile or redo onboarding (Step 4)
  └─ YES → Check: Correct RLS policies?
           ├─ NO → Run RLS SQL (Step 2)
           └─ YES → Check: Using WITH CHECK for INSERT?
                    ├─ NO → Run RLS SQL (Step 2)
                    └─ YES → Debug permissions in Supabase

Error: "401 Unauthorized" on storage
  ↓
Check: Storage RLS policies exist?
  ├─ NO → Create policies (Step 3)
  └─ YES → Check: User authenticated?
           ├─ NO → Log in first
           └─ YES → Check: Expression correct?
                    ├─ NO → Update policies
                    └─ YES → Check bucket permissions
```

---

## Success Indicators

```
✅ Working Configuration:

Storage:
  ✅ Bucket exists and is public
  ✅ RLS policies allow auth users to upload
  ✅ Files accessible via public URL

Database:
  ✅ Table exists with correct schema
  ✅ RLS enabled
  ✅ Policies use WITH CHECK for INSERT
  ✅ User owns the mosque (auth.uid() = owner_uid)

Application:
  ✅ Console shows "Database insert successful"
  ✅ Image appears in UI immediately
  ✅ Toast shows "Slides Uploaded"
  ✅ No error messages
```
