package com.buyoungsil.checkcheck.feature.group.domain.usecase

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import javax.inject.Inject

class GetGroupByIdUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(groupId: String): Result<Group> {
        return try {
            val group = repository.getGroupById(groupId)
                ?: return Result.failure(Exception("그룹을 찾을 수 없습니다"))
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}