package com.buyoungsil.checkcheck.feature.group.domain.model

/**
 * 그룹 통계
 */
data class GroupStatistics(
    val groupId: String,
    val period: StatisticsPeriod,

    // 전체 그룹 집계
    val totalHabitsCompleted: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalCoinsEarned: Int = 0,
    val activeMemberCount: Int = 0,
    val totalMemberCount: Int = 0,

    // 카테고리별 1등
    val categoryLeaders: Map<String, CategoryLeader> = emptyMap(),

    val generatedAt: Long = System.currentTimeMillis()
)

/**
 * 통계 기간
 */
enum class StatisticsPeriod(val displayName: String) {
    DAILY("오늘"),
    WEEKLY("이번 주"),
    MONTHLY("이번 달"),
    ALL_TIME("전체")
}

/**
 * 카테고리별 리더
 */
data class CategoryLeader(
    val category: String,
    val categoryIcon: String,
    val userId: String,
    val userName: String,
    val habitName: String,
    val streakDays: Int,
    val completionRate: Float
)