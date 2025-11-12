package com.buyoungsil.checkcheck.core.domain.model

/**
 * 사용자 도메인 모델
 * ✅ FCM 토큰 필드 추가
 */
data class User(
    val id: String,
    val displayName: String,  // 전역 닉네임
    val email: String? = null,
    val photoUrl: String? = null,
    val fcmToken: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)