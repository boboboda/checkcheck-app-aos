package com.buyoungsil.checkcheck.feature.habit.domain.model

/**
 * 습관 마일스톤 (연속 달성 보상)
 */
data class HabitMilestone(
    val days: Int,      // 연속 일수
    val coins: Int      // 지급 코인
)

/**
 * 마일스톤 상수
 */
object HabitMilestones {
    val MILESTONES = listOf(
        HabitMilestone(3, 2),       // 3일 연속 → 2코인
        HabitMilestone(7, 5),       // 7일 연속 → 5코인
        HabitMilestone(14, 10),     // 14일 연속 → 10코인
        HabitMilestone(21, 20),     // 21일 연속 → 20코인
        HabitMilestone(30, 50),     // 30일 연속 → 50코인
        HabitMilestone(50, 100),    // 50일 연속 → 100코인
        HabitMilestone(100, 200)    // 100일 연속 → 200코인
    )

    /**
     * 특정 streak에 해당하는 마일스톤 찾기
     */
    fun getMilestone(streak: Int): HabitMilestone? {
        return MILESTONES.find { it.days == streak }
    }

    /**
     * 다음 마일스톤 찾기
     */
    fun getNextMilestone(currentStreak: Int): HabitMilestone? {
        return MILESTONES.firstOrNull { it.days > currentStreak }
    }

    /**
     * 다음 마일스톤까지 남은 일수
     */
    fun getDaysUntilNextMilestone(currentStreak: Int): Int? {
        val nextMilestone = getNextMilestone(currentStreak)
        return nextMilestone?.let { it.days - currentStreak }
    }
}