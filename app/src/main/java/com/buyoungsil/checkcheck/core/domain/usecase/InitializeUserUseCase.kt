package com.buyoungsil.checkcheck.core.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.core.domain.model.User
import com.buyoungsil.checkcheck.core.domain.repository.UserRepository
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import javax.inject.Inject

/**
 * 로그인 시 User 문서 초기화 UseCase
 * ✅ 익명 로그인 후 Firestore에 User 문서 생성
 * ✅ 코인 지갑 자동 생성 추가
 */
class InitializeUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val coinRepository: CoinRepository  // ✅ 추가
) {
    companion object {
        private const val TAG = "InitializeUserUseCase"
    }

    suspend operator fun invoke(userId: String) {
        try {
            // 1. 기존 User 확인
            val existingUser = userRepository.getUser(userId)

            if (existingUser == null) {
                // User 문서가 없으면 생성
                val newUser = User(
                    id = userId,
                    email = null,
                    displayName = "익명 사용자",
                    photoUrl = null,
                    fcmToken = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.saveUser(newUser)
                Log.d(TAG, "✅ User 문서 생성 완료")
            }

            // 2. 코인 지갑 생성 (이미 있으면 실패해도 무시)
            coinRepository.createCoinWallet(userId)
                .onSuccess {
                    Log.d(TAG, "✅ 코인 지갑 생성 완료")
                }
                .onFailure { error ->
                    Log.d(TAG, "⚠️ 코인 지갑 생성 실패 (이미 존재할 수 있음): ${error.message}")
                }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 사용자 초기화 실패", e)
            e.printStackTrace()
        }
    }
}