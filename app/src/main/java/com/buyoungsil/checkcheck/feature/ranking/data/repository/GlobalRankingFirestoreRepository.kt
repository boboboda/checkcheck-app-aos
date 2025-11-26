package com.buyoungsil.checkcheck.feature.ranking.data.repository

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCategory
import com.buyoungsil.checkcheck.feature.ranking.domain.model.GlobalHabitRanking
import com.buyoungsil.checkcheck.feature.ranking.domain.model.MyRankingInfo
import com.buyoungsil.checkcheck.feature.ranking.domain.model.UserRanking
import com.buyoungsil.checkcheck.feature.ranking.domain.repository.GlobalRankingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalRankingFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : GlobalRankingRepository {

    companion object {
        private const val TAG = "GlobalRankingRepo"
        private const val COLLECTION = "habitRankings"
    }

    private fun getCurrentMonth(): String {
        val now = java.time.LocalDate.now()
        return "${now.year}_${now.monthValue.toString().padStart(2, '0')}"
    }

    override suspend fun updateMyRanking(
        userId: String,
        userName: String,
        habitTitle: String,
        currentStreak: Int,
        totalChecks: Int,
        completionRate: Float
    ): Result<Unit> {
        // Firebase Functions가 처리
        return Result.success(Unit)
    }

    override suspend fun getTopRankings(
        habitTitle: String,
        limit: Int
    ): Result<GlobalHabitRanking> {
        return try {
            Log.d(TAG, "=== 랭킹 조회: $habitTitle ===")

            val currentMonth = getCurrentMonth()
            val categories = listOf("HEALTH", "PRODUCTIVITY", "LIFE", "LEARNING", "RELATIONSHIP", "FINANCE")

            for (category in categories) {
                val docId = "${currentMonth}_${category}_${habitTitle}"

                val doc = firestore
                    .collection(COLLECTION)
                    .document(docId)
                    .get()
                    .await()

                if (doc.exists()) {
                    val rankingsList = doc.get("rankings") as? List<Map<String, Any>> ?: emptyList()

                    val rankings = rankingsList.mapNotNull { map ->
                        try {
                            UserRanking(
                                userId = map["userId"] as? String ?: "",
                                userName = map["userName"] as? String ?: "",
                                currentStreak = (map["currentStreak"] as? Long)?.toInt() ?: 0,
                                totalChecks = (map["totalChecks"] as? Long)?.toInt() ?: 0,
                                completionRate = (map["completionRate"] as? Double)?.toFloat() ?: 0f,
                                rank = (map["rank"] as? Long)?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.take(limit)

                    Log.d(TAG, "✅ 랭킹 조회 완료: ${rankings.size}명")

                    return Result.success(
                        GlobalHabitRanking(
                            habitTitle = habitTitle,
                            userRankings = rankings
                        )
                    )
                }
            }

            Result.success(GlobalHabitRanking(habitTitle = habitTitle, userRankings = emptyList()))

        } catch (e: Exception) {
            Log.e(TAG, "❌ 랭킹 조회 실패", e)
            Result.failure(e)
        }
    }

    override suspend fun getMyRanking(userId: String, habitTitle: String): Result<MyRankingInfo> {
        return try {
            val ranking = getTopRankings(habitTitle, 1000).getOrThrow()
            val myRanking = ranking.userRankings.find { it.userId == userId }
                ?: return Result.failure(Exception("랭킹 정보 없음"))

            Result.success(
                MyRankingInfo(
                    habitTitle = habitTitle,
                    myRank = myRanking.rank,
                    totalUsers = ranking.userRankings.size,
                    myStats = myRanking
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHabitsByCategory(category: HabitCategory): Result<List<String>> {
        return try {
            val currentMonth = getCurrentMonth()

            val snapshot = firestore
                .collection(COLLECTION)
                .whereEqualTo("month", currentMonth)
                .whereEqualTo("category", category.name)
                .get()
                .await()

            val habitTitles = snapshot.documents.mapNotNull { it.getString("habitTitle") }

            Log.d(TAG, "✅ ${category.name} 습관: ${habitTitles.size}개")
            Result.success(habitTitles)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 습관 목록 조회 실패", e)
            Result.failure(e)
        }
    }
}