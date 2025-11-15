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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 습관 생성 ViewModel
 * ✅ 알림 관련 로직 제거
 * ✅ loadGroups의 무한 collect 문제 수정
 */
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
                // ✅ first()를 사용하여 첫 번째 값만 가져오고 종료
                val groups = getMyGroupsUseCase(currentUserId).first()
                val preselectedGroup = groups.find { it.id == preselectedGroupId }
                _uiState.update {
                    it.copy(
                        availableGroups = groups,
                        groupShared = preselectedGroup != null,
                        selectedGroup = preselectedGroup
                    )
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

    fun onGroupChallengeToggle(isChallenge: Boolean) {
        _uiState.update { it.copy(isGroupChallenge = isChallenge) }
    }

    fun onCreateHabit() {
        val currentState = _uiState.value

        android.util.Log.d("CreateHabitVM", "=== 습관 생성 시작 ===")
        android.util.Log.d("CreateHabitVM", "title: ${currentState.title}")
        android.util.Log.d("CreateHabitVM", "loading: ${currentState.loading}")

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "습관 이름을 입력해주세요", loading = false) }
            return
        }

        if (currentState.groupShared && currentState.selectedGroup == null) {
            _uiState.update { it.copy(error = "그룹을 선택해주세요", loading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            android.util.Log.d("CreateHabitVM", "로딩 시작")

            val habit = Habit(
                id = "",
                userId = currentUserId,
                title = currentState.title,
                description = currentState.description.takeIf { it.isNotBlank() },
                icon = currentState.icon,
                color = currentState.color,
                groupShared = currentState.groupShared,
                groupId = currentState.selectedGroup?.id,
                isGroupChallenge = currentState.isGroupChallenge  // ✅ 추가
            )

            createHabitUseCase(habit)
                .onSuccess {
                    android.util.Log.d("CreateHabitVM", "✅ 습관 생성 성공!")
                    _uiState.update {
                        it.copy(
                            loading = false,
                            success = true
                        )
                    }
                    android.util.Log.d("CreateHabitVM", "success = ${_uiState.value.success}")
                }
                .onFailure { error ->
                    android.util.Log.e("CreateHabitVM", "❌ 습관 생성 실패: ${error.message}")

                    // ✅ 제한 관련 에러는 사용자 친화적 메시지로 변환
                    val userFriendlyMessage = when {
                        error.message?.contains("최대 10개") == true ->
                            "습관은 최대 10개까지만 만들 수 있어요 😢"
                        error.message?.contains("동시에 5개") == true ->
                            "동시에 진행할 수 있는 습관은 최대 5개예요 💪"
                        else ->
                            error.message ?: "습관 생성에 실패했어요"
                    }

                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = userFriendlyMessage
                        )
                    }
                }
        }
    }
}