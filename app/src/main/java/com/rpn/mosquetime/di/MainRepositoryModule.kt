

package com.rpn.mosquetime.di

import com.google.firebase.firestore.FirebaseFirestore
import com.rpn.mosquetime.data.repository.AuthRepositoryImpl
import com.rpn.mosquetime.data.repository.SettingsRepository
import com.rpn.mosquetime.data.repository.TimeRepositoryImpl
import com.rpn.mosquetime.domain.repository.AuthRepository
import com.rpn.mosquetime.domain.repository.TimeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mainRepositoryModule = module {
    single { FirebaseFirestore.getInstance() }
    single { SettingsRepository(androidContext()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<TimeRepository> { TimeRepositoryImpl() }
}
