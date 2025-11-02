package com.buyoungsil.checkcheck.feature.habit.data.firebase

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class HabitFirestoreDto(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String? = null,
    val icon: String = "📌",
    val color: String = "#6650a4",
    val isGroupShared: Boolean = false,
    val groupId: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    val isActive: Boolean = true
) {
    // Firestore는 기본 생성자 필요
    constructor() : this(
        id = "",
        userId = "",
        title = "",
        description = null,
        icon = "📌",
        color = "#6650a4",
        isGroupShared = false,
        groupId = null,
        createdAt = null,
        isActive = true
    )

    fun toDomain(): Habit {
        return Habit(
            id = id,
            userId = userId,
            title = title,
            description = description,
            icon = icon,
            color = color,
            isGroupShared = isGroupShared,
            groupId = groupId,
            createdAt = createdAt?.time ?: System.currentTimeMillis(),
            isActive = isActive
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
                isGroupShared = habit.isGroupShared,
                groupId = habit.groupId,
                createdAt = Date(habit.createdAt),
                isActive = habit.isActive
            )
        }
    }
}