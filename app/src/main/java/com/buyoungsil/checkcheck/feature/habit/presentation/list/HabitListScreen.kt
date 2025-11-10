package com.buyoungsil.checkcheck.feature.habit.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 습관 목록 화면 - MZ 오렌지 테마
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    viewModel: HabitListViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "내 습관",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
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
                contentDescription = "습관 추가"
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

                uiState.habits.isEmpty() -> {
                    // 빈 상태
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 80.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
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

                else -> {
                    // 습관 목록
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
                                }
                            }
                        }

                        // 습관 카드들
                        items(
                            items = uiState.habits,
                            key = { it.habit.id }
                        ) { habitWithStats ->
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
                TextButton(
                    onClick = {
                        viewModel.onDeleteHabit(habitId)
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