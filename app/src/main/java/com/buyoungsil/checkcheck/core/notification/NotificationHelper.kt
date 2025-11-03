package com.buyoungsil.checkcheck.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.buyoungsil.checkcheck.MainActivity
import com.buyoungsil.checkcheck.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 관리 헬퍼
 * ✅ Task 알림 추가
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_TASK_REMINDER = "task_reminder"
        const val CHANNEL_ID_GROUP_ACTIVITY = "group_activity"
        const val CHANNEL_ID_ACHIEVEMENT = "achievement"

        const val NOTIFICATION_ID_TASK_BASE = 1000
        const val NOTIFICATION_ID_GROUP_BASE = 2000
        const val NOTIFICATION_ID_ACHIEVEMENT_BASE = 3000
    }

    init {
        createNotificationChannels()
    }

    /**
     * 알림 채널 생성 (Android 8.0+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_TASK_REMINDER,
                    "할일 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "할일 마감 알림"
                    enableVibration(true)
                },

                NotificationChannel(
                    CHANNEL_ID_GROUP_ACTIVITY,
                    "그룹 활동",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "그룹 멤버 활동 알림"
                    enableVibration(true)
                },

                NotificationChannel(
                    CHANNEL_ID_ACHIEVEMENT,
                    "달성 축하",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "습관 달성 축하 알림"
                    enableVibration(true)
                }
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    /**
     * 할일 마감 알림 표시
     * ✅ 새로 추가
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTaskReminder(
        taskId: String,
        taskTitle: String,
        groupName: String,
        minutesLeft: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeText = when {
            minutesLeft >= 60 -> "${minutesLeft / 60}시간 후"
            else -> "${minutesLeft}분 후"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASK_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ $groupName - 할일 마감 알림")
            .setContentText("'$taskTitle' $timeText 마감이에요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationId = NOTIFICATION_ID_TASK_BASE + taskId.hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * 그룹 활동 알림 표시
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showGroupActivity(
        groupId: String,
        groupName: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("groupId", groupId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            groupId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GROUP_ACTIVITY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(groupName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = NOTIFICATION_ID_GROUP_BASE + groupId.hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * 달성 축하 알림 표시
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAchievement(
        habitId: String,
        habitTitle: String,
        streakDays: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ACHIEVEMENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🎉 축하합니다!")
            .setContentText("'$habitTitle' $streakDays 일 연속 달성!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = NOTIFICATION_ID_ACHIEVEMENT_BASE + habitId.hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}