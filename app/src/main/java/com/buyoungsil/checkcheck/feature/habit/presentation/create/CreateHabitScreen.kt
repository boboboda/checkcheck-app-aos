package com.buyoungsil.checkcheck.feature.habit.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.notification.rememberNotificationPermissionState
import com.buyoungsil.checkcheck.core.ui.components.ReminderSettingDialog

/**
 * 습관 생성 화면
 * ✅ 알림 설정 UI 추가
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    viewModel: CreateHabitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberNotificationPermissionState()  // ✅
    var showReminderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("습관 만들기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아이콘 선택
            Text(
                text = "아이콘",
                style = MaterialTheme.typography.titleMedium
            )
            // ... 아이콘 선택 UI (기존 코드) ...

            // 습관 제목
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("습관 이름") },
                placeholder = { Text("예: 물 2L 마시기") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null && uiState.title.isBlank()
            )

            // 설명
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("설명 (선택)") },
                placeholder = { Text("습관에 대한 설명을 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // ✅ 알림 설정 버튼
            OutlinedButton(
                onClick = {
                    if (permissionState.hasPermission) {
                        showReminderDialog = true
                    } else {
                        permissionState.requestPermission()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Notifications, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (uiState.reminderTime != null) {
                        "알림: ${uiState.reminderTime}"
                    } else {
                        "알림 설정"
                    }
                )
            }

            // 그룹 공유 설정
            // ... 기존 그룹 공유 UI ...

            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 생성 버튼
            Button(
                onClick = { viewModel.onCreateHabit() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.loading
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("습관 만들기")
                }
            }
        }

        // ✅ 알림 설정 다이얼로그
        if (showReminderDialog) {
            ReminderSettingDialog(
                currentTime = uiState.reminderTime,
                enabled = uiState.reminderEnabled,
                onDismiss = { showReminderDialog = false },
                onConfirm = { time, enabled ->
                    viewModel.onReminderChange(time, enabled)
                    showReminderDialog = false
                }
            )
        }
    }
}