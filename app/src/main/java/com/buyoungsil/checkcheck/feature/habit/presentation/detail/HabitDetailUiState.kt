package com.buyoungsil.checkcheck.feature.habit.presentation.detail

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics

/**
 * 습관 상세 화면 UI 상태
 */
data class HabitDetailUiState(
    val habit: Habit? = null,
    val statistics: HabitStatistics? = null,
    val availableGroups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val isUpdating: Boolean = false,
    val deleteSuccess: Boolean = false,
    val updateSuccess: Boolean = false
)