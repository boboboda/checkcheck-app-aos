package com.buyoungsil.checkcheck.feature.group.domain.model

/**
 * 그룹 티어 시스템
 *
 * 코인으로 업그레이드하여 더 많은 멤버를 초대할 수 있음
 */
enum class GroupTier(
    val displayName: String,
    val maxMembers: Int,
    val upgradeCost: Int?,  // null이면 최고 티어
    val icon: String
) {
    BASIC(
        displayName = "기본",
        maxMembers = 10,
        upgradeCost = 50,
        icon = "🥉"
    ),
    STANDARD(
        displayName = "스탠다드",
        maxMembers = 20,
        upgradeCost = 100,
        icon = "🥈"
    ),
    PREMIUM(
        displayName = "프리미엄",
        maxMembers = 50,
        upgradeCost = 200,
        icon = "🥇"
    ),
    UNLIMITED(
        displayName = "무제한",
        maxMembers = Int.MAX_VALUE,
        upgradeCost = null,
        icon = "💎"
    );

    /**
     * 다음 티어 가져오기
     */
    fun getNextTier(): GroupTier? {
        val allTiers = values()
        val currentIndex = allTiers.indexOf(this)
        return if (currentIndex < allTiers.size - 1) {
            allTiers[currentIndex + 1]
        } else {
            null
        }
    }

    /**
     * 업그레이드 가능 여부
     */
    fun canUpgrade(): Boolean = upgradeCost != null

    companion object {
        /**
         * 기본 티어
         */
        fun default() = BASIC
    }
}