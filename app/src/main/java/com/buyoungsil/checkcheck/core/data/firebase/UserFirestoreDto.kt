package com.buyoungsil.checkcheck.core.data.firebase

import com.buyoungsil.checkcheck.core.domain.model.User
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore용 User DTO
 * ✅ FCM 토큰 저장
 */
data class UserFirestoreDto(
    @DocumentId
    val id: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val fcmToken: String? = null,  // ✅ FCM 토큰
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", null, null, null, null, null, null)

    fun toDomain(): User {
        return User(
            id = id,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            fcmToken = fcmToken,
            createdAt = createdAt?.time ?: System.currentTimeMillis(),
            updatedAt = updatedAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(user: User): UserFirestoreDto {
            return UserFirestoreDto(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                fcmToken = user.fcmToken,
                createdAt = Date(user.createdAt),
                updatedAt = Date(user.updatedAt)
            )
        }
    }
}