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
 * ✅ 달성률: 생성일부터 오늘까지 체크해야 할 날 대비 실제 체크한 날의 비율
 * ✅ 스트릭: 연속으로 체크한 일수
 * ✅ 실시간 동기화 Flow 사용
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
        Log.d(TAG, "getAllHabits Flow 시작 - userId: $userId")

        val listener = habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getAllHabits 에러", error)
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getAllHabits 데이터 수신: ${habits.size}개")
                trySend(habits)
            }

        awaitClose {
            Log.d(TAG, "getAllHabits Flow 종료")
            listener.remove()
        }
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
        Log.d(TAG, "=== getPersonalHabits Flow 시작 ===")
        Log.d(TAG, "userId: $userId")

        val listener = habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("groupShared", false)
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getPersonalHabits 에러", error)
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getPersonalHabits 데이터 수신: ${habits.size}개")
                trySend(habits)
            }

        awaitClose {
            Log.d(TAG, "getPersonalHabits Flow 종료")
            listener.remove()
        }
    }

    override fun getGroupHabits(groupId: String): Flow<List<Habit>> = callbackFlow {
        Log.d(TAG, "getGroupHabits Flow 시작 - groupId: $groupId")

        val listener = habitsCollection
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("groupShared", true)
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "getGroupHabits 에러", error)
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getGroupHabits 데이터 수신: ${habits.size}개")
                trySend(habits)
            }

        awaitClose {
            Log.d(TAG, "getGroupHabits Flow 종료")
            listener.remove()
        }
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
        Log.d(TAG, "=== getChecksByHabit Flow 시작 (habitId=$habitId) ===")

        val listener = checksCollection
            .whereEqualTo("habitId", habitId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getChecksByHabit 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val checks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getChecksByHabit 데이터 수신: ${checks.size}개")
                trySend(checks)
            }

        awaitClose {
            Log.d(TAG, "getChecksByHabit Flow 종료")
            listener.remove()
        }
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
        Log.d(TAG, "=== getChecksByUserAndDate Flow 시작 (userId=$userId, date=$date) ===")

        val listener = checksCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getChecksByUserAndDate 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val checks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getChecksByUserAndDate 데이터 수신: ${checks.size}개")
                trySend(checks)
            }

        awaitClose {
            Log.d(TAG, "getChecksByUserAndDate Flow 종료")
            listener.remove()
        }
    }

    override fun getChecksByDateRange(
        habitId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitCheck>> = callbackFlow {
        Log.d(TAG, "=== getChecksByDateRange Flow 시작 ===")

        val listener = checksCollection
            .whereEqualTo("habitId", habitId)
            .whereGreaterThanOrEqualTo("date", startDate.toString())
            .whereLessThanOrEqualTo("date", endDate.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getChecksByDateRange 에러: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val checks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getChecksByDateRange 데이터 수신: ${checks.size}개")
                trySend(checks)
            }

        awaitClose {
            Log.d(TAG, "getChecksByDateRange Flow 종료")
            listener.remove()
        }
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
        Log.d(TAG, "=== toggleHabitCheck 시작 (habitId=$habitId, date=$date) ===")

        val existingCheck = getCheckByDate(habitId, date)

        if (existingCheck != null) {
            Log.d(TAG, "기존 체크 삭제")
            deleteCheck(existingCheck)
        } else {
            Log.d(TAG, "새 체크 추가")
            val newCheck = HabitCheck(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                userId = userId,
                date = date,
                completed = true
            )
            insertCheck(newCheck)
        }

        Log.d(TAG, "✅ toggleHabitCheck 완료")
    }

    // ==================== Statistics ====================

    override suspend fun getHabitStatistics(habitId: String): HabitStatistics {
        val habit = getHabitById(habitId)
            ?: return HabitStatistics(habitId = habitId)

        // ✅ Flow 대신 직접 조회 (타이밍 이슈 해결)
        val allChecks = try {
            checksCollection
                .whereEqualTo("habitId", habitId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
                }
        } catch (e: Exception) {
            Log.e(TAG, "체크 조회 실패", e)
            emptyList()
        }

        val completedChecks = allChecks.filter { it.completed }

        val totalChecks = completedChecks.size

        // ✅ 현재 스트릭 계산 (같은 데이터 사용)
        val currentStreak = calculateCurrentStreak(completedChecks)

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
        // ✅ 직접 조회로 변경
        val allChecks = try {
            checksCollection
                .whereEqualTo("habitId", habitId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(HabitCheckFirestoreDto::class.java)?.toDomain()
                }
        } catch (e: Exception) {
            Log.e(TAG, "체크 조회 실패", e)
            emptyList()
        }

        val completedChecks = allChecks.filter { it.completed }
        return calculateCurrentStreak(completedChecks)
    }

    /**
     * ✅ 현재 스트릭 계산 (체크 목록 기반)
     * 오늘부터 역순으로 연속된 체크 일수를 계산
     */
    private fun calculateCurrentStreak(completedChecks: List<HabitCheck>): Int {
        if (completedChecks.isEmpty()) return 0

        val today = LocalDate.now()
        val sortedDates = completedChecks.map { it.date }.sortedDescending()

        // 오늘 체크가 없으면 0
        if (!sortedDates.contains(today)) return 0

        var streak = 0
        var currentDate = today

        for (date in sortedDates) {
            if (date == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (date < currentDate) {
                // 연속이 끊김
                break
            }
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

    // ==================== 🆕 그룹 공유 습관 조회 ====================

    /**
     * 그룹에 공유된 모든 습관 조회
     */
    override fun getSharedHabitsInGroup(groupId: String): Flow<List<Habit>> = callbackFlow {
        Log.d(TAG, "=== getSharedHabitsInGroup Flow 시작 ===")
        Log.d(TAG, "groupId: $groupId")

        val listener = habitsCollection
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("groupShared", true)
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getSharedHabitsInGroup 에러", error)
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getSharedHabitsInGroup 데이터 수신: ${habits.size}개")
                trySend(habits)
            }

        awaitClose {
            Log.d(TAG, "getSharedHabitsInGroup Flow 종료")
            listener.remove()
        }
    }

    /**
     * 특정 사용자가 특정 그룹에 공유한 습관 조회
     */
    override fun getSharedHabitsByUser(userId: String, groupId: String): Flow<List<Habit>> = callbackFlow {
        Log.d(TAG, "=== getSharedHabitsByUser Flow 시작 ===")
        Log.d(TAG, "userId: $userId, groupId: $groupId")

        val listener = habitsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("groupShared", true)
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ getSharedHabitsByUser 에러", error)
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                Log.d(TAG, "✅ getSharedHabitsByUser 데이터 수신: ${habits.size}개")
                trySend(habits)
            }

        awaitClose {
            Log.d(TAG, "getSharedHabitsByUser Flow 종료")
            listener.remove()
        }
    }
}