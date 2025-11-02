package com.buyoungsil.checkcheck.feature.group.domain.repository

import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAllGroups(): Flow<List<Group>>
    suspend fun getGroupById(groupId: String): Group?
    fun getGroupsByOwner(userId: String): Flow<List<Group>>
    fun getGroupsByMember(userId: String): Flow<List<Group>>
    suspend fun getGroupByInviteCode(inviteCode: String): Group?
    suspend fun createGroup(group: Group)
    suspend fun updateGroup(group: Group)
    suspend fun deleteGroup(groupId: String)
    suspend fun joinGroup(groupId: String, userId: String)
    suspend fun leaveGroup(groupId: String, userId: String)
}