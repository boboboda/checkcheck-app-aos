package com.buyoungsil.checkcheck.feature.ranking.domain.repository

import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCategory
import com.buyoungsil.checkcheck.feature.ranking.domain.model.GlobalHabitRanking
import com.buyoungsil.checkcheck.feature.ranking.domain.model.MyRankingInfo

/**
 * 글로벌 습관 랭킹 Repository
 */
interface GlobalRankingRepository {

    /**
     * 내 랭킹 정보 업데이트
     */
    suspend fun updateMyRanking(
        userId: String,
        userName: String,
        habitTitle: String,
        currentStreak: Int,
        totalChecks: Int,
        completionRate: Float
    ): Result<Unit>

    /**
     * 특정 습관의 전체 랭킹 조회 (상위 100명)
     */
    suspend fun getTopRankings(
        habitTitle: String,
        limit: Int = 100
    ): Result<GlobalHabitRanking>

    /**
     * 내 랭킹 조회
     */
    suspend fun getMyRanking(
        userId: String,
        habitTitle: String
    ): Result<MyRankingInfo>


    suspend fun getHabitsByCategory(category: HabitCategory): Result<List<String>>

}