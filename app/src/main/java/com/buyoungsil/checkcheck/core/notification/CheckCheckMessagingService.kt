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
import com.buyoungsil.checkcheck.core.domain.usecase.UpdateFcmTokenUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * FCM 메시지 수신 서비스
 *
 * ✅ Hilt를 통한 의존성 주입
 * ✅ 토큰 자동 저장
 * ✅ task_created 알림 수신 시 WorkManager 등록
 */
@AndroidEntryPoint
class CheckCheckMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var taskReminderScheduler: TaskReminderScheduler  // ✅ 추가

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "CheckCheckFCM"
        private const val CHANNEL_ID = "checkcheck_fcm"
        private const val NOTIFICATION_ID = 100
    }

    /**
     * ✅ FCM 토큰이 생성/갱신될 때 호출
     * Firestore에 자동 저장
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 새 FCM 토큰 생성: $token")

        // 현재 로그인된 사용자의 토큰 저장
        val userId = auth.currentUser?.uid
        if (userId != null) {
            serviceScope.launch {
                try {
                    updateFcmTokenUseCase(userId, token)
                    Log.d(TAG, "✅ FCM 토큰 Firestore 저장 완료")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ FCM 토큰 저장 실패", e)
                }
            }
        } else {
            Log.w(TAG, "⚠️ 로그인 안 된 상태 - 토큰 저장 건너뜀")
        }
    }

    /**
     * FCM 메시지 수신 시 호출
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📨 FCM 메시지 수신")
        Log.d(TAG, "   From: ${remoteMessage.from}")
        Log.d(TAG, "   Data: ${remoteMessage.data}")

        // ✅ task_created 타입인 경우 WorkManager 등록
        val messageType = remoteMessage.data["type"]
        if (messageType == "task_created") {
            handleTaskCreatedNotification(remoteMessage.data)
        }

        // 알림 채널 생성
        createNotificationChannel()

        // 알림 표시
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "CheckCheck"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "새 알림이 도착했습니다"

        showNotification(title, body, remoteMessage.data)
    }

    /**
     * ✅ 할일 생성 알림 처리 - WorkManager 등록
     */
    private fun handleTaskCreatedNotification(data: Map<String, String>) {
        try {
            val taskId = data["taskId"] ?: return
            val taskTitle = data["taskTitle"] ?: return
            val groupName = data["groupName"] ?: "그룹"
            val dueDateStr = data["dueDate"] ?: return  // "2025-11-15"
            val dueTimeStr = data["dueTime"]  // "14:30" or null
            val reminderEnabled = data["reminderEnabled"]?.toBoolean() ?: false
            val reminderMinutesBefore = data["reminderMinutesBefore"]?.toInt() ?: 60

            if (!reminderEnabled) {
                Log.d(TAG, "⏭️ 알림 비활성화 상태 - WorkManager 등록 건너뜀")
                return
            }

            // LocalDateTime 생성
            val dueDate = LocalDate.parse(dueDateStr)
            val dueTime = if (dueTimeStr != null) {
                LocalTime.parse(dueTimeStr)
            } else {
                LocalTime.of(23, 59)
            }
            val dueDateTime = LocalDateTime.of(dueDate, dueTime)

            // WorkManager 등록
            taskReminderScheduler.scheduleTaskReminder(
                taskId = taskId,
                taskTitle = taskTitle,
                groupName = groupName,
                dueDateTime = dueDateTime,
                minutesBefore = reminderMinutesBefore
            )

            Log.d(TAG, "✅ WorkManager 등록 완료: $taskTitle (${reminderMinutesBefore}분 전)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ WorkManager 등록 실패", e)
        }
    }

    /**
     * 알림 채널 생성 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CheckCheck 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "FCM 푸시 알림"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 알림 표시
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // 알림 데이터에서 navigation 정보 추출
            data["groupId"]?.let { putExtra("groupId", it) }
            data["taskId"]?.let { putExtra("taskId", it) }
            data["habitId"]?.let { putExtra("habitId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        Log.d(TAG, "✅ 알림 표시 완료: $title")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext.cancel()
    }
}