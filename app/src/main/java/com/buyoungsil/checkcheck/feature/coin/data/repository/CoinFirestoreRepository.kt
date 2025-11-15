package com.buyoungsil.checkcheck.feature.coin.data.repository

import android.util.Log
import com.buyoungsil.checkcheck.feature.coin.data.firebase.CoinTransactionFirestoreDto
import com.buyoungsil.checkcheck.feature.coin.data.firebase.CoinWalletFirestoreDto
import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinTransaction
import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet
import com.buyoungsil.checkcheck.feature.coin.domain.model.HabitLimits
import com.buyoungsil.checkcheck.feature.coin.domain.model.TransactionType
import com.buyoungsil.checkcheck.feature.coin.domain.repository.CoinRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Firebase Firestore 기반 코인 Repository 구현
 */
class CoinFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CoinRepository {

    private val walletsCollection = firestore.collection("coinWallets")
    private val transactionsCollection = firestore.collection("coinTransactions")
    private val usersCollection = firestore.collection("users")

    companion object {
        private const val TAG = "CoinFirestoreRepo"
    }

    override fun getCoinWallet(userId: String): Flow<CoinWallet?> = callbackFlow {
        Log.d(TAG, "=== getCoinWallet Flow 시작 ===")
        Log.d(TAG, "userId: $userId")


        val listener = walletsCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getCoinWallet 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                // 🆕 userId를 파라미터로 전달
                val wallet = snapshot?.toObject(CoinWalletFirestoreDto::class.java)?.toDomain(userId)
                Log.d(TAG, "✅ getCoinWallet 데이터 수신: ${wallet?.totalCoins ?: 0}코인")
                trySend(wallet)
            }

        awaitClose {
            Log.d(TAG, "getCoinWallet Flow 종료")
            listener.remove()
        }
    }

    override fun getCoinTransactions(userId: String): Flow<List<CoinTransaction>> = callbackFlow {
        Log.d(TAG, "=== getCoinTransactions Flow 시작 ===")
        Log.d(TAG, "userId: $userId")

        // 즉시 빈 리스트 emit
        trySend(emptyList())

        val listener = transactionsCollection
            .where(
                com.google.firebase.firestore.Filter.or(
                    com.google.firebase.firestore.Filter.equalTo("fromUserId", userId),
                    com.google.firebase.firestore.Filter.equalTo("toUserId", userId)
                )
            )
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getCoinTransactions 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CoinTransactionFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getCoinTransactions 데이터 수신: ${transactions.size}개")
                trySend(transactions)
            }

        awaitClose {
            Log.d(TAG, "getCoinTransactions Flow 종료")
            listener.remove()
        }
    }


    override suspend fun createCoinWallet(userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "💰 코인 지갑 생성 시작")
            Log.d(TAG, "userId: $userId")

            // ✅ 1. 먼저 지갑이 있는지 확인
            val existingWallet = walletsCollection.document(userId).get().await()

            if (existingWallet.exists()) {
                Log.d(TAG, "⚠️ 이미 코인 지갑이 존재함 - 건너뜀")
                Log.d(TAG, "========================================")
                return Result.success(Unit)
            }

            // ✅ 2. 없을 때만 생성
            val wallet = CoinWallet(userId = userId)
            val dto = CoinWalletFirestoreDto.fromDomain(wallet)

            walletsCollection.document(userId)
                .set(dto)
                .await()

            Log.d(TAG, "✅ 코인 지갑 생성 완료")
            Log.d(TAG, "========================================")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 코인 지갑 생성 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }

    override suspend fun giftCoins(
        fromUserId: String,
        toUserId: String,
        amount: Int,
        message: String?
    ): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🎁 코인 선물 시작")
            Log.d(TAG, "from: $fromUserId, to: $toUserId, amount: $amount")

            // 1. 보내는 사람 지갑 확인
            val fromWalletDoc = walletsCollection.document(fromUserId).get().await()
            val fromWallet = fromWalletDoc.toObject(CoinWalletFirestoreDto::class.java)
                ?: throw Exception("보내는 사람의 지갑을 찾을 수 없습니다")

            val totalCoins = fromWallet.familyCoins + fromWallet.rewardCoins
            if (totalCoins < amount) {
                throw Exception("코인이 부족합니다 (보유: ${totalCoins}, 필요: ${amount})")
            }

            // 2. 사용자 이름 가져오기
            val fromUserDoc = usersCollection.document(fromUserId).get().await()
            val toUserDoc = usersCollection.document(toUserId).get().await()
            val fromUserName = fromUserDoc.getString("displayName") ?: "누군가"
            val toUserName = toUserDoc.getString("displayName") ?: "누군가"

            // 3. Firestore 배치 작업
            firestore.runBatch { batch ->
                // 3-1. 보내는 사람 지갑 차감
                batch.update(
                    walletsCollection.document(fromUserId),
                    mapOf(
                        "familyCoins" to FieldValue.increment(-amount.toLong()),
                        "totalSpent" to FieldValue.increment(amount.toLong())
                    )
                )

                // 3-2. 받는 사람 지갑 증가
                batch.update(
                    walletsCollection.document(toUserId),
                    mapOf(
                        "familyCoins" to FieldValue.increment(amount.toLong()),
                        "totalEarned" to FieldValue.increment(amount.toLong())
                    )
                )

                // 3-3. 거래 내역 생성
                val transaction = CoinTransaction(
                    id = transactionsCollection.document().id,
                    fromUserId = fromUserId,
                    fromUserName = fromUserName,
                    toUserId = toUserId,
                    toUserName = toUserName,
                    amount = amount,
                    type = TransactionType.GIFT,
                    message = message
                )
                val transactionDto = CoinTransactionFirestoreDto.fromDomain(transaction)
                batch.set(transactionsCollection.document(transaction.id), transactionDto)
            }.await()

            Log.d(TAG, "✅ 코인 선물 완료")
            Log.d(TAG, "========================================")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 코인 선물 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }




    override suspend fun rewardHabitCompletion(
        userId: String,
        habitId: String,
        coins: Int
    ): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🎁 습관 완료 보상 시작")
            Log.d(TAG, "userId: $userId, habitId: $habitId, coins: $coins")

            // 1. 현재 지갑 조회
            val walletDoc = walletsCollection.document(userId).get().await()
            val walletDto = walletDoc.toObject(CoinWalletFirestoreDto::class.java)
                ?: throw Exception("코인 지갑을 찾을 수 없습니다")

            val currentWallet = walletDto.toDomain(userId)

            // 2. 월간/일간 리셋 체크
            val now = System.currentTimeMillis()
            val monthStart = HabitLimits.getCurrentMonthStartTimestamp()
            val dayStart = HabitLimits.getCurrentDayStartTimestamp()

            val needsMonthReset = currentWallet.lastMonthReset < monthStart
            val needsDayReset = currentWallet.lastDayReset < dayStart

            val newMonthlyCoins = if (needsMonthReset) coins else currentWallet.monthlyRewardCoins + coins
            val newDailyCoins = if (needsDayReset) coins else currentWallet.dailyRewardCoins + coins

            Log.d(TAG, "월간 코인: $newMonthlyCoins/${HabitLimits.MAX_MONTHLY_HABIT_COINS}")
            Log.d(TAG, "일간 코인: $newDailyCoins/${HabitLimits.MAX_DAILY_HABIT_COINS}")

            // 3. 배치 작업 (Firestore Transaction)
            firestore.runBatch { batch ->
                // 3-1. 지갑 업데이트
                val updateMap = mutableMapOf<String, Any>(
                    "rewardCoins" to FieldValue.increment(coins.toLong()),
                    "totalEarned" to FieldValue.increment(coins.toLong()),
                    "monthlyRewardCoins" to newMonthlyCoins,
                    "dailyRewardCoins" to newDailyCoins,
                    "lastUpdated" to Date(now)
                )

                if (needsMonthReset) {
                    updateMap["lastMonthReset"] = Date(now)
                }
                if (needsDayReset) {
                    updateMap["lastDayReset"] = Date(now)
                }

                batch.update(walletsCollection.document(userId), updateMap)

                // 3-2. 거래 내역 생성
                val transaction = CoinTransaction(
                    id = transactionsCollection.document().id,
                    fromUserId = "system",
                    fromUserName = "시스템",
                    toUserId = userId,
                    toUserName = "",
                    amount = coins,
                    type = TransactionType.HABIT_REWARD,
                    relatedHabitId = habitId,
                    message = "습관 마일스톤 달성 보상"
                )
                val transactionDto = CoinTransactionFirestoreDto.fromDomain(transaction)
                batch.set(transactionsCollection.document(transaction.id), transactionDto)
            }.await()

            Log.d(TAG, "✅ 습관 완료 보상 성공")
            Log.d(TAG, "========================================")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 습관 완료 보상 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }

    override suspend fun rewardTaskCompletion(
        userId: String,
        taskId: String,
        amount: Int,
        fromUserId: String
    ): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "✅ 할일 완료 보상 지급 시작")
            Log.d(TAG, "  - 받는 사람: $userId")
            Log.d(TAG, "  - 주는 사람: $fromUserId")
            Log.d(TAG, "  - 금액: $amount")
            Log.d(TAG, "  - taskId: $taskId")

            // 1. 주는 사람 지갑 확인 (잔액 검증) ✨ 추가
            val fromWalletDoc = walletsCollection.document(fromUserId).get().await()
            val fromWallet = fromWalletDoc.toObject(CoinWalletFirestoreDto::class.java)
                ?: throw Exception("코인 지갑을 찾을 수 없습니다")

            val totalCoins = fromWallet.familyCoins + fromWallet.rewardCoins
            if (totalCoins < amount) {
                throw Exception("코인이 부족합니다 (보유: ${totalCoins}, 필요: ${amount})")
            }
            Log.d(TAG, "  - 주는 사람 잔액 확인 완료: ${totalCoins}코인")

            // 2. 사용자 이름 가져오기
            val userDoc = usersCollection.document(userId).get().await()
            val fromUserDoc = usersCollection.document(fromUserId).get().await()
            val userName = userDoc.getString("displayName") ?: "누군가"
            val fromUserName = fromUserDoc.getString("displayName") ?: "누군가"

            // 3. Firestore 배치 작업
            firestore.runBatch { batch ->
                // 3-1. 주는 사람 지갑 차감 ✨ 추가
                batch.update(
                    walletsCollection.document(fromUserId),
                    mapOf(
                        "familyCoins" to FieldValue.increment(-amount.toLong()),
                        "totalSpent" to FieldValue.increment(amount.toLong())
                    )
                )
                Log.d(TAG, "  - $fromUserName 지갑에서 ${amount}코인 차감")

                // 3-2. 받는 사람 지갑 증가
                batch.update(
                    walletsCollection.document(userId),
                    mapOf(
                        "familyCoins" to FieldValue.increment(amount.toLong()),
                        "totalEarned" to FieldValue.increment(amount.toLong())
                    )
                )
                Log.d(TAG, "  - $userName 지갑에 ${amount}코인 지급")

                // 3-3. 거래 내역 생성
                val transaction = CoinTransaction(
                    id = transactionsCollection.document().id,
                    fromUserId = fromUserId,
                    fromUserName = fromUserName,
                    toUserId = userId,
                    toUserName = userName,
                    amount = amount,
                    type = TransactionType.TASK_COMPLETION,
                    relatedTaskId = taskId,
                    message = "할일 완료 보상"
                )
                val transactionDto = CoinTransactionFirestoreDto.fromDomain(transaction)
                batch.set(transactionsCollection.document(transaction.id), transactionDto)
            }.await()

            Log.d(TAG, "✅ 할일 완료 보상 지급 완료")
            Log.d(TAG, "  - $fromUserName → $userName: ${amount}코인")
            Log.d(TAG, "========================================")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 할일 완료 보상 지급 실패", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
}