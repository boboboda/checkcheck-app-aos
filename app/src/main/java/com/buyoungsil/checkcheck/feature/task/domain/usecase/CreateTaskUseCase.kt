package com.buyoungsil.checkcheck.feature.task.domain.usecase

import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import java.util.UUID
import javax.inject.Inject

/**
 * 할일 생성 UseCase
 * ✅ 생성된 Task 객체를 반환하도록 수정 (알림 스케줄을 위해)
 */
class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Task> {  // ✅ Unit → Task
        return try {
            val newTask = task.copy(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.createTask(newTask)
            Result.success(newTask)  // ✅ 생성된 Task 반환
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}