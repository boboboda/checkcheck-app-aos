package com.buyoungsil.checkcheck.core.notification.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.buyoungsil.checkcheck.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 할일 알림 워커
 * ✅ Task 마감 시간 알림
 */
@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TASK_ID = "taskId"
        const val KEY_TASK_TITLE = "taskTitle"
        const val KEY_GROUP_NAME = "groupName"
        const val KEY_MINUTES_LEFT = "minutesLeft"

        const val WORK_TAG_PREFIX = "task_reminder_"
    }

    override suspend fun doWork(): Result {
        return try {
            // Android 13+ 권한 체크
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    return Result.failure()
                }
            }

            val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
            val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()
            val groupName = inputData.getString(KEY_GROUP_NAME) ?: "그룹"
            val minutesLeft = inputData.getInt(KEY_MINUTES_LEFT, 60)

            // 알림 표시
            notificationHelper.showTaskReminder(
                taskId = taskId,
                taskTitle = taskTitle,
                groupName = groupName,
                minutesLeft = minutesLeft
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}