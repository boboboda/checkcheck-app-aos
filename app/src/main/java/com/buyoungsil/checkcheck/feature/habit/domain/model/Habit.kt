package com.buyoungsil.checkcheck.feature.habit.domain.model

import java.time.LocalTime

data class Habit(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val icon: String = "📌",
    val color: String = "#6650a4",
    val reminderTime: LocalTime? = null,
    val isGroupShared: Boolean = false,
    val groupId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)