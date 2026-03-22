package com.rpn.salatetime.di

import com.rpn.salatetime.data.local.datastore.SettingsRepository
import com.rpn.salatetime.data.remote.SupabaseRealtimeListener
import com.rpn.salatetime.data.repository.AuthRepositoryImpl
import com.rpn.salatetime.data.repository.ImageRepositoryImpl
import com.rpn.salatetime.data.repository.MosqueRepository
import com.rpn.salatetime.data.repository.PrayerTimeRepositoryImpl
import com.rpn.salatetime.data.repository.TimeRepositoryImpl
import com.rpn.salatetime.domain.repository.AuthRepository
import com.rpn.salatetime.domain.repository.ImageRepository
import com.rpn.salatetime.domain.repository.PrayerTimeRepository
import com.rpn.salatetime.domain.repository.TimeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { SupabaseRealtimeListener(get(),get(),get()) }
    single<PrayerTimeRepository> { PrayerTimeRepositoryImpl(get()) }
    single { MosqueRepository(get(),get()) }
    single { SettingsRepository(androidContext()) }

    single<AuthRepository> { AuthRepositoryImpl(get(),get()) }
    single<TimeRepository> { TimeRepositoryImpl(get()) }
    single<ImageRepository> { ImageRepositoryImpl(androidContext(),get()) }
}
