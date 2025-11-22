package com.buyoungsil.checkcheck.feature.statistics.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.habit.domain.repository.HabitRepository
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetPersonalHabitsUseCase
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.ranking.domain.usecase.GetGlobalRankingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 통계 ViewModel
 * ✅ 평균 달성률 계산 로직 개선 (0~1 범위를 % 단위로 변환)
 * ✅ 글로벌 랭킹 기능 추가
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val habitRepository: HabitRepository,
    private val authManager: FirebaseAuthManager,
    private val getGlobalRankingsUseCase: GetGlobalRankingsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "StatisticsViewModel"
    }

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _globalRankingState = MutableStateFlow(GlobalRankingUiState())
    val globalRankingState: StateFlow<GlobalRankingUiState> = _globalRankingState.asStateFlow()

    // ✅ 전체 습관 제목 리스트 상태 추가
    private val _allHabitTitlesState = MutableStateFlow<List<String>>(emptyList())
    val allHabitTitlesState: StateFlow<List<String>> = _allHabitTitlesState.asStateFlow()

    val currentUserId: String = authManager.currentUserId ?: "anonymous"

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

                    val validRates = habitsWithStats.mapNotNull { it.statistics?.completionRate }
                    val averageRate = if (validRates.isNotEmpty()) {
                        val avgValue = validRates.average().toFloat() * 100f
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
                            averageCompletionRate = averageRate,
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
     * ✅ 전체 습관 목록 로드 (Firestore globalHabitRankings 컬렉션에서)
     */
    fun loadAllHabits() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== 전체 습관 목록 로드 시작 ===")

                // Firestore에서 globalHabitRankings 컬렉션의 모든 문서 ID 가져오기
                // 문서 ID = 습관 제목
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("globalHabitRankings")
                    .get()
                    .await()

                val habitTitles = snapshot.documents
                    .map { it.id }
                    .distinct()
                    .sorted()

                _allHabitTitlesState.update { habitTitles }

                Log.d(TAG, "✅ 전체 습관 ${habitTitles.size}개 로드 완료")
                Log.d(TAG, "습관 목록: $habitTitles")

                // 첫 번째 습관의 랭킹 자동 로드
                if (habitTitles.isNotEmpty()) {
                    loadGlobalRanking(habitTitles.first())
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 전체 습관 목록 로드 실패", e)
                _allHabitTitlesState.update { emptyList() }
            }
        }
    }

    /**
     * 글로벌 랭킹 로드
     */
    fun loadGlobalRanking(habitTitle: String) {
        viewModelScope.launch {
            _globalRankingState.update {
                it.copy(
                    habitTitle = habitTitle,
                    isLoading = true,
                    error = null
                )
            }

            try {
                getGlobalRankingsUseCase(habitTitle, limit = 100)
                    .onSuccess { ranking ->
                        _globalRankingState.update {
                            it.copy(
                                rankings = ranking.userRankings,
                                isLoading = false,
                                error = null
                            )
                        }
                        Log.d(TAG, "✅ 글로벌 랭킹 로드 완료: ${ranking.userRankings.size}명")
                    }
                    .onFailure { error ->
                        Log.e(TAG, "❌ 글로벌 랭킹 로드 실패", error)
                        _globalRankingState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "랭킹을 불러올 수 없어요"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 글로벌 랭킹 로드 중 오류", e)
                _globalRankingState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "랭킹을 불러올 수 없어요"
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
            val habits = getPersonalHabitsUseCase(currentUserId).first()

            val checkedDates = mutableSetOf<LocalDate>()

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