package com.buyoungsil.checkcheck.feature.home

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.notification.rememberNotificationPermissionState
import com.buyoungsil.checkcheck.core.ui.components.HabitCard
import com.buyoungsil.checkcheck.feature.group.presentation.list.GroupCard
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 진짜 MZ감성 홈 화면
 * - 그라데이션 헤더
 * - 엣지 투 엣지
 * - 글래스모피즘
 * - 더 과감한 컬러
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToHabitCreate: () -> Unit,
    onNavigateToGroupList: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberNotificationPermissionState()

    var habitToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var groupToLeave by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!permissionState.hasPermission) {
                permissionState.requestPermission()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ✨ 그라데이션 배경 (퍼플 계열)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7C4DFF),  // 퍼플
                            Color(0xFFB388FF),  // 라이트 퍼플
                            Color.White
                        ),
                        startY = 0f,
                        endY = 800f
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // ✨ 그라데이션 탑바
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .statusBarsPadding()
                    ,
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "첵첵",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "오늘도 함께 성장해요 🌱",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "설정",
                                tint = Color.White
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToHabitCreate,
                    shape = CircleShape,
                    containerColor = CheckPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "습관 추가",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    uiState.error != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "😢", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "오류",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.onRetry() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = CheckPrimary
                                )
                            ) {
                                Text("다시 시도")
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // ✨ 글래스모피즘 진행률 카드
                            if (uiState.todayTotalCount > 0) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    GlassmorphismProgressCard(
                                        completed = uiState.todayCompletedCount,
                                        total = uiState.todayTotalCount,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            }

                            // 섹션 간격
                            item { Spacer(modifier = Modifier.height(8.dp)) }

                            // 내 습관 섹션
                            item {
                                MZSectionHeader(
                                    title = "내 습관",
                                    count = uiState.habits.size,
                                    emoji = "🎯",
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }

                            if (uiState.habits.isEmpty()) {
                                item {
                                    MZEmptyCard(
                                        emoji = "🌟",
                                        title = "첫 습관을 만들어보세요",
                                        description = "작은 습관이 큰 변화를 만들어요",
                                        buttonText = "습관 만들기",
                                        onButtonClick = onNavigateToHabitCreate,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            } else {
                                items(
                                    items = uiState.habits,
                                    key = { it.habit.id }  // ✨ 각 습관의 고유 ID를 key로 사용
                                ) { habitWithStats ->
                                    HabitCard(
                                        habitWithStats = habitWithStats,
                                        onCheck = { viewModel.onHabitCheck(habitWithStats.habit.id) },
                                        onDelete = {
                                            habitToDelete = habitWithStats.habit.id to habitWithStats.habit.title
                                        },
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            }

                            // 섹션 간격
                            item { Spacer(modifier = Modifier.height(8.dp)) }

                            // 내 그룹 섹션
                            item {
                                MZSectionHeader(
                                    title = "내 그룹",
                                    count = uiState.groups.size,
                                    emoji = "👥",
                                    actionText = "전체보기",
                                    onActionClick = onNavigateToGroupList,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }

                            if (uiState.groups.isEmpty()) {
                                item {
                                    MZEmptyCard(
                                        emoji = "💕",
                                        title = "함께할 그룹이 필요해요",
                                        description = "가족, 친구들과 함께 성장하세요",
                                        buttonText = "그룹 보기",
                                        onButtonClick = onNavigateToGroupList,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            } else {
                                items(uiState.groups.take(3)) { group ->
                                    GroupCard(
                                        group = group,
                                        onClick = { onNavigateToGroupDetail(group.id) },
                                        onLeave = {
                                            groupToLeave = group.id to group.name
                                        },
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 다이얼로그들
    habitToDelete?.let { (id, title) ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("습관 삭제") },
            text = { Text("'$title' 습관을 삭제하시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteHabit(id)
                        habitToDelete = null
                    }
                ) {
                    Text("삭제", color = CheckError)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    groupToLeave?.let { (id, name) ->
        AlertDialog(
            onDismissRequest = { groupToLeave = null },
            title = { Text("그룹 나가기") },
            text = { Text("'$name' 그룹에서 나가시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onLeaveGroup(id)
                        groupToLeave = null
                    }
                ) {
                    Text("나가기", color = CheckError)
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToLeave = null }) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * ✨ 글래스모피즘 진행률 카드
 */
@Composable
private fun GlassmorphismProgressCard(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000),
        label = "progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CheckShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "오늘의 달성률",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CheckGray900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completed 개 완료 / $total 개",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CheckGray600
                    )
                }

                // 큰 퍼센트
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = CheckPrimary
                )
            }

            // 그라데이션 진행률 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(CheckShapes.ProgressBar)
                    .background(CheckGray100)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CheckShapes.ProgressBar)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    CheckPrimary,
                                    CheckSecondary
                                )
                            )
                        )
                )
            }

            // 격려 메시지
            Text(
                text = when {
                    progress >= 1f -> "🎉 완벽해요! 최고예요!"
                    progress >= 0.8f -> "💪 거의 다 왔어요!"
                    progress >= 0.5f -> "🔥 절반 넘었네요!"
                    else -> "✨ 화이팅!"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = CheckPrimary
            )
        }
    }
}

/**
 * ✨ MZ 스타일 섹션 헤더
 */
@Composable
private fun MZSectionHeader(
    title: String,
    count: Int,
    emoji: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Surface(
                shape = CircleShape,
                color = CheckPrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = CheckPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    fontWeight = FontWeight.Bold,
                    color = CheckPrimary
                )
            }
        }
    }
}

/**
 * ✨ MZ 스타일 빈 상태
 */
@Composable
private fun MZEmptyCard(
    emoji: String,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CheckShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = CheckBgTertiary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 72.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = CheckGray600
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onButtonClick,
                shape = CheckShapes.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CheckPrimary
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}