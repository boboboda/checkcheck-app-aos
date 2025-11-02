package com.buyoungsil.checkcheck.feature.home

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats

data class HomeUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val groups: List<Group> = emptyList(),
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)