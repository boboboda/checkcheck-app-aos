package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import java.util.UUID
import javax.inject.Inject

/**
 * 습관 생성 UseCase
 * ✅ 생성된 Habit 객체를 반환하도록 수정 (알림 설정을 위해)
 * ✅ 디버깅 로그 추가
 */
class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit): Result<Habit> {
        return try {
            Log.d("CreateHabitUseCase", "=== UseCase 시작 ===")
            Log.d("CreateHabitUseCase", "habit: $habit")

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