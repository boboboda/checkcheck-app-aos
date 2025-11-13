package com.buyoungsil.checkcheck.feature.coin.domain.model

enum class TransactionType(val displayName: String, val icon: String) {
    HABIT_REWARD("습관 연속 달성 보상", "🏆"),
    TASK_COMPLETION("할일 완료 보상", "✅"),
    CHALLENGE_REWARD("챌린지 달성 보상", "🎯"),
    GIFT("선물", "🎁"),
    COUPON_PURCHASE("쿠폰 구매", "🎫"),
    COIN_CHARGE("코인 충전", "💳"),
    AD_REWARD("광고 시청 보상", "📺")
}