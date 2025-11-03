package com.buyoungsil.checkcheck.feature.habit.data.firebase

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalTime
import java.util.Date

/**
 * Firestore용 Habit DTO
 * ✅ reminderTime, reminderEnabled 추가
 */
data class HabitFirestoreDto(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String? = null,
    val icon: String = "📌",
    val color: String = "#6650a4",
    val reminderTime: String? = null,      // ✅ "HH:mm" 형식
    val reminderEnabled: Boolean = false,  // ✅
    val groupShared: Boolean = false,
    val groupId: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    val active: Boolean = true
) {
    // Firestore는 기본 생성자 필요
    constructor() : this(
        id = "",
        userId = "",
        title = "",
        description = null,
        icon = "📌",
        color = "#6650a4",
        reminderTime = null,
        reminderEnabled = false,
        groupShared = false,
        groupId = null,
        createdAt = null,
        active = true
    )

    fun toDomain(): Habit {
        return Habit(
            id = id,
            userId = userId,
            title = title,
            description = description,
            icon = icon,
            color = color,
            reminderTime = reminderTime?.let { LocalTime.parse(it) },  // ✅
            reminderEnabled = reminderEnabled,                          // ✅
            groupShared = groupShared,
            groupId = groupId,
            createdAt = createdAt?.time ?: System.currentTimeMillis(),
            active = active
        )
    }

    companion object {
        fun fromDomain(habit: Habit): HabitFirestoreDto {
            return HabitFirestoreDto(
                id = habit.id,
                userId = habit.userId,
                title = habit.title,
                description = habit.description,
                icon = habit.icon,
                color = habit.color,
                reminderTime = habit.reminderTime?.toString(),  // ✅
                reminderEnabled = habit.reminderEnabled,        // ✅
                groupShared = habit.groupShared,
                groupId = habit.groupId,
                createdAt = Date(habit.createdAt),
                active = habit.active
            )
        }
    }
}