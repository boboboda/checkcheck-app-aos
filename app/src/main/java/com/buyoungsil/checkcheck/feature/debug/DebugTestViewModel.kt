package com.buyoungsil.checkcheck.feature.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.CheckHabitMilestoneUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetPersonalHabitsUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DebugTestUiState(
    val habits: List<Habit> = emptyList(),
    val habitStats: Map<String, HabitStatistics> = emptyMap(),
    val loading: Boolean = true,
    val testMessage: String? = null
)

@HiltViewModel
class DebugTestViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val checkHabitMilestoneUseCase: CheckHabitMilestoneUseCase,
    private val authManager: FirebaseAuthManager,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    companion object {
        private const val TAG = "DebugTestViewModel"
    }

    private val _uiState = MutableStateFlow(DebugTestUiState())
    val uiState: StateFlow<DebugTestUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        Log.d(TAG, "🧪 디버그 테스트 ViewModel 초기화")
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            try {
                getPersonalHabitsUseCase(currentUserId)
                    .drop(1) // 첫 번째 빈 emit 무시
                    .collect { habits ->
                        Log.d(TAG, "✅ 습관 ${habits.size}개 로드됨")

                        // 각 습관의 통계 조회
                        val stats = habits.associate { habit ->
                            val habitStats = getHabitStatisticsUseCase(habit.id).getOrNull()
                            habit.id to (habitStats ?: HabitStatistics(
                                habitId = habit.id,
                                totalChecks = 0,  // ✅ 올바른 필드명
                                currentStreak = 0,
                                longestStreak = 0,
                                completionRate = 0f,
                                thisWeekChecks = 0,
                                thisMonthChecks = 0
                            ))
                        }

                        _uiState.update {
                            it.copy(
                                habits = habits,
                                habitStats = stats,
                                loading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 습관 로드 실패", e)
                _uiState.update {
                    it.copy(
                        loading = false,
                        testMessage = "습관 로드 실패: ${e.message}"
                    )
                }
            }
        }
    }

    fun testMilestone(habitId: String, testStreakDays: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🧪 마일스톤 테스트: habitId=$habitId, streak=$testStreakDays")

                val habit = _uiState.value.habits.find { it.id == habitId }
                if (habit == null) {
                    _uiState.update {
                        it.copy(testMessage = "❌ 습관을 찾을 수 없습니다")
                    }
                    return@launch
                }

                val result = checkHabitMilestoneUseCase(
                    habitId = habitId,
                    userId = currentUserId,
                    currentStreak = testStreakDays
                )

                result.onSuccess { coinsAwarded ->
                    val message = if (coinsAwarded != null) {
                        "🎉 ${habit.title}: ${testStreakDays}일 달성! ${coinsAwarded}코인 획득!"
                    } else {
                        "ℹ️ ${testStreakDays}일은 마일스톤이 아니거나 이미 지급되었습니다"
                    }

                    _uiState.update { it.copy(testMessage = message) }
                    Log.d(TAG, message)

                    // 습관 정보 새로고침
                    loadHabits()
                }.onFailure { error ->
                    val message = "❌ 테스트 실패: ${error.message}"
                    _uiState.update { it.copy(testMessage = message) }
                    Log.e(TAG, message, error)
                }
            } catch (e: Exception) {
                val message = "❌ 테스트 중 오류: ${e.message}"
                _uiState.update { it.copy(testMessage = message) }
                Log.e(TAG, message, e)
            }
        }
    }

    fun resetHabitRewards(habitId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🧪 보상 기록 초기화: $habitId")

                val habit = _uiState.value.habits.find { it.id == habitId }

                // 1. 습관의 lastRewardStreak 초기화
                firestore.collection("habits")
                    .document(habitId)
                    .update(
                        mapOf(
                            "lastRewardStreak" to 0,
                            "lastRewardDate" to null
                        )
                    )
                    .await()

                // 2. 보상 기록 삭제
                val records = firestore.collection("habitRewardRecords")
                    .whereEqualTo("habitId", habitId)
                    .get()
                    .await()

                records.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                val message = "✅ ${habit?.title ?: "습관"} 보상 기록 초기화 완료 (${records.size()}개 삭제)"
                _uiState.update { it.copy(testMessage = message) }
                Log.d(TAG, message)

                // 습관 정보 새로고침
                loadHabits()
            } catch (e: Exception) {
                val message = "❌ 초기화 실패: ${e.message}"
                _uiState.update { it.copy(testMessage = message) }
                Log.e(TAG, message, e)
            }
        }
    }

    fun clearTestMessage() {
        _uiState.update { it.copy(testMessage = null) }
    }
}