package com.buyoungsil.checkcheck.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.buyoungsil.checkcheck.MainActivity
import com.buyoungsil.checkcheck.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM 메시지 수신 서비스
 *
 * 역할:
 * 1. FCM 토큰 생성/갱신 처리 (onNewToken)
 * 2. 푸시 알림 수신 및 표시 (onMessageReceived)
 */
class CheckCheckMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "CheckCheckFCM"
        private const val CHANNEL_ID = "checkcheck_fcm"
        private const val NOTIFICATION_ID = 100
    }

    /**
     * FCM 토큰이 생성/갱신될 때 호출
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 새 FCM 토큰: $token")

        // TODO: 서버에 토큰 저장 (필요시)
        // TODO: Firestore에 토큰 저장 (그룹 알림용)
    }

    /**
     * FCM 메시지를 수신했을 때 호출
     *
     * 이 함수에서 알림을 직접 만들어야 해!
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📨 FCM 메시지 수신: ${remoteMessage.notification?.title}")

        // ✅ 알림 채널 생성 (없으면)
        createNotificationChannel()

        // ✅ 알림 내용 추출
        val title = remoteMessage.notification?.title ?: "CheckCheck"
        val body = remoteMessage.notification?.body ?: "새 알림이 도착했습니다"

        // ✅ 알림 표시
        showNotification(title, body)
    }

    /**
     * 알림 채널 생성 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CheckCheck 알림"
            val descriptionText = "FCM 푸시 알림"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "✅ 알림 채널 생성 완료: $CHANNEL_ID")
        }
    }

    /**
     * 알림 표시
     */
    private fun showNotification(title: String, body: String) {
        // 앱을 여는 Intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌드
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)  // 알림 아이콘
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)  // 클릭하면 알림 사라짐
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))  // 긴 텍스트 지원
            .build()

        // 알림 매니저로 알림 표시
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        Log.d(TAG, "✅ 알림 표시 완료: $title")
    }
}