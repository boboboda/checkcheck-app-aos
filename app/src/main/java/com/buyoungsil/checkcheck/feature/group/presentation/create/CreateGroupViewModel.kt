package com.buyoungsil.checkcheck.feature.group.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType
import com.buyoungsil.checkcheck.feature.group.domain.usecase.CreateGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onTypeChange(type: GroupType) {
        _uiState.update { it.copy(type = type, icon = type.icon) }
    }

    fun onCreateGroup() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(error = "그룹 이름을 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val group = Group(
                id = "",
                name = currentState.name,
                icon = currentState.icon,
                type = currentState.type,
                inviteCode = "",
                ownerId = currentUserId
            )

            createGroupUseCase(group, currentUserId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "그룹 생성 실패"
                        )
                    }
                }
        }
    }
}