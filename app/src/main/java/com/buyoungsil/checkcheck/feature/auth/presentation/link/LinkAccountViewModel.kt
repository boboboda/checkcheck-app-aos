package com.buyoungsil.checkcheck.feature.auth.presentation.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkAccountUiState(
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LinkAccountViewModel @Inject constructor(
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkAccountUiState())
    val uiState: StateFlow<LinkAccountUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onPasswordConfirmChange(passwordConfirm: String) {
        _uiState.update { it.copy(passwordConfirm = passwordConfirm, error = null) }
    }

    fun onLinkAccount() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val passwordConfirm = _uiState.value.passwordConfirm

        // 유효성 검사
        when {
            email.isBlank() -> {
                _uiState.update { it.copy(error = "이메일을 입력해주세요") }
                return
            }
            !isValidEmail(email) -> {
                _uiState.update { it.copy(error = "올바른 이메일 형식이 아닙니다") }
                return
            }
            password.isBlank() -> {
                _uiState.update { it.copy(error = "비밀번호를 입력해주세요") }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(error = "비밀번호는 6자 이상이어야 합니다") }
                return
            }
            password != passwordConfirm -> {
                _uiState.update { it.copy(error = "비밀번호가 일치하지 않습니다") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authManager.linkAnonymousWithEmail(email, password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            success = true
                        )
                    }
                }
                .onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("email address is already in use") == true ->
                            "이미 사용 중인 이메일입니다"
                        exception.message?.contains("network error") == true ->
                            "네트워크 연결을 확인해주세요"
                        exception.message?.contains("익명 사용자가 아닙니다") == true ->
                            "이미 연동된 계정입니다"
                        else -> "계정 연동에 실패했습니다: ${exception.message}"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}