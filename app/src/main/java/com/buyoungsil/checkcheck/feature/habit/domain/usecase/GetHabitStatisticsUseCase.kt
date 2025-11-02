package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * 습관 통계 조회 UseCase
 */
class GetHabitStatisticsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): Result<HabitStatistics> {
        return try {
            val statistics = repository.getHabitStatistics(habitId)
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}