package com.buyoungsil.checkcheck.core.notification.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
        private const val TAG = "TaskReminderWorker"
        const val KEY_TASK_ID = "taskId"
        const val KEY_TASK_TITLE = "taskTitle"
        const val KEY_GROUP_NAME = "groupName"
        const val KEY_MINUTES_LEFT = "minutesLeft"

        const val WORK_TAG_PREFIX = "task_reminder_"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "========================================")
        Log.d(TAG, "⏰ TaskReminderWorker 실행!")
        Log.d(TAG, "========================================")

        return try {
            // Android 13+ 권한 체크
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                Log.d(TAG, "알림 권한: $hasPermission")

                if (!hasPermission) {
                    Log.e(TAG, "❌ 알림 권한 없음!")
                    return Result.failure()
                }
            }

            val taskId = inputData.getString(KEY_TASK_ID)
            val taskTitle = inputData.getString(KEY_TASK_TITLE)
            val groupName = inputData.getString(KEY_GROUP_NAME) ?: "그룹"
            val minutesLeft = inputData.getInt(KEY_MINUTES_LEFT, 60)

            Log.d(TAG, "할일 정보:")
            Log.d(TAG, "  - taskId: $taskId")
            Log.d(TAG, "  - taskTitle: $taskTitle")
            Log.d(TAG, "  - groupName: $groupName")
            Log.d(TAG, "  - minutesLeft: $minutesLeft")

            if (taskId == null || taskTitle == null) {
                Log.e(TAG, "❌ 필수 데이터 없음!")
                return Result.failure()
            }

            // 알림 표시
            Log.d(TAG, "알림 표시 시도...")
            notificationHelper.showTaskReminder(
                taskId = taskId,
                taskTitle = taskTitle,
                groupName = groupName,
                minutesLeft = minutesLeft
            )
            Log.d(TAG, "✅ 알림 표시 완료!")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ WorkManager 실행 실패", e)
            Result.failure()
        }
    }
}