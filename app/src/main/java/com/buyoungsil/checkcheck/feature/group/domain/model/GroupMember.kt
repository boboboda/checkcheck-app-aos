package com.buyoungsil.checkcheck.feature.group.domain.model

data class GroupMember(
    val userId: String,
    val groupId: String,
    val displayName: String,
    val photoUrl: String? = null,
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis()
)

enum class MemberRole {
    OWNER,   // 그룹장
    ADMIN,   // 관리자
    MEMBER   // 일반 멤버
}