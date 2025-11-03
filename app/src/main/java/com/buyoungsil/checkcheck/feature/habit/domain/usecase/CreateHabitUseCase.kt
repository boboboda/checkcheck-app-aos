package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import java.util.UUID
import javax.inject.Inject

/**
 * 습관 생성 UseCase
 * ✅ 생성된 Habit 객체를 반환하도록 수정 (알림 설정을 위해)
 */
class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit): Result<Habit> {  // ✅ Unit → Habit
        return try {
            val newHabit = habit.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertHabit(newHabit)
            Result.success(newHabit)  // ✅ 생성된 Habit 반환
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}