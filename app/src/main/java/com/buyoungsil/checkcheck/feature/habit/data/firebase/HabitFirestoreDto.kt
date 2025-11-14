package com.buyoungsil.checkcheck.feature.habit.data.firebase

import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore용 Habit DTO
 * ✅ 코인 보상 필드 추가
 */
data class HabitFirestoreDto(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String? = null,
    val icon: String = "📌",
    val color: String = "#6650a4",
    val groupShared: Boolean = false,
    val groupId: String? = null,
    val isGroupChallenge: Boolean = false,

    // 🆕 코인 보상 필드
    val coinRewardEnabled: Boolean = true,
    val lastRewardStreak: Int = 0,
    val lastRewardDate: Long? = null,

    @ServerTimestamp
    val createdAt: Date? = null,
    val active: Boolean = true
) {
    constructor() : this(
        id = "",
        userId = "",
        title = "",
        description = null,
        icon = "📌",
        color = "#6650a4",
        groupShared = false,
        groupId = null,
        isGroupChallenge = false,
        coinRewardEnabled = true, // 🆕
        lastRewardStreak = 0,     // 🆕
        lastRewardDate = null,    // 🆕
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
            groupShared = groupShared,
            groupId = groupId,
            isGroupChallenge = isGroupChallenge,
            coinRewardEnabled = coinRewardEnabled,     // 🆕
            lastRewardStreak = lastRewardStreak,       // 🆕
            lastRewardDate = lastRewardDate,           // 🆕
            createdAt = createdAt?.time ?: System.currentTimeMillis(),
            updatedAt = createdAt?.time ?: System.currentTimeMillis(),
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
                groupShared = habit.groupShared,
                groupId = habit.groupId,
                isGroupChallenge = habit.isGroupChallenge,
                coinRewardEnabled = habit.coinRewardEnabled,   // 🆕
                lastRewardStreak = habit.lastRewardStreak,     // 🆕
                lastRewardDate = habit.lastRewardDate,         // 🆕
                createdAt = Date(habit.createdAt),
                active = habit.active
            )
        }
    }
}