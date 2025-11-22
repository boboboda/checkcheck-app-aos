package com.buyoungsil.checkcheck.core.data.firebase

import com.buyoungsil.checkcheck.core.domain.model.User
import com.google.firebase.Timestamp
import java.util.Date

data class UserFirestoreDto(
    val id: String = "",  // ✅ @DocumentId 제거!
    val displayName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val fcmToken: String? = null,
    val createdAt: Timestamp? = null,  // ✅ Date → Timestamp
    val updatedAt: Timestamp? = null   // ✅ Date → Timestamp
) {
    constructor() : this("", "", null, null, null, null, null)

    fun toDomain(): User {
        return User(
            id = id,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            fcmToken = fcmToken,
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
            updatedAt = updatedAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(user: User): UserFirestoreDto {
            return UserFirestoreDto(
                id = user.id,
                displayName = user.displayName,
                email = user.email,
                photoUrl = user.photoUrl,
                fcmToken = user.fcmToken,
                createdAt = Timestamp(Date(user.createdAt)),
                updatedAt = Timestamp(Date(user.updatedAt))
            )
        }
    }
}