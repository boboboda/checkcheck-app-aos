package com.buyoungsil.checkcheck.feature.group.data.repository

import com.buyoungsil.checkcheck.feature.group.data.local.GroupDao
import com.buyoungsil.checkcheck.feature.group.data.local.GroupEntity
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao
) : GroupRepository {

    override fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGroupById(groupId: String): Group? {
        return groupDao.getGroupById(groupId)?.toDomain()
    }

    override fun getGroupsByOwner(userId: String): Flow<List<Group>> {
        return groupDao.getGroupsByOwner(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getGroupsByMember(userId: String): Flow<List<Group>> {
        return groupDao.getGroupsByMember(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGroupByInviteCode(inviteCode: String): Group? {
        return groupDao.getGroupByInviteCode(inviteCode)?.toDomain()
    }

    override suspend fun createGroup(group: Group) {
        groupDao.insertGroup(GroupEntity.fromDomain(group))
    }

    override suspend fun updateGroup(group: Group) {
        groupDao.updateGroup(GroupEntity.fromDomain(group))
    }

    override suspend fun deleteGroup(groupId: String) {
        groupDao.deleteGroupById(groupId)
    }

    override suspend fun joinGroup(groupId: String, userId: String) {
        val group = getGroupById(groupId) ?: return
        val updatedGroup = group.copy(
            memberIds = group.memberIds + userId
        )
        updateGroup(updatedGroup)
    }

    override suspend fun leaveGroup(groupId: String, userId: String) {
        val group = getGroupById(groupId) ?: return
        val updatedGroup = group.copy(
            memberIds = group.memberIds - userId
        )
        updateGroup(updatedGroup)
    }
}