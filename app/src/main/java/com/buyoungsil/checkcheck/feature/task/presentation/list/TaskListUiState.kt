package com.buyoungsil.checkcheck.feature.task.presentation.list

import com.buyoungsil.checkcheck.feature.task.domain.model.Task

/**
 * 태스크 리스트 UI 상태
 *
 * ✅ isPersonalMode 추가: 개인 태스크인지 그룹 태스크인지 구분
 */
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPersonalMode: Boolean = false  // ✅ 개인 태스크 모드 여부
)