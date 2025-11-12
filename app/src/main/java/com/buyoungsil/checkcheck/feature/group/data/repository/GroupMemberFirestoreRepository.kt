// app/src/main/java/com/buyoungsil/checkcheck/feature/group/data/repository/GroupMemberFirestoreRepository.kt
package com.buyoungsil.checkcheck.feature.group.data.repository

import android.util.Log
import com.buyoungsil.checkcheck.feature.group.data.firebase.GroupMemberFirestoreDto
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupMember
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupMemberRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GroupMemberFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : GroupMemberRepository {

    companion object {
        private const val TAG = "GroupMemberRepo"
    }

    /**
     * groups/{groupId}/members 서브컬렉션 참조
     */
    private fun getMembersCollection(groupId: String) =
        firestore.collection("groups").document(groupId).collection("members")

    override fun getGroupMembers(groupId: String): Flow<List<GroupMember>> = callbackFlow {
        Log.d(TAG, "=== getGroupMembers Flow 시작 (groupId=$groupId) ===")

        // 즉시 빈 리스트 emit
        trySend(emptyList())

        val listener = getMembersCollection(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getGroupMembers 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val members = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupMemberFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getGroupMembers 데이터 수신: ${members.size}명")
                trySend(members)
            }

        awaitClose {
            Log.d(TAG, "getGroupMembers Flow 종료")
            listener.remove()
        }
    }

    override suspend fun getGroupMember(groupId: String, userId: String): GroupMember? {
        return try {
            val doc = getMembersCollection(groupId).document(userId).get().await()
            doc.toObject(GroupMemberFirestoreDto::class.java)?.toDomain()
        } catch (e: Exception) {
            Log.e(TAG, "❌ getGroupMember 에러: ${e.message}", e)
            null
        }
    }

    override suspend fun addGroupMember(member: GroupMember) {
        try {
            Log.d(TAG, "=== addGroupMember ===")
            Log.d(TAG, "groupId: ${member.groupId}")
            Log.d(TAG, "userId: ${member.userId}")
            Log.d(TAG, "displayName: ${member.displayName}")

            val dto = GroupMemberFirestoreDto.fromDomain(member)
            getMembersCollection(member.groupId)
                .document(member.userId)
                .set(dto)
                .await()

            Log.d(TAG, "✅ GroupMember 추가 완료")
        } catch (e: Exception) {
            Log.e(TAG, "❌ addGroupMember 실패: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateMemberDisplayName(
        groupId: String,
        userId: String,
        displayName: String
    ) {
        try {
            getMembersCollection(groupId)
                .document(userId)
                .update("displayName", displayName)
                .await()

            Log.d(TAG, "✅ 그룹 닉네임 업데이트: $displayName")
        } catch (e: Exception) {
            Log.e(TAG, "❌ updateMemberDisplayName 실패: ${e.message}", e)
            throw e
        }
    }

    override suspend fun removeGroupMember(groupId: String, userId: String) {
        try {
            getMembersCollection(groupId)
                .document(userId)
                .delete()
                .await()

            Log.d(TAG, "✅ GroupMember 삭제 완료")
        } catch (e: Exception) {
            Log.e(TAG, "❌ removeGroupMember 실패: ${e.message}", e)
            throw e
        }
    }
}