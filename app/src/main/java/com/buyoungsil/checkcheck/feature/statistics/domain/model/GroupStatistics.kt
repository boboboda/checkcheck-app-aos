package com.buyoungsil.checkcheck.feature.statistics.domain.model

import com.google.firebase.Timestamp

/**
 * 그룹 통계 모델
 */
data class GroupStatistics(
    val groupId: String,
    val groupName: String,
    val period: StatisticsPeriod,

    // 전체 집계
    val totalHabitsCompleted: Int = 0,
    val totalTasksCompleted: Int = 0,
    val activeMemberCount: Int = 0,
    val totalMemberCount: Int = 0,

    // 평균 지표
    val avgCompletionRate: Float = 0f,
    val avgStreakDays: Float = 0f,
    val activeMemberRate: Float = 0f,

    // 멤버 랭킹 (Top 3)
    val topContributors: List<MemberRank> = emptyList(),

    // 최장 연속 기록 (Top 3)
    val longestStreaks: List<StreakRank> = emptyList(),

    val generatedAt: Timestamp = Timestamp.now()
)

/**
 * 멤버 랭킹 정보
 */
data class MemberRank(
    val userId: String,
    val userName: String,
    val completionCount: Int,
    val completionRate: Float,
    val rank: Int
)

/**
 * 연속 기록 랭킹
 */
data class StreakRank(
    val userId: String,
    val userName: String,
    val habitTitle: String,
    val streakDays: Int,
    val rank: Int
)

/**
 * 통계 기간
 */
enum class StatisticsPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    ALL_TIME
}