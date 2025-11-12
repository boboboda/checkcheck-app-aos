package com.buyoungsil.checkcheck.feature.group.presentation.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember
import com.buyoungsil.checkcheck.feature.group.domain.model.MemberRole
import com.buyoungsil.checkcheck.feature.group.domain.usecase.AddGroupMemberUseCase
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
    private val joinGroupUseCase: JoinGroupUseCase,
    private val addGroupMemberUseCase: AddGroupMemberUseCase,  // ✅ 추가
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    private val _inviteCode = MutableStateFlow("")
    val inviteCode: StateFlow<String> = _inviteCode.asStateFlow()

    // ✅ 닉네임 추가
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    fun onInviteCodeChange(code: String) {
        _inviteCode.value = code.uppercase()
        _error.value = null
    }

    // ✅ 추가
    fun onNicknameChange(nickname: String) {
        _nickname.value = nickname
        _error.value = null
    }

    fun onJoinGroup() {
        if (_inviteCode.value.isBlank()) {
            _error.value = "초대 코드를 입력해주세요"
            return
        }

        // ✅ 닉네임 검증 추가
        if (_nickname.value.isBlank()) {
            _error.value = "그룹 내 닉네임을 입력해주세요"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            joinGroupUseCase(_inviteCode.value, currentUserId)
                .onSuccess { group ->
                    // ✅ 그룹 참여 후 GroupMember 추가
                    val member = GroupMember(
                        userId = currentUserId,
                        groupId = group.id,
                        displayName = _nickname.value,  // ✅ 닉네임 저장
                        role = MemberRole.MEMBER
                    )

                    addGroupMemberUseCase(member)
                        .onSuccess {
                            _isLoading.value = false
                            _isSuccess.value = true
                        }
                        .onFailure { error ->
                            _isLoading.value = false
                            _error.value = error.message ?: "그룹 멤버 추가 실패"
                        }
                }
                .onFailure { e ->
                    _isLoading.value = false
                    _error.value = e.message ?: "그룹 참여 실패"
                }
        }
    }
}