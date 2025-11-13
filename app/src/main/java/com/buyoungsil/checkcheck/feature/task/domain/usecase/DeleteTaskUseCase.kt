package com.buyoungsil.checkcheck.feature.task.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.core.notification.TaskReminderScheduler
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * 할일 삭제 UseCase
 * ✅ 워커 취소 기능 추가
 */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val taskReminderScheduler: TaskReminderScheduler
) {
    companion object {
        private const val TAG = "DeleteTaskUseCase"
    }

    suspend operator fun invoke(taskId: String): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🗑️ 할일 삭제 처리 시작")
            Log.d(TAG, "  - taskId: $taskId")

            // 1. 할일 삭제 처리
            repository.deleteTask(taskId)
            Log.d(TAG, "✅ Repository 삭제 처리 성공")

            // 2. WorkManager 워커 취소
            taskReminderScheduler.cancelTaskReminder(taskId)
            Log.d(TAG, "✅ WorkManager 워커 취소 완료")

            Log.d(TAG, "========================================")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 할일 삭제 처리 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
}