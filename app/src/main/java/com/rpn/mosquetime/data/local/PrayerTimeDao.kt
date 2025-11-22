package com.rpn.mosquetime.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rpn.mosquetime.data.local.entity.PrayerTimeEntity
import com.rpn.mosquetime.domain.manager.SyncStatus


@Dao
interface PrayerTimeDao {
    @Query("SELECT * FROM prayer_times WHERE mosqueId = :mosqueId ORDER BY date DESC")
    suspend fun getPrayerTimes(mosqueId: String): List<PrayerTimeEntity>

    @Query("SELECT * FROM prayer_times WHERE mosqueId = :mosqueId AND date = :date")
    suspend fun getPrayerTimeForDate(mosqueId: String, date: String): PrayerTimeEntity?

    @Query("SELECT * FROM prayer_times WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingPrayerTimes(): List<PrayerTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(prayerTime: PrayerTimeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(prayerTimes: List<PrayerTimeEntity>)

    @Update
    suspend fun updatePrayerTime(prayerTime: PrayerTimeEntity)

    @Query("UPDATE prayer_times SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("DELETE FROM prayer_times WHERE mosqueId = :mosqueId")
    suspend fun deletePrayerTimesForMosque(mosqueId: String)
}