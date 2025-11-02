package com.buyoungsil.checkcheck.feature.group.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM user_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM user_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Query("SELECT * FROM user_groups WHERE ownerId = :userId")
    fun getGroupsByOwner(userId: String): Flow<List<GroupEntity>>

    @Query("SELECT * FROM user_groups WHERE memberIds LIKE '%' || :userId || '%'")
    fun getGroupsByMember(userId: String): Flow<List<GroupEntity>>

    @Query("SELECT * FROM user_groups WHERE inviteCode = :inviteCode")
    suspend fun getGroupByInviteCode(inviteCode: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM user_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: String)
}