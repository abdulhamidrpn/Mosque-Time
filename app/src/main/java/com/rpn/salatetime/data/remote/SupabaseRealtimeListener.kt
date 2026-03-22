package com.rpn.salatetime.data.remote

import com.rpn.salatetime.data.local.db.MosqueDao
import com.rpn.salatetime.data.local.db.toEntity
import com.rpn.salatetime.domain.model.MosqueDto
import com.rpn.salatetime.domain.model.MosqueSlideDto
import com.rpn.salatetime.domain.model.PrayerTimeDto
import com.rpn.salatetime.domain.repository.ImageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber

class SupabaseRealtimeListener(
    private val supabase: SupabaseClient,
    private val mosqueDao: MosqueDao,
    private val imageRepository: ImageRepository,
) {

    @OptIn(SupabaseExperimental::class)
    suspend fun startListening(ownerUid: String) = coroutineScope {
        Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Timber.d("▶ startListening — ownerUid=$ownerUid")
        Timber.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // ── Step 1: Resolve mosqueId ──────────────────────────────────────────
        // We need the mosqueId before anything else because slides and prayer
        // times have a FK → mosques.id.  Inserting them before the parent row
        // exists will cause a FK violation.
        val mosqueId = resolveMosqueId(ownerUid)
        Timber.d("✔ mosqueId resolved: $mosqueId")

        // ── Step 2: Build the mosque flow ─────────────────────────────────────
        val mosqueFlow = supabase.from("mosques")
            .selectSingleValueAsFlow(MosqueDto::id) { eq("owner_uid", ownerUid) }

        // ── Step 3: Block until the mosque parent row is saved to Room ────────
        // We MUST await the first emission fully (including image download) so
        // that the FK parent exists in Room before we launch the slide/prayer
        // coroutines below.
        Timber.d("⏳ Waiting for first mosque emission from Supabase…")
        val firstMosque = mosqueFlow.first()
        Timber.d("✔ Mosque received from Supabase — id=${firstMosque.id}, image=${firstMosque.image}")

        // Download mosque image synchronously before writing to Room
        syncMosqueImage(firstMosque)
        Timber.d("✔ Mosque parent row secured in Room — FK constraint satisfied")

        // ── Step 4: Now launch all three continuous listeners in parallel ─────
        // Safe to do in parallel because the FK parent row now exists.

        // 4a. Mosque live updates
        launch {
            Timber.d("▶ mosque collector started")
            mosqueFlow.collect { dto ->
                Timber.d("↻ Mosque update received — id=${dto.id}, image=${dto.image}")
                syncMosqueImage(dto)
            }
        }

        // 4b. Slides
        launch {
            Timber.d("▶ slides collector started — mosqueId=$mosqueId")
            supabase.from("mosque_slides")
                .selectAsFlow(
                    primaryKey = MosqueSlideDto::id,
                    filter = FilterOperation("mosque_id", FilterOperator.EQ, mosqueId),
                )
                .collect { dtos ->
                    Timber.d("↻ Slides update received — ${dtos.size} slides")
                    syncSlideImages(dtos)
                }
        }

        // 4c. Prayer times (no images — just upsert directly)
        launch {
            Timber.d("▶ prayer times collector started — mosqueId=$mosqueId")
            supabase.from("prayer_times")
                .selectAsFlow(
                    primaryKey = PrayerTimeDto::mosqueId,
                    filter = FilterOperation("mosque_id", FilterOperator.EQ, mosqueId),
                )
                .collect { dtos ->
                    Timber.d("↻ Prayer times update received — ${dtos.size} records")
                    mosqueDao.upsertPrayerTimes(dtos.map { it.toEntity() })
                    Timber.d("✔ Prayer times saved to Room")
                }
        }
    }

    fun stopListening() {
        supabase.realtime.disconnect()
        Timber.d("■ Realtime listener stopped")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mosque image sync
    //
    // Three cases:
    //  A — no image URL in the record          → save with localPath = null
    //  B — same URL, cached file still on disk → reuse localPath, skip download
    //  C — URL changed or file missing         → evict old, download new, save
    //
    // We always wait for the download to finish before calling insertMosque()
    // so Room is never written with a localPath that doesn't exist on disk.
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun syncMosqueImage(dto: MosqueDto) {
        val incomingUrl = dto.image.orEmpty()
        val existing = mosqueDao.getMosqueById(dto.id)
        val storedUrl = existing?.image.orEmpty()
        val storedPath = existing?.localPath

        Timber.d("  syncMosqueImage()")
        Timber.d("    incoming url  = $incomingUrl")
        Timber.d("    stored url    = $storedUrl")
        Timber.d("    stored path   = $storedPath")

        val localPath: String? = when {

            // A — no image
            incomingUrl.isBlank() -> {
                Timber.d("    → Case A: no image URL — localPath = null")
                null
            }

            // B — same URL and file still on disk
            incomingUrl == storedUrl
                    && storedPath != null
                    && java.io.File(storedPath).exists() -> {
                Timber.d("    → Case B: URL unchanged, file on disk — reusing $storedPath")
                storedPath
            }

            // C — URL changed or file missing: evict if needed, then download
            else -> {
                if (incomingUrl != storedUrl && storedUrl.isNotBlank()) {
                    Timber.d("    → Case C: URL changed — evicting old cached file")
                    imageRepository.evictCachedFile(storedUrl)
                } else {
                    Timber.d("    → Case C: file missing or first time — downloading")
                }

                Timber.d("    ⬇ Downloading mosque image: $incomingUrl")
                val file = imageRepository.cacheImage(url = incomingUrl, prefix = "mosque_bg")
                Timber.d("    ✔ Mosque image cached → name=${file?.name}, path=${file?.absolutePath}")
                file?.absolutePath
            }
        }

        mosqueDao.insertMosque(dto.toEntity().copy(localPath = localPath))
        Timber.d("    ✔ Mosque saved to Room — id=${dto.id}, localPath=$localPath")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Slide image sync
    //
    // Three cases per slide (same logic as mosque, applied per-slide):
    //  A — same URL, file on disk → reuse
    //  B — URL changed            → evict old, download new
    //  C — new slide / missing    → download fresh
    //
    // All slides are resolved sequentially so each download is confirmed before
    // writing the entity, then the whole batch is saved in one Room transaction.
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun syncSlideImages(dtos: List<MosqueSlideDto>) {
        if (dtos.isEmpty()) {
            Timber.d("  syncSlideImages() — empty list, nothing to do")
            return
        }

        Timber.d("  syncSlideImages() — ${dtos.size} slides incoming")

        val existingById = mosqueDao
            .getSlidesByMosqueId(dtos.first().mosqueId)
            .associateBy { it.id }

        Timber.d("  existing slides in Room: ${existingById.keys}")

        val updatedEntities = dtos.map { dto ->
            val existing = existingById[dto.id]
            val storedUrl = existing?.imageUrl.orEmpty()
            val storedPath = existing?.localPath
            val incomingUrl = dto.imageUrl.orEmpty()
            val prefix = "slide_%02d".format(dto.displayOrder)

            Timber.d("  ┌ slide id=${dto.id}, order=${dto.displayOrder}")
            Timber.d("  │ incoming url = $incomingUrl")
            Timber.d("  │ stored url   = $storedUrl")
            Timber.d("  │ stored path  = $storedPath")

            val localPath: String? = when {

                // A — same URL and file on disk
                incomingUrl == storedUrl
                        && storedPath != null
                        && java.io.File(storedPath).exists() -> {
                    Timber.d("  │ → Case A: unchanged — reusing $storedPath")
                    storedPath
                }

                // B — URL changed → evict, download new
                incomingUrl != storedUrl && storedUrl.isNotBlank() -> {
                    Timber.d("  │ → Case B: URL changed — evicting old file")
                    imageRepository.evictCachedFile(storedUrl)
                    Timber.d("  │ ⬇ Downloading new image: $incomingUrl")
                    val file = imageRepository.cacheImage(url = incomingUrl, prefix = prefix)
                    Timber.d("  │ ✔ Downloaded → name=${file?.name}, path=${file?.absolutePath}")
                    file?.absolutePath
                }

                // C — new slide or file missing
                else -> {
                    Timber.d("  │ → Case C: new/missing — downloading: $incomingUrl")
                    val file = imageRepository.cacheImage(url = incomingUrl, prefix = prefix)
                    Timber.d("  │ ✔ Downloaded → name=${file?.name}, path=${file?.absolutePath}")
                    file?.absolutePath
                }
            }

            Timber.d("  └ slide ${dto.id} localPath = $localPath")
            dto.toEntity().copy(localPath = localPath)
        }

        mosqueDao.updateSlides(updatedEntities)
        Timber.d("  ✔ ${updatedEntities.size} slides saved to Room")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Resolve mosqueId
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun resolveMosqueId(ownerUid: String): String {
        val cached = mosqueDao.getMosqueByOwnerUid(ownerUid).firstOrNull()?.id
        if (!cached.isNullOrEmpty()) {
            Timber.d("  mosqueId from Room cache: $cached")
            return cached
        }

        Timber.d("  mosqueId not in Room — fetching from Supabase")
        val mosque = supabase.from("mosques")
            .select { filter { eq("owner_uid", ownerUid) } }
            .decodeSingleOrNull<MosqueDto>()
            ?: throw IllegalStateException("Mosque not found for ownerUid=$ownerUid")

        Timber.d("  mosqueId from Supabase: ${mosque.id}")
        return mosque.id
    }
}

class SupabaseRealtimeListenerOld(
    private val supabase: SupabaseClient,
    private val mosqueDao: MosqueDao,
    private val imageRepository: ImageRepository,
) {

    @OptIn(SupabaseExperimental::class)
    suspend fun startListening(ownerUid: String) = coroutineScope {
        Timber.d("Starting realtime listener")
        val mosqueId = resolveMosqueId(ownerUid)

        // 1. Fetch and save Mosque record FIRST
        val mosqueFlow = supabase.from("mosques")
            .selectSingleValueAsFlow(MosqueDto::id) { eq("owner_uid", ownerUid) }

        // Wait for the first emission and save it to Room
        val firstMosque = mosqueFlow.first()
        val localThumb = firstMosque.image?.let { imageRepository.cacheImage(it)?.absolutePath }
        Timber.d(
            "Parent Mosque record received from Supabase " +
                    "\n→ id=${firstMosque}, \nlocalPath=$localThumb"
        )
        mosqueDao.insertMosque(firstMosque.toEntity().copy(localPath = localThumb))
        Timber.d("Parent Mosque record secured in Room")

        // 2. Now that the Foreign Key parent exists, launch the continuous listeners
        launch {
            mosqueFlow.collect { dto ->
                val path = dto.image?.let { imageRepository.cacheImage(it)?.absolutePath }
                mosqueDao.insertMosque(dto.toEntity().copy(localPath = path))
            }
        }

        launch {
            supabase.from("mosque_slides").selectAsFlow(
                primaryKey = MosqueSlideDto::id,
                filter = FilterOperation("mosque_id", FilterOperator.EQ, mosqueId)
            ).collect { dtos ->
                val entities = dtos.map {
                    it.toEntity().copy(
                        localPath = imageRepository.cacheImage(it.imageUrl)?.absolutePath
                    )
                }
                Timber.d("Slide images updated — ${entities.size} records")
                mosqueDao.updateSlides(entities)
            }
        }

        launch {
            supabase.from("prayer_times").selectAsFlow(
                primaryKey = PrayerTimeDto::mosqueId,
                filter = FilterOperation("mosque_id", FilterOperator.EQ, mosqueId)
            ).collect { dtos ->
                mosqueDao.upsertPrayerTimes(dtos.map { it.toEntity() })
            }
        }
    }

    fun stopListening() {
        supabase.realtime.disconnect()
    }

    private suspend fun resolveMosqueId(ownerUid: String): String {
        // check local database via repository
        val cached = mosqueDao.getMosqueByOwnerUid(ownerUid).firstOrNull()?.id
        if (!cached.isNullOrEmpty()) return cached

        // otherwise query Supabase directly
        val mosque = supabase.from("mosques")
            .select { filter { eq("owner_uid", ownerUid) } }
            .decodeSingleOrNull<MosqueDto>()
            ?: throw Exception("Mosque not found for owner $ownerUid")
        return mosque.id
    }

}
