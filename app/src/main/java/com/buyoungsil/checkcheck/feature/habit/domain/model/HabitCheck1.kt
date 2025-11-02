package com.buyoungsil.checkcheck.feature.habit.domain.model

import java.time.LocalDate

/**
 * 습관 체크 도메인 모델
 */
data class HabitCheck(
    val id: String,
    val habitId: String,
    val userId: String,
    val date: LocalDate,
    val isCompleted: Boolean = true,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val checkedAt: Long = System.currentTimeMillis()
)