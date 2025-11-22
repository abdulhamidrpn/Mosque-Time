package com.rpn.mosquetime.data.local

import androidx.room.TypeConverter
import com.rpn.mosquetime.domain.manager.SyncStatus

// Type Converters
class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus = SyncStatus.valueOf(status)
}