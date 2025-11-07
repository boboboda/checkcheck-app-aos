package com.buyoungsil.checkcheck.core.domain.usecase

import com.buyoungsil.checkcheck.core.domain.model.User
import com.buyoungsil.checkcheck.core.domain.repository.UserRepository
import javax.inject.Inject

/**
 * 로그인 시 User 문서 초기화 UseCase
 * ✅ 익명 로그인 후 Firestore에 User 문서 생성
 */
class InitializeUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String) {
        try {
            // 기존 User 확인
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}