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
    val maxMembers: Int = 20,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class GroupType(val displayName: String, val icon: String) {
    FAMILY("가족", "👨‍👩‍👧‍👦"),
    COUPLE("연인", "💑"),
    STUDY("스터디", "📚"),
    EXERCISE("운동", "🏃‍♂️"),
    PROJECT("프로젝트", "💼"),
    CUSTOM("커스텀", "🎯")
}