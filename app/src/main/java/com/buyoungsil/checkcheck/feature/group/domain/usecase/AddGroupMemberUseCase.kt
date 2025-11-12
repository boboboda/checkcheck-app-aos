// app/src/main/java/com/buyoungsil/checkcheck/feature/group/domain/usecase/AddGroupMemberUseCase.kt
package com.buyoungsil.checkcheck.feature.group.domain.usecase

import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupMemberRepository
import javax.inject.Inject

class AddGroupMemberUseCase @Inject constructor(
    private val repository: GroupMemberRepository
) {
    suspend operator fun invoke(member: GroupMember): Result<Unit> {
        return try {
            repository.addGroupMember(member)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}