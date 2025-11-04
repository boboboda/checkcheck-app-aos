package com.buyoungsil.checkcheck.core.domain.usecase

import com.buyoungsil.checkcheck.core.domain.repository.UserRepository
import javax.inject.Inject

/**
 * FCM 토큰 업데이트 UseCase
 */
class UpdateFcmTokenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, fcmToken: String) {
        try {
            userRepository.updateFcmToken(userId, fcmToken)
        } catch (e: Exception) {
            // 토큰 업데이트 실패 시 로그만 남기고 앱은 정상 동작
            e.printStackTrace()
        }
    }
}