// app/src/main/java/com/buyoungsil/checkcheck/core/data/firebase/UserFirestoreDto.kt
package com.buyoungsil.checkcheck.core.data.firebase

import com.buyoungsil.checkcheck.core.domain.model.User
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserFirestoreDto(
    @DocumentId
    val id: String = "",
    val displayName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val fcmToken: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    constructor() : this("", "", null, null, null, null, null)

    fun toDomain(): User {
        return User(
            id = id,
            displayName = displayName,
            email = email,
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
                displayName = user.displayName,
                email = user.email,
                photoUrl = user.photoUrl,
                fcmToken = user.fcmToken,
                createdAt = Date(user.createdAt),
                updatedAt = Date(user.updatedAt)
            )
        }
    }
}