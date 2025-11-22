package com.rpn.mosquetime.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.rpn.mosquetime.data.local.MosqueDatabase
import com.rpn.mosquetime.data.manager.FirestoreSyncManagerWithMappers
import com.rpn.mosquetime.data.manager.ImageCacheManager
import com.rpn.mosquetime.data.mapper.FirebaseToEntityMapper
import com.rpn.mosquetime.data.repository.SettingsRepository

// ================================================================================================
// STEP 8: WORKER FOR BACKGROUND SYNC
// ================================================================================================

class SyncWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val mosqueId = inputData.getString("mosque_id") ?: return Result.failure()

            // Initialize dependencies (ideally inject these)
            val database = MosqueDatabase.getDatabase(applicationContext)
            val imageCacheManager = ImageCacheManager(applicationContext)
            val firebaseToEntityMapper = FirebaseToEntityMapper()
            val syncManager = FirestoreSyncManagerWithMappers(
                FirebaseFirestore.getInstance(),
                database,
                imageCacheManager,
                firebaseToEntityMapper,
                SettingsRepository(context = context)
            )

            val result = syncManager.fullSync(mosqueId)

            if (result.success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}