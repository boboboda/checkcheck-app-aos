package com.buyoungsil.checkcheck.feature.habit.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.group.domain.usecase.GetMyGroupsUseCase
import com.buyoungsil.checkcheck.feature.habit.domain.model.Habit
import com.buyoungsil.checkcheck.feature.habit.domain.usecase.CreateHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
    private val authManager: FirebaseAuthManager,
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
                            groupShared = preselectedGroup != null,  // ✅ isGroupShared → groupShared
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

    fun onGroupSharedToggle(shared: Boolean) {  // ✅ isShared → shared
        _uiState.update {
            it.copy(
                groupShared = shared,  // ✅ isGroupShared → groupShared
                selectedGroup = if (!shared) null else it.selectedGroup
            )
        }
    }

    fun onGroupSelect(group: com.buyoungsil.checkcheck.feature.group.domain.model.Group) {
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun onCreateHabit() {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "습관 이름을 입력해주세요") }
            return
        }

        if (currentState.groupShared && currentState.selectedGroup == null) {  // ✅
            _uiState.update { it.copy(error = "그룹을 선택해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }  // ✅ isLoading → loading

            val habit = Habit(
                id = "",
                userId = currentUserId,
                title = currentState.title,
                description = currentState.description.takeIf { it.isNotBlank() },
                icon = currentState.icon,
                color = currentState.color,
                groupShared = currentState.groupShared,  // ✅ isGroupShared → groupShared
                groupId = currentState.selectedGroup?.id
            )

            createHabitUseCase(habit)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            loading = false,  // ✅ isLoading → loading
                            success = true    // ✅ isSuccess → success
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,  // ✅ isLoading → loading
                            error = error.message ?: "습관 생성 실패"
                        )
                    }
                }
        }
    }
}