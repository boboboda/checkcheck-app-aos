package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPersonalHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(userId: String): Flow<List<Habit>> {
        return repository.getPersonalHabits(userId)
    }
}