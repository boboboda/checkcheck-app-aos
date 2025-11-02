package com.buyoungsil.checkcheck.feature.group.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupType

@Entity(tableName = "user_groups")  // ← groups → user_groups로 변경
data class GroupEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val inviteCode: String,
    val ownerId: String,
    val memberIds: String,
    val maxMembers: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Group {
        return Group(
            id = id,
            name = name,
            icon = icon,
            type = GroupType.valueOf(type),
            inviteCode = inviteCode,
            ownerId = ownerId,
            memberIds = memberIds.split(",").filter { it.isNotBlank() },
            maxMembers = maxMembers,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(group: Group): GroupEntity {
            return GroupEntity(
                id = group.id,
                name = group.name,
                icon = group.icon,
                type = group.type.name,
                inviteCode = group.inviteCode,
                ownerId = group.ownerId,
                memberIds = group.memberIds.joinToString(","),
                maxMembers = group.maxMembers,
                createdAt = group.createdAt,
                updatedAt = group.updatedAt
            )
        }
    }
}