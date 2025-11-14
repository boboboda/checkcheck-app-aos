package com.buyoungsil.checkcheck.core.constants

/**
 * 습관 및 코인 시스템 제한 상수
 *
 * 어뷰징 방어를 위한 제한 값들을 정의합니다.
 *
 * ⚠️ 중요: 이 제한이 없으면 한 사용자가 월 8,700코인(87,000원) 부정 획득 가능!
 *
 * @since 2025-01-15
 */
object HabitLimits {

    // ==================== 습관 생성 제한 ====================

    /**
     * 사용자당 최대 습관 개수
     *
     * 이유: 과도한 습관 생성으로 인한 어뷰징 방지
     */
    const val MAX_HABITS_PER_USER = 10

    /**
     * 동시 진행 가능한 활성 습관 개수
     *
     * 이유: 실제로 관리 가능한 습관 수 제한
     */
    const val MAX_ACTIVE_HABITS = 5

    /**
     * 습관 생성 후 재생성 쿨다운 (시간)
     *
     * 이유: 연속 생성/삭제를 통한 마일스톤 어뷰징 방지
     */
    const val HABIT_CREATION_COOLDOWN_HOURS = 24

    // ==================== 코인 지급 제한 ====================

    /**
     * 하루 최대 획득 가능 코인 (습관 보상)
     *
     * 이유: 일일 과도한 코인 획득 방지
     */
    const val MAX_DAILY_HABIT_COINS = 10

    /**
     * 월간 최대 획득 가능 코인 (습관 보상)
     *
     * 계산 근거:
     * - 3일 마일스톤: 10회/월 = 20코인
     * - 7일 마일스톤: 4회/월 = 20코인
     * - 14일 마일스톤: 2회/월 = 20코인
     * - 21일 마일스톤: 1회/월 = 20코인
     * - 30일 마일스톤: 1회/월 = 50코인
     * - 기타 = 70코인
     *
     * 합계 = 200코인/월 (2,000원)
     *
     * 이유: 월간 과도한 코인 획득 방지, 인플레이션 차단
     */
    const val MAX_MONTHLY_HABIT_COINS = 200

    // ==================== 계정 검증 ====================

    /**
     * 코인 지급 시작 가능 최소 계정 나이 (일)
     *
     * 이유: 신규 계정의 즉시 어뷰징 방지
     */
    const val MIN_ACCOUNT_AGE_DAYS = 7

    /**
     * 전화번호 인증 필요 여부
     *
     * 이유: 다중 계정 생성 방지
     *
     * TODO: Phase 6에서 구현 예정
     */
    const val REQUIRE_PHONE_VERIFICATION = false  // 현재는 비활성화

    // ==================== 마일스톤 ====================

    /**
     * 코인 보상 마일스톤 정의
     *
     * 마일스톤별 지급 코인:
     * - 3일: 2코인 (20원)
     * - 7일: 5코인 (50원)
     * - 14일: 10코인 (100원)
     * - 21일: 20코인 (200원)
     * - 30일: 50코인 (500원)
     * - 50일: 100코인 (1,000원)
     * - 100일: 200코인 (2,000원)
     */
    data class CoinRewardMilestone(
        val days: Int,     // 연속 일수
        val coins: Int     // 지급 코인
    )

    val REWARD_MILESTONES = listOf(
        CoinRewardMilestone(3, 2),
        CoinRewardMilestone(7, 5),
        CoinRewardMilestone(14, 10),
        CoinRewardMilestone(21, 20),
        CoinRewardMilestone(30, 50),
        CoinRewardMilestone(50, 100),
        CoinRewardMilestone(100, 200)
    )

    // ==================== 헬퍼 함수 ====================

    /**
     * 주어진 연속 일수가 마일스톤인지 확인
     *
     * @param streakDays 연속 일수
     * @return 마일스톤이면 해당 CoinRewardMilestone, 아니면 null
     */
    fun getMilestone(streakDays: Int): CoinRewardMilestone? {
        return REWARD_MILESTONES.find { it.days == streakDays }
    }

    /**
     * 다음 마일스톤까지 남은 일수 계산
     *
     * @param currentStreak 현재 연속 일수
     * @return 다음 마일스톤까지 남은 일수 (마일스톤이 없으면 null)
     */
    fun getDaysUntilNextMilestone(currentStreak: Int): Int? {
        val nextMilestone = REWARD_MILESTONES
            .filter { it.days > currentStreak }
            .minByOrNull { it.days }

        return nextMilestone?.let { it.days - currentStreak }
    }

    /**
     * 다음 마일스톤 정보 가져오기
     *
     * @param currentStreak 현재 연속 일수
     * @return 다음 마일스톤 (없으면 null)
     */
    fun getNextMilestone(currentStreak: Int): CoinRewardMilestone? {
        return REWARD_MILESTONES
            .filter { it.days > currentStreak }
            .minByOrNull { it.days }
    }
}