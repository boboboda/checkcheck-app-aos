package com.buyoungsil.checkcheck.feature.coin.domain.model

/**
 * 코인 거래 내역 도메인 모델
 *
 * @property id 거래 ID
 * @property fromUserId 보낸 사람 ID ("system" = 시스템)
 * @property fromUserName 보낸 사람 이름
 * @property toUserId 받는 사람 ID
 * @property toUserName 받는 사람 이름
 * @property amount 코인 금액
 * @property type 거래 타입
 * @property relatedTaskId 관련 할일 ID (할일 보상인 경우)
 * @property relatedHabitId 관련 습관 ID (습관 보상인 경우)
 * @property message 메시지 (선물인 경우)
 * @property timestamp 거래 시간
 */
data class CoinTransaction(
    val id: String = "",
    val fromUserId: String,
    val fromUserName: String = "",
    val toUserId: String,
    val toUserName: String = "",
    val amount: Int,
    val type: TransactionType,
    val relatedTaskId: String? = null,
    val relatedHabitId: String? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)