package com.buyoungsil.checkcheck.feature.group.presentation.create

import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType

data class CreateGroupUiState(
    val name: String = "",
    val type: GroupType = GroupType.FAMILY,
    val icon: String = "👨‍👩‍👧‍👦",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)