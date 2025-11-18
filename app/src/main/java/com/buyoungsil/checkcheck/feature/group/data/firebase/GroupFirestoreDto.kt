package com.buyoungsil.checkcheck.feature.group.data.firebase

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupTier
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GroupFirestoreDto(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val icon: String = "👥",
    val type: String = GroupType.FAMILY.name,
    val inviteCode: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val tier: String = GroupTier.BASIC.name,  // ✨ 추가
    val maxMembers: Int = 10,  // ✨ 추가
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
val updatedAt: Date? = null  // ✨ 추가
) {
    constructor() : this(
        "", "", "👥", GroupType.FAMILY.name, "", "",
        emptyList(), GroupTier.BASIC.name, 10, null, null
    )
    fun toDomain(): Group {
        return Group(
            id = id,
            name = name,
            icon = icon,
            type = try {
                GroupType.valueOf(type)
            } catch (e: Exception) {
                GroupType.FAMILY
            },
            inviteCode = inviteCode,
            ownerId = ownerId,
            memberIds = memberIds,
            createdAt = createdAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(group: Group): GroupFirestoreDto {
            return GroupFirestoreDto(
                id = group.id,
                name = group.name,
                icon = group.icon,
                type = group.type.name,
                inviteCode = group.inviteCode,
                ownerId = group.ownerId,
                memberIds = group.memberIds,
                createdAt = Date(group.createdAt)
            )
        }
    }
}