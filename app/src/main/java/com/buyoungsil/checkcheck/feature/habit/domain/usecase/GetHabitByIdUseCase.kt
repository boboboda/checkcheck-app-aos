package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * 특정 습관 조회 UseCase
 */
class GetHabitByIdUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    companion object {
        private const val TAG = "GetHabitByIdUseCase"
    }

    suspend operator fun invoke(habitId: String): Result<Habit?> {
        return try {
            Log.d(TAG, "습관 조회: $habitId")
            val habit = repository.getHabitById(habitId)

            if (habit != null) {
                Log.d(TAG, "✅ 습관 조회 성공: ${habit.title}")
            } else {
                Log.w(TAG, "⚠️ 습관을 찾을 수 없음")
            }

            Result.success(habit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 습관 조회 실패", e)
            Result.failure(e)
        }
    }
}