package com.buyoungsil.checkcheck.feature.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitListViewModel
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitCard
import com.buyoungsil.checkcheck.feature.task.presentation.list.TaskListViewModel
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.group.domain.model.Group
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    habitViewModel: HabitListViewModel = hiltViewModel(),
    taskViewModel: TaskListViewModel = hiltViewModel(),
    onNavigateToHabitCreate: (String?) -> Unit,
    onNavigateToGroupList: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHabitList: () -> Unit,
    onNavigateToPersonalTaskList: () -> Unit,
    onNavigateToPersonalTaskCreate: () -> Unit,
    onNavigateToCoinWallet: () -> Unit,
    onNavigateToDebug: () -> Unit
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val habitUiState by habitViewModel.uiState.collectAsState()
    val taskUiState by taskViewModel.uiState.collectAsState()

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
                    // ✅ HomeViewModel의 totalCoins 사용
                    Surface(
                        modifier = Modifier
                            .clickable { onNavigateToCoinWallet() }
                            .padding(end = 8.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💰", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${homeUiState.totalCoins}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground
                )
            )
        },
        containerColor = OrangeBackground
    ) { paddingValues ->

        if (homeUiState.isLoading || habitUiState.loading || taskUiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                HabitStatisticsCard(habitUiState = habitUiState)
            }

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
                        Text(text = "✅", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "나의 습관",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onNavigateToHabitList) {
                        Text("전체보기")
                    }
                }
            }

            if (habitUiState.habits.isEmpty()) {
                item {
                    EmptyHabitCard(onNavigateToHabitCreate = { onNavigateToHabitCreate(null) })
                }
            } else {
                items(
                    items = habitUiState.habits.take(3),
                    key = { it.habit.id }
                ) { habitWithStats ->
                    HabitCard(
                        habitName = habitWithStats.habit.title,
                        isCompleted = habitWithStats.isCheckedToday,
                        streak = habitWithStats.statistics?.currentStreak ?: 0,
                        completionRate = habitWithStats.statistics?.completionRate ?: 0f,
                        habitIcon = habitWithStats.habit.icon,
                        nextMilestoneInfo = habitWithStats.nextMilestoneInfo,
                        onCheck = {
                            habitViewModel.onHabitCheck(habitWithStats.habit.id)
                        }
                    )
                }
            }

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
                        Text(text = "🔥", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "긴급 할일",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                val urgentTasks = remember(taskUiState.tasks) {
                    taskViewModel.getUrgentTasks()
                }

                if (urgentTasks.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ComponentShapes.TaskCard,
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "긴급한 할일이 없어요 👍",
                            modifier = Modifier.padding(16.dp),
                            color = TextSecondaryLight,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        urgentTasks.take(3).forEach { task ->
                            UrgentTaskCard(
                                taskName = task.title,
                                priority = task.priority,
                                dueDate = task.dueDate,
                                dueTime = task.dueTime,
                                onComplete = { taskViewModel.onCompleteTask(task.id) }
                            )
                        }
                    }
                }
            }

            // ✅ "나의 할일" 헤더 수정 - "전체보기"로 통일
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
                        Text(text = "📝", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "나의 할일",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onNavigateToPersonalTaskList) {
                        Text("전체보기")
                    }
                }
            }

            item {
                val personalTasks = remember(taskUiState.tasks) {
                    taskViewModel.getPersonalTasksOnly()
                }

                if (personalTasks.isEmpty()) {
                    EmptyPersonalTaskCard(onNavigateToPersonalTaskCreate = onNavigateToPersonalTaskCreate)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        personalTasks.take(5).forEach { task ->
                            PersonalTaskCard(
                                taskName = task.title,
                                priority = task.priority,
                                dueDate = task.dueDate,
                                dueTime = task.dueTime,
                                onComplete = { taskViewModel.onCompleteTask(task.id) }
                            )
                        }
                    }
                }
            }

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
                        Text(text = "👥", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "나의 그룹",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onNavigateToGroupList) {
                        Text("전체보기")
                    }
                }
            }

            if (homeUiState.groups.isEmpty()) {
                item {
                    EmptyGroupCard(onNavigateToGroupList = onNavigateToGroupList)
                }
            } else {
                items(
                    items = homeUiState.groups.take(3),
                    key = { it.id }
                ) { group ->
                    GroupItemCard(
                        group = group,
                        onClick = { onNavigateToGroupDetail(group.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun UrgentTaskCard(
    taskName: String,
    priority: TaskPriority,
    dueDate: LocalDate?,
    dueTime: LocalTime?,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.TaskCard,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE5E5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (priority) {
                            TaskPriority.URGENT -> "🚨"
                            TaskPriority.NORMAL -> "📌"
                            TaskPriority.LOW -> "💡"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = taskName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (dueDate != null) {
                    val deadlineText = formatDueDateTime(dueDate, dueTime)
                    Text(
                        text = "⏰ $deadlineText",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF3B3B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Checkbox(
                checked = false,
                onCheckedChange = { onComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = OrangePrimary,
                    uncheckedColor = TextSecondaryLight
                )
            )
        }
    }
}

@Composable
private fun PersonalTaskCard(
    taskName: String,
    priority: TaskPriority,
    dueDate: LocalDate?,
    dueTime: LocalTime?,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.TaskCard,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (priority) {
                            TaskPriority.URGENT -> "🚨"
                            TaskPriority.NORMAL -> "📌"
                            TaskPriority.LOW -> "💡"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = taskName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (dueDate != null) {
                    val deadlineText = formatDueDateTime(dueDate, dueTime)
                    Text(
                        text = "📅 $deadlineText",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            Checkbox(
                checked = false,
                onCheckedChange = { onComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = OrangePrimary,
                    uncheckedColor = TextSecondaryLight
                )
            )
        }
    }
}

@Composable
private fun EmptyHabitCard(onNavigateToHabitCreate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "아직 습관이 없어요",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onNavigateToHabitCreate,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("첫 습관 만들기")
            }
        }
    }
}

@Composable
private fun EmptyPersonalTaskCard(onNavigateToPersonalTaskCreate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.TaskCard,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "할일을 추가해보세요",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onNavigateToPersonalTaskCreate,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("할일 추가하기")
            }
        }
    }
}

@Composable
private fun EmptyGroupCard(onNavigateToGroupList: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "아직 그룹이 없어요",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onNavigateToGroupList,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("그룹 만들기")
            }
        }
    }
}

private fun formatDueDateTime(dueDate: LocalDate, dueTime: LocalTime?): String {
    val deadline = if (dueTime != null) {
        LocalDateTime.of(dueDate, dueTime)
    } else {
        LocalDateTime.of(dueDate, LocalTime.of(23, 59))
    }

    val now = LocalDateTime.now()
    val hours = java.time.Duration.between(now, deadline).toHours()

    return when {
        hours < 0 -> "기한 지남"
        hours < 1 -> "${java.time.Duration.between(now, deadline).toMinutes()}분 후"
        hours < 24 -> "${hours}시간 후"
        else -> "${hours / 24}일 후"
    }
}

@Composable
private fun HabitStatisticsCard(
    habitUiState: com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitListUiState
) {
    val totalHabits = habitUiState.habits.size
    val completedToday = habitUiState.habits.count { it.isCheckedToday }
    val completionRate = if (totalHabits > 0) {
        (completedToday.toFloat() / totalHabits * 100).toInt()
    } else 0

    val avgStreak = if (totalHabits > 0) {
        habitUiState.habits.mapNotNull { it.statistics?.currentStreak }.average().toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.StatCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 오늘의 달성률",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$completionRate%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }

            LinearProgressIndicator(
                progress = { completionRate / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = OrangePrimary,
                trackColor = Color(0xFFFFE5D9)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItemSmall(label = "완료", value = "$completedToday/$totalHabits")
                StatItemSmall(label = "평균 스트릭", value = "${avgStreak}일")
            }
        }
    }
}

@Composable
private fun StatItemSmall(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
        )
    }
}

@Composable
private fun GroupItemCard(
    group: Group,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ComponentShapes.GroupCard,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(OrangePrimary, OrangeSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.icon,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${group.memberIds.size}명",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondaryLight
            )
        }
    }
}

private fun getTodayDate(): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
    return today.format(formatter)
}