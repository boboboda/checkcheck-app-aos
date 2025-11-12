package com.buyoungsil.checkcheck.feature.home

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.*
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.feature.task.presentation.list.TaskCard
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * 🧡 오렌지 테마 홈 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToHabitCreate: (String?) -> Unit,
    onNavigateToGroupList: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHabitList: () -> Unit,  // ✅ 추가
    onNavigateToPersonalTaskCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "체크체크",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getTodayDate(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = TextPrimaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToHabitCreate(null) },
                containerColor = OrangePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "습관 추가")
            }
        },
        containerColor = OrangeBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    // 로딩
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = OrangePrimary
                        )
                    }
                }

                uiState.error != null -> {
                    // 에러
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "😢",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "오류가 발생했어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OrangeGradientButton(
                            text = "다시 시도",
                            onClick = { viewModel.onRetry() }
                        )
                    }
                }

                else -> {
                    // 메인 콘텐츠
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // ✅ 긴급 할일 섹션 (새로 추가)
                        if (uiState.urgentTasks.isNotEmpty()) {
                            item {
                                UrgentTasksSection(
                                    tasks = uiState.urgentTasks,
                                    onTaskClick = { task ->
                                        // TODO: Task 상세 화면으로 이동
                                        onNavigateToGroupDetail(task.groupId)
                                    }
                                )
                            }
                        }

                        // 오늘의 진행률 카드
                        item {
                            TodayProgressCard(
                                completedCount = uiState.todayCompletedCount,
                                totalCount = uiState.todayTotalCount
                            )
                        }

                        // 습관 섹션
                        item {
                            SectionHeader(
                                title = "📝 내 습관",
                                actionText = if (uiState.habits.isEmpty()) "추가" else "전체보기",  // ✅ 수정
                                onActionClick = {
                                    if (uiState.habits.isEmpty()) {
                                        onNavigateToHabitCreate(null)
                                    } else {
                                        onNavigateToHabitList()  // ✅ 습관 리스트로 이동
                                    }
                                }
                            )
                        }

                        if (uiState.habits.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = "📝",
                                    message = "아직 습관이 없어요",
                                    actionText = "습관 추가",
                                    onActionClick = { onNavigateToHabitCreate(null) }
                                )
                            }
                        } else {
                            // ✅ 스와이프 가능한 습관 카드들
                            items(
                                items = uiState.habits,
                                key = { it.habit.id }
                            ) { habitWithStats ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        when (dismissValue) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                showDeleteDialog = habitWithStats.habit.id
                                                false
                                            }
                                            else -> false
                                        }
                                    },
                                    positionalThreshold = { it * 0.25f }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {
                                        val color by animateColorAsState(
                                            targetValue = when (dismissState.targetValue) {
                                                SwipeToDismissBoxValue.EndToStart -> ErrorRed
                                                else -> Color.Transparent
                                            },
                                            label = "background"
                                        )

                                        val scale by animateFloatAsState(
                                            targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.3f else 0.8f,
                                            label = "scale"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color, ComponentShapes.HabitCard)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "삭제",
                                                tint = Color.White,
                                                modifier = Modifier.scale(scale)
                                            )
                                        }
                                    },
                                    content = {
                                        HabitItemCard(
                                            habitWithStats = habitWithStats,
                                            onCheck = { viewModel.onHabitCheck(habitWithStats.habit.id) },
                                            onDelete = { showDeleteDialog = habitWithStats.habit.id }
                                        )
                                    }
                                )
                            }
                        }

                        // HomeScreen.kt에 개인 할일 섹션 추가

// LazyColumn 내부에 추가
// "긴급 할일" 섹션 다음에 배치

// ✅ 개인 할일 섹션
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📝",
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = "내 할일",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    if (uiState.personalTasks.isNotEmpty()) {
                                        Surface(
                                            shape = ComponentShapes.Badge,
                                            color = OrangePrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${uiState.personalTasks.size}",
                                                style = CustomTypography.chip,
                                                fontWeight = FontWeight.Bold,
                                                color = OrangePrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // 할일 추가 버튼
                                IconButton(
                                    onClick = { onNavigateToPersonalTaskCreate() }  // ✅ 개인 할일 생성
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "할일 추가",
                                        tint = OrangePrimary
                                    )
                                }
                            }
                        }

                    // 개인 할일 목록
                        if (uiState.personalTasks.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ComponentShapes.HabitCard,
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "📝",
                                            fontSize = 32.sp
                                        )
                                        Text(
                                            text = "개인 할일이 없어요",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondaryLight
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = uiState.personalTasks.take(3),  // 최대 3개만 표시
                                key = { it.id }
                            ) { task ->
                                TaskCard(
                                    taskName = task.title,
                                    isCompleted = task.status == TaskStatus.COMPLETED,
                                    priority = task.priority.name.lowercase(),
                                    dueDate = task.dueDate,
                                    dueTime = task.dueTime,
                                    reminderMinutes = if (task.reminderEnabled) task.reminderMinutesBefore else null,
                                    assignee = task.assigneeName,
                                    createdBy = task.createdBy,
                                    currentUserId = viewModel.currentUserId,
                                    onCheck = { /* TODO: 완료 처리 */ },
                                    onDelete = { /* TODO: 삭제 */ }
                                )
                            }
                        }

                        // 그룹 섹션
                        if (uiState.groups.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "👥 내 그룹",
                                    actionText = "전체보기",
                                    onActionClick = onNavigateToGroupList
                                )
                            }

                            items(
                                items = uiState.groups.take(3),
                                key = { it.id }
                            ) { group ->
                                SimpleGroupCard(
                                    group = group,
                                    onClick = { onNavigateToGroupDetail(group.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 삭제 확인 다이얼로그
    showDeleteDialog?.let { habitId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    "습관 삭제",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("정말 이 습관을 삭제하시겠어요?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteHabit(habitId)
                        showDeleteDialog = null
                    }
                ) {
                    Text(
                        "삭제",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 오늘의 진행률 카드
 */
@Composable
private fun TodayProgressCard(
    completedCount: Int,
    totalCount: Int
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.StatCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "오늘의 목표",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "$completedCount / $totalCount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(ComponentShapes.Chip),
                color = OrangePrimary,
                trackColor = DividerLight,
            )

            Text(
                text = if (totalCount > 0) {
                    when {
                        progress >= 1f -> "🎉 완벽해요!"
                        progress >= 0.8f -> "💪 거의 다 왔어요!"
                        progress >= 0.5f -> "👍 절반 완료!"
                        progress > 0f -> "🔥 시작이 좋아요!"
                        else -> "시작해볼까요?"
                    }
                } else {
                    "오늘 습관을 추가해보세요!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
            )
        }
    }
}

/**
 * 섹션 헤더
 */
@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }
        }
    }
}

/**
 * 습관 아이템 카드
 */
@Composable
private fun HabitItemCard(
    habitWithStats: HabitWithStats,
    onCheck: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = habitWithStats.habit
    val isChecked = habitWithStats.isCheckedToday

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCheck),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) CheckedBackground else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isChecked) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ComponentShapes.IconBackground)
                    .background(
                        Brush.linearGradient(
                            colors = if (isChecked) {
                                listOf(OrangePrimary, OrangeSecondary)
                            } else {
                                listOf(OrangeSurfaceVariant, OrangeSurfaceVariant)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.icon,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                habitWithStats.statistics?.let { stats ->
                    Text(
                        text = "🔥 ${stats.currentStreak}일 연속",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            // 체크 버튼
            IconButton(
                onClick = onCheck,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isChecked) "완료" else "미완료",
                    tint = if (isChecked) OrangePrimary else DividerLight,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * 간단한 그룹 카드
 */
@Composable
private fun SimpleGroupCard(
    group: Group,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ComponentShapes.IconBackground)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                getGroupTypeColor(group.type.name.lowercase()),
                                getGroupTypeColor(group.type.name.lowercase()).copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.icon,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "👥 ${group.memberIds.size}명",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondaryLight
            )
        }
    }
}

/**
 * 빈 상태 카드
 */
@Composable
private fun EmptyStateCard(
    icon: String,
    message: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = icon,
                fontSize = 48.sp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryLight
            )
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                ),
                shape = ComponentShapes.PrimaryButton
            ) {
                Text(
                    text = actionText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 오늘 날짜 포맷
 */
private fun getTodayDate(): String {
    val formatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
    return LocalDate.now().format(formatter)
}

/**
 * ✅ 긴급 할일 섹션 - 플랫 디자인으로 수정
 */
@Composable
private fun UrgentTasksSection(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = ErrorRed.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // ✅ 0으로 변경
        border = null // ✅ 테두리 제거
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🚨",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "긴급 할일",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    Badge(
                        containerColor = ErrorRed
                    ) {
                        Text(
                            text = tasks.size.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = ErrorRed.copy(alpha = 0.2f))

            // 할일 목록
            tasks.forEach { task ->
                UrgentTaskItem(
                    task = task,
                    onClick = { onTaskClick(task) }
                )
            }
        }
    }
}

/**
 * ✅ 긴급 할일 아이템 - 플랫 디자인
 */
@Composable
private fun UrgentTaskItem(
    task: Task,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 우선순위 아이콘 (플랫하게)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ErrorRed.copy(alpha = 0.15f)), // ✅ 단순 배경색
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = task.priority.icon,
                fontSize = 20.sp
            )
        }

        // 할일 정보
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight,
                maxLines = 1
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // D-day
                task.dueDate?.let { dueDate ->
                    val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), dueDate).toInt()
                    Surface(
                        shape = ComponentShapes.Chip,
                        color = when {
                            daysUntil < 0 -> ErrorRed.copy(alpha = 0.15f)
                            daysUntil == 0 -> WarningAmber.copy(alpha = 0.15f)
                            else -> ErrorRed.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = when {
                                daysUntil < 0 -> "마감 초과"
                                daysUntil == 0 -> "오늘 마감"
                                else -> "D-$daysUntil"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 화살표
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondaryLight
        )
    }
}