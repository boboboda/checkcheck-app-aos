package com.buyoungsil.checkcheck.core.notification

import android.content.Context
import androidx.work.*
import com.buyoungsil.checkcheck.core.notification.domain.model.DayOfWeek
import com.buyoungsil.checkcheck.core.notification.domain.model.Reminder
import com.buyoungsil.checkcheck.core.notification.worker.HabitReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 스케줄 관리자
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * 습관 리마인더 스케줄 설정
     */
    fun scheduleHabitReminder(reminder: Reminder) {
        if (!reminder.enabled) {
            cancelHabitReminder(reminder.habitId)
            return
        }

        // 오늘 알림 시간까지 남은 시간 계산
        val initialDelay = calculateInitialDelay(reminder.time)

        // WorkManager 설정
        val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            1, TimeUnit.DAYS // 매일 반복
        )
            .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    HabitReminderWorker.KEY_HABIT_ID to reminder.habitId,
                    HabitReminderWorker.KEY_HABIT_TITLE to reminder.habitTitle,
                    HabitReminderWorker.KEY_HABIT_ICON to "📌",
                    HabitReminderWorker.KEY_MESSAGE to reminder.message
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .addTag(getWorkTag(reminder.habitId))
            .build()

        // 기존 작업 취소 후 새로 등록
        workManager.cancelAllWorkByTag(getWorkTag(reminder.habitId))
        workManager.enqueue(workRequest)
    }

    /**
     * 습관 리마인더 취소
     */
    fun cancelHabitReminder(habitId: String) {
        workManager.cancelAllWorkByTag(getWorkTag(habitId))
    }

    /**
     * 모든 리마인더 취소
     */
    fun cancelAllReminders() {
        workManager.cancelAllWork()
    }

    /**
     * 초기 지연 시간 계산
     */
    private fun calculateInitialDelay(targetTime: LocalTime): Duration {
        val now = LocalDateTime.now()
        var target = now.with(targetTime)

        // 이미 지난 시간이면 내일로 설정
        if (target.isBefore(now) || target.isEqual(now)) {
            target = target.plusDays(1)
        }

        return Duration.between(now, target)
    }

    /**
     * Work 태그 생성
     */
    private fun getWorkTag(habitId: String): String {
        return "${HabitReminderWorker.WORK_TAG_PREFIX}$habitId"
    }

    /**
     * 특정 요일에만 알림 (고급 기능, 나중에 구현)
     */
    private fun shouldShowReminderToday(reminder: Reminder): Boolean {
        val today = java.time.LocalDate.now().dayOfWeek
        return reminder.days.any { it.toJavaDayOfWeek() == today }
    }
}