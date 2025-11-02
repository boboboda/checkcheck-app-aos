package com.buyoungsil.checkcheck.feature.group.presentation.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.feature.group.domain.usecase.JoinGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinGroupViewModel @Inject constructor(
    private val joinGroupUseCase: JoinGroupUseCase
) : ViewModel() {

    private val _inviteCode = MutableStateFlow("")
    val inviteCode: StateFlow<String> = _inviteCode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val currentUserId = "test_user_id"

    fun onInviteCodeChange(code: String) {
        _inviteCode.value = code.uppercase()
        _error.value = null
    }

    fun onJoinGroup() {
        if (_inviteCode.value.isBlank()) {
            _error.value = "초대 코드를 입력해주세요"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            joinGroupUseCase(_inviteCode.value, currentUserId)
                .onSuccess {
                    _isLoading.value = false
                    _isSuccess.value = true
                }
                .onFailure { e ->
                    _isLoading.value = false
                    _error.value = e.message ?: "그룹 참여 실패"
                }
        }
    }
}