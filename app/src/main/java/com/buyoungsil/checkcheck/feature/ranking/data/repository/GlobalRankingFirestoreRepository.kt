package com.buyoungsil.checkcheck.feature.ranking.data.repository

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCategory
import com.buyoungsil.checkcheck.feature.ranking.data.firebase.GlobalRankingFirestoreDto
import com.buyoungsil.checkcheck.feature.ranking.domain.model.GlobalHabitRanking
import com.buyoungsil.checkcheck.feature.ranking.domain.model.MyRankingInfo
import com.buyoungsil.checkcheck.feature.ranking.domain.repository.GlobalRankingRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore 기반 글로벌 랭킹 Repository 구현
 */
@Singleton
class GlobalRankingFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : GlobalRankingRepository {

    companion object {
        private const val TAG = "GlobalRankingRepo"
        private const val COLLECTION_RANKINGS = "globalHabitRankings"
        private const val SUB_COLLECTION_RANKINGS = "rankings"
    }

    override suspend fun updateMyRanking(
        userId: String,
        userName: String,
        habitTitle: String,
        currentStreak: Int,
        totalChecks: Int,
        completionRate: Float
    ): Result<Unit> {
        return try {
            Log.d(TAG, "=== 랭킹 업데이트 ===")
            Log.d(TAG, "습관: $habitTitle")
            Log.d(TAG, "사용자: $userName")
            Log.d(TAG, "연속: $currentStreak 일, 체크: $totalChecks 회")

            val dto = GlobalRankingFirestoreDto.fromStats(
                userId = userId,
                userName = userName,
                currentStreak = currentStreak,
                totalChecks = totalChecks,
                completionRate = completionRate
            )

            firestore
                .collection(COLLECTION_RANKINGS)
                .document(habitTitle)
                .collection(SUB_COLLECTION_RANKINGS)
                .document(userId)
                .set(dto)
                .await()

            Log.d(TAG, "✅ 랭킹 업데이트 완료")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 랭킹 업데이트 실패", e)
            Result.failure(e)
        }
    }

    override suspend fun getTopRankings(
        habitTitle: String,
        limit: Int
    ): Result<GlobalHabitRanking> {
        return try {
            Log.d(TAG, "=== 랭킹 조회 ===")
            Log.d(TAG, "습관: $habitTitle")

            val snapshot = firestore
                .collection(COLLECTION_RANKINGS)
                .document(habitTitle)
                .collection(SUB_COLLECTION_RANKINGS)
                .orderBy("currentStreak", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val rankings = snapshot.documents.mapIndexed { index, doc ->
                val dto = doc.toObject(GlobalRankingFirestoreDto::class.java)
                    ?: return@mapIndexed null  // ✅ null이면 null 반환
                dto.toDomain(rank = index + 1)
            }.filterNotNull()  // ✅ null 제거

            Log.d(TAG, "✅ 랭킹 조회 완료: ${rankings.size}명")

            Result.success(
                GlobalHabitRanking(
                    habitTitle = habitTitle,
                    userRankings = rankings
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ 랭킹 조회 실패", e)
            Result.failure(e)
        }
    }

    override suspend fun getMyRanking(
        userId: String,
        habitTitle: String
    ): Result<MyRankingInfo> {
        return try {
            Log.d(TAG, "=== 내 랭킹 조회 ===")
            Log.d(TAG, "습관: $habitTitle")

            // 1. 전체 랭킹 조회
            val allRankings = getTopRankings(habitTitle, 100).getOrThrow()

            // 2. 내 랭킹 찾기
            val myRanking = allRankings.userRankings.find { it.userId == userId }
                ?: return Result.failure(Exception("랭킹 정보를 찾을 수 없습니다"))

            val info = MyRankingInfo(
                habitTitle = habitTitle,
                myRank = myRanking.rank,
                totalUsers = allRankings.userRankings.size,
                myStats = myRanking
            )

            Log.d(TAG, "✅ 내 랭킹: ${info.myRank}위 / ${info.totalUsers}명")

            Result.success(info)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 내 랭킹 조회 실패", e)
            Result.failure(e)
        }
    }

    override suspend fun getHabitsByCategory(category: HabitCategory): Result<List<String>> {
        return try {
            // globalHabitRankings 컬렉션에서 모든 습관 제목 가져오기
            val snapshot = firestore
                .collection(COLLECTION_RANKINGS)
                .get()
                .await()

            val habitTitles = snapshot.documents
                .map { it.id }  // 문서 ID = 습관 제목
                .distinct()

            Log.d(TAG, "✅ 전체 습관 ${habitTitles.size}개 조회")
            Result.success(habitTitles)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 습관 목록 조회 실패", e)
            Result.failure(e)
        }
    }
}