package com.buyoungsil.checkcheck.core.notification

import android.content.Context
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
        // 알림 시간 계산
        val reminderTime = dueDateTime.minusMinutes(minutesBefore.toLong())
        val now = LocalDateTime.now()

        // 이미 지난 시간이면 스케줄 안 함
        if (reminderTime.isBefore(now) || reminderTime.isEqual(now)) {
            return
        }

        val delay = Duration.between(now, reminderTime)

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