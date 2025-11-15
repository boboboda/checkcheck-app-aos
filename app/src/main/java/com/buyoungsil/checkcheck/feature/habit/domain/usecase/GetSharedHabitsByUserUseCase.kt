package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 특정 사용자가 특정 그룹에 공유한 습관 조회 UseCase
 */
class GetSharedHabitsByUserUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(userId: String, groupId: String): Flow<List<Habit>> {
        return repository.getSharedHabitsByUser(userId, groupId)
    }
}