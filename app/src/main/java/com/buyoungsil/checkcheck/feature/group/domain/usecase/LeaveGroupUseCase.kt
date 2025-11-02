package com.buyoungsil.checkcheck.feature.group.domain.usecase

import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import javax.inject.Inject

class LeaveGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(groupId: String, userId: String): Result<Unit> {
        return try {
            val group = repository.getGroupById(groupId)
                ?: return Result.failure(Exception("그룹을 찾을 수 없습니다"))

            if (group.ownerId == userId) {
                return Result.failure(Exception("그룹장은 나갈 수 없습니다. 그룹을 삭제해주세요."))
            }

            repository.leaveGroup(groupId, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}