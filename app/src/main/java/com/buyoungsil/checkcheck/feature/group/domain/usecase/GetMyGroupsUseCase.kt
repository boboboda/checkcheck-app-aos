package com.buyoungsil.checkcheck.feature.group.domain.usecase

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyGroupsUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(userId: String): Flow<List<Group>> {
        return repository.getGroupsByMember(userId)
    }
}