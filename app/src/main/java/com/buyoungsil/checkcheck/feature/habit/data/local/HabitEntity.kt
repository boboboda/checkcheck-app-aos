package com.buyoungsil.checkcheck.feature.habit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit

/**
 * Habit Room Entity
 * ✅ 알림 필드 제거
 * ✅ isGroupChallenge 추가
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val icon: String,
    val color: String,
    val groupShared: Boolean,
    val groupId: String?,
    val isGroupChallenge: Boolean,  // ✅ 추가
    val createdAt: Long,
    val updatedAt: Long,
    val active: Boolean
) {
    fun toDomain(): Habit {
        return Habit(
            id = id,
            userId = userId,
            title = title,
            description = description,
            icon = icon,
            color = color,
            groupShared = groupShared,
            groupId = groupId,
            isGroupChallenge = isGroupChallenge,  // ✅ 추가
            createdAt = createdAt,
            updatedAt = updatedAt,
            active = active
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
                groupShared = habit.groupShared,
                groupId = habit.groupId,
                isGroupChallenge = habit.isGroupChallenge,  // ✅ 추가
                createdAt = habit.createdAt,
                updatedAt = habit.updatedAt,
                active = habit.active
            )
        }
    }
}