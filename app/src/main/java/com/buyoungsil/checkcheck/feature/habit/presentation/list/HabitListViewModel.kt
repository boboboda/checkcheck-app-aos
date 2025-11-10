package com.buyoungsil.checkcheck.feature.habit.presentation.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 습관 목록 ViewModel
 * ✅ 로딩 상태 개선 - 첫 번째 빈 emit 무시
 */
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val toggleHabitCheckUseCase: ToggleHabitCheckUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
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
                    .drop(1)  // ✅ 첫 번째 빈 emit 무시!
                    .collect { habits ->
                        Log.d(TAG, "✅ 습관 데이터 수신: ${habits.size}개")

                        val habitsWithStats = habits.map { habit ->
                            val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                            val isCheckedToday = stats?.let {
                                it.currentStreak > 0
                            } ?: false

                            Log.d(TAG, "  - ${habit.title}: stats=${stats != null}, checked=$isCheckedToday")

                            HabitWithStats(
                                habit = habit,
                                statistics = stats,
                                isCheckedToday = isCheckedToday
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
                        Log.d(TAG, "✅ UI State 업데이트 완료 - loading=${_uiState.value.loading}")
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

    fun onHabitCheck(habitId: String) {
        viewModelScope.launch {
            Log.d(TAG, "습관 체크 토글: $habitId")
            toggleHabitCheckUseCase(
                habitId = habitId,
                userId = currentUserId,
                date = LocalDate.now()
            )
        }
    }

    fun onDeleteHabit(habitId: String) {
        viewModelScope.launch {
            Log.d(TAG, "습관 삭제: $habitId")
            deleteHabitUseCase(habitId)
        }
    }

    fun onRetry() {
        Log.d(TAG, "다시 시도")
        loadHabits()
    }
}