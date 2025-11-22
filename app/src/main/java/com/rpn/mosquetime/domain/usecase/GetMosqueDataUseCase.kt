package com.rpn.mosquetime.domain.usecase

import com.rpn.mosquetime.domain.repository.MainRepository

class GetMosqueDataUseCase(private val repository: MainRepository) {

    suspend operator fun invoke() = repository.getCompleteMosqueInfo("86ohJRfbniiVd3vapwHL")
}
