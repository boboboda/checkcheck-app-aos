package com.buyoungsil.checkcheck.feature.coin.presentation.wallet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.coin.domain.usecase.GetCoinTransactionsUseCase
import com.buyoungsil.checkcheck.feature.coin.domain.usecase.GetCoinWalletUseCase
import com.buyoungsil.checkcheck.feature.coin.domain.usecase.GiftCoinsUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetGroupMembersUseCase
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetMyGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinWalletViewModel @Inject constructor(
    private val getCoinWalletUseCase: GetCoinWalletUseCase,
    private val getCoinTransactionsUseCase: GetCoinTransactionsUseCase,
    private val giftCoinsUseCase: GiftCoinsUseCase,
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "CoinWalletViewModel"
    }

    private val _uiState = MutableStateFlow(CoinWalletUiState())
    val uiState: StateFlow<CoinWalletUiState> = _uiState.asStateFlow()

    val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadWalletData()
        loadGroupMembers()
    }

    private fun loadWalletData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 코인 지갑 조회
                getCoinWalletUseCase(currentUserId).collect { wallet ->
                    _uiState.update { it.copy(wallet = wallet, isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "코인 지갑 로드 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "코인 지갑을 불러올 수 없습니다"
                    )
                }
            }
        }

        viewModelScope.launch {
            try {
                // 거래 내역 조회
                getCoinTransactionsUseCase(currentUserId).collect { transactions ->
                    _uiState.update { it.copy(transactions = transactions) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "거래 내역 로드 실패", e)
            }
        }
    }

    private fun loadGroupMembers() {
        viewModelScope.launch {
            try {
                // 내가 속한 그룹들 조회
                getMyGroupsUseCase(currentUserId).collect { groups ->
                    Log.d(TAG, "내 그룹 수: ${groups.size}")

                    // 모든 그룹의 멤버를 합쳐서 중복 제거
                    val allMembers = mutableSetOf<String>() // userId를 Set으로 중복 관리
                    val memberList = mutableListOf<com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember>()

                    // 각 그룹의 멤버를 순차적으로 조회
                    groups.forEach { group ->
                        launch {
                            getGroupMembersUseCase(group.id).collect { members ->
                                Log.d(TAG, "그룹 ${group.name} 멤버 수: ${members.size}")
                                members.forEach { member ->
                                    // 중복 제거 (userId 기준)
                                    if (allMembers.add(member.userId)) {
                                        memberList.add(member)
                                    }
                                }
                                _uiState.update { it.copy(groupMembers = memberList.toList()) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "그룹 멤버 로드 실패", e)
            }
        }
    }

    fun giftCoins(toUserId: String, amount: Int, message: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            giftCoinsUseCase(currentUserId, toUserId, amount, message)
                .onSuccess {
                    Log.d(TAG, "코인 선물 성공")
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                .onFailure { error ->
                    Log.e(TAG, "코인 선물 실패", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "코인 선물 실패"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}