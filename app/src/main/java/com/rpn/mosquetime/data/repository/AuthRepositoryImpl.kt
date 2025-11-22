package com.rpn.mosquetime.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.rpn.mosquetime.domain.repository.AuthRepository
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val settingsRepository: SettingsRepository
) : AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun login(email: String, password: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading())
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Save user in DataStore
                settingsRepository.setUser(
                    id = user.uid,
                    email = user.email ?: "",
                    userName = user.displayName ?: ""
                )
                emit(Result.Success(data = user))
            } else {
                emit(Result.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Login failed"))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Flow<Result<FirebaseUser>> = flow {
        emit(Result.Loading())
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Save user in DataStore
                settingsRepository.setUser(
                    id = user.uid,
                    email = user.email ?: "",
                    userName = user.displayName ?: name
                )
                emit(Result.Success(data = user))
            } else {
                emit(Result.Error("Registration failed"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Registration failed"))
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        settingsRepository.clearUserData()
    }

    override fun getCurrentUser(): Flow<FirebaseUser?> = flow {
        emit(firebaseAuth.currentUser)
    }
}