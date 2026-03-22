package com.rpn.salatetime.data.repository

import com.rpn.salatetime.data.remote.SupabaseRealtimeListener
import com.rpn.salatetime.domain.repository.PrayerTimeRepository

class PrayerTimeRepositoryImpl(
    private val supabaseRealtimeListener: SupabaseRealtimeListener
): PrayerTimeRepository {
    val ownerUid = "fe0e1fb2-e00f-42df-b9d9-b87e188ace46"
    override suspend fun startRealtimeSync() {
        supabaseRealtimeListener.startListening(ownerUid)
    }

    override suspend fun stopRealtimeSync() {
        supabaseRealtimeListener.stopListening()
    }

    override suspend fun getPrayerTimeForDate(date: String) {
        TODO("Not yet implemented")
    }

    override suspend fun fetchAndCacheRange(fromDate: String, toDate: String) {
        TODO("Not yet implemented")
    }

}