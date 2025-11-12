// app/src/main/java/com/buyoungsil/checkcheck/feature/group/presentation/create/CreateGroupUiState.kt
package com.buyoungsil.checkcheck.feature.group.presentation.create

import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType

data class CreateGroupUiState(
    val name: String = "",
    val type: GroupType = GroupType.FAMILY,
    val icon: String = GroupType.FAMILY.icon,
    val nickname: String = "",  // ✅ 추가
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)