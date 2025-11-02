package com.buyoungsil.checkcheck.feature.group.data.repository

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
 */
class GroupFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : GroupRepository {

    private val groupsCollection = firestore.collection("groups")

    // ==================== 모든 그룹 ====================
    override fun getAllGroups(): Flow<List<Group>> = callbackFlow {
        val listener = groupsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(groups)
            }

        awaitClose { listener.remove() }
    }

    // ==================== ID로 조회 ====================
    override suspend fun getGroupById(groupId: String): Group? {
        return try {
            val doc = groupsCollection.document(groupId).get().await()
            doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    // ==================== 내가 소유한 그룹 ====================
    override fun getGroupsByOwner(userId: String): Flow<List<Group>> = callbackFlow {
        val listener = groupsCollection
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(groups)
            }

        awaitClose { listener.remove() }
    }

    // ==================== 내가 속한 그룹 ====================
    override fun getGroupsByMember(userId: String): Flow<List<Group>> = callbackFlow {
        val listener = groupsCollection
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(groups)
            }

        awaitClose { listener.remove() }
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
            null
        }
    }

    // ==================== 그룹 생성 ====================
    override suspend fun createGroup(group: Group) {
        val dto = GroupFirestoreDto.fromDomain(group)
        val docId = if (group.id.isEmpty()) {
            groupsCollection.document().id
        } else {
            group.id
        }

        groupsCollection.document(docId)
            .set(dto.copy(id = docId))
            .await()
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
        groupsCollection.document(groupId)
            .update("memberIds", FieldValue.arrayUnion(userId))
            .await()
    }

    // ==================== 그룹 탈퇴 ====================
    override suspend fun leaveGroup(groupId: String, userId: String) {
        groupsCollection.document(groupId)
            .update("memberIds", FieldValue.arrayRemove(userId))
            .await()
    }
}