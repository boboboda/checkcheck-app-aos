package com.buyoungsil.checkcheck.feature.task.data.repository

import com.buyoungsil.checkcheck.feature.task.data.local.TaskDao
import com.buyoungsil.checkcheck.feature.task.data.local.TaskEntity
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getTasksByGroup(groupId: String): Flow<List<Task>> {
        return taskDao.getTasksByGroup(groupId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override fun getTasksByStatus(groupId: String, status: TaskStatus): Flow<List<Task>> {
        return taskDao.getTasksByStatus(groupId, status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMyTasks(userId: String): Flow<List<Task>> {
        return taskDao.getMyTasks(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createTask(task: Task) {
        taskDao.insertTask(TaskEntity.fromDomain(task))
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(TaskEntity.fromDomain(task))
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    override suspend fun completeTask(taskId: String, userId: String) {
        val task = getTaskById(taskId) ?: return
        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            completedBy = userId,
            completedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        updateTask(completedTask)
    }

    override fun getPersonalTasks(userId: String): Flow<List<Task>> {
        TODO("Not yet implemented")
    }
}