package com.buyoungsil.checkcheck.feature.group.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetMyGroupsUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.LeaveGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupListUiState())
    val uiState: StateFlow<GroupListUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getMyGroupsUseCase(currentUserId).collect { groups ->
                    _uiState.update {
                        it.copy(
                            groups = groups,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "그룹 로드 실패"
                    )
                }
            }
        }
    }

    fun onLeaveGroup(groupId: String) {
        viewModelScope.launch {
            leaveGroupUseCase(groupId, currentUserId)
        }
    }

    fun onRetry() {
        loadGroups()
    }
}