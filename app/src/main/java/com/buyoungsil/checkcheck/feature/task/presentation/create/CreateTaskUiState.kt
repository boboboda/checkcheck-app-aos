package com.buyoungsil.checkcheck.feature.task.presentation.create

import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority

data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.NORMAL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)