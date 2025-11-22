package com.rpn.mosquetime.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rpn.mosquetime.data.local.entity.MosqueMessageEntity
import com.rpn.mosquetime.domain.manager.SyncStatus

@Dao
interface MosqueMessageDao {
    @Query("SELECT * FROM mosque_messages WHERE mosqueId = :mosqueId ORDER BY orderIndex ASC")
    suspend fun getMessages(mosqueId: String): List<MosqueMessageEntity>

    @Query("SELECT * FROM mosque_messages WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingMessages(): List<MosqueMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MosqueMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MosqueMessageEntity>)

    @Update
    suspend fun updateMessage(message: MosqueMessageEntity)

    @Query("UPDATE mosque_messages SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("DELETE FROM mosque_messages WHERE mosqueId = :mosqueId")
    suspend fun deleteMessagesForMosque(mosqueId: String)
}