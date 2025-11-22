package com.rpn.mosquetime.di

import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.rpn.mosquetime.data.local.MosqueDatabase
import com.rpn.mosquetime.data.manager.FirestoreSyncManagerWithMappers
import com.rpn.mosquetime.data.manager.ImageCacheManager
import com.rpn.mosquetime.data.manager.NetworkConnectivityObserver
import com.rpn.mosquetime.data.mapper.FirebaseToEntityMapper
import com.rpn.mosquetime.data.mapper.MessageMapper
import com.rpn.mosquetime.data.mapper.MosqueInfoMapper
import com.rpn.mosquetime.data.mapper.PrayerTimeMapper
import com.rpn.mosquetime.data.repository.MainRepositoryImpl
import com.rpn.mosquetime.data.repository.OfflineFirstRepositoryWithMappers
import com.rpn.mosquetime.domain.manager.ConnectivityObserver
import com.rpn.mosquetime.domain.manager.SyncManager
import com.rpn.mosquetime.domain.repository.MainRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val appModule = module {

    // Database
    single { MosqueDatabase.getDatabase(androidContext()) }
    single { get<MosqueDatabase>().mosqueInfoDao() }
    single { get<MosqueDatabase>().prayerTimeDao() }
    single { get<MosqueDatabase>().mosqueMessageDao() }

    // Connectivity
    single<ConnectivityObserver> {
        NetworkConnectivityObserver(androidContext())
    }

    // Image Cache
    single { ImageCacheManager(androidContext()) }

    // Mappers
    single { MosqueInfoMapper() }
    single { PrayerTimeMapper() }
    single { MessageMapper() }
    single { FirebaseToEntityMapper() }

    // Sync Manager with Mappers
    single<SyncManager> {
        FirestoreSyncManagerWithMappers(
            firestore = FirebaseFirestore.getInstance(),
            database = get(),
            imageCacheManager = get(),
            firebaseToEntityMapper = get(),
            settingsRepository = get()
        )
    }

    // Repository with Mappers
    single {
        OfflineFirstRepositoryWithMappers(
            database = get(),
            syncManager = get(),
            connectivityObserver = get(),
            imageCacheManager = get(),
            mosqueInfoMapper = get(),
            prayerTimeMapper = get(),
            messageMapper = get()
        )
    }

    single<MainRepository> {
        MainRepositoryImpl(
            offlineFirstRepository = get(),
            workManager = WorkManager.getInstance(androidContext()),
            context = androidContext()
        )
    }
}
