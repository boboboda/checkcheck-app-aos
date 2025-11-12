package com.buyoungsil.checkcheck.feature.task.domain.repository

import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksByGroup(groupId: String): Flow<List<Task>>
    suspend fun getTaskById(taskId: String): Task?
    fun getTasksByStatus(groupId: String, status: TaskStatus): Flow<List<Task>>
    fun getMyTasks(userId: String): Flow<List<Task>>
    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: String)
    suspend fun completeTask(taskId: String, userId: String)
    fun getPersonalTasks(userId: String): Flow<List<Task>>
}