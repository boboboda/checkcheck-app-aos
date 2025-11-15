package com.buyoungsil.checkcheck.feature.group.presentation.detail

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.task.domain.model.Task

data class GroupDetailUiState(
    val group: Group? = null,
    val sharedHabits: List<HabitWithStats> = emptyList(),
    val sharedHabitsByMember: Map<String, List<HabitWithStats>> = emptyMap(),
    val groupMembers: List<GroupMember> = emptyList(),  // 🆕 추가
    val tasks: List<Task> = emptyList(),
    val memberCount: Int = 0,
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 0,
    val currentUserId: String = "",  // ✅ 추가
    val myNickname: String? = null,  // ✅ 추가
    val isLoading: Boolean = false,
    val error: String? = null
)