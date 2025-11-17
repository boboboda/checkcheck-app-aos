package com.buyoungsil.checkcheck.feature.habit.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 습관 상세 화면
 *
 * ✅ 습관 기본 정보 표시
 * ✅ 통계 정보 (연속 기록, 달성률)
 * ✅ 그룹 공유 토글 (핵심!)
 * ✅ 습관 삭제
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    viewModel: HabitDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 삭제 성공 시 화면 닫기
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    // 저장 성공 메시지
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            snackbarHostState.showSnackbar("✅ 변경사항이 저장되었습니다")
        }
    }

    // 에러 메시지
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "습관 상세",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onShowDeleteDialog(true) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = Color.Red
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
        containerColor = OrangeBackground
    ) { paddingValues ->
        when {
            // 로딩 중
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }

            // 데이터 로드됨
            uiState.habit != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. 습관 기본 정보
                    HabitInfoCard(
                        habit = uiState.habit!!,
                        statistics = uiState.statistics
                    )

                    // 2. 그룹 공유 설정 (핵심!)
                    GroupShareCard(
                        groupShared = uiState.habit!!.groupShared,
                        availableGroups = uiState.availableGroups,
                        selectedGroup = uiState.selectedGroup,
                        onGroupSharedToggle = viewModel::onGroupSharedToggle,
                        onGroupSelect = viewModel::onGroupSelect
                    )

                    // 3. 저장 버튼
                    Button(
                        onClick = { viewModel.onSaveChanges() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary
                        ),
                        enabled = !uiState.isUpdating
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (uiState.isUpdating) "저장 중..." else "변경사항 저장",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // 삭제 확인 다이얼로그
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onShowDeleteDialog(false) },
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
                    onClick = { viewModel.onDeleteHabit() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onShowDeleteDialog(false) }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 습관 기본 정보 카드
 */
@Composable
private fun HabitInfoCard(
    habit: com.buyoungsil.checkcheck.feature.habit.domain.model.Habit,
    statistics: com.buyoungsil.checkcheck.feature.habit.domain.model.HabitStatistics?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아이콘과 제목
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = habit.icon,
                    style = MaterialTheme.typography.displaySmall
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    if (!habit.description.isNullOrBlank()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                }
            }

            Divider(color = DividerLight)

            // 통계 정보
            if (statistics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 연속 기록
                    StatItem(
                        label = "연속 기록",
                        value = "${statistics.currentStreak}일",
                        icon = "🔥"
                    )

                    // 달성률
                    StatItem(
                        label = "달성률",
                        value = "${(statistics.completionRate * 100).toInt()}%",
                        icon = "📊"
                    )

                    // 총 완료
                    StatItem(
                        label = "총 체크",
                        value = "${statistics.totalChecks}회",  // ✅ 올바른 필드
                        icon = "✅"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OrangePrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
        )
    }
}

@Composable
private fun GroupShareCard(
    groupShared: Boolean,
    availableGroups: List<Group>,
    selectedGroup: Group?,
    onGroupSharedToggle: (Boolean) -> Unit,
    onGroupSelect: (Group) -> Unit
) {
    // 🔍 디버깅 로그
    LaunchedEffect(availableGroups) {
        android.util.Log.d("GroupShareCard", "=== GroupShareCard 렌더링 ===")
        android.util.Log.d("GroupShareCard", "groupShared: $groupShared")
        android.util.Log.d("GroupShareCard", "availableGroups: ${availableGroups.size}개")
        availableGroups.forEach { group ->
            android.util.Log.d("GroupShareCard", "- ${group.name} (${group.id})")
        }
        android.util.Log.d("GroupShareCard", "selectedGroup: ${selectedGroup?.name}")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "👥 그룹 공유 설정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            // 그룹 공유 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "그룹에 공유",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = if (groupShared) "그룹원들이 내 습관을 볼 수 있어요"
                        else "나만 볼 수 있는 습관이에요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }

                Switch(
                    checked = groupShared,
                    onCheckedChange = onGroupSharedToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OrangePrimary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = DividerLight
                    )
                )
            }

            // 🔍 디버깅용 임시 표시
            Text(
                text = "디버그: 그룹 ${availableGroups.size}개 로딩됨",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red
            )

            // 그룹 선택
            if (groupShared) {
                Divider(color = DividerLight)

                if (availableGroups.isEmpty()) {
                    Text(
                        text = "⚠️ 가입한 그룹이 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryLight
                    )
                } else {
                    Text(
                        text = "공유할 그룹 선택",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryLight
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableGroups.forEach { group ->
                            GroupSelectItem(
                                group = group,
                                isSelected = group.id == selectedGroup?.id,
                                onClick = { onGroupSelect(group) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun GroupSelectItem(
    group: Group,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) OrangePrimary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.icon,
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) OrangePrimary else TextPrimaryLight
                )
                Text(
                    text = "${group.memberIds.size}명",  // ✅ memberIds.size 사용
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleLarge,
                    color = OrangePrimary
                )
            }
        }
    }
}