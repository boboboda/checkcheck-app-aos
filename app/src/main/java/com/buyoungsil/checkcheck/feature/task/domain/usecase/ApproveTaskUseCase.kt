package com.buyoungsil.checkcheck.feature.task.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import com.buyoungsil.checkcheck.feature.task.domain.model.ApprovalStatus
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * 태스크 승인 UseCase
 */
class ApproveTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val coinRepository: CoinRepository
) {
    companion object {
        private const val TAG = "ApproveTaskUseCase"
    }

    /**
     * 태스크 승인/거부
     *
     * @param taskId 태스크 ID
     * @param approverId 승인자 ID (태스크 생성자여야 함)
     * @param approved true: 승인, false: 거부
     */
    suspend operator fun invoke(
        taskId: String,
        approverId: String,
        approved: Boolean
    ): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, if (approved) "✅ 태스크 승인 처리 시작" else "❌ 태스크 거부 처리 시작")
            Log.d(TAG, "  - taskId: $taskId")
            Log.d(TAG, "  - approverId: $approverId")

            // 1. 태스크 조회
            val task = taskRepository.getTaskById(taskId)
            if (task == null) {
                Log.e(TAG, "❌ 태스크를 찾을 수 없음")
                return Result.failure(Exception("태스크를 찾을 수 없습니다"))
            }

            // 2. 승인자 권한 검증 (태스크 생성자만 승인 가능)
            if (task.createdBy != approverId) {
                Log.e(TAG, "❌ 승인 권한 없음 (생성자: ${task.createdBy}, 승인자: $approverId)")
                return Result.failure(Exception("태스크를 생성한 사람만 승인할 수 있습니다"))
            }

            // 3. 상태 검증 (승인 대기 상태여야 함)
            if (task.status != TaskStatus.WAITING_APPROVAL) {
                Log.e(TAG, "❌ 잘못된 상태 (현재: ${task.status})")
                return Result.failure(Exception("승인 대기 중인 태스크가 아닙니다"))
            }

            if (approved) {
                // ✅ 승인 처리
                Log.d(TAG, "✅ 승인 처리 중...")

                // 3-1. 태스크 상태 업데이트
                val approvedTask = task.copy(
                    status = TaskStatus.COMPLETED,
                    approvalStatus = ApprovalStatus.APPROVED,
                    approvedBy = approverId,
                    approvedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                taskRepository.updateTask(approvedTask)
                Log.d(TAG, "✅ 태스크 상태 업데이트 완료")

                // 3-2. 코인 지급
                if (task.coinReward > 0 && task.completedBy != null) {
                    Log.d(TAG, "💰 코인 지급 시작: ${task.coinReward}코인")

                    coinRepository.rewardTaskCompletion(
                        userId = task.completedBy,
                        taskId = taskId,
                        amount = task.coinReward,
                        fromUserId = task.createdBy
                    ).onSuccess {
                        Log.d(TAG, "✅ 코인 지급 완료")
                    }.onFailure { error ->
                        Log.e(TAG, "❌ 코인 지급 실패", error)
                        // 코인 지급 실패해도 승인은 유지
                    }
                } else {
                    Log.d(TAG, "⏭️ 코인 보상 없음 - 건너뜀")
                }

                Log.d(TAG, "🎉 태스크 승인 완료!")
            } else {
                // ❌ 거부 처리
                Log.d(TAG, "❌ 거부 처리 중...")

                val rejectedTask = task.copy(
                    status = TaskStatus.PENDING,  // 대기 상태로 되돌림
                    approvalStatus = ApprovalStatus.REJECTED,
                    approvedBy = approverId,
                    approvedAt = System.currentTimeMillis(),
                    completedBy = null,  // 완료 정보 제거
                    completedAt = null,
                    updatedAt = System.currentTimeMillis()
                )
                taskRepository.updateTask(rejectedTask)
                Log.d(TAG, "✅ 태스크 거부 완료 (대기 상태로 복원)")
            }

            Log.d(TAG, "========================================")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 승인/거부 처리 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
}