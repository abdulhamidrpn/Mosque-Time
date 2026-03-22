package com.rpn.salatetime.data.repository

import com.rpn.salatetime.data.local.db.MosqueDao
import com.rpn.salatetime.data.remote.SupabaseRealtimeListener
import com.rpn.salatetime.domain.model.MosqueCompositeData
import com.rpn.salatetime.domain.model.MosqueEntity
import com.rpn.salatetime.domain.model.MosqueSlideEntity
import com.rpn.salatetime.domain.model.PrayerTimeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

class MosqueRepository(
    private val mosqueDao: MosqueDao,
    private val realtimeListener: SupabaseRealtimeListener,
) {

    // ── READ: reactive Room streams ───────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMosqueCombineDataByUid(ownerUid: String, date: String): Flow<MosqueCompositeData?> {
        return mosqueDao.getMosqueByOwnerUid(ownerUid)
            .flatMapLatest { mosque ->
                if (mosque == null) {
                    flowOf(null)
                } else {
                    combine(
                        mosqueDao.getPrayerTimeForDate(mosque.id,date),
                        mosqueDao.getSlides(mosque.id),
                    ) { todayPrayer, slides ->
                        MosqueCompositeData(
                            mosque = mosque,
                            todayPrayerTime = todayPrayer,
                            slides = slides)
                    }
                }
            }
            .flowOn(Dispatchers.IO)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMosqueDataByUid(ownerUid: String): Flow<MosqueEntity?> {
        return mosqueDao.getMosqueByOwnerUid(ownerUid)
            .flowOn(Dispatchers.IO)
    }

    fun getSlides(mosqueId: String): Flow<List<MosqueSlideEntity>> {
        return mosqueDao.getSlides(mosqueId)
            .flowOn(Dispatchers.IO)
    }

    fun getPrayerTimeByDate(mosqueId: String, date: String): Flow<PrayerTimeEntity?> {
        return mosqueDao.getPrayerTimeForDate(mosqueId, date)
            .flowOn(Dispatchers.IO)
    }


    // ── WRITE / SYNC lifecycle ────────────────────────────────────────────────

    suspend fun updateMosque(mosque: MosqueEntity) {
        mosqueDao.insertMosque(mosque)
    }

    suspend fun initializeSync(ownerUid: String) {
        realtimeListener.startListening(ownerUid)
    }

    suspend fun stopSync() {
        realtimeListener.stopListening()
    }
}
