package com.rpn.salatetime.domain.repository

import com.rpn.salatetime.utils.Result
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Flow<Result<UserInfo>>
    suspend fun register(
        name: String = "admin",
        email: String,
        password: String
    ): Flow<com.rpn.salatetime.utils.Result<UserInfo>>

    suspend fun logout()
    fun getCurrentUser(): Flow<UserInfo?>
}
