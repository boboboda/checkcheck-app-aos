package com.buyoungsil.checkcheck.feature.task.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.task.domain.usecase.CreateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val authManager: FirebaseAuthManager,  // ✨ 추가
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    // ✅ Firebase UID 사용
    private val currentUserId: String
        get() = authManager.currentUserId ?: "anonymous"

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onPriorityChange(priority: TaskPriority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onCreateTask() {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "할일 제목을 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val task = Task(
                id = "",
                groupId = groupId,
                title = currentState.title,
                description = currentState.description.takeIf { it.isNotBlank() },
                priority = currentState.priority,
                createdBy = currentUserId  // ✅ Firebase UID
            )

            createTaskUseCase(task)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "할일 생성 실패"
                        )
                    }
                }
        }
    }
}