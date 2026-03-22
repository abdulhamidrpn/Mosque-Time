package com.rpn.salatetime

import android.app.Application
import com.rpn.salatetime.di.appModule
import com.rpn.salatetime.di.networkModule
import com.rpn.salatetime.di.repositoryModule
import com.rpn.salatetime.di.viewModelModule
import com.rpn.salatetime.ui.theme.AppStrings
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class SalateTimeApp : Application() {


    override fun onCreate() {
        super.onCreate()

        val modules =
            listOf(
                appModule,
                networkModule,
                viewModelModule,
                repositoryModule
                /*imageModule,
                mainRepositoryModule, useCaseModule, utilsModule*/
            )
        startKoin {
            androidContext(this@SalateTimeApp)
            modules(modules)
        }

        // Logging
        /*if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }*/
        Timber.plant(Timber.DebugTree())
        Timber.i("PrayerTimesApp initialised")
    }
}
