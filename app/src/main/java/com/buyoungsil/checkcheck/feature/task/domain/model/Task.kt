package com.buyoungsil.checkcheck.feature.task.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * 할일 도메인 모델
 * ✅ 알림 필드 추가
 */
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
    val dueTime: LocalTime? = null,               // ✅ 마감 시간
    val reminderEnabled: Boolean = false,         // ✅ 알림 활성화
    val reminderMinutesBefore: Int = 60,          // ✅ 몇 분 전 알림 (기본 1시간)
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