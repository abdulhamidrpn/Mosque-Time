package com.rpn.mosquetime.domain.manager


// ================================================================================================
// STEP 6: SYNC MANAGER
// ================================================================================================
enum class SyncStatus {
    SYNCED, PENDING_UPLOAD, PENDING_DOWNLOAD, FAILED, DELETED
}
data class SyncResult(
    val success: Boolean,
    val message: String,
    val syncedItems: Int = 0,
    val failedItems: Int = 0
)

interface SyncManager {
    suspend fun syncMosqueData(mosqueId: String): SyncResult
    suspend fun syncPrayerTimes(mosqueId: String): SyncResult
    suspend fun syncMessages(mosqueId: String): SyncResult
    suspend fun syncUser(userId: String): SyncResult
    suspend fun fullSync(mosqueId: String): SyncResult
}