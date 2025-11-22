package com.rpn.mosquetime.data.manager

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.rpn.mosquetime.data.local.MosqueDatabase
import com.rpn.mosquetime.data.local.dto.MosqueInfoDto
import com.rpn.mosquetime.data.local.dto.MosqueTimeDto
import com.rpn.mosquetime.data.local.entity.MosqueMessageEntity
import com.rpn.mosquetime.data.local.entity.PrayerTimeEntity
import com.rpn.mosquetime.data.mapper.FirebaseToEntityMapper
import com.rpn.mosquetime.data.repository.SettingsRepository
import com.rpn.mosquetime.domain.manager.SyncManager
import com.rpn.mosquetime.domain.manager.SyncResult
import com.rpn.mosquetime.utils.toDataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManagerWithMappers(
    private val firestore: FirebaseFirestore,
    private val database: MosqueDatabase,
    private val imageCacheManager: ImageCacheManager,
    private val firebaseToEntityMapper: FirebaseToEntityMapper,
    private val settingsRepository: SettingsRepository
) : SyncManager {

    private val TAG = "FirestoreSyncManager"

    override suspend fun syncMosqueData(mosqueId: String): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val document = firestore.collection("MOSQUE_LIST")
                    .document(mosqueId)
                    .get()
                    .await()

                if (document.exists()) {
                    Log.d("TAGTRACKER", "syncMosqueData: Document Exists ${document.data}")
                    val firebaseMasjidInfo =
                        document.toObject(MosqueInfoDto::class.java)
                    firebaseMasjidInfo?.let { info ->
                        // Handle image caching
                        val localImagePath =
                            info.image.takeIf { it?.isNotEmpty() == true }?.let { url ->
                                val fileName = extractFileNameFromUrl(url)
                                imageCacheManager.downloadAndCacheImage(url, fileName)
                            }

                        // Use mapper to convert Firebase model to Entity
                        val entity =
                            firebaseToEntityMapper.mapMasjidInfoToEntity(info, localImagePath)
                        database.mosqueInfoDao().insertMosque(entity)
                    }
                    Log.d(TAG, "syncMosqueData: Mosque data synced for $mosqueId")
                    SyncResult(true, "Mosque data synced successfully", 1, 0)
                } else {
                    SyncResult(false, "Mosque not found", 0, 1)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing mosque data", e)
                SyncResult(false, "Sync failed: ${e.message}", 0, 1)
            }
        }

    override suspend fun syncPrayerTimes(mosqueId: String): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val document = firestore.collection("MOSQUE_LIST")
                    .document(mosqueId)
                    .collection("Time Range")
                    .document("Current Running Time")
                    .get()
                    .await()

                if (document.exists()) {
                    Log.d("TAGTRACKER", "syncPrayerTimes: Document Exists ${document.data}")
                    val data = document.data
                    val prayerTimeEntities = mutableListOf<PrayerTimeEntity>()

                    data?.forEach { (dateKey, timeData) ->
                        if (timeData is Map<*, *>) {
                            try {
                                val mosqueTime = timeData.toDataClass<MosqueTimeDto>()
                                // Use mapper to convert Firebase model to Entity
                                val entity = firebaseToEntityMapper.mapMosqueTimeToEntity(
                                    mosqueTime, mosqueId, dateKey
                                )
                                prayerTimeEntities.add(entity)
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "Error converting mosque time data for date: $dateKey",
                                    e
                                )
                            }
                        }
                    }

                    // Clear old prayer times and insert new ones
                    database.prayerTimeDao().deletePrayerTimesForMosque(mosqueId)
                    database.prayerTimeDao().insertPrayerTimes(prayerTimeEntities)

                    Log.d(
                        TAG,
                        "syncPrayerTimes: Inserted ${prayerTimeEntities.size} prayer times for mosque $mosqueId"
                    )
                    SyncResult(true, "Prayer times synced successfully", prayerTimeEntities.size, 0)
                } else {
                    SyncResult(false, "Prayer times not found", 0, 1)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing prayer times", e)
                SyncResult(false, "Sync failed: ${e.message}", 0, 1)
            }
        }

    override suspend fun syncMessages(mosqueId: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection("MOSQUE_LIST")
                .document(mosqueId)
                .collection("Messages")
                .document("Current Messages")
                .get()
                .await()

            if (document.exists()) {
                Log.d(
                    "TAGTRACKER", "syncMessages: " +
                            "Document Exists ${document.data}"
                )
                val data = document.data
                val messageEntities = mutableListOf<MosqueMessageEntity>()
                var index = 0

                data?.toSortedMap()?.forEach { (key, url) ->
                    if (url is String) {
                        val fileName = "${mosqueId}_message_${key}"
                        val localPath = imageCacheManager.downloadAndCacheImage(url, fileName)

                        // Use mapper to convert to entity
                        val entity = firebaseToEntityMapper.mapMessageToEntity(
                            messageKey = key,
                            messageUrl = url,
                            mosqueId = mosqueId,
                            orderIndex = index++,
                            localImagePath = localPath
                        )
                        messageEntities.add(entity)
                    }
                }

                // Clear old messages and insert new ones
                database.mosqueMessageDao().deleteMessagesForMosque(mosqueId)
                database.mosqueMessageDao().insertMessages(messageEntities)

                Log.d(
                    TAG,
                    "syncMessages: " + "Inserted ${messageEntities.size} messages for mosque $mosqueId"
                )
                SyncResult(true, "Messages synced successfully", messageEntities.size, 0)
            } else {
                SyncResult(false, "Messages not found", 0, 1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing messages", e)
            SyncResult(false, "Sync failed: ${e.message}", 0, 1)
        }
    }

    override suspend fun fullSync(mosqueId: String): SyncResult = withContext(Dispatchers.IO) {
        Log.d("TAGTRACKER", "fullSync: Implementing full sync for mosque $mosqueId")
        val results = listOf(
            syncMosqueData(mosqueId),
            syncPrayerTimes(mosqueId),
            syncMessages(mosqueId)
        )

        val totalSynced = results.sumOf { it.syncedItems }
        val totalFailed = results.sumOf { it.failedItems }
        val allSuccessful = results.all { it.success }

        Log.d(
            "TAGTRACKER", "fullSync: " +
                    "Sync completed. Success: $allSuccessful, " +
                    "Total Synced: $totalSynced, " +
                    "Total Failed: $totalFailed"
        )
        SyncResult(
            success = allSuccessful,
            message = if (allSuccessful) "Full sync completed successfully"
            else "Partial sync completed with errors",
            syncedItems = totalSynced,
            failedItems = totalFailed
        )
    }

    // ✅ New function: sync mosque by userId
    override suspend fun syncUser(userId: String): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val query = firestore.collection("MOSQUE_LIST")
                    .whereEqualTo("ownerUid", userId)

                query.addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(TAG, "getMyMosqueByUserId: listen:error", e)
                        return@addSnapshotListener
                    }

                    if (snapshots == null) return@addSnapshotListener

                    for (dc in snapshots.documentChanges) {
                        val mosqueInfo = dc.document.toObject(MosqueInfoDto::class.java)
                            .copy(documentId = dc.document.id) // ensure we store Firestore ID

                        when (dc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                Log.d(
                                    TAG,
                                    "getMyMosqueByUserId: Mosque found: ${mosqueInfo.masjidName} (${mosqueInfo.documentId})"
                                )

                                // ✅ Call fullSync with the mosqueId
                                CoroutineScope(Dispatchers.IO).launch {
                                    settingsRepository.setMosqueId(mosqueInfo.documentId)
                                    fullSync(mosqueInfo.documentId)
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                Log.d(
                                    TAG,
                                    "getMyMosqueByUserId: Mosque removed: ${mosqueInfo.documentId}"
                                )
                            }
                        }
                    }
                }

                SyncResult(
                    success = true,
                    message = "Listening for mosque updates",
                    syncedItems = 1,
                    failedItems = 0
                )
            } catch (e: Exception) {
                Log.e(TAG, "getMyMosqueByUserId: Failed", e)
                SyncResult(
                    success = false,
                    message = "Failed: ${e.message}",
                    syncedItems = 0,
                    failedItems = 1
                )
            }
        }

    private fun extractFileNameFromUrl(url: String): String {
        return url.substringAfterLast("/").substringBefore("?")
    }
}
