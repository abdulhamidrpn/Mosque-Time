package com.rpn.mosquetime.data.repository


import android.util.Log
import com.rpn.mosquetime.data.local.MosqueDatabase
import com.rpn.mosquetime.data.manager.ImageCacheManager
import com.rpn.mosquetime.data.mapper.MessageMapper
import com.rpn.mosquetime.data.mapper.MosqueInfoMapper
import com.rpn.mosquetime.data.mapper.PrayerTimeMapper
import com.rpn.mosquetime.domain.manager.ConnectivityObserver
import com.rpn.mosquetime.domain.manager.SyncManager
import com.rpn.mosquetime.domain.manager.SyncResult
import com.rpn.mosquetime.domain.model.MasjidInfo
import com.rpn.mosquetime.domain.model.Message
import com.rpn.mosquetime.domain.model.PrayerTime
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class OfflineFirstRepositoryWithMappers(
    private val database: MosqueDatabase,
    private val syncManager: SyncManager,
    private val connectivityObserver: ConnectivityObserver,
    private val imageCacheManager: ImageCacheManager,
    private val mosqueInfoMapper: MosqueInfoMapper,
    private val prayerTimeMapper: PrayerTimeMapper,
    private val messageMapper: MessageMapper
) {
    private val TAG = "OfflineFirstRepository"

    private val _syncStatus = MutableStateFlow<SyncResult?>(null)
    val syncStatus: StateFlow<SyncResult?> = _syncStatus.asStateFlow()


    // Get complete mosque info with prayer times and messages
    suspend fun getCompleteMosqueInfo(mosqueId: String): Flow<Result<MasjidInfo>> =
        flow {
            Log.d("TAGTRACKER", "getCompleteMosqueInfo: OfflineFirstRepositoryWithMappers called")
            emit(Result.Loading())

            try {
                // Always emit cached data first
                val cachedMosque = database.mosqueInfoDao().getMosque(mosqueId)
                val cachedPrayerTimes = database.prayerTimeDao().getPrayerTimes(mosqueId)
                val cachedMessages = database.mosqueMessageDao().getMessages(mosqueId)

                if (cachedMosque != null) {
                    Log.d("TAGTRACKER", "getCompleteMosqueInfo: Available Cached Mosque")
                    // Map entities to domain models
                    val domainMosque = mosqueInfoMapper.mapToDomain(cachedMosque)
                    val domainPrayerTimes = prayerTimeMapper.mapToDomain(cachedPrayerTimes)
                    val domainMessages = messageMapper.mapToDomain(cachedMessages)

                    val completeMosqueInfo = domainMosque.copy(
                        prayerTimes = domainPrayerTimes,
                        imageMessages = domainMessages
                    )

                    emit(Result.Success(completeMosqueInfo))
                }

                // Try to sync if online
                connectivityObserver.observe().first { status ->
                    if (status == ConnectivityObserver.Status.AVAILABLE) {
                        Log.d("TAGTRACKER", "getCompleteMosqueInfo: Online")
                        try {
                            val syncResult = syncManager.fullSync(mosqueId)
                            _syncStatus.value = syncResult

                            if (syncResult.success) {
                                // Re-fetch from database after sync
                                val updatedMosque = database.mosqueInfoDao().getMosque(mosqueId)
                                val updatedPrayerTimes =
                                    database.prayerTimeDao().getPrayerTimes(mosqueId)
                                val updatedMessages =
                                    database.mosqueMessageDao().getMessages(mosqueId)

                                if (updatedMosque != null) {
                                    val domainMosque = mosqueInfoMapper.mapToDomain(updatedMosque)
                                    val domainPrayerTimes =
                                        prayerTimeMapper.mapToDomain(updatedPrayerTimes)
                                    val domainMessages = messageMapper.mapToDomain(updatedMessages)

                                    val completeMosqueInfo = domainMosque.copy(
                                        prayerTimes = domainPrayerTimes,
                                        imageMessages = domainMessages
                                    )

                                    emit(
                                        Result.Success(
                                            completeMosqueInfo
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Sync failed", e)
                            // Still return cached data even if sync fails
                            if (cachedMosque != null) {
                                val domainMosque = mosqueInfoMapper.mapToDomain(cachedMosque)
                                val domainPrayerTimes =
                                    prayerTimeMapper.mapToDomain(cachedPrayerTimes)
                                val domainMessages = messageMapper.mapToDomain(cachedMessages)

                                val completeMosqueInfo = domainMosque.copy(
                                    prayerTimes = domainPrayerTimes,
                                    imageMessages = domainMessages
                                )

                                emit(
                                    Result.Success(
                                        completeMosqueInfo
                                    )
                                )
                            } else {
                                emit(Result.Error("No data available"))
                            }
                        }
                    }
                    false // Continue observing
                }

                if (cachedMosque == null) {
                    Log.d("TAGTRACKER", "getCompleteMosqueInfo: No Cached Mosque")
                    emit(Result.Error("No data available offline"))
                }

            } catch (e: Exception) {
                emit(Result.Error("Error: ${e.message}"))
            }
        }

    // Get prayer times only
    suspend fun getPrayerTimes(mosqueId: String): Flow<Result<List<PrayerTime>>> =
        flow {
            emit(Result.Loading())

            try {
                val cachedTimes = database.prayerTimeDao().getPrayerTimes(mosqueId)
                if (cachedTimes.isNotEmpty()) {
                    val domainPrayerTimes = prayerTimeMapper.mapToDomain(cachedTimes)
                    emit(Result.Success(domainPrayerTimes))
                }

                // Sync logic similar to above...
                if (cachedTimes.isEmpty()) {
                    emit(Result.Error("No prayer times available offline"))
                }

            } catch (e: Exception) {
                emit(Result.Error("Error: ${e.message}"))
            }
        }

    // Get messages only
    suspend fun getMessages(mosqueId: String): Flow<Result<List<Message>>> =
        flow {
            emit(Result.Loading())

            try {
                val cachedMessages = database.mosqueMessageDao().getMessages(mosqueId)
                if (cachedMessages.isNotEmpty()) {
                    val domainMessages = messageMapper.mapToDomain(cachedMessages)
                    emit(Result.Success(domainMessages))
                }

                // Sync logic similar to above...
                if (cachedMessages.isEmpty()) {
                    emit(Result.Error("No messages available offline"))
                }

            } catch (e: Exception) {
                emit(Result.Error("Error: ${e.message}"))
            }
        }

    // Manual sync trigger
    suspend fun syncUser(userId: String): SyncResult {
        return try {
            Log.d("TAGTRACKER", "syncUser: called")
            val result = syncManager.syncUser(userId)
            _syncStatus.value = result
            result
        } catch (e: Exception) {
            val errorResult = SyncResult(false, "Sync failed: ${e.message}", 0, 1)
            _syncStatus.value = errorResult
            errorResult
        }
    }
    // Manual sync trigger
    suspend fun forceSync(mosqueId: String): SyncResult {
        return try {
            Log.d("TAGTRACKER", "forceSync: called")
            val result = syncManager.fullSync(mosqueId)
            _syncStatus.value = result
            result
        } catch (e: Exception) {
            val errorResult = SyncResult(false, "Sync failed: ${e.message}", 0, 1)
            _syncStatus.value = errorResult
            errorResult
        }
    }
}