package com.buyoungsil.checkcheck.feature.habit.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.*
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 습관 생성 화면 - 아이콘 선택 기능 추가
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    viewModel: CreateHabitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showIconPicker by remember { mutableStateOf(false) }

    // success가 true가 되면 뒤로가기
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "습관 만들기",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight,
                    navigationIconContentColor = TextPrimaryLight
                )
            )
        },
        containerColor = OrangeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 습관 이름
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.HabitCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "습관 이름",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onTitleChange(it) },
                        placeholder = { Text("예: 물 2L 마시기") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ComponentShapes.TextField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerLight,
                            cursorColor = OrangePrimary,
                        ),
                        isError = uiState.error != null && uiState.title.isBlank()
                    )
                }
            }

            // 설명
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.HabitCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "설명 (선택)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        placeholder = { Text("이 습관에 대해 간단히 설명해주세요") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = ComponentShapes.TextField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerLight,
                            cursorColor = OrangePrimary,
                        )
                    )
                }
            }

            // ✨ 아이콘 선택 (개선된 부분)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.HabitCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "아이콘",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    // 선택된 아이콘 표시 및 변경 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 현재 선택된 아이콘
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(OrangePrimary, OrangeSecondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.icon,
                                    fontSize = 32.sp
                                )
                            }

                            Column {
                                Text(
                                    text = "선택된 아이콘",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                                Text(
                                    text = uiState.icon,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 변경 버튼
                        OutlinedButton(
                            onClick = { showIconPicker = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OrangePrimary
                            ),
                            shape = ComponentShapes.SecondaryButton
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("변경")
                        }
                    }
                }
            }

            // 그룹 공유 (availableGroups가 있을 때만)
            if (uiState.availableGroups.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ComponentShapes.HabitCard,
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "그룹 공유",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Text(
                                    text = "그룹 멤버들과 공유합니다",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }

                            Switch(
                                checked = uiState.groupShared,
                                onCheckedChange = { viewModel.onGroupSharedToggle(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                    checkedTrackColor = OrangePrimary
                                )
                            )
                        }

                        // 그룹 선택
                        if (uiState.groupShared) {
                            HorizontalDivider(color = DividerLight)

                            Text(
                                text = "공유할 그룹",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.availableGroups.forEach { group ->
                                    FilterChip(
                                        selected = uiState.selectedGroup?.id == group.id,
                                        onClick = { viewModel.onGroupSelect(group) },
                                        label = { Text(group.name) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = getGroupTypeColor(group.type.name.lowercase()).copy(alpha = 0.15f),
                                            selectedLabelColor = getGroupTypeColor(group.type.name.lowercase())
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 에러 메시지
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ComponentShapes.HabitCard,
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorRed,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 생성 버튼
            OrangeGradientButton(
                text = if (uiState.loading) "생성 중..." else "습관 만들기",
                onClick = { viewModel.onCreateHabit() },
                enabled = !uiState.loading && uiState.title.isNotBlank(),
                icon = Icons.Default.Add
            )
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
    val icons = remember {
        listOf(
            // 건강 & 운동
            "💪", "🏃", "🏋️", "🧘", "🚴", "⚽", "🏀", "🎾", "🏊",

            // 음식 & 건강
            "🍎", "🥗", "🥑", "🥕", "🥤", "💧", "☕", "🍽️",

            // 공부 & 업무
            "📚", "📖", "✍️", "📝", "💼", "💻", "🎯", "🎓",

            // 취미 & 여가
            "🎵", "🎸", "🎨", "🖌️", "📷", "🎮", "🎬", "📺",

            // 일상 & 루틴
            "😴", "🛏️", "🚿", "🧹", "🧺", "🪥", "💆", "🧖",

            // 감정 & 마음
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🤍", "❤️‍🔥",

            // 자연 & 식물
            "🌱", "🌿", "🌻", "🌺", "🌸", "🌼", "🌲", "🍃",

            // 시간 & 달성
            "⏰", "⏱️", "⌛", "🔔", "🔥", "⭐", "✨", "🎉"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "아이콘 선택",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(400.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(icons) { icon ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (icon == currentIcon) {
                                    Brush.linearGradient(
                                        colors = listOf(OrangePrimary, OrangeSecondary)
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            OrangeSurfaceVariant,
                                            OrangeSurfaceVariant
                                        )
                                    )
                                }
                            )
                            .border(
                                width = if (icon == currentIcon) 2.dp else 0.dp,
                                color = if (icon == currentIcon) OrangePrimary else androidx.compose.ui.graphics.Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onIconSelected(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon,
                            fontSize = 28.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = OrangePrimary)
            }
        },
        shape = ComponentShapes.Dialog
    )
}