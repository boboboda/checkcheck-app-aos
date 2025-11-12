// app/src/main/java/com/buyoungsil/checkcheck/feature/task/presentation/create/CreateTaskUiState.kt
package com.buyoungsil.checkcheck.feature.task.presentation.create

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember  // ✅ 추가
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import java.time.LocalDate
import java.time.LocalTime

data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.NORMAL,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 60,
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val selectedGroup: Group? = null,
    val groupMembers: List<GroupMember> = emptyList(),  // ✅ 추가
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)