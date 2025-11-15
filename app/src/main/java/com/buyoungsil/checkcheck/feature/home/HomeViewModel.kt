package com.buyoungsil.checkcheck.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.coin.domain.usecase.GetCoinWalletUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetMyGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val getCoinWalletUseCase: GetCoinWalletUseCase,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        Log.d(TAG, "=== HomeViewModel 초기화 ===")
        Log.d(TAG, "currentUserId: $currentUserId")
        loadData()
    }

    private fun loadData() {
        Log.d(TAG, "=== 홈 데이터 로딩 시작 ===")

        // ✅ 그룹 목록 로딩 (별도 launch)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getMyGroupsUseCase(currentUserId)
                    .catch { e ->
                        Log.e(TAG, "❌ 그룹 로딩 실패", e)
                    }
                    .collect { groups ->
                        Log.d(TAG, "✅ 그룹 ${groups.size}개 로드됨")
                        _uiState.update {
                            it.copy(
                                groups = groups,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 그룹 로딩 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "그룹 로딩 실패"
                    )
                }
            }
        }

        // ✅ 코인 지갑 로딩 (별도 launch로 분리!)
        viewModelScope.launch {
            try {
                getCoinWalletUseCase(currentUserId)
                    .catch { e ->
                        Log.e(TAG, "❌ 코인 지갑 로딩 실패", e)
                    }
                    .collect { wallet ->
                        if (wallet != null) {
                            Log.d(TAG, "✅ 코인 지갑 로드: ${wallet.totalCoins}코인")
                            _uiState.update { it.copy(totalCoins = wallet.totalCoins) }
                        } else {
                            Log.d(TAG, "⚠️ 코인 지갑이 null (아직 생성 안됨)")
                            _uiState.update { it.copy(totalCoins = 0) }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 코인 지갑 로딩 실패", e)
                _uiState.update { it.copy(totalCoins = 0) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onRetry() {
        Log.d(TAG, "다시 시도")
        loadData()
    }
}