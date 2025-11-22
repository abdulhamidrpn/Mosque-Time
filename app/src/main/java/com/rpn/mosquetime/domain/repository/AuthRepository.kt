package com.rpn.mosquetime.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Flow<Result<FirebaseUser>>
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Result<FirebaseUser>>

    suspend fun logout()
    fun getCurrentUser(): Flow<FirebaseUser?>
}
