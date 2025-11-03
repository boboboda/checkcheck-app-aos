package com.buyoungsil.checkcheck.feature.habit.domain.model

import java.time.LocalTime

/**
 * 습관 도메인 모델
 * ✅ reminderTime, reminderEnabled 추가
 */
data class Habit(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val icon: String = "📌",
    val color: String = "#6650a4",
    val reminderTime: LocalTime? = null,      // ✅ 알림 시간
    val reminderEnabled: Boolean = false,     // ✅ 알림 활성화
    val groupShared: Boolean = false,
    val groupId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val active: Boolean = true
)