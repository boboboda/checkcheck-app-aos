package com.buyoungsil.checkcheck.feature.habit.presentation.create

import com.buyoungsil.checkcheck.feature.group.domain.model.Group

data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val icon: String = "📌",
    val color: String = "#6650a4",
    val isGroupShared: Boolean = false,  // ← 추가
    val selectedGroup: Group? = null,     // ← 추가
    val availableGroups: List<Group> = emptyList(),  // ← 추가
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)