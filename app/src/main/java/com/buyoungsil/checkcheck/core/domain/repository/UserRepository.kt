package com.buyoungsil.checkcheck.core.domain.repository

import com.buyoungsil.checkcheck.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun getUser(userId: String): User?

    fun getUserFlow(userId: String): Flow<User?>

    suspend fun saveUser(user: User)

    suspend fun updateFcmToken(userId: String, fcmToken: String)

    suspend fun getMembersFcmTokens(memberIds: List<String>): List<String>

    // ✅ 추가
    suspend fun updateDisplayName(userId: String, displayName: String)
}