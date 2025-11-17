package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * 습관 수정 UseCase
 *
 * ✅ 기존 습관 정보 업데이트
 * ✅ 그룹 공유 상태 변경 가능
 */
class UpdateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    companion object {
        private const val TAG = "UpdateHabitUseCase"
    }

    suspend operator fun invoke(habit: Habit): Result<Habit> {
        return try {
            Log.d(TAG, "=== 습관 수정 시작 ===")
            Log.d(TAG, "habitId: ${habit.id}")
            Log.d(TAG, "groupShared: ${habit.groupShared}")
            Log.d(TAG, "groupId: ${habit.groupId}")

            // updatedAt 갱신
            val updatedHabit = habit.copy(
                updatedAt = System.currentTimeMillis()
            )

            repository.updateHabit(updatedHabit)

            Log.d(TAG, "✅ 습관 수정 완료")
            Result.success(updatedHabit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 습관 수정 실패: ${e.message}", e)
            Result.failure(e)
        }
    }
}