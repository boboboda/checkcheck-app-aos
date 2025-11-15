package com.buyoungsil.checkcheck.feature.task.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.core.notification.TaskReminderScheduler
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import com.buyoungsil.checkcheck.feature.task.domain.model.ApprovalStatus
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * 태스크 완료 처리 UseCase
 *
 * ✅ 승인 프로세스 통합:
 * - requiresApproval = false → 즉시 완료 & 코인 지급
 * - requiresApproval = true → 승인 대기 상태로 변경
 */
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

            // 1. Task 조회
            val task = repository.getTaskById(taskId)
            if (task == null) {
                Log.e(TAG, "❌ 태스크를 찾을 수 없음")
                return Result.failure(Exception("태스크를 찾을 수 없습니다"))
            }

            Log.d(TAG, "  - coinReward: ${task.coinReward}")
            Log.d(TAG, "  - requiresApproval: ${task.requiresApproval}")

            // 2. WorkManager 워커 취소
            taskReminderScheduler.cancelTaskReminder(taskId)
            Log.d(TAG, "✅ WorkManager 워커 취소 완료")

            // 3. 승인 필요 여부에 따라 분기 처리
            if (task.requiresApproval && task.coinReward > 0) {
                // ✨ 승인 필요한 경우: 승인 대기 상태로 변경
                Log.d(TAG, "🕐 승인 필요 → 승인 대기 상태로 변경")

                val waitingTask = task.copy(
                    status = TaskStatus.WAITING_APPROVAL,
                    approvalStatus = ApprovalStatus.PENDING,
                    completedBy = userId,
                    completedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateTask(waitingTask)

                Log.d(TAG, "✅ 승인 대기 상태로 변경 완료")
                Log.d(TAG, "📢 태스크 생성자(${task.createdBy})에게 승인 요청 알림 필요")

                // TODO: Firebase Functions로 승인 요청 알림 전송

            } else {
                // ✨ 승인 불필요한 경우: 즉시 완료 & 코인 지급
                Log.d(TAG, "⚡ 승인 불필요 → 즉시 완료 처리")

                repository.completeTask(taskId, userId)
                Log.d(TAG, "✅ 태스크 완료 처리 성공")

                // 코인 보상 지급
                if (task.coinReward > 0) {
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