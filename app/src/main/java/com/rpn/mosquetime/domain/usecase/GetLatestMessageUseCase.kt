package com.rpn.mosquetime.domain.usecase

import com.rpn.mosquetime.domain.repository.MainRepository
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetLatestMessageUseCase(
    private val repository: MainRepository
) {
    suspend operator fun invoke(mosqueId: String): Flow<Result<String>> {
        return repository.getMessages(mosqueId).map {
            when (it) {
                is Result.Success -> {
                    val latestMessage = it.data?.firstOrNull()?.message ?: ""
                    Result.Success(latestMessage)
                }
                is Result.Error -> {
                    Result.Error(it.message ?: "Unknown error")
                }
                is Result.Loading -> {
                    Result.Loading()
                }
            }
        }
    }
}
