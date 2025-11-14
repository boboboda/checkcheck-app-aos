package com.buyoungsil.checkcheck.feature.habit.data.firebase

import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitRewardRecord
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * 습관 보상 기록 Firestore DTO
 */
data class HabitRewardRecordFirestoreDto(
    @DocumentId
    val id: String = "",
    val habitId: String = "",
    val userId: String = "",
    val streakDays: Int = 0,
    val coinsAwarded: Int = 0,
    @ServerTimestamp
    val awardedAt: Date? = null
) {
    constructor() : this("", "", "", 0, 0, null)

    fun toDomain(): HabitRewardRecord {
        return HabitRewardRecord(
            id = id,
            habitId = habitId,
            userId = userId,
            streakDays = streakDays,
            coinsAwarded = coinsAwarded,
            awardedAt = awardedAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(record: HabitRewardRecord): HabitRewardRecordFirestoreDto {
            return HabitRewardRecordFirestoreDto(
                id = record.id,
                habitId = record.habitId,
                userId = record.userId,
                streakDays = record.streakDays,
                coinsAwarded = record.coinsAwarded,
                awardedAt = Date(record.awardedAt)
            )
        }
    }
}