package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class ToggleHabitCheckUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(
        habitId: String,
        userId: String,
        date: LocalDate = LocalDate.now()
    ): Result<Unit> {
        return try {
            repository.toggleHabitCheck(habitId, userId, date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}