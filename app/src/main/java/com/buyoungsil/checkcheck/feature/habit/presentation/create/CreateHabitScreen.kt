package com.buyoungsil.checkcheck.feature.habit.presentation.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 습관 생성 화면
 * ✅ 알림 설정 UI 제거 (습관은 알림 불필요)
 * ✅ 아이콘 선택 UI 추가
 * ✅ 그룹 공유 설정 UI 추가
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    viewModel: CreateHabitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showIconPicker by remember { mutableStateOf(false) }

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아이콘 선택
            Text(
                text = "아이콘",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showIconPicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.icon,
                        fontSize = 32.sp,
                        modifier = Modifier.size(48.dp),
                        textAlign = TextAlign.Center
                    )
                    Column {
                        Text(
                            text = "아이콘 선택",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "습관을 표현할 이모지를 선택하세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

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

            // 그룹 공유 설정
            if (uiState.availableGroups.isNotEmpty()) {
                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "그룹과 공유",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "그룹 멤버들이 내 습관을 볼 수 있습니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.groupShared,
                        onCheckedChange = { viewModel.onGroupSharedToggle(it) }
                    )
                }

                // 그룹 선택 (공유가 활성화된 경우)
                if (uiState.groupShared) {
                    Text(
                        text = "공유할 그룹 선택",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.availableGroups.forEach { group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onGroupSelect(group) },
                                border = if (group.id == uiState.selectedGroup?.id) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    null
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (group.id == uiState.selectedGroup?.id) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.icon,
                                        fontSize = 24.sp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = group.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = group.type.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (group.id == uiState.selectedGroup?.id) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "선택됨",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
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
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("습관 만들기")
                }
            }
        }
    }

    // 아이콘 선택 다이얼로그
    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = uiState.icon,
            onIconSelected = { icon ->
                viewModel.onIconChange(icon)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

/**
 * 아이콘 선택 다이얼로그
 */
@Composable
private fun IconPickerDialog(
    currentIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 카테고리별 이모지
    val iconCategories = remember {
        mapOf(
            "생활" to listOf("📌", "✅", "💧", "☀️", "🌙", "⏰", "📱", "💻", "📚", "✏️", "🎯", "🔔"),
            "건강" to listOf("💪", "🏃", "🧘", "🥗", "🍎", "🥛", "💊", "🏥", "😴", "🧠", "❤️", "🦷"),
            "공부" to listOf("📖", "✍️", "🎓", "📝", "🔬", "🧮", "📊", "💡", "🎨", "🎵", "🌍", "🔍"),
            "운동" to listOf("⚽", "🏀", "🎾", "🏊", "🚴", "⛷️", "🏋️", "🤸", "🧗", "🏃‍♀️", "🤾", "🥋"),
            "취미" to listOf("🎮", "🎬", "📷", "🎸", "🎤", "🎭", "🎲", "🧩", "🎪", "🎨", "✂️", "🧶"),
            "관계" to listOf("👨‍👩‍👧‍👦", "💑", "👫", "🤝", "💬", "☎️", "✉️", "🎁", "🎉", "🎂", "❤️", "💕")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "아이콘 선택",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                iconCategories.forEach { (category, icons) ->
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((icons.size / 6 + 1) * 56.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(icons) { icon ->
                            IconItem(
                                icon = icon,
                                isSelected = icon == currentIcon,
                                onClick = { onIconSelected(icon) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * 아이콘 아이템
 */
@Composable
private fun IconItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }
    }
}