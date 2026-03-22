package com.rpn.salatetime.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rpn.salatetime.domain.model.MosqueEntity
import com.rpn.salatetime.domain.model.MosqueSlideEntity
import com.rpn.salatetime.domain.model.PrayerTimeEntity


@Database(
    entities = [
        MosqueEntity::class,
        PrayerTimeEntity::class,
        MosqueSlideEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MosqueDatabase : RoomDatabase() {
    abstract fun mosqueDao(): MosqueDao

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