package com.buyoungsil.checkcheck.feature.group.presentation.upgrade

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import com.buyoungsil.checkcheck.feature.coin.domain.usecase.GetCoinWalletUseCase
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetGroupByIdUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.UpgradeGroupTierUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpgradeGroupTierUiState(
    val group: Group? = null,
    val wallet: CoinWallet? = null,
    val loading: Boolean = false,
    val upgradeSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UpgradeGroupTierViewModel @Inject constructor(
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val getCoinWalletUseCase: GetCoinWalletUseCase,
    private val upgradeGroupTierUseCase: UpgradeGroupTierUseCase,
    private val authManager: FirebaseAuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "UpgradeGroupTierVM"
    }

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(UpgradeGroupTierUiState())
    val uiState: StateFlow<UpgradeGroupTierUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: ""

    init {
        Log.d(TAG, "=== ViewModel 초기화 ===")
        Log.d(TAG, "groupId: $groupId")
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // 그룹 정보 로드
                val groupResult = getGroupByIdUseCase(groupId)
                if (groupResult.isSuccess) {
                    val group = groupResult.getOrNull()
                    _uiState.update { it.copy(group = group) }
                    Log.d(TAG, "✅ 그룹 정보 로드 완료: ${group?.name}")
                } else {
                    Log.e(TAG, "❌ 그룹 정보 로드 실패")
                    _uiState.update { it.copy(error = "그룹 정보를 불러올 수 없습니다") }
                }

                // 코인 지갑 로드
                getCoinWalletUseCase(currentUserId)
                    .catch { e ->
                        Log.e(TAG, "❌ 지갑 로드 실패", e)
                    }
                    .collect { wallet ->
                        _uiState.update { it.copy(wallet = wallet) }
                        Log.d(TAG, "✅ 지갑 정보 로드 완료: ${wallet?.familyCoins ?: 0} + ${wallet?.rewardCoins ?: 0}")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 데이터 로드 실패", e)
                _uiState.update { it.copy(error = e.message ?: "알 수 없는 오류가 발생했습니다") }
            }
        }
    }

    fun upgradeGroupTier() {
        viewModelScope.launch {
            Log.d(TAG, "=== 티어 업그레이드 시작 ===")
            _uiState.update { it.copy(loading = true, error = null) }

            try {
                val result = upgradeGroupTierUseCase(
                    groupId = groupId,
                    userId = currentUserId
                )

                if (result.isSuccess) {
                    Log.d(TAG, "✅ 티어 업그레이드 성공")
                    _uiState.update {
                        it.copy(
                            loading = false,
                            upgradeSuccess = true
                        )
                    }
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "업그레이드에 실패했습니다"
                    Log.e(TAG, "❌ 티어 업그레이드 실패: $errorMessage")
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = errorMessage
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 티어 업그레이드 예외 발생", e)
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "알 수 없는 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}