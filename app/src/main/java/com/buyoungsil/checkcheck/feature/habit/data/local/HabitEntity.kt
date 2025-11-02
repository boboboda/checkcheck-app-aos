package com.buyoungsil.checkcheck.feature.habit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import java.time.LocalTime

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val icon: String,
    val color: String,
    val reminderTime: String?,
    val isGroupShared: Boolean,
    val groupId: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Habit {
        return Habit(
            id = id,
            userId = userId,
            title = title,
            description = description,
            icon = icon,
            color = color,
            reminderTime = reminderTime?.let { LocalTime.parse(it) },
            isGroupShared = isGroupShared,
            groupId = groupId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(habit: Habit): HabitEntity {
            return HabitEntity(
                id = habit.id,
                userId = habit.userId,
                title = habit.title,
                description = habit.description,
                icon = habit.icon,
                color = habit.color,
                reminderTime = habit.reminderTime?.toString(),
                isGroupShared = habit.isGroupShared,
                groupId = habit.groupId,
                createdAt = habit.createdAt,
                updatedAt = habit.updatedAt
            )
        }
    }
}