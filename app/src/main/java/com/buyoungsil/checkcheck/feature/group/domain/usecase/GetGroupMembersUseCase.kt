// app/src/main/java/com/buyoungsil/checkcheck/feature/group/domain/usecase/GetGroupMembersUseCase.kt
package com.buyoungsil.checkcheck.feature.group.domain.usecase

import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupMemberRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupMembersUseCase @Inject constructor(
    private val repository: GroupMemberRepository
) {
    operator fun invoke(groupId: String): Flow<List<GroupMember>> {
        return repository.getGroupMembers(groupId)
    }
}