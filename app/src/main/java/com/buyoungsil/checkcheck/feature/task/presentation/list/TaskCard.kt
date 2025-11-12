package com.buyoungsil.checkcheck.feature.task.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 🧡 오렌지 테마 할일 카드
 * ✅ 마감 시간, 알림 설정 표시 추가
 * ✅ 시간 기반 마감 초과 판정 추가
 * ✅ 마감 초과 시 알림 배지 숨김
 * ✅ 삭제 기능 추가 (본인 작성만)
 */
@Composable
fun TaskCard(
    taskName: String,
    isCompleted: Boolean,
    priority: String = "medium",
    dueDate: LocalDate? = null,
    dueTime: LocalTime? = null,
    reminderMinutes: Int? = null,
    assignee: String? = null,
    taskIcon: String = "📋",
    createdBy: String? = null,  // ✅ 추가
    currentUserId: String? = null,  // ✅ 추가
    onCheck: () -> Unit,
    onDelete: (() -> Unit)? = null,  // ✅ 추가
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val priorityColor = getPriorityColor(priority)
    val today = LocalDate.now()
    val now = LocalTime.now()

    val isOverdue = when {
        dueDate == null -> false
        dueDate < today -> true
        dueDate == today && dueTime != null && dueTime < now -> true
        else -> false
    }

    val daysUntilDue = dueDate?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    val isUrgent = daysUntilDue != null && daysUntilDue <= 2 && daysUntilDue >= 0 && !isOverdue

    // ✅ 본인이 작성한 것인지 확인
    val canDelete = createdBy != null && currentUserId != null && createdBy == currentUserId

    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isCompleted) CheckedBackground else Color.White,
        animationSpec = spring(),
        label = "cardColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick),
        shape = ComponentShapes.TaskCard,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 1.dp else 3.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                priorityColor.copy(alpha = 0.8f),
                                priorityColor
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = taskIcon,
                            fontSize = 20.sp
                        )

                        Column {
                            Text(
                                text = taskName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Bold,
                                color = if (isCompleted) TextSecondaryLight else TextPrimaryLight,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                            )

                            if (assignee != null && !isCompleted) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "👤 $assignee",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ✅ 삭제 버튼 (본인 작성만)
                        if (canDelete && onDelete != null) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "삭제",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 체크박스
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    if (isCompleted) {
                                        Brush.linearGradient(
                                            colors = listOf(OrangePrimary, OrangeSecondary)
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                UncheckedBackground,
                                                UncheckedBackground
                                            )
                                        )
                                    }
                                )
                                .clickable(onClick = onCheck),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "완료",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = ComponentShapes.Badge,
                            color = priorityColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = getPriorityName(priority),
                                style = CustomTypography.chip,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (dueDate != null) {
                            Surface(
                                shape = ComponentShapes.Badge,
                                color = when {
                                    isOverdue -> ErrorRed.copy(alpha = 0.15f)
                                    isUrgent -> WarningAmber.copy(alpha = 0.15f)
                                    else -> InfoBlue.copy(alpha = 0.15f)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when {
                                            isOverdue -> "⚠️"
                                            isUrgent -> "⏰"
                                            else -> "📅"
                                        },
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = buildString {
                                            when {
                                                isOverdue && daysUntilDue != null && daysUntilDue < 0 -> {
                                                    append("${-daysUntilDue}일 지남")
                                                }
                                                isOverdue && daysUntilDue == 0 -> {
                                                    append("마감 초과")
                                                }
                                                daysUntilDue == 0 -> {
                                                    append("오늘")
                                                }
                                                else -> {
                                                    append("D-$daysUntilDue")
                                                }
                                            }

                                            if (dueTime != null) {
                                                append(" ${dueTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                                            }
                                        },
                                        style = CustomTypography.chip,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isOverdue -> ErrorRed
                                            isUrgent -> WarningAmber
                                            else -> InfoBlue
                                        }
                                    )
                                }
                            }
                        }

                        if (reminderMinutes != null && reminderMinutes > 0 && !isOverdue) {
                            Surface(
                                shape = ComponentShapes.Badge,
                                color = OrangePrimary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = when {
                                            reminderMinutes >= 1440 -> "${reminderMinutes / 1440}일 전"
                                            reminderMinutes >= 60 -> "${reminderMinutes / 60}시간 전"
                                            else -> "${reminderMinutes}분 전"
                                        },
                                        style = CustomTypography.chip,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangePrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * 우선순위 이름 반환
 */
private fun getPriorityName(priority: String): String {
    return when (priority) {
        "urgent" -> "🚨 긴급"
        "high" -> "⚡ 높음"
        "normal" -> "📌 보통"
        "low" -> "💤 낮음"
        else -> "📌 보통"
    }
}