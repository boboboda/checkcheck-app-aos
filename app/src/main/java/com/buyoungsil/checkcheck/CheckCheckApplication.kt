package com.buyoungsil.checkcheck

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import java.security.MessageDigest
import javax.inject.Inject
import androidx.work.Configuration

@HiltAndroidApp
class CheckCheckApplication() : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()


        // ✅ 키 해시 출력 (개발용)
        printKeyHash()

        // Firebase 초기화
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "✅ Firebase 초기화 성공")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firebase 초기화 실패", e)
        }

        // ✅ 카카오 SDK 초기화
        try {
            val kakaoAppKey = BuildConfig.KAKAO_NATIVE_APP_KEY
            if (kakaoAppKey.isNotEmpty() && kakaoAppKey != "\"\"" && kakaoAppKey != "null") {
                KakaoSdk.init(this, kakaoAppKey)
                Log.d(TAG, "✅ 카카오 SDK 초기화 완료: $kakaoAppKey")
            } else {
                Log.w(TAG, "⚠️ 카카오 앱 키가 설정되지 않았습니다")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 카카오 SDK 초기화 실패", e)
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

    // ✅ WorkManager Configuration 제공 (HiltWorkerFactory 사용)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    companion object {
        private const val TAG = "CheckCheckApp"
    }


    /**
     * 키 해시 출력 (카카오 개발자 콘솔에 등록용)
     */
    private fun printKeyHash() {
        try {
            val info = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d(TAG, "🔑 키 해시: $keyHash")
            }
        } catch (e: Exception) {
            Log.e(TAG, "키 해시 생성 실패", e)
        }
    }
}