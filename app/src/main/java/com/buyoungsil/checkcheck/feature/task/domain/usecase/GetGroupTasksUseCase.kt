package com.buyoungsil.checkcheck.feature.task.domain.usecase

import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(groupId: String): Flow<List<Task>> {
        return repository.getTasksByGroup(groupId)
    }
}