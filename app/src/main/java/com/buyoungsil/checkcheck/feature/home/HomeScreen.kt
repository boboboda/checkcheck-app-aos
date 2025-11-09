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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.notification.rememberNotificationPermissionState
import com.buyoungsil.checkcheck.core.ui.components.GlassCard
import com.buyoungsil.checkcheck.core.ui.components.GlassIconBackground
import com.buyoungsil.checkcheck.core.ui.components.GlassProgressBar
import com.buyoungsil.checkcheck.feature.group.presentation.list.GlassGroupCard
import com.buyoungsil.checkcheck.feature.habit.presentation.list.GlassHabitCard
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🔥 Glassmorphism 홈 화면
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

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            // 🔥 글라스 탑바
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GlassWhite15,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "체크체크",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    IconButton(onClick = onNavigateToSettings) {
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
                containerColor = GlassWhite25,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "습관 추가")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 오늘 현황 카드
            item {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    val totalHabits = uiState.habits.size
                    val completedHabits = uiState.habits.count { it.isCheckedToday }
                    val progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(durationMillis = 800), label = ""
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "오늘의 습관",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$completedHabits / $totalHabits 완료",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        GlassProgressBar(progress = animatedProgress)

                        Text(
                            text = when {
                                progress >= 1f -> "🎉 완벽해요! 최고예요!"
                                progress >= 0.8f -> "💪 거의 다 왔어요!"
                                progress >= 0.5f -> "🔥 절반 넘었네요!"
                                else -> "✨ 화이팅!"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // 내 습관 섹션
            item {
                GlassSectionHeader(
                    title = "내 습관",
                    count = uiState.habits.size,
                    emoji = "📱",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            if (uiState.habits.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "아직 습관이 없어요",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+ 버튼을 눌러 습관을 추가하세요!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.habits,
                    key = { it.habit.id }
                ) { habitWithStats ->
                    GlassHabitCard(
                        habitWithStats = habitWithStats,
                        onCheck = { viewModel.onHabitCheck(habitWithStats.habit.id) },
                        onDelete = {
                            habitToDelete = habitWithStats.habit.id to habitWithStats.habit.title
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // 내 그룹 섹션
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GlassSectionHeader(
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
                    GlassCard(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "아직 그룹이 없어요",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "그룹을 생성하거나 초대코드로 참여하세요!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.groups.take(3),
                    key = { it.id }
                ) { group ->
                    GlassGroupCard(
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

    // 습관 삭제 다이얼로그
    habitToDelete?.let { (habitId, habitTitle) ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("습관 삭제", color = Color.White) },
            text = { Text("'$habitTitle' 습관을 삭제하시겠습니까?", color = Color.White) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteHabit(habitId)
                        habitToDelete = null
                    }
                ) {
                    Text("삭제", color = CheckError)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("취소", color = Color.White)
                }
            },
            containerColor = GlassWhite20
        )
    }

    // 그룹 나가기 다이얼로그
    groupToLeave?.let { (groupId, groupName) ->
        AlertDialog(
            onDismissRequest = { groupToLeave = null },
            title = { Text("그룹 나가기", color = Color.White) },
            text = { Text("'$groupName' 그룹에서 나가시겠습니까?", color = Color.White) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onLeaveGroup(groupId)
                        groupToLeave = null
                    }
                ) {
                    Text("나가기", color = CheckError)
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToLeave = null }) {
                    Text("취소", color = Color.White)
                }
            },
            containerColor = GlassWhite20
        )
    }
}

/**
 * 🔥 글라스 섹션 헤더
 */
@Composable
private fun GlassSectionHeader(
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
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Surface(
                shape = CircleShape,
                color = GlassWhite25
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}