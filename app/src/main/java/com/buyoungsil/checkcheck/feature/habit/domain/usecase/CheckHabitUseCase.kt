package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.core.domain.repository.UserRepository
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.ranking.domain.usecase.UpdateGlobalRankingUseCase
import java.time.LocalDate
import javax.inject.Inject

/**
 * 습관 체크 UseCase
 *
 * ✅ 토글 방식에서 **체크만 가능** 방식으로 변경
 * ✅ 글로벌 랭킹 자동 업데이트 추가
 */
class CheckHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val updateGlobalRankingUseCase: UpdateGlobalRankingUseCase,
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "CheckHabitUseCase"
    }

    suspend operator fun invoke(
        habitId: String,
        userId: String,
        date: LocalDate = LocalDate.now()
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "=== 습관 체크 시작 ===")
            Log.d(TAG, "habitId: $habitId")
            Log.d(TAG, "userId: $userId")
            Log.d(TAG, "date: $date")

            // 1. 이미 체크했는지 확인
            val existingCheck = repository.getCheckByDate(habitId, date)

            if (existingCheck != null) {
                Log.d(TAG, "⚠️ 이미 체크되어 있음 - 동작 없음")
                return Result.success(false)
            }

            // 2. 새 체크 추가
            Log.d(TAG, "✅ 새 체크 추가")
            repository.toggleHabitCheck(habitId, userId, date)

            // 3. 글로벌 랭킹 자동 업데이트
            try {
                Log.d(TAG, "🌐 글로벌 랭킹 업데이트 시작")

                val habit = repository.getHabitById(habitId)
                val stats = getHabitStatisticsUseCase(habitId).getOrNull()
                val user = userRepository.getUser(userId)

                Log.d(TAG, "habit: ${habit?.title}")
                Log.d(TAG, "stats: streak=${stats?.currentStreak}, checks=${stats?.totalChecks}")
                Log.d(TAG, "user: ${user?.displayName}")

                if (habit != null && stats != null && user != null) {
                    updateGlobalRankingUseCase(
                        userId = user.id,
                        userName = user.displayName ?: "익명 사용자",
                        habitTitle = habit.title,
                        currentStreak = stats.currentStreak,
                        totalChecks = stats.totalChecks,
                        completionRate = stats.completionRate
                    ).onSuccess {
                        Log.d(TAG, "✅ 글로벌 랭킹 업데이트 완료")
                    }.onFailure { error ->
                        Log.w(TAG, "⚠️ 글로벌 랭킹 업데이트 실패: ${error.message}")
                    }
                } else {
                    Log.w(TAG, "⚠️ 랭킹 업데이트 스킵: habit=$habit, stats=$stats, user=$user")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ 글로벌 랭킹 업데이트 중 오류", e)
            }

            Log.d(TAG, "🎉 습관 체크 완료")
            Result.success(true)

        } catch (e: Exception) {
            Log.e(TAG, "❌ 습관 체크 실패", e)
            Result.failure(e)
        }
    }

    suspend fun isChecked(
        habitId: String,
        date: LocalDate = LocalDate.now()
    ): Boolean {
        return try {
            val check = repository.getCheckByDate(habitId, date)
            check != null && check.completed
        } catch (e: Exception) {
            Log.e(TAG, "체크 상태 확인 실패", e)
            false
        }
    }
}
