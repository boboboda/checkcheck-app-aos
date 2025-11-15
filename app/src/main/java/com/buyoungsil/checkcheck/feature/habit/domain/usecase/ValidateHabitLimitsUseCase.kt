package com.buyoungsil.checkcheck.feature.habit.domain.usecase

import com.buyoungsil.checkcheck.feature.coin.domain.model.HabitLimits
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 습관 제한 검증 UseCase
 * ✅ @Inject 생성자 주입 사용
 */
class ValidateHabitLimitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val coinRepository: CoinRepository
) {

    /**
     * 습관 생성 가능 여부
     */
    suspend fun canCreateHabit(userId: String): Pair<Boolean, String?> {
        // 1. 전체 습관 개수 체크
        val allHabits = habitRepository.getPersonalHabits(userId).first()
        if (allHabits.size >= HabitLimits.MAX_HABITS_PER_USER) {
            return false to HabitLimits.ERROR_MAX_HABITS
        }

        // 2. 활성 습관 개수 체크 (HabitStatistics의 currentStreak > 0인 습관)
        var activeCount = 0
        for (habit in allHabits) {
            val stats = habitRepository.getHabitStatistics(habit.id)
            if (stats.currentStreak > 0) {
                activeCount++
            }
        }

        if (activeCount >= HabitLimits.MAX_ACTIVE_HABITS) {
            return false to HabitLimits.ERROR_MAX_ACTIVE_HABITS
        }

        return true to null
    }

    /**
     * 코인 지급 가능 여부
     */
    suspend fun canRewardCoins(
        userId: String,
        coinAmount: Int
    ): Pair<Boolean, String?> {
        val wallet = coinRepository.getCoinWallet(userId).first() ?: return true to null

        val now = System.currentTimeMillis()
        val monthStart = HabitLimits.getCurrentMonthStartTimestamp()
        val dayStart = HabitLimits.getCurrentDayStartTimestamp()

        val currentMonthlyCoins = if (wallet.lastMonthReset < monthStart) 0 else wallet.monthlyRewardCoins
        val currentDailyCoins = if (wallet.lastDayReset < dayStart) 0 else wallet.dailyRewardCoins

        // 일간 제한 체크
        if (currentDailyCoins + coinAmount > HabitLimits.MAX_DAILY_HABIT_COINS) {
            return false to HabitLimits.ERROR_DAILY_COIN_LIMIT
        }

        // 월간 제한 체크
        if (currentMonthlyCoins + coinAmount > HabitLimits.MAX_MONTHLY_HABIT_COINS) {
            return false to HabitLimits.ERROR_MONTHLY_COIN_LIMIT
        }

        return true to null
    }
}