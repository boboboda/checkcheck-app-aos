package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 습관 체크 UseCase
 *
 * ✅ 토글 방식에서 **체크만 가능** 방식으로 변경
 * ✅ Firebase Functions가 자동으로 글로벌 랭킹 업데이트
 */
class CheckHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase
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

            // 3. 글로벌 랭킹은 Firebase Functions가 자동 업데이트
            Log.d(TAG, "🔥 Firebase Functions가 자동으로 랭킹 업데이트")

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