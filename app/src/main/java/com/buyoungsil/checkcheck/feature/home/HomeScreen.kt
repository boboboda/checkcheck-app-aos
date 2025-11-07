package com.buyoungsil.checkcheck.feature.home

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.notification.rememberNotificationPermissionState
import com.buyoungsil.checkcheck.core.ui.components.HabitCard
import com.buyoungsil.checkcheck.feature.group.presentation.list.GroupCard

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

    // ✨ 삭제/탈퇴 확인 다이얼로그 상태
    var habitToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // (id, title)
    var groupToLeave by remember { mutableStateOf<Pair<String, String>?>(null) } // (id, name)

    // 앱 시작 시 알림 권한 자동 요청 (Android 13+만)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!permissionState.hasPermission) {
                permissionState.requestPermission()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("체크체크")
                        if (uiState.todayTotalCount > 0) {
                            Text(
                                text = "오늘 ${uiState.todayCompletedCount}/${uiState.todayTotalCount} 완료 🎉",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "설정")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToHabitCreate) {
                Icon(Icons.Default.Add, "습관 추가")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "오류",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.onRetry() }) {
                            Text("다시 시도")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 내 습관 섹션
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📌 내 습관 (${uiState.habits.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (uiState.habits.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "아직 습관이 없어요",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "+ 버튼을 눌러 첫 습관을 만들어보세요!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(uiState.habits) { habitWithStats ->
                                HabitCard(
                                    habitWithStats = habitWithStats,
                                    onCheck = { viewModel.onHabitCheck(habitWithStats.habit.id) },
                                    onDelete = {
                                        habitToDelete = habitWithStats.habit.id to habitWithStats.habit.title
                                    }
                                )
                            }
                        }

                        // 내 그룹 섹션
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "👥 내 그룹 (${uiState.groups.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = onNavigateToGroupList) {
                                    Text("전체 보기")
                                }
                            }
                        }

                        if (uiState.groups.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "아직 그룹이 없어요",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "그룹을 만들거나 초대받아보세요!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(uiState.groups) { group ->
                                GroupCard(
                                    group = group,
                                    onClick = { onNavigateToGroupDetail(group.id) },
                                    onLeave = {
                                        groupToLeave = group.id to group.name
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ✨ 습관 삭제 확인 다이얼로그
    habitToDelete?.let { (habitId, habitTitle) ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            title = { Text("습관 삭제") },
            text = { Text("'$habitTitle' 습관을 삭제하시겠습니까?\n모든 체크 기록도 함께 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteHabit(habitId)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    // ✨ 그룹 탈퇴 확인 다이얼로그
    groupToLeave?.let { (groupId, groupName) ->
        AlertDialog(
            onDismissRequest = { groupToLeave = null },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            title = { Text("그룹 탈퇴") },
            text = { Text("'$groupName' 그룹에서 탈퇴하시겠습니까?\n그룹 습관은 더 이상 볼 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onLeaveGroup(groupId)
                        groupToLeave = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("탈퇴")
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