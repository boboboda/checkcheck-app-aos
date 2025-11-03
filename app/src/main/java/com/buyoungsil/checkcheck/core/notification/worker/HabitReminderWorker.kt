package com.buyoungsil.checkcheck.core.notification.worker

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.buyoungsil.checkcheck.core.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 습관 리마인더 워커
 */
@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_HABIT_ID = "habitId"
        const val KEY_HABIT_TITLE = "habitTitle"
        const val KEY_HABIT_ICON = "habitIcon"
        const val KEY_MESSAGE = "message"

        const val WORK_TAG_PREFIX = "habit_reminder_"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            val habitId = inputData.getString(KEY_HABIT_ID) ?: return Result.failure()
            val habitTitle = inputData.getString(KEY_HABIT_TITLE) ?: return Result.failure()
            val habitIcon = inputData.getString(KEY_HABIT_ICON) ?: "📌"
            val message = inputData.getString(KEY_MESSAGE)

            // 알림 표시
            notificationHelper.showHabitReminder(
                habitId = habitId,
                habitTitle = habitTitle,
                habitIcon = habitIcon,
                message = message
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}