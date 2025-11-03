package com.buyoungsil.checkcheck.feature.task.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalTime

/**
 * Task Room Entity
 * ✅ 알림 필드 추가
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val title: String,
    val description: String?,
    val assigneeId: String?,
    val assigneeName: String?,
    val status: String,
    val priority: String,
    val dueDate: String?,                  // LocalDate → String
    val dueTime: String?,                  // ✅ LocalTime → String
    val reminderEnabled: Boolean,          // ✅
    val reminderMinutesBefore: Int,        // ✅
    val completedBy: String?,
    val completedAt: Long?,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Task {
        return Task(
            id = id,
            groupId = groupId,
            title = title,
            description = description,
            assigneeId = assigneeId,
            assigneeName = assigneeName,
            status = TaskStatus.valueOf(status),
            priority = TaskPriority.valueOf(priority),
            dueDate = dueDate?.let { LocalDate.parse(it) },
            dueTime = dueTime?.let { LocalTime.parse(it) },          // ✅
            reminderEnabled = reminderEnabled,                        // ✅
            reminderMinutesBefore = reminderMinutesBefore,            // ✅
            completedBy = completedBy,
            completedAt = completedAt,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(task: Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                groupId = task.groupId,
                title = task.title,
                description = task.description,
                assigneeId = task.assigneeId,
                assigneeName = task.assigneeName,
                status = task.status.name,
                priority = task.priority.name,
                dueDate = task.dueDate?.toString(),
                dueTime = task.dueTime?.toString(),                   // ✅
                reminderEnabled = task.reminderEnabled,               // ✅
                reminderMinutesBefore = task.reminderMinutesBefore,   // ✅
                completedBy = task.completedBy,
                completedAt = task.completedAt,
                createdBy = task.createdBy,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt
            )
        }
    }
}