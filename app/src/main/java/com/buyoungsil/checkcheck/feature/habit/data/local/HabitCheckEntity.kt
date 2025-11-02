package com.buyoungsil.checkcheck.feature.habit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCheck
import java.time.LocalDate

@Entity(tableName = "habit_checks")
data class HabitCheckEntity(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val userId: String,
    val date: String,
    val isCompleted: Boolean,
    val note: String?,
    val createdAt: Long
) {
    fun toDomain(): HabitCheck {
        return HabitCheck(
            id = id,
            habitId = habitId,
            userId = userId,
            date = LocalDate.parse(date),
            isCompleted = isCompleted,
            note = note,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(check: HabitCheck): HabitCheckEntity {
            return HabitCheckEntity(
                id = check.id,
                habitId = check.habitId,
                userId = check.userId,
                date = check.date.toString(),
                isCompleted = check.isCompleted,
                note = check.note,
                createdAt = check.createdAt
            )
        }
    }
}