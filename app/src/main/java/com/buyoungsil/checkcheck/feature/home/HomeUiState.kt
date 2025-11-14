package com.buyoungsil.checkcheck.feature.home

import com.buyoungsil.checkcheck.feature.group.domain.model.Group

/**
 * 홈 화면 UI State
 *
 * ✅ 리팩토링: 단일 책임 원칙 적용
 * - 습관 관련 제거 (HabitListViewModel로 이동)
 * - 태스크 관련 제거 (TaskListViewModel로 이동)
 * - 그룹 + 코인만 관리
 */
data class HomeUiState(
    val groups: List<Group> = emptyList(),
    val totalCoins: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)