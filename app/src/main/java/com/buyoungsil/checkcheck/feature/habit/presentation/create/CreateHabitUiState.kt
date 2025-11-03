package com.buyoungsil.checkcheck.feature.habit.presentation.create

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import java.time.LocalTime

/**
 * 습관 생성 UI State
 * ✅ reminderTime, reminderEnabled 추가
 */
data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val icon: String = "📌",
    val color: String = "#6650a4",
    val groupShared: Boolean = false,
    val selectedGroup: Group? = null,
    val availableGroups: List<Group> = emptyList(),
    val reminderTime: LocalTime? = null,      // ✅ 알림 시간
    val reminderEnabled: Boolean = false,     // ✅ 알림 활성화
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)