package com.buyoungsil.checkcheck.feature.coin.domain.model

import java.util.Calendar

/**
 * 습관 시스템의 제한 상수들
 * 어뷰징 방지 및 시스템 안정성을 위한 제한값 정의
 */
object HabitLimits {

    // ============================================
    // 습관 개수 제한
    // ============================================

    /** 사용자당 최대 습관 개수 */
    const val MAX_HABITS_PER_USER = 10

    /** 동시에 진행 가능한 습관 개수 */
    const val MAX_ACTIVE_HABITS = 5

    // ============================================
    // 코인 제한
    // ============================================

    /** 하루 최대 획득 가능한 습관 보상 코인 */
    const val MAX_DAILY_HABIT_COINS = 10

    /** 한 달 최대 획득 가능한 습관 보상 코인 */
    const val MAX_MONTHLY_HABIT_COINS = 200

    // ============================================
    // 에러 메시지
    // ============================================

    const val ERROR_MAX_HABITS = "습관은 최대 ${MAX_HABITS_PER_USER}개까지만 생성할 수 있습니다"
    const val ERROR_MAX_ACTIVE_HABITS = "동시에 진행할 수 있는 습관은 ${MAX_ACTIVE_HABITS}개입니다. 일부 습관을 완료하거나 삭제해주세요"
    const val ERROR_DAILY_COIN_LIMIT = "오늘 획득 가능한 코인을 모두 받았습니다 (최대 ${MAX_DAILY_HABIT_COINS}코인/일)"
    const val ERROR_MONTHLY_COIN_LIMIT = "이번 달 획득 가능한 코인을 모두 받았습니다 (최대 ${MAX_MONTHLY_HABIT_COINS}코인/월)"

    // ============================================
    // 헬퍼 함수
    // ============================================

    /**
     * 현재 월의 시작 타임스탬프 (UTC 기준)
     */
    fun getCurrentMonthStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 현재 일의 시작 타임스탬프 (UTC 기준)
     */
    fun getCurrentDayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}