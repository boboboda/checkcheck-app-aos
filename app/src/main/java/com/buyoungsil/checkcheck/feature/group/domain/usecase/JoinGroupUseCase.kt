package com.buyoungsil.checkcheck.feature.group.domain.usecase

import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import javax.inject.Inject

class JoinGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(inviteCode: String, userId: String): Result<Unit> {
        return try {
            val group = repository.getGroupByInviteCode(inviteCode)
                ?: return Result.failure(Exception("그룹을 찾을 수 없습니다"))

            if (group.memberIds.contains(userId)) {
                return Result.failure(Exception("이미 가입된 그룹입니다"))
            }

            if (group.memberIds.size >= group.maxMembers) {
                return Result.failure(Exception("그룹 인원이 가득 찼습니다"))
            }

            repository.joinGroup(group.id, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}