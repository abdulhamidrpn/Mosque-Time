package com.rpn.mosquetime.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.rpn.mosquetime.domain.repository.AuthRepository
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.Flow

class LoginUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Flow<Result<FirebaseUser>> {
        return repository.login(email, password)
    }
}