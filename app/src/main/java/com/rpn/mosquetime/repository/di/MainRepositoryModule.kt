package com.rpn.mosquetime.repository.di

import com.rpn.mosquetime.repository.AuthAppRepository
import com.rpn.mosquetime.repository.FireStoreRepository
import org.koin.dsl.module

/**
 * Repository dependency injection module.
 */
val mainRepositoryModule = module {
    // Repositories
    single { AuthAppRepository(get(),get()) }
    single { FireStoreRepository() }
}
