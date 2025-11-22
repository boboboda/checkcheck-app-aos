package com.buyoungsil.checkcheck.feature.ranking.data.firebase

import com.buyoungsil.checkcheck.feature.ranking.domain.model.UserRanking
import com.google.firebase.Timestamp

/**
 * Firestore용 글로벌 랭킹 DTO
 *
 * Firestore 구조:
 * globalHabitRankings/{habitTitle}/rankings/{userId}
 */
data class GlobalRankingFirestoreDto(
    val userId: String = "",
    val userName: String = "",
    val currentStreak: Int = 0,
    val totalChecks: Int = 0,
    val completionRate: Float = 0f,
    val lastUpdated: Timestamp = Timestamp.now()
) {
    fun toDomain(rank: Int): UserRanking {
        return UserRanking(
            userId = userId,
            userName = userName,
            currentStreak = currentStreak,
            totalChecks = totalChecks,
            completionRate = completionRate,
            rank = rank
        )
    }

    companion object {
        fun fromStats(
            userId: String,
            userName: String,
            currentStreak: Int,
            totalChecks: Int,
            completionRate: Float
        ): GlobalRankingFirestoreDto {
            return GlobalRankingFirestoreDto(
                userId = userId,
                userName = userName,
                currentStreak = currentStreak,
                totalChecks = totalChecks,
                completionRate = completionRate,
                lastUpdated = Timestamp.now()
            )
        }
    }
}