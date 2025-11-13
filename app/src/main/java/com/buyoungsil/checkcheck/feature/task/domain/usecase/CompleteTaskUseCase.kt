package com.buyoungsil.checkcheck.feature.task.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.core.notification.TaskReminderScheduler
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val taskReminderScheduler: TaskReminderScheduler,
    private val coinRepository: CoinRepository
) {
    companion object {
        private const val TAG = "CompleteTaskUseCase"
    }

    suspend operator fun invoke(taskId: String, userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "✅ 할일 완료 처리 시작")
            Log.d(TAG, "  - taskId: $taskId")
            Log.d(TAG, "  - userId: $userId")

            // 1. Task 조회 (코인 보상 확인용)
            val task = repository.getTaskById(taskId)
            Log.d(TAG, "  - coinReward: ${task?.coinReward ?: 0}")

            // 2. 할일 완료 처리
            repository.completeTask(taskId, userId)
            Log.d(TAG, "✅ Repository 완료 처리 성공")

            // 3. WorkManager 워커 취소
            taskReminderScheduler.cancelTaskReminder(taskId)
            Log.d(TAG, "✅ WorkManager 워커 취소 완료")

            // 4. 코인 보상 지급
            if (task != null && task.coinReward > 0) {
                Log.d(TAG, "💰 코인 보상 지급 시작: ${task.coinReward}코인")

                coinRepository.rewardTaskCompletion(
                    userId = userId,
                    taskId = taskId,
                    amount = task.coinReward,
                    fromUserId = task.createdBy
                ).onSuccess {
                    Log.d(TAG, "✅ 코인 보상 지급 완료")
                }.onFailure { error ->
                    Log.e(TAG, "❌ 코인 보상 지급 실패", error)
                    // 코인 지급 실패해도 할일 완료는 유지
                }
            } else {
                Log.d(TAG, "⏭️ 코인 보상 없음 - 건너뜀")
            }

            Log.d(TAG, "========================================")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 할일 완료 처리 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
}