package com.buyoungsil.checkcheck.core.data.repository

import android.util.Log
import com.buyoungsil.checkcheck.core.data.firebase.UserFirestoreDto
import com.buyoungsil.checkcheck.core.domain.model.User
import com.buyoungsil.checkcheck.core.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserRepository Firestore 구현체
 * ✅ FCM 토큰 저장 기능
 */
@Singleton
class UserFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    // ==================== 사용자 정보 가져오기 ====================


    // ==================== 사용자 정보 가져오기 ====================
    override suspend fun getUser(userId: String): User? {
        return try {
            Log.d("UserRepo", "=== getUser 시작: $userId ===")
            val snapshot = usersCollection.document(userId).get().await()

            Log.d("UserRepo", "snapshot exists: ${snapshot.exists()}")
            Log.d("UserRepo", "snapshot data: ${snapshot.data}")

            val dto = snapshot.toObject(UserFirestoreDto::class.java)
            Log.d("UserRepo", "DTO 변환 결과: $dto")

            val user = dto?.toDomain()
            Log.d("UserRepo", "Domain 변환 결과: $user")

            user
        } catch (e: Exception) {
            Log.e("UserRepo", "❌ getUser 실패", e)
            null
        }
    }

    // ==================== 사용자 정보 실시간 구독 ====================
    override fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(UserFirestoreDto::class.java)?.toDomain()
                trySend(user)
            }

        awaitClose { listener.remove() }
    }

    // ==================== 사용자 정보 저장 ====================
    override suspend fun saveUser(user: User) {
        val dto = UserFirestoreDto.fromDomain(user)
        usersCollection.document(user.id)
            .set(dto)
            .await()
    }

    // ==================== FCM 토큰 업데이트 ====================
    override suspend fun updateFcmToken(userId: String, fcmToken: String) {
        usersCollection.document(userId)
            .set(
                mapOf(
                    "id" to userId,
                    "fcmToken" to fcmToken,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()
    }

    // ==================== 그룹 멤버들의 FCM 토큰 가져오기 ====================
    override suspend fun getMembersFcmTokens(memberIds: List<String>): List<String> {
        if (memberIds.isEmpty()) return emptyList()

        return try {
            // Firestore 'in' 쿼리는 최대 10개까지만 가능
            // 10개씩 나눠서 조회
            val tokens = mutableListOf<String>()

            memberIds.chunked(10).forEach { chunk ->
                val snapshot = usersCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()

                val chunkTokens = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserFirestoreDto::class.java)?.fcmToken
                }
                tokens.addAll(chunkTokens)
            }

            tokens.filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==================== displayName 업데이트 ====================
    override suspend fun updateDisplayName(userId: String, displayName: String) {
        usersCollection.document(userId)
            .update(
                mapOf(
                    "displayName" to displayName,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            )
            .await()
    }
}