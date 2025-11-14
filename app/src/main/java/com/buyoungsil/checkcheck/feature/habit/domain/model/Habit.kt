package com.buyoungsil.checkcheck.feature.habit.domain.model

/**
 * 습관 도메인 모델
 * ✅ 코인 보상 필드 추가
 */
data class Habit(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val icon: String = "📌",
    val color: String = "#6650a4",
    val groupShared: Boolean = false,
    val groupId: String? = null,
    val isGroupChallenge: Boolean = false,

    // 🆕 코인 보상 관련 필드
    val coinRewardEnabled: Boolean = true,        // 코인 보상 활성화 여부
    val lastRewardStreak: Int = 0,                // 마지막으로 보상받은 streak 일수
    val lastRewardDate: Long? = null,             // 마지막 보상 날짜

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val active: Boolean = true
)