package com.rpn.mosquetime.di


import android.app.Application
import coil.ImageLoader
import coil.disk.DiskCache
import com.rpn.mosquetime.data.repository.ImageRepositoryImpl
import com.rpn.mosquetime.domain.repository.ImageRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val imageModule = module {

    single {
        ImageLoader.Builder(androidContext())
            .diskCache {
                DiskCache.Builder()
                    .directory(androidContext().cacheDir.resolve("coil_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50MB
                    .build()
            }
            .crossfade(true)
            .allowHardware(false)
            .build()
    }

    single<ImageRepository> {
        ImageRepositoryImpl(
            context = androidContext(),
            imageLoader = get()
        )
    }
}
