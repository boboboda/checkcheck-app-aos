package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.coin.domain.usecase.RewardHabitCompletionUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitMilestones
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitRewardRecord
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/**
 * 습관 체크 시 마일스톤 달성 여부 확인 및 코인 지급 UseCase
 *
 * ✅ 월간 코인 제한 검증 통합
 *
 * @since 2025-01-15 (월간 제한 추가)
 */
class CheckHabitMilestoneUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val rewardHabitCompletionUseCase: RewardHabitCompletionUseCase,
    private val validateHabitLimitsUseCase: ValidateHabitLimitsUseCase,  // ✅ 코인 제한 검증용
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "CheckHabitMilestone"
        private const val REWARD_RECORDS_COLLECTION = "habitRewardRecords"
    }

    /**
     * 습관 체크 후 마일스톤 확인 및 보상
     *
     * @param habitId 습관 ID
     * @param userId 사용자 ID
     * @param currentStreak 현재 연속 일수
     * @return 지급된 코인 수 (지급되지 않으면 null)
     */
    suspend operator fun invoke(
        habitId: String,
        userId: String,
        currentStreak: Int
    ): Result<Int?> {
        return try {
            Log.d(TAG, "=== 마일스톤 체크 시작 ===")
            Log.d(TAG, "habitId: $habitId")
            Log.d(TAG, "userId: $userId")
            Log.d(TAG, "currentStreak: $currentStreak")

            // 1. 습관 정보 조회
            val habit = habitRepository.getHabitById(habitId)
            if (habit == null) {
                Log.e(TAG, "❌ 습관을 찾을 수 없음")
                return Result.failure(Exception("습관을 찾을 수 없습니다"))
            }

            // 2. 코인 보상이 비활성화되어 있으면 종료
            if (!habit.coinRewardEnabled) {
                Log.d(TAG, "코인 보상 비활성화 상태")
                return Result.success(null)
            }

            // 3. 현재 streak에 해당하는 마일스톤 찾기
            val milestone = HabitMilestones.getMilestone(currentStreak)
            if (milestone == null) {
                Log.d(TAG, "해당 streak에 마일스톤 없음")
                return Result.success(null)
            }

            Log.d(TAG, "✅ 마일스톤 발견: ${milestone.days}일 → ${milestone.coins}코인")

            // 4. 이미 보상받았는지 확인
            if (habit.lastRewardStreak >= currentStreak) {
                Log.d(TAG, "이미 보상받은 마일스톤 (lastRewardStreak: ${habit.lastRewardStreak})")
                return Result.success(null)
            }

            // 5. 중복 지급 체크 (보상 기록 확인)
            val alreadyRewarded = checkIfAlreadyRewarded(habitId, userId, currentStreak)
            if (alreadyRewarded) {
                Log.d(TAG, "❌ 이미 보상 기록 존재")
                return Result.success(null)
            }

            // ✅ 6. 코인 지급 가능 여부 검증 (월간/일간 제한 포함)
            Log.d(TAG, "코인 지급 가능 여부 검증 중...")
            val (canReward, errorMessage) = validateHabitLimitsUseCase.canRewardCoins(
                userId = userId,
                coinAmount = milestone.coins
            )

            if (!canReward) {
                Log.w(TAG, "❌ 코인 지급 불가: $errorMessage")
                return Result.failure(Exception(errorMessage ?: "코인 지급 불가"))
            }
            Log.d(TAG, "✅ 코인 지급 가능")

            // 7. 코인 지급
            Log.d(TAG, "💰 코인 지급 시작...")
            val rewardResult = rewardHabitCompletionUseCase(
                userId = userId,
                habitId = habitId,
                coins = milestone.coins
            )

            if (rewardResult.isFailure) {
                Log.e(TAG, "❌ 코인 지급 실패", rewardResult.exceptionOrNull())
                return Result.failure(rewardResult.exceptionOrNull() ?: Exception("코인 지급 실패"))
            }
            Log.d(TAG, "✅ 코인 지급 완료")

            // 8. 월간/일간 코인 기록은 RewardHabitCompletionUseCase에서 자동으로 업데이트됨

            // 9. 습관의 lastRewardStreak 업데이트
            updateHabitRewardInfo(habitId, currentStreak)

            // 10. 보상 기록 저장 (중복 지급 방지)
            saveRewardRecord(habitId, userId, currentStreak, milestone.coins)

            Log.d(TAG, "🎉 마일스톤 달성 완료! ${milestone.coins}코인 지급됨")
            Result.success(milestone.coins)

        } catch (e: Exception) {
            Log.e(TAG, "❌ 마일스톤 체크 실패", e)
            Result.failure(e)
        }
    }

    /**
     * 이미 보상받았는지 확인
     */
    private suspend fun checkIfAlreadyRewarded(
        habitId: String,
        userId: String,
        streakDays: Int
    ): Boolean {
        return try {
            val snapshot = firestore.collection(REWARD_RECORDS_COLLECTION)
                .whereEqualTo("habitId", habitId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("streakDays", streakDays)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "보상 기록 확인 실패", e)
            false
        }
    }

    /**
     * 습관의 보상 정보 업데이트
     */
    private suspend fun updateHabitRewardInfo(habitId: String, streakDays: Int) {
        try {
            firestore.collection("habits")
                .document(habitId)
                .update(
                    mapOf(
                        "lastRewardStreak" to streakDays,
                        "lastRewardDate" to System.currentTimeMillis()
                    )
                )
                .await()

            Log.d(TAG, "✅ 습관 보상 정보 업데이트 완료")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 습관 보상 정보 업데이트 실패", e)
            throw e
        }
    }

    /**
     * 보상 기록 저장
     */
    private suspend fun saveRewardRecord(
        habitId: String,
        userId: String,
        streakDays: Int,
        coinsAwarded: Int
    ) {
        try {
            val record = HabitRewardRecord(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                userId = userId,
                streakDays = streakDays,
                coinsAwarded = coinsAwarded,
                awardedAt = System.currentTimeMillis()
            )

            firestore.collection(REWARD_RECORDS_COLLECTION)
                .document(record.id)
                .set(
                    mapOf(
                        "id" to record.id,
                        "habitId" to record.habitId,
                        "userId" to record.userId,
                        "streakDays" to record.streakDays,
                        "coinsAwarded" to record.coinsAwarded,
                        "awardedAt" to record.awardedAt
                    )
                )
                .await()

            Log.d(TAG, "✅ 보상 기록 저장 완료")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 보상 기록 저장 실패", e)
            throw e
        }
    }
}