package com.buyoungsil.checkcheck.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetPersonalHabitsUseCase
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.statistics.presentation.StatisticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val authManager: FirebaseAuthManager  // ✨ 추가
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    // ✅ Firebase UID 사용
    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getPersonalHabitsUseCase(currentUserId).collect { habits ->
                    val habitsWithStats = habits.map { habit ->
                        val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                        val isCheckedToday = stats?.currentStreak ?: 0 >= 1

                        HabitWithStats(
                            habit = habit,
                            statistics = stats,
                            isCheckedToday = isCheckedToday
                        )
                    }

                    // 전체 통계 계산
                    val totalChecks = habitsWithStats.sumOf { it.statistics?.totalChecks ?: 0 }
                    val averageRate = if (habitsWithStats.isNotEmpty()) {
                        habitsWithStats.mapNotNull { it.statistics?.completionRate }.average().toFloat()
                    } else 0f

                    val longestStreak = habitsWithStats.maxOfOrNull {
                        it.statistics?.longestStreak ?: 0
                    } ?: 0

                    val currentStreak = habitsWithStats.maxOfOrNull {
                        it.statistics?.currentStreak ?: 0
                    } ?: 0

                    val thisWeekChecks = habitsWithStats.sumOf {
                        it.statistics?.thisWeekChecks ?: 0
                    }

                    val thisMonthChecks = habitsWithStats.sumOf {
                        it.statistics?.thisMonthChecks ?: 0
                    }

                    _uiState.update {
                        it.copy(
                            habits = habitsWithStats.sortedByDescending {
                                it.statistics?.currentStreak ?: 0
                            },
                            totalHabits = habitsWithStats.size,
                            totalChecks = totalChecks,
                            averageCompletionRate = averageRate,
                            longestStreak = longestStreak,
                            currentStreak = currentStreak,
                            thisWeekChecks = thisWeekChecks,
                            thisMonthChecks = thisMonthChecks,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "통계 로드 실패"
                    )
                }
            }
        }
    }

    fun onRetry() {
        loadStatistics()
    }
}