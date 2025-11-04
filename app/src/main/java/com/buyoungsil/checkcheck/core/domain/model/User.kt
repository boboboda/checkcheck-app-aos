package com.buyoungsil.checkcheck.core.domain.model

/**
 * 사용자 도메인 모델
 * ✅ FCM 토큰 필드 추가
 */
data class User(
    val id: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val fcmToken: String? = null,  // ✅ FCM 토큰 추가
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)