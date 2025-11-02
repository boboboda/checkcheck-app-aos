package com.buyoungsil.checkcheck.feature.group.presentation.list

import com.buyoungsil.checkcheck.feature.group.domain.model.Group

data class GroupListUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)