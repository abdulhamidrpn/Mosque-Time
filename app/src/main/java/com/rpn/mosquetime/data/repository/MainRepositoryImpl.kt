package com.rpn.mosquetime.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.rpn.mosquetime.data.workers.SyncWorker
import com.rpn.mosquetime.domain.model.MasjidInfo
import com.rpn.mosquetime.domain.model.Message
import com.rpn.mosquetime.domain.model.PrayerTime
import com.rpn.mosquetime.domain.repository.MainRepository
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit


class MainRepositoryImpl(
    private val offlineFirstRepository: OfflineFirstRepositoryWithMappers,
    private val workManager: WorkManager,
    private val context: Context
) : KoinComponent, MainRepository {

    // Schedule periodic sync
    override fun schedulePeriodicSync(mosqueId: String) {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf("mosque_id" to mosqueId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "mosque_sync_$mosqueId",
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest
        )
    }

    // Get complete mosque info (returns domain model)
    override suspend fun getCompleteMosqueInfo(mosqueId: String): Flow<Result<MasjidInfo>> =
        offlineFirstRepository.getCompleteMosqueInfo(mosqueId)

    // Get prayer times (returns domain model)
    override suspend fun getPrayerTimes(mosqueId: String): Flow<Result<List<PrayerTime>>> =
        offlineFirstRepository.getPrayerTimes(mosqueId)

    // Get messages (returns domain model)
    override suspend fun getMessages(mosqueId: String): Flow<Result<List<Message>>> =
        offlineFirstRepository.getMessages(mosqueId)

    // Observe sync status
    override val syncStatus = offlineFirstRepository.syncStatus

    // User sync trigger
    override suspend fun syncUser(userId: String) =
        offlineFirstRepository.syncUser(userId)

    // Manual sync trigger
    override suspend fun manualSync(mosqueId: String) =
        offlineFirstRepository.forceSync(mosqueId)
}