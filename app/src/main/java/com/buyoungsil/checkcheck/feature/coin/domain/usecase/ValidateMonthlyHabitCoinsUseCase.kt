package com.buyoungsil.checkcheck.feature.coin.domain.usecase

import android.util.Log
import com.buyoungsil.checkcheck.core.constants.HabitLimits
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * 월간 코인 획득 제한 검증 UseCase
 *
 * 어뷰징 방어를 위해 사용자의 월간 습관 보상 코인 획득량을 제한합니다.
 *
 * ## 제한 사항
 * - 월 최대 200코인 (습관 보상만 해당)
 * - familyCoins (선물, 태스크 보상 등)는 제한 없음
 *
 * ## Firestore 구조
 * ```
 * monthlyHabitCoins/{userId}/
 *   {month}/  // 예: 2025-01
 *     totalCoins: Int     // 이번 달 누적 획득 코인
 *     lastUpdated: Long   // 마지막 업데이트 시각
 * ```
 *
 * @since 2025-01-15
 */
class ValidateMonthlyHabitCoinsUseCase @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "ValidateMonthlyCoins"
        private const val COLLECTION_NAME = "monthlyHabitCoins"
    }

    /**
     * 월간 코인 획득 제한을 검증합니다
     *
     * @param userId 사용자 ID
     * @param additionalCoins 추가로 획득하려는 코인
     * @return 획득 가능하면 Result.success, 초과 시 Result.failure
     */
    suspend operator fun invoke(
        userId: String,
        additionalCoins: Int
    ): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "월간 코인 제한 검증 시작")
            Log.d(TAG, "userId: $userId, 추가 코인: $additionalCoins")

            // 1. 현재 월 문자열 생성 (예: "2025-01")
            val currentMonth = getCurrentMonthString()
            Log.d(TAG, "현재 월: $currentMonth")

            // 2. Firestore에서 월간 누적 코인 조회
            val monthDoc = firestore
                .collection(COLLECTION_NAME)
                .document(userId)
                .collection("months")
                .document(currentMonth)
                .get()
                .await()

            val currentMonthlyCoins = if (monthDoc.exists()) {
                (monthDoc.getLong("totalCoins") ?: 0L).toInt()
            } else {
                0
            }

            Log.d(TAG, "이번 달 누적 코인: $currentMonthlyCoins")

            // 3. 제한 검증
            val totalAfterReward = currentMonthlyCoins + additionalCoins
            val maxCoins = HabitLimits.MAX_MONTHLY_HABIT_COINS

            Log.d(TAG, "추가 후 예상 합계: $totalAfterReward")
            Log.d(TAG, "월간 최대 허용: $maxCoins")

            if (totalAfterReward > maxCoins) {
                val remaining = maxCoins - currentMonthlyCoins
                Log.w(TAG, "❌ 월간 코인 제한 초과!")
                Log.w(TAG, "현재: $currentMonthlyCoins, 추가: $additionalCoins, 최대: $maxCoins")
                Log.w(TAG, "남은 코인: $remaining")
                Log.d(TAG, "========================================")

                return Result.failure(
                    MonthlyCoinsLimitExceededException(
                        currentCoins = currentMonthlyCoins,
                        attemptedCoins = additionalCoins,
                        maxCoins = maxCoins,
                        remainingCoins = maxOf(0, remaining)
                    )
                )
            }

            Log.d(TAG, "✅ 월간 코인 제한 통과 (여유: ${maxCoins - totalAfterReward}코인)")
            Log.d(TAG, "========================================")
            Result.success(Unit)

        } catch (e: MonthlyCoinsLimitExceededException) {
            // 이미 로그 출력됨
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 월간 코인 검증 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }

    /**
     * 월간 코인 누적 기록 업데이트
     *
     * 실제 코인 지급 후 호출되어야 합니다.
     *
     * @param userId 사용자 ID
     * @param coins 추가된 코인
     */
    suspend fun recordMonthlyCoins(
        userId: String,
        coins: Int
    ) {
        try {
            Log.d(TAG, "월간 코인 기록 업데이트: userId=$userId, coins=$coins")

            val currentMonth = getCurrentMonthString()
            val monthDocRef = firestore
                .collection(COLLECTION_NAME)
                .document(userId)
                .collection("months")
                .document(currentMonth)

            // FieldValue.increment 사용 (원자적 증가)
            monthDocRef.set(
                mapOf(
                    "totalCoins" to com.google.firebase.firestore.FieldValue.increment(coins.toLong()),
                    "lastUpdated" to System.currentTimeMillis()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()

            Log.d(TAG, "✅ 월간 코인 기록 업데이트 완료")

        } catch (e: Exception) {
            Log.e(TAG, "❌ 월간 코인 기록 업데이트 실패", e)
            // 실패해도 코인 지급은 이미 완료되었으므로 throw하지 않음
        }
    }

    /**
     * 사용자의 월간 누적 코인 조회
     *
     * @param userId 사용자 ID
     * @return 이번 달 누적 코인
     */
    suspend fun getMonthlyCoins(userId: String): Int {
        return try {
            val currentMonth = getCurrentMonthString()
            val monthDoc = firestore
                .collection(COLLECTION_NAME)
                .document(userId)
                .collection("months")
                .document(currentMonth)
                .get()
                .await()

            if (monthDoc.exists()) {
                (monthDoc.getLong("totalCoins") ?: 0L).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "월간 코인 조회 실패", e)
            0
        }
    }

    /**
     * 월간 잔여 코인 계산
     *
     * @param userId 사용자 ID
     * @return 이번 달 추가로 획득 가능한 코인
     */
    suspend fun getRemainingMonthlyCoins(userId: String): Int {
        val currentCoins = getMonthlyCoins(userId)
        val maxCoins = HabitLimits.MAX_MONTHLY_HABIT_COINS
        return maxOf(0, maxCoins - currentCoins)
    }

    /**
     * 현재 월 문자열 생성
     *
     * @return "YYYY-MM" 형식 (예: "2025-01")
     */
    private fun getCurrentMonthString(): String {
        val now = LocalDate.now()
        val year = now.year
        val month = now.monthValue.toString().padStart(2, '0')
        return "$year-$month"
    }
}

/**
 * 월간 코인 제한 초과 예외
 *
 * @property currentCoins 현재 월 누적 코인
 * @property attemptedCoins 추가하려던 코인
 * @property maxCoins 월간 최대 코인
 * @property remainingCoins 남은 코인
 */
class MonthlyCoinsLimitExceededException(
    val currentCoins: Int,
    val attemptedCoins: Int,
    val maxCoins: Int,
    val remainingCoins: Int
) : Exception(
    "월간 코인 제한 초과: 현재 $currentCoins 코인, 추가 시도 $attemptedCoins 코인, " +
            "최대 $maxCoins 코인 (남은 코인: $remainingCoins)"
)