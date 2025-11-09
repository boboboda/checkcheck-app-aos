package com.buyoungsil.checkcheck.feature.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetPersonalHabitsUseCase
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.statistics.presentation.StatisticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 통계 ViewModel
 * ✅ 평균 달성률 계산 로직 개선 (0~1 범위를 % 단위로 변환)
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val habitRepository: HabitRepository,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "StatisticsViewModel"
    }

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                combine(
                    getPersonalHabitsUseCase(currentUserId),
                    getMonthlyChecks()
                ) { habits, monthlyCheckDates ->
                    Log.d(TAG, "=== 통계 로드 시작 ===")
                    Log.d(TAG, "습관 수: ${habits.size}")
                    Log.d(TAG, "이번 달 체크한 날: ${monthlyCheckDates.size}일")

                    val habitsWithStats = habits.map { habit ->
                        val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                        val isCheckedToday = stats?.currentStreak ?: 0 >= 1

                        // ✅ 각 습관 통계 로깅
                        Log.d(TAG, "  습관: ${habit.title}")
                        Log.d(TAG, "    - 총 체크: ${stats?.totalChecks}")
                        Log.d(TAG, "    - 달성률 (0~1): ${stats?.completionRate}")
                        Log.d(TAG, "    - 달성률 (%): ${stats?.completionRate?.let { it * 100 }}%")
                        Log.d(TAG, "    - 현재 스트릭: ${stats?.currentStreak}")

                        HabitWithStats(
                            habit = habit,
                            statistics = stats,
                            isCheckedToday = isCheckedToday
                        )
                    }

                    // ✅ 평균 달성률 계산 (0~1 범위의 값들을 평균내고 100을 곱함)
                    val validRates = habitsWithStats.mapNotNull { it.statistics?.completionRate }
                    val averageRate = if (validRates.isNotEmpty()) {
                        val avgValue = validRates.average().toFloat() * 100f  // ✅ 100 곱하기
                        Log.d(TAG, "=== 평균 달성률 계산 ===")
                        Log.d(TAG, "  유효한 달성률(0~1): $validRates")
                        Log.d(TAG, "  평균(0~1): ${validRates.average()}")
                        Log.d(TAG, "  평균(%): $avgValue%")
                        avgValue
                    } else {
                        Log.d(TAG, "  평균 달성률: 데이터 없음")
                        0f
                    }

                    val totalChecks = habitsWithStats.sumOf { it.statistics?.totalChecks ?: 0 }

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

                    Log.d(TAG, "=== 최종 통계 ===")
                    Log.d(TAG, "  총 체크: $totalChecks")
                    Log.d(TAG, "  평균 달성률: $averageRate%")
                    Log.d(TAG, "  최장 스트릭: $longestStreak 일")
                    Log.d(TAG, "  현재 스트릭: $currentStreak 일")
                    Log.d(TAG, "  이번 주 체크: $thisWeekChecks")
                    Log.d(TAG, "  이번 달 체크: $thisMonthChecks")

                    _uiState.update {
                        it.copy(
                            habits = habitsWithStats.sortedByDescending {
                                it.statistics?.currentStreak ?: 0
                            },
                            totalHabits = habitsWithStats.size,
                            totalChecks = totalChecks,
                            averageCompletionRate = averageRate,  // ✅ 이미 % 단위
                            longestStreak = longestStreak,
                            currentStreak = currentStreak,
                            thisWeekChecks = thisWeekChecks,
                            thisMonthChecks = thisMonthChecks,
                            monthlyCheckDates = monthlyCheckDates,
                            isLoading = false,
                            error = null
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                Log.e(TAG, "통계 로드 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "통계 로드 실패"
                    )
                }
            }
        }
    }

    /**
     * ✅ 이번 달 전체 습관의 체크 데이터 가져오기
     */
    private fun getMonthlyChecks(): Flow<Set<LocalDate>> = flow {
        val currentMonth = YearMonth.now()
        val monthStart = currentMonth.atDay(1)
        val monthEnd = currentMonth.atEndOfMonth()

        try {
            // 모든 습관 가져오기
            val habits = getPersonalHabitsUseCase(currentUserId).first()

            val checkedDates = mutableSetOf<LocalDate>()

            // 각 습관의 이번 달 체크 데이터 수집
            habits.forEach { habit ->
                habitRepository.getChecksByDateRange(
                    habitId = habit.id,
                    startDate = monthStart,
                    endDate = monthEnd
                ).first().forEach { check ->
                    if (check.completed) {
                        checkedDates.add(check.date)
                    }
                }
            }

            Log.d(TAG, "이번 달 체크한 날짜들: $checkedDates")
            emit(checkedDates)
        } catch (e: Exception) {
            Log.e(TAG, "월간 체크 데이터 로드 실패", e)
            emit(emptySet())
        }
    }

    fun onRetry() {
        loadStatistics()
    }
}