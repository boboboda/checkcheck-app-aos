package com.buyoungsil.checkcheck.feature.habit.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.core.notification.ReminderScheduler
import com.buyoungsil.checkcheck.core.notification.domain.model.Reminder
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetMyGroupsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.CreateHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val authManager: FirebaseAuthManager,
    private val reminderScheduler: ReminderScheduler,  // ✅ 추가
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val preselectedGroupId: String? =
        savedStateHandle.get<String>("groupId")

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            try {
                getMyGroupsUseCase(currentUserId).collect { groups ->
                    _uiState.update {
                        val preselectedGroup = groups.find { it.id == preselectedGroupId }
                        it.copy(
                            availableGroups = groups,
                            groupShared = preselectedGroup != null,
                            selectedGroup = preselectedGroup
                        )
                    }
                }
            } catch (e: Exception) {
                // 그룹 로드 실패해도 습관은 만들 수 있음
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onIconChange(icon: String) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun onColorChange(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun onGroupSharedToggle(shared: Boolean) {
        _uiState.update {
            it.copy(
                groupShared = shared,
                selectedGroup = if (!shared) null else it.selectedGroup
            )
        }
    }

    fun onGroupSelect(group: com.buyoungsil.checkcheck.feature.group.domain.model.Group) {
        _uiState.update { it.copy(selectedGroup = group) }
    }

    // ✅ 알림 설정 변경
    fun onReminderChange(time: LocalTime?, enabled: Boolean) {
        _uiState.update {
            it.copy(
                reminderTime = time,
                reminderEnabled = enabled
            )
        }
    }

    fun onCreateHabit() {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "습관 이름을 입력해주세요") }
            return
        }

        if (currentState.groupShared && currentState.selectedGroup == null) {
            _uiState.update { it.copy(error = "그룹을 선택해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            val habit = Habit(
                id = "",
                userId = currentUserId,
                title = currentState.title,
                description = currentState.description.takeIf { it.isNotBlank() },
                icon = currentState.icon,
                color = currentState.color,
                reminderTime = currentState.reminderTime,      // ✅
                reminderEnabled = currentState.reminderEnabled, // ✅
                groupShared = currentState.groupShared,
                groupId = currentState.selectedGroup?.id
            )

            createHabitUseCase(habit)
                .onSuccess { createdHabit ->
                    // ✅ 알림 스케줄 설정
                    if (createdHabit.reminderEnabled && createdHabit.reminderTime != null) {
                        reminderScheduler.scheduleHabitReminder(
                            Reminder(
                                id = UUID.randomUUID().toString(),
                                habitId = createdHabit.id,
                                habitTitle = createdHabit.title,
                                time = createdHabit.reminderTime,
                                enabled = true
                            )
                        )
                    }

                    _uiState.update {
                        it.copy(
                            loading = false,
                            success = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = error.message ?: "습관 생성 실패"
                        )
                    }
                }
        }
    }
}