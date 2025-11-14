// TaskListScreen.kt - 기존 코드에 최소한만 수정
// 수정 사항:
// 1. 함수 시그니처에 groupId, onNavigateBack 파라미터 추가
// 2. TopAppBar에 뒤로가기 버튼만 추가
// 3. 나머지는 모두 기존 코드 유지

package com.buyoungsil.checkcheck.feature.task.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack  // ✅ 추가
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.OrangeFAB
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 할일 목록 화면 - MZ 오렌지 테마
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    groupId: String? = null,  // ✅ 추가 (null이면 개인 모드)
    viewModel: TaskListViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateBack: (() -> Unit)? = null  // ✅ 추가 (뒤로가기 콜백)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // ✅ 개인 모드 여부 확인
    val isPersonalMode = groupId == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isPersonalMode) "나의 할일" else "그룹 할일",  // ✅ 수정
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                },
                // ✅ 추가: 뒤로가기 버튼
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight
                )
            )
        },
        containerColor = OrangeBackground,
        floatingActionButton = {
            OrangeFAB(
                onClick = onNavigateToCreate,
                icon = Icons.Default.Add,
                contentDescription = "할일 추가"
            )
        }
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = OrangePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "불러오는 중...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight
                            )
                        }
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
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = uiState.error ?: "오류가 발생했어요",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "다시 시도해주세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.onRetry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("다시 시도")
                        }
                    }
                }

                uiState.tasks.isEmpty() -> {
                    // 빈 상태
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "✅",
                            fontSize = 80.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (isPersonalMode) "아직 나의 할일이 없어요" else "아직 할일이 없어요",  // ✅ 수정
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "첫 할일을 만들어보세요!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onNavigateToCreate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("할일 추가하기")
                        }
                    }
                }

                else -> {
                    // 할일 목록 - 기존 코드 그대로 유지
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 상태별 그룹핑
                        val pendingTasks = uiState.tasks.filter { it.status == TaskStatus.PENDING }
                        val inProgressTasks = uiState.tasks.filter { it.status == TaskStatus.IN_PROGRESS }
                        val completedTasks = uiState.tasks.filter { it.status == TaskStatus.COMPLETED }

                        // 헤더
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ComponentShapes.TaskCard,
                                colors = CardDefaults.cardColors(
                                    containerColor = androidx.compose.ui.graphics.Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "✅ 총 ${uiState.tasks.size}개의 할일",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        StatusChip(
                                            label = "진행중",
                                            count = inProgressTasks.size,
                                            color = OrangePrimary
                                        )
                                        StatusChip(
                                            label = "대기",
                                            count = pendingTasks.size,
                                            color = WarningAmber
                                        )
                                        StatusChip(
                                            label = "완료",
                                            count = completedTasks.size,
                                            color = SuccessOrange
                                        )
                                    }
                                }
                            }
                        }

                        // 🔄 진행 중
                        if (inProgressTasks.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    icon = "🔄",
                                    title = "진행 중",
                                    count = inProgressTasks.size
                                )
                            }
                            items(
                                items = inProgressTasks,
                                key = { it.id }
                            ) { task ->
                                TaskCard(
                                    taskName = task.title,
                                    isCompleted = task.status == TaskStatus.COMPLETED,
                                    priority = task.priority.name.lowercase(),
                                    dueDate = task.dueDate,
                                    assignee = task.assigneeName,
                                    onCheck = { viewModel.onCompleteTask(task.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        // ⏰ 대기 중
                        if (pendingTasks.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    icon = "⏰",
                                    title = "대기 중",
                                    count = pendingTasks.size
                                )
                            }
                            items(
                                items = pendingTasks,
                                key = { it.id }
                            ) { task ->
                                TaskCard(
                                    taskName = task.title,
                                    isCompleted = task.status == TaskStatus.COMPLETED,
                                    priority = task.priority.name.lowercase(),
                                    dueDate = task.dueDate,
                                    assignee = task.assigneeName,
                                    onCheck = { viewModel.onCompleteTask(task.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        // ✅ 완료
                        if (completedTasks.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    icon = "✅",
                                    title = "완료",
                                    count = completedTasks.size
                                )
                            }
                            items(
                                items = completedTasks,
                                key = { it.id }
                            ) { task ->
                                TaskCard(
                                    taskName = task.title,
                                    isCompleted = true,
                                    priority = task.priority.name.lowercase(),
                                    dueDate = task.dueDate,
                                    assignee = task.assigneeName,
                                    onCheck = { viewModel.onCompleteTask(task.id) }
                                )
                            }
                        }

                        // 하단 여백
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // 삭제 확인 다이얼로그
    showDeleteDialog?.let { taskId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    text = "할일 삭제",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("정말 이 할일을 삭제하시겠어요?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteTask(taskId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Text(
                        text = "삭제",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 📌 섹션 헤더
 */
@Composable
private fun SectionHeader(
    icon: String,
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondaryLight
        )
    }
}

/**
 * 상태 칩
 */
@Composable
private fun StatusChip(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = ComponentShapes.Chip,
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}