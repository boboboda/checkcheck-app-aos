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

/**
 * 홈 화면 ViewModel
 *
 * ✅ 리팩토링: 단일 책임 원칙 적용
 * - 습관 로직 → HabitListViewModel
 * - 태스크 로직 → TaskListViewModel (별도 생성 필요)
 * - 홈 화면은 그룹 목록 + 코인만 관리
 *
 * @since 2025-01-15 (리팩토링)
 */
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

    /**
     * 홈 화면 데이터 로딩
     * - 그룹 목록
     * - 코인 지갑
     */
    private fun loadData() {
        Log.d(TAG, "=== 홈 데이터 로딩 시작 ===")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. 그룹 목록 로딩
                getMyGroupsUseCase(currentUserId)
                    .drop(1)  // 첫 빈 emit 무시
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

                // 2. 코인 지갑 로딩
                getCoinWalletUseCase(currentUserId)
                    .catch { e ->
                        Log.e(TAG, "❌ 코인 지갑 로딩 실패", e)
                    }
                    .collect { wallet ->
                        if (wallet != null) {
                            Log.d(TAG, "✅ 코인 지갑 로드: ${wallet.totalCoins}코인")
                            _uiState.update { it.copy(totalCoins = wallet.totalCoins) }
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 홈 데이터 로딩 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "데이터 로딩 실패"
                    )
                }
            }
        }
    }

    /**
     * 에러 상태 초기화
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 다시 시도
     */
    fun onRetry() {
        Log.d(TAG, "다시 시도")
        loadData()
    }
}
