package com.buyoungsil.checkcheck.feature.coin.domain.repository

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinTransaction
import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import kotlinx.coroutines.flow.Flow

/**
 * 코인 Repository 인터페이스
 */
interface CoinRepository {

    /**
     * 코인 지갑 조회
     */
    fun getCoinWallet(userId: String): Flow<CoinWallet?>

    /**
     * 코인 거래 내역 조회
     */
    fun getCoinTransactions(userId: String): Flow<List<CoinTransaction>>

    /**
     * 코인 지갑 생성
     */
    suspend fun createCoinWallet(userId: String): Result<Unit>

    /**
     * 코인 선물하기
     */
    suspend fun giftCoins(
        fromUserId: String,
        toUserId: String,
        amount: Int,
        message: String?
    ): Result<Unit>

    /**
     * 습관 보상 지급 (시스템)
     */
    suspend fun rewardHabitCompletion(
        userId: String,
        habitId: String,
        amount: Int
    ): Result<Unit>

    /**
     * 할일 완료 보상 지급
     */
    suspend fun rewardTaskCompletion(
        userId: String,
        taskId: String,
        amount: Int,
        fromUserId: String
    ): Result<Unit>
}