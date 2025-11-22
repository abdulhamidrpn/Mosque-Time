package com.rpn.mosquetime.di

import com.rpn.mosquetime.domain.usecase.GetMosqueDataUseCase
import com.rpn.mosquetime.domain.usecase.LoginUserUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { GetMosqueDataUseCase(get()) }
    factory { LoginUserUseCase(get()) }
    // Add other use cases: GetPlaylistDetails, RemoveFromPlaylist, etc.
}