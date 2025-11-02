package com.buyoungsil.checkcheck.feature.task.domain.model

import java.time.LocalDate

data class Task(
    val id: String,
    val groupId: String,
    val title: String,
    val description: String? = null,
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val dueDate: LocalDate? = null,
    val completedBy: String? = null,
    val completedAt: Long? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TaskStatus(val displayName: String, val icon: String) {
    PENDING("대기중", "⏰"),
    IN_PROGRESS("진행중", "🔄"),
    COMPLETED("완료", "✅"),
    EXPIRED("만료", "❌")
}

enum class TaskPriority(val displayName: String, val color: String) {
    URGENT("긴급", "#FF0000"),
    NORMAL("보통", "#6650a4"),
    LOW("나중", "#999999")
}