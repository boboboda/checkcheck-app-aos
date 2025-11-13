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
 * ✅ 완전 재구현 버전
 */
@AndroidEntryPoint
class CheckCheckMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var taskReminderScheduler: TaskReminderScheduler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "CheckCheckFCM"
        private const val CHANNEL_ID = "checkcheck_notifications"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    /**
     * ✅ FCM 토큰 생성/갱신 시 호출
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "========================================")
        Log.d(TAG, "🔑 FCM 토큰 생성/갱신")
        Log.d(TAG, "토큰: ${token.take(50)}...")
        Log.d(TAG, "========================================")

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
            Log.w(TAG, "⚠️ 사용자 미로그인 - 토큰 저장 보류")
        }
    }

    /**
     * ✅ FCM 메시지 수신 시 호출
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "========================================")
        Log.d(TAG, "📨 FCM 메시지 수신!")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Notification: ${remoteMessage.notification}")
        Log.d(TAG, "Data: ${remoteMessage.data}")
        Log.d(TAG, "========================================")

        // 알림 채널 생성
        createNotificationChannel()

        // 메시지 타입에 따라 처리
        val messageType = remoteMessage.data["type"] ?: ""
        Log.d(TAG, "메시지 타입: $messageType")

        when (messageType) {
            "task_created" -> {
                Log.d(TAG, "→ 할일 생성 알림 처리")
                handleTaskCreated(remoteMessage)
            }
            "task_completed" -> {
                Log.d(TAG, "→ 할일 완료 알림 처리")
                handleTaskCompleted(remoteMessage)
            }
            "habit_checked" -> {
                Log.d(TAG, "→ 습관 체크 알림 처리")
                handleHabitChecked(remoteMessage)
            }
            "member_joined" -> {
                Log.d(TAG, "→ 멤버 참여 알림 처리")
                handleMemberJoined(remoteMessage)
            }
            else -> {
                Log.d(TAG, "→ 기본 알림 처리")
                showBasicNotification(remoteMessage)
            }
        }
    }

    /**
     * 할일 생성 알림 처리
     */
    private fun handleTaskCreated(remoteMessage: RemoteMessage) {
        try {
            val data = remoteMessage.data
            val taskId = data["taskId"] ?: return
            val taskTitle = data["taskTitle"] ?: return
            val groupName = data["groupName"] ?: "그룹"

            Log.d(TAG, "할일 정보:")
            Log.d(TAG, "  - taskId: $taskId")
            Log.d(TAG, "  - taskTitle: $taskTitle")
            Log.d(TAG, "  - groupName: $groupName")

            // 1. 즉시 알림 표시
            val title = "${groupName} - 새 할일"
            val body = remoteMessage.notification?.body ?: "'$taskTitle' 할일이 등록되었습니다"

            showNotification(
                notificationId = NOTIFICATION_ID_BASE + taskId.hashCode(),
                title = title,
                body = body,
                data = data
            )
            Log.d(TAG, "✅ 즉시 알림 표시 완료")

            // 2. WorkManager 등록 (마감 알림용)
            val dueDateStr = data["dueDate"]
            val dueTimeStr = data["dueTime"]
            val reminderEnabled = data["reminderEnabled"]?.toBoolean() ?: false
            val reminderMinutesBefore = data["reminderMinutesBefore"]?.toInt() ?: 60

            Log.d(TAG, "알림 설정:")
            Log.d(TAG, "  - dueDate: $dueDateStr")
            Log.d(TAG, "  - dueTime: $dueTimeStr")
            Log.d(TAG, "  - reminderEnabled: $reminderEnabled")
            Log.d(TAG, "  - reminderMinutesBefore: $reminderMinutesBefore")

            if (reminderEnabled && dueDateStr != null) {
                val dueDate = LocalDate.parse(dueDateStr)
                val dueTime = if (dueTimeStr != null && dueTimeStr.isNotEmpty()) {
                    LocalTime.parse(dueTimeStr)
                } else {
                    LocalTime.of(23, 59)
                }
                val dueDateTime = LocalDateTime.of(dueDate, dueTime)

                taskReminderScheduler.scheduleTaskReminder(
                    taskId = taskId,
                    taskTitle = taskTitle,
                    groupName = groupName,
                    dueDateTime = dueDateTime,
                    minutesBefore = reminderMinutesBefore
                )
                Log.d(TAG, "✅ WorkManager 등록 완료: $taskTitle (${reminderMinutesBefore}분 전)")
            } else {
                Log.d(TAG, "⏭️ 알림 비활성화 또는 마감일 없음 - WorkManager 등록 건너뜀")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 할일 생성 알림 처리 실패", e)
        }
    }

    /**
     * 할일 완료 알림 처리
     * ✅ 워커 취소 기능 추가
     */
    private fun handleTaskCompleted(remoteMessage: RemoteMessage) {
        try {
            val data = remoteMessage.data
            val taskId = data["taskId"] ?: ""

            Log.d(TAG, "할일 완료 알림 수신:")
            Log.d(TAG, "  - taskId: $taskId")

            // ✅ 1. WorkManager 워커 취소
            if (taskId.isNotEmpty()) {
                Log.d(TAG, "📌 WorkManager 취소 시도")
                taskReminderScheduler.cancelTaskReminder(taskId)
                Log.d(TAG, "✅ WorkManager 취소 완료")
            } else {
                Log.w(TAG, "⚠️ taskId가 비어있음 - 워커 취소 건너뜀")
            }

            // ✅ 2. 즉시 알림 표시
            val title = remoteMessage.notification?.title ?: "할일 완료"
            val body = remoteMessage.notification?.body ?: "멤버가 할일을 완료했습니다"

            showNotification(
                notificationId = NOTIFICATION_ID_BASE + 1,
                title = title,
                body = body,
                data = data
            )
            Log.d(TAG, "✅ 할일 완료 알림 표시 완료")

        } catch (e: Exception) {
            Log.e(TAG, "❌ 할일 완료 알림 처리 실패", e)
        }
    }

    /**
     * 습관 체크 알림 처리
     */
    private fun handleHabitChecked(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "습관 달성"
        val body = remoteMessage.notification?.body ?: "멤버가 습관을 완료했습니다"

        showNotification(
            notificationId = NOTIFICATION_ID_BASE + 2,
            title = title,
            body = body,
            data = remoteMessage.data
        )
        Log.d(TAG, "✅ 습관 체크 알림 표시 완료")
    }

    /**
     * 멤버 참여 알림 처리
     */
    private fun handleMemberJoined(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "새 멤버"
        val body = remoteMessage.notification?.body ?: "새 멤버가 그룹에 참여했습니다"

        showNotification(
            notificationId = NOTIFICATION_ID_BASE + 3,
            title = title,
            body = body,
            data = remoteMessage.data
        )
        Log.d(TAG, "✅ 멤버 참여 알림 표시 완료")
    }

    /**
     * 기본 알림 처리
     */
    private fun showBasicNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "CheckCheck"
        val body = remoteMessage.notification?.body ?: "새 알림이 도착했습니다"

        showNotification(
            notificationId = NOTIFICATION_ID_BASE,
            title = title,
            body = body,
            data = remoteMessage.data
        )
        Log.d(TAG, "✅ 기본 알림 표시 완료")
    }

    /**
     * 알림 채널 생성
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CheckCheck 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "그룹 활동 및 할일 알림"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "✅ 알림 채널 생성 완료")
        }
    }

    /**
     * 알림 표시
     */
    private fun showNotification(
        notificationId: Int,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data["groupId"]?.let { putExtra("groupId", it) }
            data["taskId"]?.let { putExtra("taskId", it) }
            data["habitId"]?.let { putExtra("habitId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
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
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "✅ 알림 표시: $title")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext.cancel()
        Log.d(TAG, "서비스 종료")
    }
}