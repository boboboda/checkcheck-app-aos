package com.buyoungsil.checkcheck.feature.habit.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.OrangeFAB
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 습관 목록 화면
 * ✨ SwipeToDismissBox로 스와이프 삭제 구현
 * ✅ 로딩 처리 개선 (로딩 → 데이터 없음/리스트)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    viewModel: HabitListViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "습관 관리",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight,
                    navigationIconContentColor = TextPrimaryLight
                )
            )
        },
        floatingActionButton = {
            // ✅ 로딩 중이 아닐 때만 FAB 표시
            if (!uiState.loading) {
                OrangeFAB(
                    onClick = onNavigateToCreate,
                    icon = Icons.Default.Add,
                    contentDescription = "습관 추가"
                )
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
                // ✅ 1순위: 로딩 중
                uiState.loading -> {
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
                                text = "습관을 불러오는 중...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }

                // ✅ 2순위: 에러
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "오류가 발생했어요",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.onRetry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary
                            )
                        ) {
                            Text("다시 시도")
                        }
                    }
                }

                // ✅ 3순위: 데이터 없음 (로딩 완료 후)
                !uiState.loading && uiState.habits.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📝",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "아직 습관이 없어요",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "첫 습관을 만들어보세요!",
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
                            Text("습관 추가하기")
                        }
                    }
                }

                // ✅ 4순위: 데이터 있음 (로딩 완료 후)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 헤더
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ComponentShapes.HabitCard,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
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
                                        text = "💪 총 ${uiState.habits.size}개의 습관",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    Text(
                                        text = "매일 꾸준히 실천해보세요!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondaryLight
                                    )
                                    Text(
                                        text = "💡 팁: 습관 카드를 왼쪽으로 스와이프해서 삭제할 수 있어요",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // ✨ 스와이프로 삭제 가능한 습관 카드들
                        items(
                            items = uiState.habits,
                            key = { it.habit.id }
                        ) { habitWithStats ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            showDeleteDialog = habitWithStats.habit.id
                                            false // 다이얼로그 확인 후 삭제
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
                                    // 스와이프 배경 (삭제 표시)
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
                                    HabitCard(
                                        habitName = habitWithStats.habit.title,
                                        isCompleted = habitWithStats.isCheckedToday,
                                        streak = habitWithStats.statistics?.currentStreak ?: 0,
                                        completionRate = habitWithStats.statistics?.completionRate ?: 0f,
                                        habitIcon = habitWithStats.habit.icon,
                                        onCheck = {
                                            viewModel.onHabitCheck(habitWithStats.habit.id)
                                        }
                                    )
                                }
                            )
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
    showDeleteDialog?.let { habitId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    text = "습관 삭제",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("정말 이 습관을 삭제하시겠어요?\n체크 기록도 함께 삭제됩니다.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteHabit(habitId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {
                    Text("삭제")
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