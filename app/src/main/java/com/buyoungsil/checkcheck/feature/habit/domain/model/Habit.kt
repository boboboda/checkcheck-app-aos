package com.buyoungsil.checkcheck.feature.habit.domain.model

/**
 * 습관 도메인 모델
 * ✅ 알림 필드 제거 (습관은 알림 불필요)
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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val active: Boolean = true
)