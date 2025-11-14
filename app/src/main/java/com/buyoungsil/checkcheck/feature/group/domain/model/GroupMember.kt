package com.buyoungsil.checkcheck.feature.group.domain.model

data class GroupMember(
    val userId: String,
    val groupId: String,
    val displayName: String,
    val photoUrl: String? = null,
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    // 🆕 그룹 이름 추가 (UI 표시용)
    val groupName: String = ""  // "가족", "회사" 등
)

enum class MemberRole {
    OWNER,   // 그룹장
    ADMIN,   // 관리자
    MEMBER   // 일반 멤버
}
