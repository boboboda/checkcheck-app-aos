package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 그룹에 공유된 모든 습관 조회 UseCase
 */
class GetSharedHabitsInGroupUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(groupId: String): Flow<List<Habit>> {
        return repository.getSharedHabitsInGroup(groupId)
    }
}
