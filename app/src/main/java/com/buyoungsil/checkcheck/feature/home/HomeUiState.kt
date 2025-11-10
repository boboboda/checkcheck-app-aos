package com.buyoungsil.checkcheck.feature.home

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.task.domain.model.Task

data class HomeUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val groups: List<Group> = emptyList(),
    val urgentTasks: List<Task> = emptyList(),  // ✅ 추가
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)