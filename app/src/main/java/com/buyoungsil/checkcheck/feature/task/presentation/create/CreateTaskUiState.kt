package com.buyoungsil.checkcheck.feature.task.presentation.create

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import java.time.LocalDate
import java.time.LocalTime

/**
 * 할일 생성 UI State
 * ✅ 알림 필드 추가
 */
data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.NORMAL,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,              // ✅ 마감 시간
    val reminderEnabled: Boolean = false,        // ✅ 알림 활성화
    val reminderMinutesBefore: Int = 60,         // ✅ 몇 분 전 (기본 1시간)
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val selectedGroup: Group? = null,
    val availableGroups: List<Group> = emptyList(),
    val loading: Boolean = false,            // ✅ isLoading → loading
    val error: String? = null,
    val success: Boolean = false             // ✅ isSuccess → success
)