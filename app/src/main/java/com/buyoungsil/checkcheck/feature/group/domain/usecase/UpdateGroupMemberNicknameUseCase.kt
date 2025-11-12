// app/src/main/java/com/buyoungsil/checkcheck/feature/group/domain/usecase/UpdateGroupMemberNicknameUseCase.kt
package com.buyoungsil.checkcheck.feature.group.domain.usecase

import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupMemberRepository
import javax.inject.Inject

class UpdateGroupMemberNicknameUseCase @Inject constructor(
    private val repository: GroupMemberRepository
) {
    suspend operator fun invoke(
        groupId: String,
        userId: String,
        displayName: String
    ): Result<Unit> {
        return try {
            repository.updateMemberDisplayName(groupId, userId, displayName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}