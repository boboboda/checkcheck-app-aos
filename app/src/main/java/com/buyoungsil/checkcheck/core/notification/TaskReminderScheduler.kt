package com.buyoungsil.checkcheck.core.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import com.buyoungsil.checkcheck.core.notification.worker.TaskReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 할일 알림 스케줄러
 * ✅ Task 마감일 기반 알림 스케줄링
 */
@Singleton
class TaskReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val TAG = "TaskReminderScheduler"
    }
    /**
     * 할일 알림 스케줄 설정
     */
    fun scheduleTaskReminder(
        taskId: String,
        taskTitle: String,
        groupName: String,
        dueDateTime: LocalDateTime,
        minutesBefore: Int = 60
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📅 WorkManager 스케줄링 시작")
        Log.d(TAG, "  - taskId: $taskId")
        Log.d(TAG, "  - taskTitle: $taskTitle")
        Log.d(TAG, "  - groupName: $groupName")
        Log.d(TAG, "  - dueDateTime: $dueDateTime")
        Log.d(TAG, "  - minutesBefore: $minutesBefore")

        // 알림 시간 계산
        val reminderTime = dueDateTime.minusMinutes(minutesBefore.toLong())
        val now = LocalDateTime.now()

        Log.d("TaskReminderScheduler", "  - 현재 시간: $now")
        Log.d("TaskReminderScheduler", "  - 알림 시간: $reminderTime")

        // 이미 지난 시간이면 스케줄 안 함
        if (reminderTime.isBefore(now) || reminderTime.isEqual(now)) {
            Log.w("TaskReminderScheduler", "⚠️ 알림 시간이 이미 지남 - 스케줄 안 함")
            Log.d("TaskReminderScheduler", "========================================")
            return
        }

        val delay = Duration.between(now, reminderTime)
        Log.d("TaskReminderScheduler", "  - 지연 시간: ${delay.toMinutes()}분 (${delay.seconds}초)")

        // WorkManager 설정
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    TaskReminderWorker.KEY_TASK_ID to taskId,
                    TaskReminderWorker.KEY_TASK_TITLE to taskTitle,
                    TaskReminderWorker.KEY_GROUP_NAME to groupName,
                    TaskReminderWorker.KEY_MINUTES_LEFT to minutesBefore
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .addTag(getWorkTag(taskId))
            .build()

        // 기존 작업 취소 후 새로 등록
        workManager.cancelAllWorkByTag(getWorkTag(taskId))
        workManager.enqueue(workRequest)

        Log.d("TaskReminderScheduler", "✅ WorkManager 등록 완료!")
        Log.d("TaskReminderScheduler", "========================================")
    }

    /**
     * 할일 알림 취소
     */
    fun cancelTaskReminder(taskId: String) {
        workManager.cancelAllWorkByTag(getWorkTag(taskId))
    }

    /**
     * 모든 할일 알림 취소
     */
    fun cancelAllTaskReminders() {
        workManager.cancelAllWorkByTag(TaskReminderWorker.WORK_TAG_PREFIX)
    }

    /**
     * Work 태그 생성
     */
    private fun getWorkTag(taskId: String): String {
        return "${TaskReminderWorker.WORK_TAG_PREFIX}$taskId"
    }
}