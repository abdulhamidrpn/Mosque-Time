package com.rpn.mosquetime.domain.repository


import com.rpn.mosquetime.domain.manager.SyncResult
import com.rpn.mosquetime.domain.model.MasjidInfo
import com.rpn.mosquetime.domain.model.Message
import com.rpn.mosquetime.domain.model.PrayerTime
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface MainRepository {

    // Schedule periodic sync
    fun schedulePeriodicSync(mosqueId: String)

    // Get complete mosque info (returns domain model)
    suspend fun getCompleteMosqueInfo(mosqueId: String): Flow<Result<MasjidInfo>>

    // Get prayer times (returns domain model)
    suspend fun getPrayerTimes(mosqueId: String): Flow<Result<List<PrayerTime>>>

    // Get messages (returns domain model)
    suspend fun getMessages(mosqueId: String): Flow<Result<List<Message>>>

    // Observe sync status
    val syncStatus: StateFlow<SyncResult?>

    // Manual sync trigger
    suspend fun syncUser(userId: String) : SyncResult
    suspend fun manualSync(mosqueId: String) : SyncResult
}