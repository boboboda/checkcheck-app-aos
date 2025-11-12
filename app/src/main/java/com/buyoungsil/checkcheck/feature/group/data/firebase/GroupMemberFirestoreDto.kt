// app/src/main/java/com/buyoungsil/checkcheck/feature/group/data/firebase/GroupMemberFirestoreDto.kt
package com.buyoungsil.checkcheck.feature.group.data.firebase

import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember
import com.buyoungsil.checkcheck.feature.group.domain.model.MemberRole
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GroupMemberFirestoreDto(
    @DocumentId
    val userId: String = "",
    val groupId: String = "",
    val displayName: String = "",  // 그룹 내 닉네임
    val photoUrl: String? = null,
    val role: String = MemberRole.MEMBER.name,
    @ServerTimestamp
    val joinedAt: Date? = null
) {
    constructor() : this("", "", "", null, MemberRole.MEMBER.name, null)

    fun toDomain(): GroupMember {
        return GroupMember(
            userId = userId,
            groupId = groupId,
            displayName = displayName,
            photoUrl = photoUrl,
            role = try {
                MemberRole.valueOf(role)
            } catch (e: Exception) {
                MemberRole.MEMBER
            },
            joinedAt = joinedAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(member: GroupMember): GroupMemberFirestoreDto {
            return GroupMemberFirestoreDto(
                userId = member.userId,
                groupId = member.groupId,
                displayName = member.displayName,
                photoUrl = member.photoUrl,
                role = member.role.name,
                joinedAt = Date(member.joinedAt)
            )
        }
    }
}