package com.buyoungsil.checkcheck.feature.group.domain.repository

import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember
import kotlinx.coroutines.flow.Flow

interface GroupMemberRepository {

    /**
     * 그룹의 모든 멤버 조회
     */
    fun getGroupMembers(groupId: String): Flow<List<GroupMember>>

    /**
     * 특정 멤버 조회
     */
    suspend fun getGroupMember(groupId: String, userId: String): GroupMember?

    /**
     * 멤버 추가 (그룹 가입 시)
     */
    suspend fun addGroupMember(member: GroupMember)

    /**
     * 멤버 닉네임 업데이트
     */
    suspend fun updateMemberDisplayName(groupId: String, userId: String, displayName: String)

    /**
     * 멤버 삭제 (그룹 탈퇴 시)
     */
    suspend fun removeGroupMember(groupId: String, userId: String)
}