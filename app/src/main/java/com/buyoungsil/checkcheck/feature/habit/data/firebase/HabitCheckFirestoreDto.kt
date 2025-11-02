package com.buyoungsil.checkcheck.feature.habit.data.firebase

import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCheck
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDate
import java.util.Date

data class HabitCheckFirestoreDto(
    @DocumentId
    val id: String = "",
    val habitId: String = "",
    val userId: String = "",
    val date: String = "",  // ← Firestore는 String 저장
    val isCompleted: Boolean = false,
    @ServerTimestamp
    val checkedAt: Date? = null
) {
    constructor() : this("", "", "", "", false, null)

    fun toDomain(): HabitCheck {
        return HabitCheck(
            id = id,
            habitId = habitId,
            userId = userId,
            date = LocalDate.parse(date),  // ← String을 LocalDate로 변환
            isCompleted = isCompleted,
            checkedAt = checkedAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(check: HabitCheck): HabitCheckFirestoreDto {
            return HabitCheckFirestoreDto(
                id = check.id,
                habitId = check.habitId,
                userId = check.userId,
                date = check.date.toString(),  // ← LocalDate를 String으로 변환
                isCompleted = check.isCompleted,
                checkedAt = Date(check.checkedAt)
            )
        }
    }
}