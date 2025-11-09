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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

/**
 * Firebase Firestore 기반 Habit Repository 구현
 * ✅ 달성률: 생성일부터 오늘까지 체크해야 할 날 대비 실제 체크한 날의 비율
 * ✅ 스트릭: 연속으로 체크한 일수
 */
class HabitFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HabitRepository {

    private val habitsCollection = firestore.collection("habits")
    private val checksCollection = firestore.collection("habit_checks")

    companion object {
        private const val TAG = "HabitFirestoreRepo"
    }

    // ==================== Habit CRUD ====================

    override fun getAllHabits(userId: String): Flow<List<Habit>> = callbackFlow {
        val listener = habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("active", true)
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
            .whereEqualTo("groupShared", false)
            .whereEqualTo("active", true)
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
            .whereEqualTo("groupShared", true)
            .whereEqualTo("active", true)
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
        try {
            Log.d(TAG, "=== insertHabit 시작 ===")
            Log.d(TAG, "habit.id: ${habit.id}")

            val dto = HabitFirestoreDto.fromDomain(habit)
            Log.d(TAG, "DTO 변환 완료")

            val docId = if (habit.id.isEmpty()) {
                habitsCollection.document().id
            } else {
                habit.id
            }
            Log.d(TAG, "docId: $docId")

            Log.d(TAG, "Firestore set() 호출 전...")
            habitsCollection.document(docId)
                .set(dto.copy(id = docId))
                .await()
            Log.d(TAG, "✅ Firestore set() 완료!")

        } catch (e: Exception) {
            Log.e(TAG, "❌ insertHabit 에러: ${e.message}", e)
            throw e
        }
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
            .update("active", false)
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
                completed = true
            )
            insertCheck(newCheck)
        }
    }

    // ==================== Statistics ====================

    override suspend fun getHabitStatistics(habitId: String): HabitStatistics {
        val habit = getHabitById(habitId)
            ?: return HabitStatistics(habitId = habitId)

        val allChecks = getChecksByHabit(habitId).first()
        val completedChecks = allChecks.filter { it.completed }

        val totalChecks = completedChecks.size
        val currentStreak = getCurrentStreak(habitId)

        // ✅ longestStreak 계산
        val longestStreak = calculateLongestStreak(completedChecks)

        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val monthStart = today.withDayOfMonth(1)

        val thisWeekChecks = completedChecks.count { check ->
            check.date >= weekStart && check.date <= today
        }

        val thisMonthChecks = completedChecks.count { check ->
            check.date >= monthStart && check.date <= today
        }

        // ✅ 달성률 계산: 생성일부터 오늘까지의 일수 대비 체크한 일수
        val createdDate = LocalDate.ofEpochDay(habit.createdAt / (1000 * 60 * 60 * 24))
        val daysSinceCreation = ChronoUnit.DAYS.between(createdDate, today).toInt() + 1

        // 달성률 = 총 체크 수 / 생성 후 경과 일수
        val completionRate = if (daysSinceCreation > 0) {
            (totalChecks.toFloat() / daysSinceCreation.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        Log.d(TAG, "=== 습관 통계 계산 ===")
        Log.d(TAG, "습관: ${habit.title}")
        Log.d(TAG, "생성일: $createdDate")
        Log.d(TAG, "오늘: $today")
        Log.d(TAG, "경과 일수: $daysSinceCreation 일")
        Log.d(TAG, "총 체크: $totalChecks 회")
        Log.d(TAG, "달성률 (0~1): $completionRate")
        Log.d(TAG, "달성률 (%): ${(completionRate * 100).toInt()}%")
        Log.d(TAG, "현재 스트릭: $currentStreak 일 (연속)")
        Log.d(TAG, "최장 스트릭: $longestStreak 일")

        return HabitStatistics(
            habitId = habitId,
            totalChecks = totalChecks,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            completionRate = completionRate,  // ✅ 0~1 범위
            thisWeekChecks = thisWeekChecks,
            thisMonthChecks = thisMonthChecks
        )
    }

    override suspend fun getCurrentStreak(habitId: String): Int {
        val today = LocalDate.now()
        var streak = 0
        var currentDate = today

        while (true) {
            val check = getCheckByDate(habitId, currentDate)
            if (check == null || !check.completed) {
                break
            }
            streak++
            currentDate = currentDate.minusDays(1)
        }

        return streak
    }

    /**
     * ✅ 최장 스트릭 계산
     * 전체 체크 기록에서 가장 긴 연속 체크 일수를 찾음
     */
    private fun calculateLongestStreak(completedChecks: List<HabitCheck>): Int {
        if (completedChecks.isEmpty()) return 0

        // 날짜순 정렬
        val sortedDates = completedChecks.map { it.date }.sorted()

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val prevDate = sortedDates[i - 1]
            val currDate = sortedDates[i]

            // 연속된 날짜인지 확인
            if (ChronoUnit.DAYS.between(prevDate, currDate) == 1L) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }
}