package com.buyoungsil.checkcheck.feature.habit.presentation.list

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
 */
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val getPersonalHabitsUseCase: GetPersonalHabitsUseCase,
    private val toggleHabitCheckUseCase: ToggleHabitCheckUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()



    // TODO: 실제로는 Auth에서 가져와야 함
    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getPersonalHabitsUseCase(currentUserId)
                    .collect { habits ->
                        val habitsWithStats = habits.map { habit ->
                            val stats = getHabitStatisticsUseCase(habit.id).getOrNull()
                            val isCheckedToday = stats?.let {
                                // 오늘 체크 여부는 currentStreak > 0 으로 간단히 판단
                                // TODO: 더 정확한 로직 필요
                                it.currentStreak > 0
                            } ?: false

                            HabitWithStats(
                                habit = habit,
                                statistics = stats,
                                isCheckedToday = isCheckedToday
                            )
                        }

                        _uiState.update {
                            it.copy(
                                habits = habitsWithStats,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "알 수 없는 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun onHabitCheck(habitId: String) {
        viewModelScope.launch {
            toggleHabitCheckUseCase(
                habitId = habitId,
                userId = currentUserId,
                date = LocalDate.now()
            )
            // 체크 후 통계 갱신을 위해 다시 로드
            loadHabits()
        }
    }

    fun onDeleteHabit(habitId: String) {
        viewModelScope.launch {
            deleteHabitUseCase(habitId)
        }
    }

    fun onRetry() {
        loadHabits()
    }
}