package com.buyoungsil.checkcheck

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application 클래스
 * ✅ WorkManager 수동 초기화 추가
 */
@HiltAndroidApp
class CheckCheckApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // WorkManager 초기화는 getWorkManagerConfiguration에서 자동으로 됨
    }

    // ✅ WorkManager Configuration 제공
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}