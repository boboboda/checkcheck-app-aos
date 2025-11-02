package com.buyoungsil.checkcheck.feature.task.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getTasksByGroup(groupId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE groupId = :groupId AND status = :status ORDER BY priority DESC, createdAt DESC")
    fun getTasksByStatus(groupId: String, status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE assigneeId = :userId AND status != 'COMPLETED' ORDER BY priority DESC")
    fun getMyTasks(userId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
}