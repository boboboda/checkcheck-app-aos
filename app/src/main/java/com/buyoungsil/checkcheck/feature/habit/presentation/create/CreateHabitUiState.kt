package com.buyoungsil.checkcheck.feature.habit.presentation.create

import com.buyoungsil.checkcheck.feature.group.domain.model.Group

/**
 * 습관 생성 UI State
 * ✅ 알림 필드 제거
 */
data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val icon: String = "📌",
    val color: String = "#6650a4",
    val groupShared: Boolean = false,
    val selectedGroup: Group? = null,
    val availableGroups: List<Group> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)