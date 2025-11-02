package com.buyoungsil.checkcheck.feature.habit.presentation.create

import com.buyoungsil.checkcheck.feature.group.domain.model.Group

data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val icon: String = "📌",
    val color: String = "#6650a4",
    val groupShared: Boolean = false,  // ✅ isGroupShared → groupShared
    val selectedGroup: Group? = null,
    val availableGroups: List<Group> = emptyList(),
    val loading: Boolean = false,  // ✅ isLoading → loading
    val error: String? = null,
    val success: Boolean = false  // ✅ isSuccess → success
)