package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import java.util.UUID
import javax.inject.Inject

/**
 * 습관 생성 UseCase
 *
 * 🆕 변경 사항:
 * - ValidateHabitLimitsUseCase를 통한 습관 개수 제한 체크
 */
class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val validateHabitLimitsUseCase: ValidateHabitLimitsUseCase
) {
    suspend operator fun invoke(habit: Habit): Result<Habit> {
        return try {
            Log.d("CreateHabitUseCase", "=== UseCase 시작 ===")
            Log.d("CreateHabitUseCase", "habit: $habit")

            // 🆕 1. 습관 개수 제한 체크
            val (canCreate, errorMessage) = validateHabitLimitsUseCase.canCreateHabit(habit.userId)
            if (!canCreate) {
                Log.w("CreateHabitUseCase", "⚠️ 습관 생성 제한: $errorMessage")
                return Result.failure(Exception(errorMessage))
            }

            // 2. 습관 생성
            val newHabit = habit.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            Log.d("CreateHabitUseCase", "newHabit ID: ${newHabit.id}")
            Log.d("CreateHabitUseCase", "repository.insertHabit() 호출 전")

            repository.insertHabit(newHabit)

            Log.d("CreateHabitUseCase", "✅ repository.insertHabit() 완료!")
            Result.success(newHabit)
        } catch (e: Exception) {
            Log.e("CreateHabitUseCase", "❌ 에러 발생: ${e.message}", e)
            Result.failure(e)
        }
    }
}