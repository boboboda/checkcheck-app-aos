package com.buyoungsil.checkcheck.feature.task.domain.usecase

import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPersonalTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(userId: String): Flow<List<Task>> {
        return repository.getPersonalTasks(userId)
    }
}