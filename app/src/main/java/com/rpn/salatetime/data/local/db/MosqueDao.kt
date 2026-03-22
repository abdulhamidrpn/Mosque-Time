package com.rpn.salatetime.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.rpn.salatetime.domain.model.MosqueCompositeData
import com.rpn.salatetime.domain.model.MosqueEntity
import com.rpn.salatetime.domain.model.MosqueSlideEntity
import com.rpn.salatetime.domain.model.PrayerTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MosqueDaonew{

    // ─────────────────────────────────────────────────────────────────────────
    // Mosque — reads
    // ─────────────────────────────────────────────────────────────────────────

    /** Reactive stream — used by MainViewModel pipeline to detect mosque changes. */
    @Query("SELECT * FROM mosques WHERE ownerUid = :uid LIMIT 1")
    fun getMosqueByOwnerUid(uid: String): Flow<MosqueEntity?>

    /**
     * Synchronous single-row lookup by primary key.
     * Used by SupabaseRealtimeListener.syncMosqueImage to compare the stored
     * image URL against the incoming one before deciding whether to re-download.
     */
    @Query("SELECT * FROM mosques WHERE id = :id LIMIT 1")
    suspend fun getMosqueById(id: String): MosqueEntity?

    // ─────────────────────────────────────────────────────────────────────────
    // Prayer times — reads
    // ─────────────────────────────────────────────────────────────────────────

    /** All prayer times for a mosque, ordered chronologically. */
    @Query("SELECT * FROM prayer_times WHERE mosqueId = :mosqueId ORDER BY date ASC")
    fun getPrayerTimes(mosqueId: String): Flow<List<PrayerTimeEntity>>

    /** Single prayer time for a specific date — used by the main display pipeline. */
    @Query("SELECT * FROM prayer_times WHERE mosqueId = :mosqueId AND date = :date LIMIT 1")
    fun getPrayerTimeForDate(mosqueId: String, date: String): Flow<PrayerTimeEntity?>

    // ─────────────────────────────────────────────────────────────────────────
    // Slides — reads
    // ─────────────────────────────────────────────────────────────────────────

    /** Reactive stream ordered by displayOrder — drives the UI slide list. */
    @Query("SELECT * FROM mosque_slides WHERE mosqueId = :mosqueId ORDER BY displayOrder ASC")
    fun getSlides(mosqueId: String): Flow<List<MosqueSlideEntity>>

    /**
     * Synchronous snapshot — used by SupabaseRealtimeListener.syncSlideImages
     * to diff incoming Supabase records against what is stored, so only changed
     * images are re-downloaded rather than all slides on every emission.
     */
    @Query("SELECT * FROM mosque_slides WHERE mosqueId = :mosqueId ORDER BY displayOrder ASC")
    suspend fun getSlidesByMosqueId(mosqueId: String): List<MosqueSlideEntity>

    // ─────────────────────────────────────────────────────────────────────────
    // Combined query — single Room round-trip for the main screen
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Emits a MosqueCompositeData whenever any of the three tables change.
     * Used by MosqueRepository.getMosqueCombineDataByUid so the ViewModel
     * never has to combine three separate flows itself.
     */
//    @Transaction
//    @Query("SELECT * FROM mosques WHERE ownerUid = :uid LIMIT 1")
//    fun getMosqueCompositeByUid(uid: String): Flow<MosqueCompositeData?>

    // ─────────────────────────────────────────────────────────────────────────
    // Mosque — writes
    // ─────────────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMosque(mosque: MosqueEntity)

    // ─────────────────────────────────────────────────────────────────────────
    // Prayer times — writes
    // ─────────────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrayerTimes(times: List<PrayerTimeEntity>)

    /** Removes all prayer times for a mosque — used before a full re-sync. */
    @Query("DELETE FROM prayer_times WHERE mosqueId = :mosqueId")
    suspend fun clearPrayerTimes(mosqueId: String)

    /**
     * Prunes prayer times older than [olderThan] (ISO date string "yyyy-MM-dd").
     * Call periodically (e.g. on app start) to keep the database lean.
     * Example: deleteOlderThan(LocalDate.now().minusDays(7).toString())
     */
    @Query("DELETE FROM prayer_times WHERE date < :olderThan")
    suspend fun deleteOlderThan(olderThan: String)

    /** Total row count — useful for first-launch detection. */
    @Query("SELECT COUNT(*) FROM prayer_times")
    suspend fun countPrayerTimes(): Int

    // ─────────────────────────────────────────────────────────────────────────
    // Slides — writes
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Replaces the full slide list for a mosque in a single transaction.
     *
     * Slides that were removed from Supabase are automatically dropped because
     * we delete all rows for the mosque before inserting the fresh set.
     * Safe because SupabaseRealtimeListener already resolved localPath for each
     * slide before calling this.
     */
//    @Transaction
//    suspend fun replaceSlides(mosqueId: String, slides: List<MosqueSlideEntity>) {
//        clearSlides(mosqueId)
//        insertSlides(slides)
//    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlides(slides: List<MosqueSlideEntity>)

    @Query("DELETE FROM mosque_slides WHERE mosqueId = :mosqueId")
    suspend fun clearSlides(mosqueId: String)
}

@Dao
interface MosqueDao {

    // 1. Get Mosque Info by UID (using a Flow for reactive updates)
    @Query("SELECT * FROM mosques WHERE ownerUid = :uid LIMIT 1")
    fun getMosqueByOwnerUid(uid: String): Flow<MosqueEntity?>

    @Query("SELECT * FROM mosques WHERE id = :id LIMIT 1")
    suspend fun getMosqueById(id: String): MosqueEntity?

    // 2. Get All Prayer Times for specific mosque
    @Query("SELECT * FROM prayer_times WHERE mosqueId = :mosqueId ORDER BY date ASC")
    fun getPrayerTimes(mosqueId: String): Flow<List<PrayerTimeEntity>>

    // 2. Get Prayer Time for a specific mosque and date
    @Query("SELECT * FROM prayer_times WHERE mosqueId = :mosqueId AND date = :date LIMIT 1")
    fun getPrayerTimeForDate(mosqueId: String, date: String): Flow<PrayerTimeEntity?>

    // 3. Get All Slides for specific mosque
    @Query("SELECT * FROM mosque_slides WHERE mosqueId = :mosqueId ORDER BY displayOrder ASC")
    fun getSlides(mosqueId: String): Flow<List<MosqueSlideEntity>>
    /**
     * Synchronous snapshot — used by SupabaseRealtimeListener.syncSlideImages
     * to diff incoming Supabase records against what is stored, so only changed
     * images are re-downloaded rather than all slides on every emission.
     */
    @Query("SELECT * FROM mosque_slides WHERE mosqueId = :mosqueId ORDER BY displayOrder ASC")
    suspend fun getSlidesByMosqueId(mosqueId: String): List<MosqueSlideEntity>

    // --- Write Operations (Upsert) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMosque(mosque: MosqueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrayerTimes(times: List<PrayerTimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSlides(slides: List<MosqueSlideEntity>)

    // Clear old data before full sync if necessary
    @Query("DELETE FROM prayer_times WHERE mosqueId = :mosqueId")
    suspend fun clearPrayerTimes(mosqueId: String)


    @Query("DELETE FROM prayer_times WHERE date < :olderThan")
    suspend fun deleteOlderThan(olderThan: String)

    /** Total row count — useful for first-launch detection. */
    @Query("SELECT COUNT(*) FROM prayer_times")
    suspend fun count(): Int
}
