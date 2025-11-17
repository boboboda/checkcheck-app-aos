package com.buyoungsil.checkcheck.feature.habit.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetMyGroupsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.DeleteHabitUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitByIdUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.GetHabitStatisticsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.UpdateHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 습관 상세 화면 ViewModel
 *
 * ✅ 습관 정보 조회
 * ✅ 통계 정보 조회
 * ✅ 그룹 공유 상태 변경
 * ✅ 습관 삭제
 */
@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val getHabitByIdUseCase: GetHabitByIdUseCase,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val authManager: FirebaseAuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: String = checkNotNull(savedStateHandle["habitId"])

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadHabit()
        loadGroups()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getHabitByIdUseCase(habitId)
                .onSuccess { habit ->
                    if (habit != null) {
                        _uiState.update { it.copy(habit = habit) }
                        loadStatistics()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "습관을 찾을 수 없습니다"
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "습관 로딩 실패"
                        )
                    }
                }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            getHabitStatisticsUseCase(habitId)
                .onSuccess { stats ->
                    _uiState.update {
                        it.copy(
                            statistics = stats,
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HabitDetailVM", "=== 그룹 로딩 시작 ===")
                android.util.Log.d("HabitDetailVM", "currentUserId: $currentUserId")

                // ✅ first() 대신 collect로 계속 구독
                getMyGroupsUseCase(currentUserId).collect { groups ->
                    android.util.Log.d("HabitDetailVM", "✅ 그룹 업데이트: ${groups.size}개")
                    groups.forEach { group ->
                        android.util.Log.d("HabitDetailVM", "- ${group.name} (${group.id})")
                    }

                    val currentHabit = _uiState.value.habit
                    val selectedGroup = groups.find { it.id == currentHabit?.groupId }

                    android.util.Log.d("HabitDetailVM", "현재 습관의 groupId: ${currentHabit?.groupId}")
                    android.util.Log.d("HabitDetailVM", "선택된 그룹: ${selectedGroup?.name}")

                    _uiState.update {
                        it.copy(
                            availableGroups = groups,
                            selectedGroup = selectedGroup
                        )
                    }

                    android.util.Log.d("HabitDetailVM", "UI 상태 업데이트 완료")
                }

            } catch (e: Exception) {
                android.util.Log.e("HabitDetailVM", "❌ 그룹 로딩 실패: ${e.message}", e)
            }
        }
    }

    /**
     * 그룹 공유 토글
     */
    fun onGroupSharedToggle(shared: Boolean) {
        val currentHabit = _uiState.value.habit ?: return

        _uiState.update {
            it.copy(
                habit = currentHabit.copy(
                    groupShared = shared,
                    groupId = if (!shared) null else it.selectedGroup?.id
                ),
                selectedGroup = if (!shared) null else it.selectedGroup
            )
        }
    }

    /**
     * 공유할 그룹 선택
     */
    fun onGroupSelect(group: Group) {
        val currentHabit = _uiState.value.habit ?: return

        _uiState.update {
            it.copy(
                selectedGroup = group,
                habit = currentHabit.copy(
                    groupId = group.id,
                    groupShared = true
                )
            )
        }
    }

    /**
     * 변경사항 저장
     */
    fun onSaveChanges() {
        val habit = _uiState.value.habit ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            updateHabitUseCase(habit)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            updateSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            error = error.message ?: "저장 실패"
                        )
                    }
                }
        }
    }

    /**
     * 삭제 다이얼로그 표시/숨김
     */
    fun onShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    /**
     * 습관 삭제
     */
    fun onDeleteHabit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }

            deleteHabitUseCase(habitId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleteSuccess = true,
                            showDeleteDialog = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            showDeleteDialog = false,
                            error = error.message ?: "삭제 실패"
                        )
                    }
                }
        }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}