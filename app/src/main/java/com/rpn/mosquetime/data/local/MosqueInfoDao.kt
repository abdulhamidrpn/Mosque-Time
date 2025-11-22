package com.rpn.mosquetime.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rpn.mosquetime.data.local.entity.MosqueInfoEntity
import com.rpn.mosquetime.domain.manager.SyncStatus

@Dao
interface MosqueInfoDao {
    @Query("SELECT * FROM mosque_info WHERE documentId = :mosqueId")
    suspend fun getMosque(mosqueId: String): MosqueInfoEntity?

    @Query("SELECT * FROM mosque_info WHERE ownerUid = :ownerUid")
    suspend fun getMosqueByOwner(ownerUid: String): MosqueInfoEntity?

    @Query("SELECT * FROM mosque_info WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingMosques(): List<MosqueInfoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMosque(mosque: MosqueInfoEntity)

    @Update
    suspend fun updateMosque(mosque: MosqueInfoEntity)

    @Query("UPDATE mosque_info SET syncStatus = :status WHERE documentId = :mosqueId")
    suspend fun updateSyncStatus(mosqueId: String, status: SyncStatus)

    @Query("DELETE FROM mosque_info WHERE documentId = :mosqueId")
    suspend fun deleteMosque(mosqueId: String)
}