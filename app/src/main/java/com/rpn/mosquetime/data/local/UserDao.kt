package com.rpn.mosquetime.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rpn.mosquetime.domain.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): User?

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()
}