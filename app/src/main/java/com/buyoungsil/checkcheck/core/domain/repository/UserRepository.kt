package com.buyoungsil.checkcheck.core.domain.repository

import com.buyoungsil.checkcheck.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * User Repository 인터페이스
 */
interface UserRepository {

    /**
     * 사용자 정보 가져오기
     */
    suspend fun getUser(userId: String): User?

    /**
     * 사용자 정보 실시간 구독
     */
    fun getUserFlow(userId: String): Flow<User?>

    /**
     * 사용자 정보 저장/업데이트
     */
    suspend fun saveUser(user: User)

    /**
     * FCM 토큰 업데이트
     */
    suspend fun updateFcmToken(userId: String, fcmToken: String)

    /**
     * 그룹 멤버들의 FCM 토큰 가져오기
     */
    suspend fun getMembersFcmTokens(memberIds: List<String>): List<String>
}