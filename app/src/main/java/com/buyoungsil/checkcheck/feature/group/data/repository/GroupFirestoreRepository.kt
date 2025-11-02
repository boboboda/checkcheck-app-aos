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

class GroupFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : GroupRepository {

    private val groupsCollection = firestore.collection("groups")

    override suspend fun createGroup(group: Group) {
        val dto = GroupFirestoreDto.fromDomain(group)
        if (group.id.isEmpty()) {
            val docRef = groupsCollection.document()
            groupsCollection.document(docRef.id)
                .set(dto.copy(id = docRef.id))
                .await()
        } else {
            groupsCollection.document(group.id)
                .set(dto)
                .await()
        }
    }

    override fun getMyGroups(userId: String): Flow<List<Group>> = callbackFlow {
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

    override suspend fun getGroupById(groupId: String): Group? {
        return try {
            val doc = groupsCollection.document(groupId).get().await()
            doc.toObject(GroupFirestoreDto::class.java)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

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

    override suspend fun joinGroup(groupId: String, userId: String) {
        groupsCollection.document(groupId)
            .update("memberIds", FieldValue.arrayUnion(userId))
            .await()
    }

    override suspend fun leaveGroup(groupId: String, userId: String) {
        groupsCollection.document(groupId)
            .update("memberIds", FieldValue.arrayRemove(userId))
            .await()
    }

    override suspend fun deleteGroup(groupId: String) {
        groupsCollection.document(groupId)
            .delete()
            .await()
    }
}