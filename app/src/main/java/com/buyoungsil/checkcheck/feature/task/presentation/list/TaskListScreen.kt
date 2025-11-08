package com.buyoungsil.checkcheck.feature.task.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("그룹 할일") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, "할일 추가")
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

                uiState.tasks.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "아직 할일이 없어요",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "첫 할일을 만들어보세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ 상태별 그룹핑 추가
                        val pendingTasks = uiState.tasks.filter { it.status == TaskStatus.PENDING }
                        val inProgressTasks = uiState.tasks.filter { it.status == TaskStatus.IN_PROGRESS }
                        val completedTasks = uiState.tasks.filter { it.status == TaskStatus.COMPLETED }

                        // 진행 중
                        if (inProgressTasks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "🔄 진행 중",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(inProgressTasks) { task ->
                                TaskCard(
                                    task = task,
                                    onComplete = { viewModel.onCompleteTask(task.id) },
                                    onDelete = { viewModel.onDeleteTask(task.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        // 대기 중
                        if (pendingTasks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "📋 대기 중",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(pendingTasks) { task ->
                                TaskCard(
                                    task = task,
                                    onComplete = { viewModel.onCompleteTask(task.id) },
                                    onDelete = { viewModel.onDeleteTask(task.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        // 완료
                        if (completedTasks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "✅ 완료",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            items(completedTasks) { task ->
                                TaskCard(
                                    task = task,
                                    onComplete = { viewModel.onCompleteTask(task.id) },
                                    onDelete = { viewModel.onDeleteTask(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
