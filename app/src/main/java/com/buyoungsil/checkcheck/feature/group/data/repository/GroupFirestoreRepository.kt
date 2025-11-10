package com.buyoungsil.checkcheck.feature.group.data.repository

import android.util.Log
import com.buyoungsil.checkcheck.feature.group.data.firebase.GroupFirestoreDto
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.repository.GroupRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase Firestore 기반 Group Repository 구현
 * ✅ 모든 Flow에 즉시 빈 리스트 emit하여 무한 로딩 방지
 */
class GroupFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : GroupRepository {

    private val groupsCollection = firestore.collection("groups")

    companion object {
        private const val TAG = "GroupFirestoreRepo"
    }

    // ==================== 모든 그룹 ====================
    override fun getAllGroups(): Flow<List<Group>> = callbackFlow {
        Log.d(TAG, "=== getAllGroups Flow 시작 ===")

        // ✨ 즉시 빈 리스트 emit (무한 로딩 방지)
        trySend(emptyList())

        val listener = groupsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getAllGroups 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getAllGroups 데이터 수신: ${groups.size}개")
                trySend(groups)
            }

        awaitClose {
            Log.d(TAG, "getAllGroups Flow 종료")
            listener.remove()
        }
    }

    // ==================== ID로 조회 ====================
    override suspend fun getGroupById(groupId: String): Group? {
        return try {
            val doc = groupsCollection.document(groupId).get().await()
            doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
        } catch (e: Exception) {
            Log.e(TAG, "❌ getGroupById 에러: ${e.message}", e)
            null
        }
    }

    // ==================== 내가 소유한 그룹 ====================
    override fun getGroupsByOwner(userId: String): Flow<List<Group>> = callbackFlow {
        Log.d(TAG, "=== getGroupsByOwner Flow 시작 (userId=$userId) ===")

        // ✨ 즉시 빈 리스트 emit (무한 로딩 방지)
        trySend(emptyList())

        val listener = groupsCollection
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getGroupsByOwner 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getGroupsByOwner 데이터 수신: ${groups.size}개")
                trySend(groups)
            }

        awaitClose {
            Log.d(TAG, "getGroupsByOwner Flow 종료")
            listener.remove()
        }
    }

    // ==================== 내가 속한 그룹 ====================
    override fun getGroupsByMember(userId: String): Flow<List<Group>> = callbackFlow {
        Log.d(TAG, "=== getGroupsByMember Flow 시작 (userId=$userId) ===")

        // ✨✨✨ 핵심! 즉시 빈 리스트 emit (무한 로딩 방지)
        trySend(emptyList())
        Log.d(TAG, "✅ 빈 리스트 즉시 emit 완료")

        val listener = groupsCollection
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getGroupsByMember 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getGroupsByMember 데이터 수신: ${groups.size}개")
                trySend(groups)
            }

        awaitClose {
            Log.d(TAG, "getGroupsByMember Flow 종료")
            listener.remove()
        }
    }

    // ==================== 초대코드로 조회 ====================
    override suspend fun getGroupByInviteCode(inviteCode: String): Group? {
        return try {
            val snapshot = groupsCollection
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()

            snapshot.documents.firstOrNull()
                ?.toObject(GroupFirestoreDto::class.java)
                ?.toDomain()
        } catch (e: Exception) {
            Log.e(TAG, "❌ getGroupByInviteCode 에러: ${e.message}", e)
            null
        }
    }

    // ==================== 그룹 생성 ====================
    override suspend fun createGroup(group: Group) {
        Log.d(TAG, "=== createGroup 시작 ===")

        val dto = GroupFirestoreDto.fromDomain(group)
        val docId = if (group.id.isEmpty()) {
            groupsCollection.document().id
        } else {
            group.id
        }

        groupsCollection.document(docId)
            .set(dto.copy(id = docId))
            .await()

        Log.d(TAG, "✅ createGroup 완료 (id=$docId)")
    }

    // ==================== 그룹 수정 ====================
    override suspend fun updateGroup(group: Group) {
        val dto = GroupFirestoreDto.fromDomain(group)
        groupsCollection.document(group.id)
            .set(dto)
            .await()
    }

    // ==================== 그룹 삭제 ====================
    override suspend fun deleteGroup(groupId: String) {
        groupsCollection.document(groupId)
            .delete()
            .await()
    }

    // ==================== 그룹 가입 ====================
    override suspend fun joinGroup(groupId: String, userId: String) {
        Log.d(TAG, "=== joinGroup (groupId=$groupId, userId=$userId) ===")

        groupsCollection.document(groupId)
            .update("memberIds", FieldValue.arrayUnion(userId))
            .await()

        Log.d(TAG, "✅ joinGroup 완료")
    }

    // ==================== 그룹 탈퇴 ====================
    override suspend fun leaveGroup(groupId: String, userId: String) {
        Log.d(TAG, "=== leaveGroup (groupId=$groupId, userId=$userId) ===")

        groupsCollection.document(groupId)
            .update("memberIds", FieldValue.arrayRemove(userId))
            .await()

        Log.d(TAG, "✅ leaveGroup 완료")
    }
}