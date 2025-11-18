package com.buyoungsil.checkcheck.feature.group.domain.model

data class Group(
    val id: String,
    val name: String,
    val icon: String = "👥",
    val type: GroupType = GroupType.CUSTOM,
    val description: String = "",
    val inviteCode: String,
    val ownerId: String,
    val memberIds: List<String> = emptyList(),
    val tier: GroupTier = GroupTier.BASIC,  // ✨ 추가
    val maxMembers: Int = tier.maxMembers,  // ✨ 티어에서 자동 설정
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 멤버 추가 가능 여부
     */
    fun canAddMember(): Boolean = memberIds.size < maxMembers

    /**
     * 현재 멤버 수
     */
    fun currentMemberCount(): Int = memberIds.size

    /**
     * 업그레이드 가능 여부
     */
    fun canUpgrade(): Boolean = tier.canUpgrade()
}

enum class GroupType(val displayName: String, val icon: String) {
    FAMILY("가족", "👨‍👩‍👧‍👦"),
    COUPLE("연인", "💑"),
    STUDY("스터디", "📚"),
    EXERCISE("운동", "🏃‍♂️"),
    PROJECT("프로젝트", "💼"),
    CUSTOM("커스텀", "🎯")
}