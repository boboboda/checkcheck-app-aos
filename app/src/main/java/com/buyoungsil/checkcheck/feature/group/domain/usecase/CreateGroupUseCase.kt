package com.buyoungsil.checkcheck.feature.group.domain.usecase

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class CreateGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(group: Group, currentUserId: String): Result<Group> {
        return try {
            val newGroup = group.copy(
                id = UUID.randomUUID().toString(),
                inviteCode = generateInviteCode(),
                ownerId = currentUserId,
                memberIds = listOf(currentUserId),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.createGroup(newGroup)
            Result.success(newGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}