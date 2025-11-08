package com.buyoungsil.checkcheck.feature.task.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.feature.task.domain.model.Task
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskPriority
import com.buyoungsil.checkcheck.feature.task.domain.model.TaskStatus
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 개선된 TaskCard
 * - 우선순위 색상 강조
 * - 마감일 D-day 표시
 * - 완료 상태 애니메이션
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED

    // 우선순위별 컬러
    val priorityColor = when (task.priority) {
        TaskPriority.URGENT -> PriorityUrgentColor
        TaskPriority.NORMAL -> PriorityNormalColor
        TaskPriority.LOW -> PriorityLowColor
    }

    // 완료 상태에 따른 애니메이션
    val cardColor by animateColorAsState(
        targetValue = if (isCompleted) CheckSuccess.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(300),
        label = "card_color"
    )

    // D-day 계산
    val dDay = task.dueDate?.let { dueDate ->
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(today, dueDate).toInt()
        when {
            days < 0 -> "D+${-days}"
            days == 0 -> "D-Day"
            else -> "D-$days"
        }
    }

    val isDueSoon = task.dueDate?.let { dueDate ->
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(today, dueDate).toInt()
        days in 0..2 && !isCompleted
    } ?: false

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CheckShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                CheckSuccess.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onComplete
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 우선순위 인디케이터 (세로 바)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(CheckShapes.ProgressBar)
                    .background(priorityColor)
            )

            // 할일 정보
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 우선순위 + 제목
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 우선순위 뱃지
                    Surface(
                        shape = CheckShapes.Chip,
                        color = priorityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = task.priority.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // 제목
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) CheckGray500 else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 설명
                if (!task.description.isNullOrBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) CheckGray400 else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                // 담당자 + 마감일
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 담당자
                    task.assigneeName?.let { name ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "👤", fontSize = 12.sp)
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = CheckGray600
                            )
                        }
                    }

                    // 마감일 + D-day
                    task.dueDate?.let { date ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅",
                                fontSize = 12.sp
                            )
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("M/d")),
                                style = MaterialTheme.typography.labelSmall,
                                color = CheckGray600
                            )

                            // D-day 강조
                            dDay?.let { day ->
                                Surface(
                                    shape = CheckShapes.Chip,
                                    color = if (isDueSoon) {
                                        PriorityUrgentColor.copy(alpha = 0.2f)
                                    } else {
                                        CheckGray200
                                    }
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDueSoon) PriorityUrgentColor else CheckGray600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 완료 체크
            IconButton(
                onClick = onComplete,
                modifier = Modifier.size(40.dp),
                enabled = !isCompleted
            ) {
                Icon(
                    imageVector = if (isCompleted) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.CheckCircle
                    },
                    contentDescription = if (isCompleted) "완료됨" else "미완료",
                    tint = if (isCompleted) CheckSuccess else CheckGray400,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 삭제 버튼
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = CheckGray400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}