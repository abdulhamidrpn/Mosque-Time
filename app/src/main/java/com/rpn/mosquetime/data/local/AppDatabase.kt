package com.rpn.mosquetime.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rpn.mosquetime.data.local.entity.MosqueInfoEntity
import com.rpn.mosquetime.data.local.entity.MosqueMessageEntity
import com.rpn.mosquetime.data.local.entity.PrayerTimeEntity


@Database(
    entities = [
        MosqueInfoEntity::class,
        PrayerTimeEntity::class,
        MosqueMessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MosqueDatabase : RoomDatabase() {
    abstract fun mosqueInfoDao(): MosqueInfoDao
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun mosqueMessageDao(): MosqueMessageDao

    companion object {
        @Volatile
        private var INSTANCE: MosqueDatabase? = null

        fun getDatabase(context: Context): MosqueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MosqueDatabase::class.java,
                    "mosque_database"
                )
                    .addMigrations()
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}