package com.buyoungsil.checkcheck.feature.task.presentation.list

import com.buyoungsil.checkcheck.feature.task.domain.model.Task

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)