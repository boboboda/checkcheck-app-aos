package com.buyoungsil.checkcheck

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CheckCheckApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Firebase 초기화
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "✅ Firebase 초기화 성공")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firebase 초기화 실패", e)
        }

        // FCM 알림 채널 생성 (Android 8.0+)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "checkcheck_channel"
            val channelName = "CheckCheck 알림"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "가족 습관 및 할일 알림"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "✅ 알림 채널 생성 완료")
        }
    }

    companion object {
        private const val TAG = "CheckCheckApp"
    }
}