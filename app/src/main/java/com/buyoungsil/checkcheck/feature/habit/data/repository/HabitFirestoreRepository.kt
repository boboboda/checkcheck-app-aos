package com.buyoungsil.checkcheck.feature.habit.data.repository

import android.util.Log
import com.buyoungsil.checkcheck.feature.habit.data.firebase.HabitCheckFirestoreDto
import com.buyoungsil.checkcheck.feature.habit.data.firebase.HabitFirestoreDto
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitCheck
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

/**
 * Firebase Firestore 기반 Habit Repository 구현
 */
class HabitFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HabitRepository {

    private val habitsCollection = firestore.collection("habits")
    private val checksCollection = firestore.collection("habit_checks")

    // ==================== Habit CRUD ====================

    override fun getAllHabits(userId: String): Flow<List<Habit>> = callbackFlow {

        val listener = habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()


                trySend(habits)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getHabitById(habitId: String): Habit? {
        return try {
            val doc = habitsCollection.document(habitId).get().await()
            doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun getPersonalHabits(userId: String): Flow<List<Habit>> = callbackFlow {
        val listener = habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isGroupShared", false)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(habits)
            }

        awaitClose { listener.remove() }
    }

    override fun getGroupHabits(groupId: String): Flow<List<Habit>> = callbackFlow {
        val listener = habitsCollection
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("isGroupShared", true)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(habits)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun insertHabit(habit: Habit) {
        val dto = HabitFirestoreDto.fromDomain(habit)
        val docId = if (habit.id.isEmpty()) {
            habitsCollection.document().id
        } else {
            habit.id
        }

        habitsCollection.document(docId)
            .set(dto.copy(id = docId))
            .await()
    }

    override suspend fun updateHabit(habit: Habit) {
        val dto = HabitFirestoreDto.fromDomain(habit)
        habitsCollection.document(habit.id)
            .set(dto)
            .await()
    }

    override suspend fun deleteHabit(habitId: String) {
        // Soft delete
        habitsCollection.document(habitId)
            .update("isActive", false)
            .await()
    }

    // ==================== Habit Check CRUD ====================

    override fun getChecksByHabit(habitId: String): Flow<List<HabitCheck>> = callbackFlow {
        val listener = checksCollection
            .whereEqualTo("habitId", habitId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val checks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(checks)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getCheckByDate(habitId: String, date: LocalDate): HabitCheck? {
        return try {
            val snapshot = checksCollection
                .whereEqualTo("habitId", habitId)
                .whereEqualTo("date", date.toString())
                .get()
                .await()

            snapshot.documents.firstOrNull()
                ?.toObject(HabitCheckFirestoreDto::class.java)
                ?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun getChecksByUserAndDate(userId: String, date: LocalDate): Flow<List<HabitCheck>> = callbackFlow {
        val listener = checksCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val checks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(checks)
            }

        awaitClose { listener.remove() }
    }

    override fun getChecksByDateRange(
        habitId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitCheck>> = callbackFlow {
        val listener = checksCollection
            .whereEqualTo("habitId", habitId)
            .whereGreaterThanOrEqualTo("date", startDate.toString())
            .whereLessThanOrEqualTo("date", endDate.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val checks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(checks)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun insertCheck(check: HabitCheck) {
        val dto = HabitCheckFirestoreDto.fromDomain(check)
        val docId = if (check.id.isEmpty()) {
            checksCollection.document().id
        } else {
            check.id
        }

        checksCollection.document(docId)
            .set(dto.copy(id = docId))
            .await()
    }

    override suspend fun deleteCheck(check: HabitCheck) {
        checksCollection.document(check.id)
            .delete()
            .await()
    }

    override suspend fun toggleHabitCheck(habitId: String, userId: String, date: LocalDate) {
        val existingCheck = getCheckByDate(habitId, date)

        if (existingCheck != null) {
            deleteCheck(existingCheck)
        } else {
            val newCheck = HabitCheck(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                userId = userId,
                date = date,
                isCompleted = true
            )
            insertCheck(newCheck)
        }
    }

    // ==================== Statistics ====================

    override suspend fun getHabitStatistics(habitId: String): HabitStatistics {
        val snapshot = checksCollection
            .whereEqualTo("habitId", habitId)
            .whereEqualTo("isCompleted", true)
            .get()
            .await()

        val checks = snapshot.documents.mapNotNull {
            it.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
        }

        val totalChecks = checks.size
        val currentStreak = getCurrentStreak(habitId)

        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val monthStart = today.withDayOfMonth(1)

        val thisWeekChecks = checks.count {
            it.date >= weekStart && it.date <= today
        }

        val thisMonthChecks = checks.count {
            it.date >= monthStart && it.date <= today
        }

        val habit = getHabitById(habitId)
        val daysSinceCreation = if (habit != null) {
            val createdDate = LocalDate.ofEpochDay(habit.createdAt / (24 * 60 * 60 * 1000))
            ChronoUnit.DAYS.between(createdDate, today).toInt() + 1
        } else {
            1
        }

        val completionRate = if (daysSinceCreation > 0) {
            totalChecks.toFloat() / daysSinceCreation
        } else {
            0f
        }

        return HabitStatistics(
            habitId = habitId,
            totalChecks = totalChecks,
            currentStreak = currentStreak,
            longestStreak = currentStreak, // TODO: 별도 계산 필요
            completionRate = completionRate.coerceIn(0f, 1f),
            thisWeekChecks = thisWeekChecks,
            thisMonthChecks = thisMonthChecks
        )
    }

    override suspend fun getCurrentStreak(habitId: String): Int {
        val today = LocalDate.now()
        var streak = 0
        var checkDate = today

        while (true) {
            val check = getCheckByDate(habitId, checkDate)
            if (check != null && check.isCompleted) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }
}