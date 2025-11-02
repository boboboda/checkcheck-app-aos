package com.buyoungsil.checkcheck.feature.habit.data.firebase

import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCheck
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDate
import java.util.Date

/**
 * Firestore용 HabitCheck DTO
 * ✅ is 접두사 제거 - Firestore 자동 변환과 일치
 */
data class HabitCheckFirestoreDto(
    @DocumentId
    val id: String = "",
    val habitId: String = "",
    val userId: String = "",
    val date: String = "",  // LocalDate를 String으로 저장
    val completed: Boolean = false,  // ✅ isCompleted → completed
    @ServerTimestamp
    val checkedAt: Date? = null
) {
    constructor() : this("", "", "", "", false, null)

    fun toDomain(): HabitCheck {
        return HabitCheck(
            id = id,
            habitId = habitId,
            userId = userId,
            date = LocalDate.parse(date),
            completed = completed,
            checkedAt = checkedAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(check: HabitCheck): HabitCheckFirestoreDto {
            return HabitCheckFirestoreDto(
                id = check.id,
                habitId = check.habitId,
                userId = check.userId,
                date = check.date.toString(),
                completed = check.completed,
                checkedAt = Date(check.checkedAt)
            )
        }
    }
}