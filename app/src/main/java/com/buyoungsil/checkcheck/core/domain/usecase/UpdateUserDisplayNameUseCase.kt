// app/src/main/java/com/buyoungsil/checkcheck/core/domain/usecase/UpdateUserDisplayNameUseCase.kt
package com.buyoungsil.checkcheck.core.domain.usecase

import com.buyoungsil.checkcheck.core.domain.repository.UserRepository
import javax.inject.Inject

/**
 * 사용자 전역 닉네임 업데이트 UseCase
 */
class UpdateUserDisplayNameUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, displayName: String): Result<Unit> {
        return try {
            userRepository.updateDisplayName(userId, displayName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}