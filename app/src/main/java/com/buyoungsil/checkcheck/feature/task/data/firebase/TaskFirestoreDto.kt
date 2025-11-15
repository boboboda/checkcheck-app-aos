package com.buyoungsil.checkcheck.feature.task.data.firebase

import com.buyoungsil.checkcheck.feature.task.domain.model.ApprovalStatus
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date

data class TaskFirestoreDto(
    @DocumentId
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val description: String? = null,
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val status: String = TaskStatus.PENDING.name,
    val priority: String = TaskPriority.NORMAL.name,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 60,
    val coinReward: Int = 0,
    val requiresApproval: Boolean = false,  // ✨ 추가
    val approvalStatus: String? = null,  // ✨ 추가
    val approvedBy: String? = null,  // ✨ 추가
    val approvedAt: Long? = null,  // ✨ 추가
    val completedBy: String? = null,
    val completedAt: Long? = null,
    val createdBy: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this(
        "", "", "", null, null, null,
        TaskStatus.PENDING.name, TaskPriority.NORMAL.name,
        null, null, false, 60, 0, false, null, null, null,  // ✨ 4개 추가
        null, null, "", null, null
    )

    fun toDomain(): Task {
        return Task(
            id = id,
            groupId = groupId,
            title = title,
            description = description,
            assigneeId = assigneeId,
            assigneeName = assigneeName,
            status = try {
                TaskStatus.valueOf(status)
            } catch (e: Exception) {
                TaskStatus.PENDING
            },
            priority = try {
                TaskPriority.valueOf(priority)
            } catch (e: Exception) {
                TaskPriority.NORMAL
            },
            dueDate = dueDate?.let { LocalDate.parse(it) },
            dueTime = dueTime?.let { LocalTime.parse(it) },
            reminderEnabled = reminderEnabled,
            reminderMinutesBefore = reminderMinutesBefore,
            coinReward = coinReward,
            requiresApproval = requiresApproval,  // ✨ 추가
            approvalStatus = approvalStatus?.let {  // ✨ 추가
                try {
                    ApprovalStatus.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            },
            approvedBy = approvedBy,  // ✨ 추가
            approvedAt = approvedAt,  // ✨ 추가
            completedBy = completedBy,
            completedAt = completedAt,
            createdBy = createdBy,
            createdAt = createdAt?.time ?: System.currentTimeMillis(),
            updatedAt = updatedAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(task: Task): TaskFirestoreDto {
            return TaskFirestoreDto(
                id = task.id,
                groupId = task.groupId,
                title = task.title,
                description = task.description,
                assigneeId = task.assigneeId,
                assigneeName = task.assigneeName,
                status = task.status.name,
                priority = task.priority.name,
                dueDate = task.dueDate?.toString(),
                dueTime = task.dueTime?.toString(),
                reminderEnabled = task.reminderEnabled,
                reminderMinutesBefore = task.reminderMinutesBefore,
                coinReward = task.coinReward,
                requiresApproval = task.requiresApproval,  // ✨ 추가
                approvalStatus = task.approvalStatus?.name,  // ✨ 추가
                approvedBy = task.approvedBy,  // ✨ 추가
                approvedAt = task.approvedAt,  // ✨ 추가
                completedBy = task.completedBy,
                completedAt = task.completedAt,
                createdBy = task.createdBy,
                createdAt = Date(task.createdAt),
                updatedAt = Date(task.updatedAt)
            )
        }
    }
}