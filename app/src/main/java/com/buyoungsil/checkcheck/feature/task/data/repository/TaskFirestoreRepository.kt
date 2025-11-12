package com.buyoungsil.checkcheck.feature.task.data.repository

import com.buyoungsil.checkcheck.feature.task.data.firebase.TaskFirestoreDto
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.domain.repository.TaskRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase Firestore 기반 Task Repository 구현
 */
class TaskFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : TaskRepository {

    private val tasksCollection = firestore.collection("tasks")

    override fun getTasksByGroup(groupId: String): Flow<List<Task>> = callbackFlow {
        val listener = tasksCollection
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TaskFirestoreDto::class.java)?.toDomain()
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return try {
            val doc = tasksCollection.document(taskId).get().await()
            doc.toObject(TaskFirestoreDto::class.java)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun getTasksByStatus(groupId: String, status: TaskStatus): Flow<List<Task>> = callbackFlow {
        val listener = tasksCollection
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TaskFirestoreDto::class.java)?.toDomain()
                }?.sortedWith(
                    compareByDescending<Task> { it.priority.ordinal }
                        .thenByDescending { it.createdAt }
                ) ?: emptyList()

                trySend(tasks)
            }

        awaitClose { listener.remove() }
    }

    override fun getMyTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val listener = tasksCollection
            .whereEqualTo("assigneeId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TaskFirestoreDto::class.java)?.toDomain()
                }?.filter { it.status != TaskStatus.COMPLETED }
                    ?.sortedByDescending { it.priority.ordinal } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun createTask(task: Task) {
        val dto = TaskFirestoreDto.fromDomain(task)
        val docId = if (task.id.isEmpty()) {
            tasksCollection.document().id
        } else {
            task.id
        }

        tasksCollection.document(docId)
            .set(dto.copy(id = docId))
            .await()
    }

    override suspend fun updateTask(task: Task) {
        val dto = TaskFirestoreDto.fromDomain(task)
        tasksCollection.document(task.id)
            .set(dto)
            .await()
    }

    override suspend fun deleteTask(taskId: String) {
        tasksCollection.document(taskId)
            .delete()
            .await()
    }

    override suspend fun completeTask(taskId: String, userId: String) {
        tasksCollection.document(taskId)
            .update(
                mapOf(
                    "status" to TaskStatus.COMPLETED.name,
                    "completedBy" to userId,
                    "completedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    // TaskFirestoreRepository.kt에 이 함수 추가

    override fun getPersonalTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val listener = tasksCollection
            .whereEqualTo("createdBy", userId)
            .whereEqualTo("groupId", "")  // ✅ 빈 문자열 = 개인 할일
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TaskFirestoreDto::class.java)?.toDomain()
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { listener.remove() }
    }
}