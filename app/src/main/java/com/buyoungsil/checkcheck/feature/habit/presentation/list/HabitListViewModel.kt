package com.buyoungsil.checkcheck.feature.habit.presentation.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
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
                    .drop(1)  // 첫 번째 빈 emit 무시
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

                // 🆕 이미 체크된 경우 메시지 표시
                _uiState.update {
                    it.copy(
                        infoMessage = "이미 오늘의 습관을 완료했습니다!"
                    )
                }
                return@launch
            }

            // 2. 습관 체크
            val checkResult = checkHabitUseCase(
                habitId = habitId,
                userId = currentUserId,
                date = LocalDate.now()
            )

            checkResult.onSuccess { wasAdded ->
                if (wasAdded) {
                    Log.d(TAG, "✅ 체크 추가 성공")

                    // 3. 잠시 대기 (Firestore 업데이트 반영)
                    kotlinx.coroutines.delay(500)

                    // 4. 최신 통계 조회
                    val stats = getHabitStatisticsUseCase(habitId).getOrNull()
                    if (stats != null && stats.currentStreak > 0) {
                        Log.d(TAG, "체크 후 currentStreak: ${stats.currentStreak}")

                        // 5. 습관 정보 조회
                        val habits = _uiState.value.habits
                        val habitWithStats = habits.find { it.habit.id == habitId }

                        // 6. 마일스톤 체크 및 코인 지급
                        val milestoneResult = checkHabitMilestoneUseCase(
                            habitId = habitId,
                            userId = currentUserId,
                            currentStreak = stats.currentStreak
                        )

                        milestoneResult.onSuccess { coinsAwarded ->
                            if (coinsAwarded != null && habitWithStats != null) {
                                Log.d(TAG, "🎉 마일스톤 달성! ${coinsAwarded}코인 획득")

                                // 마일스톤 메시지 표시
                                _uiState.update {
                                    it.copy(
                                        milestoneMessage = MilestoneMessage(
                                            habitTitle = habitWithStats.habit.title,
                                            streakDays = stats.currentStreak,
                                            coinsAwarded = coinsAwarded
                                        )
                                    )
                                }
                            }
                        }.onFailure { error ->
                            Log.e(TAG, "마일스톤 체크 실패", error)

                            // 월간 제한 초과 에러 처리
                            if (error.message?.contains("월간 코인 제한") == true) {
                                _uiState.update {
                                    it.copy(
                                        error = "이번 달 코인 제한에 도달했습니다.\n" +
                                                "다음 달에 다시 도전해주세요!"
                                    )
                                }
                            }
                        }
                    }
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ 체크 실패", error)
                _uiState.update {
                    it.copy(
                        error = error.message ?: "습관 체크에 실패했습니다"
                    )
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
     * 다시 시도
     */
    fun onRetry() {
        Log.d(TAG, "다시 시도")
        loadHabits()
    }
}