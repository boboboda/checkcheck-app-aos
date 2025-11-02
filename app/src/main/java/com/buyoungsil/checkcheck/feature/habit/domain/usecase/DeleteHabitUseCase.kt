package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * 습관 삭제 UseCase
 */
class DeleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): Result<Unit> {
        return try {
            repository.deleteHabit(habitId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}