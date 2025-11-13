package com.buyoungsil.checkcheck.feature.coin.data.firebase

import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinTransaction
import com.buyoungsil.checkcheck.feature.coin.domain.model.TransactionType
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * 코인 거래 Firestore DTO
 */
data class CoinTransactionFirestoreDto(
    @DocumentId
    val id: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val toUserName: String = "",
    val amount: Int = 0,
    val type: String = TransactionType.GIFT.name,
    val relatedTaskId: String? = null,
    val relatedHabitId: String? = null,
    val message: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
) {
    constructor() : this("", "", "", "", "", 0, TransactionType.GIFT.name, null, null, null, null)

    fun toDomain(): CoinTransaction {
        return CoinTransaction(
            id = id,
            fromUserId = fromUserId,
            fromUserName = fromUserName,
            toUserId = toUserId,
            toUserName = toUserName,
            amount = amount,
            type = try {
                TransactionType.valueOf(type)
            } catch (e: Exception) {
                TransactionType.GIFT
            },
            relatedTaskId = relatedTaskId,
            relatedHabitId = relatedHabitId,
            message = message,
            timestamp = timestamp?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(transaction: CoinTransaction): CoinTransactionFirestoreDto {
            return CoinTransactionFirestoreDto(
                id = transaction.id,
                fromUserId = transaction.fromUserId,
                fromUserName = transaction.fromUserName,
                toUserId = transaction.toUserId,
                toUserName = transaction.toUserName,
                amount = transaction.amount,
                type = transaction.type.name,
                relatedTaskId = transaction.relatedTaskId,
                relatedHabitId = transaction.relatedHabitId,
                message = transaction.message,
                timestamp = Date(transaction.timestamp)
            )
        }
    }
}