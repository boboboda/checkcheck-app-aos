package com.buyoungsil.checkcheck.feature.habit.presentation.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.coin.domain.model.HabitLimits
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 습관 목록 ViewModel
 *
 * ✅ 토글 방식 → 체크 전용 방식으로 변경
 * ✅ 마일스톤 체크 추가
 * ✅ 다음 마일스톤 정보 계산
 */
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val checkHabitUseCase: CheckHabitUseCase,  // 🆕 ToggleHabitCheckUseCase → CheckHabitUseCase
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val checkHabitMilestoneUseCase: CheckHabitMilestoneUseCase,
    private val validateHabitLimitsUseCase: ValidateHabitLimitsUseCase,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "HabitListViewModel"
    }

    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        Log.d(TAG, "=== ViewModel 초기화 ===")
        Log.d(TAG, "currentUserId: $currentUserId")
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            Log.d(TAG, "=== 습관 로딩 시작 ===")
            _uiState.update { it.copy(loading = true, error = null) }

            try {
                getPersonalHabitsUseCase(currentUserId)
                    .catch { e ->
                        Log.e(TAG, "❌ 습관 로딩 중 에러 발생", e)
                        _uiState.update {
                            it.copy(
                                loading = false,
                                error = e.message ?: "알 수 없는 오류가 발생했습니다"
                            )
                        }
                    }
                    .collect { habits ->
                        Log.d(TAG, "✅ 습관 데이터 수신: ${habits.size}개")

                        val habitsWithStats = habits.map { habit ->
                            val stats = getHabitStatisticsUseCase(habit.id).getOrNull()

                            // 오늘 체크 여부 확인 (isChecked 메서드 사용)
                            val isCheckedToday = checkHabitUseCase.isChecked(
                                habitId = habit.id,
                                date = LocalDate.now()
                            )

                            // 다음 마일스톤 정보 계산
                            val nextMilestoneInfo = if (stats != null) {
                                NextMilestoneInfo.fromCurrentStreak(stats.currentStreak)
                            } else {
                                null
                            }

                            Log.d(TAG, "  - ${habit.title}: " +
                                    "streak=${stats?.currentStreak}, " +
                                    "checked=$isCheckedToday, " +
                                    "nextMilestone=${nextMilestoneInfo != null}")

                            HabitWithStats(
                                habit = habit,
                                statistics = stats,
                                isCheckedToday = isCheckedToday,
                                nextMilestoneInfo = nextMilestoneInfo
                            )
                        }

                        Log.d(TAG, "✅ 통계 포함 습관: ${habitsWithStats.size}개")
                        _uiState.update {
                            it.copy(
                                habits = habitsWithStats,
                                loading = false,
                                error = null
                            )
                        }
                        Log.d(TAG, "✅ UI State 업데이트 완료")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 습관 로딩 실패", e)
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "알 수 없는 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    /**
     * 습관 체크 (체크만 가능, 해제 불가)
     */
    fun onHabitCheck(habitId: String) {
        viewModelScope.launch {
            Log.d(TAG, "=== 습관 체크 시작 ===")
            Log.d(TAG, "habitId: $habitId")

            // 1. 이미 체크되어 있는지 확인
            val isAlreadyChecked = checkHabitUseCase.isChecked(habitId, LocalDate.now())

            if (isAlreadyChecked) {
                Log.d(TAG, "⚠️ 이미 체크 완료 - 동작 안 함")
                _uiState.update {
                    it.copy(infoMessage = "이미 오늘의 습관을 완료했습니다!")
                }
                return@launch
            }

            // 2. 체크 추가
            checkHabitUseCase(habitId, currentUserId, LocalDate.now())
                .onSuccess {
                    Log.d(TAG, "✅ 체크 추가 성공")

                    // 3. 체크 후 통계 다시 조회
                    val stats = getHabitStatisticsUseCase(habitId).getOrNull()
                    Log.d(TAG, "체크 후 currentStreak: ${stats?.currentStreak}")

                    // ✅ 4. UI 즉시 업데이트 - 여기 추가!
                    val updatedHabits = _uiState.value.habits.map { habitWithStats ->
                        if (habitWithStats.habit.id == habitId) {
                            habitWithStats.copy(
                                statistics = stats,
                                isCheckedToday = true,
                                nextMilestoneInfo = if (stats != null) {
                                    NextMilestoneInfo.fromCurrentStreak(stats.currentStreak)
                                } else {
                                    habitWithStats.nextMilestoneInfo
                                }
                            )
                        } else {
                            habitWithStats
                        }
                    }

                    _uiState.update { it.copy(habits = updatedHabits) }
                    Log.d(TAG, "✅ UI 즉시 업데이트 완료")

                    // 5. 마일스톤 체크
                    if (stats != null && stats.currentStreak > 0) {
                        val coinsAwarded = checkHabitMilestoneUseCase(
                            habitId = habitId,
                            userId = currentUserId,
                            currentStreak = stats.currentStreak
                        ).getOrNull()

                        if (coinsAwarded != null && coinsAwarded > 0) {
                            val habit = _uiState.value.habits.find { it.habit.id == habitId }
                            _uiState.update {
                                it.copy(
                                    milestoneMessage = MilestoneMessage(
                                        habitTitle = habit?.habit?.title ?: "",
                                        streakDays = stats.currentStreak,
                                        coinsAwarded = coinsAwarded
                                    )
                                )
                            }
                        }
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ 체크 실패: ${error.message}")
                    _uiState.update {
                        it.copy(error = error.message ?: "습관 체크에 실패했습니다")
                    }
                }
        }
    }

    /**
     * 마일스톤 메시지 제거
     */
    fun clearMilestoneMessage() {
        _uiState.update { it.copy(milestoneMessage = null) }
    }

    /**
     * 안내 메시지 제거
     */
    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    /**
     * 습관 삭제
     */
    fun onDeleteHabit(habitId: String) {
        viewModelScope.launch {
            Log.d(TAG, "습관 삭제: $habitId")
            deleteHabitUseCase(habitId)
        }
    }

    /**
     * ✅ 현재 습관 제한 정보 조회
     */
    fun getHabitLimitInfo(): String {
        val currentHabits = _uiState.value.habits.size
        val activeHabits = _uiState.value.habits.count {
            (it.statistics?.currentStreak ?: 0) > 0
        }

        return "전체: $currentHabits/${HabitLimits.MAX_HABITS_PER_USER}개 | " +
                "진행 중: $activeHabits/${HabitLimits.MAX_ACTIVE_HABITS}개"
    }

    /**
     * 다시 시도
     */
    fun onRetry() {
        Log.d(TAG, "다시 시도")
        loadHabits()
    }
}