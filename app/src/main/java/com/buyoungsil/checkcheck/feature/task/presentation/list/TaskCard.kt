package com.buyoungsil.checkcheck.feature.task.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
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
import java.time.temporal.ChronoUnit

/**
 * 🧡 오렌지 테마 할일 카드
 * 우선순위 시각화 + D-day 표시
 */
@Composable
fun TaskCard(
    taskName: String,
    isCompleted: Boolean,
    priority: String = "medium",
    dueDate: LocalDate? = null,
    assignee: String? = null,
    taskIcon: String = "📋",
    onCheck: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val priorityColor = getPriorityColor(priority)
    val today = LocalDate.now()
    val daysUntilDue = dueDate?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    val isOverdue = daysUntilDue != null && daysUntilDue < 0
    val isUrgent = daysUntilDue != null && daysUntilDue <= 2 && daysUntilDue >= 0

    // 완료 시 애니메이션
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
            // 🎨 왼쪽 우선순위 세로 바
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

            // 메인 컨텐츠
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 왼쪽: 아이콘 + 할일명 + 담당자
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 아이콘
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(ComponentShapes.IconBackground)
                                .background(
                                    if (isCompleted) {
                                        OrangeSurfaceVariant
                                    } else {
                                        priorityColor.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = taskIcon,
                                fontSize = 20.sp
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // 할일명
                            Text(
                                text = taskName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                color = if (isCompleted) TextSecondaryLight else TextPrimaryLight,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                            )

                            // 담당자 표시
                            if (assignee != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "👤 $assignee",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }

                    // 오른쪽: 체크박스
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
                                        colors = listOf(UncheckedBackground, UncheckedBackground)
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

                // 하단: 우선순위 + D-day
                if (!isCompleted && (dueDate != null || priority.lowercase() != "low")) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 우선순위 뱃지
                        Surface(
                            shape = ComponentShapes.Badge,
                            color = priorityColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = getPriorityName(priority),
                                style = CustomTypography.chip,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // D-day 표시
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                                        text = when {
                                            daysUntilDue == 0 -> "오늘"
                                            isOverdue -> "${-daysUntilDue}일 지남"
                                            else -> "D-$daysUntilDue"
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
                    }
                }
            }
        }
    }
}

/**
 * 🧡 간단한 할일 카드
 */
@Composable
fun SimpleTaskCard(
    taskName: String,
    isCompleted: Boolean,
    taskIcon: String = "📋",
    onCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .clickable(onClick = onCheck),
        shape = ComponentShapes.TaskCard,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 1.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = taskIcon,
                    fontSize = 20.sp
                )

                Text(
                    text = taskName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color = if (isCompleted) TextSecondaryLight else TextPrimaryLight,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                )
            }

            Icon(
                imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Circle,
                contentDescription = if (isCompleted) "완료" else "미완료",
                tint = if (isCompleted) OrangePrimary else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 우선순위 한글명 반환
 */
private fun getPriorityName(priority: String): String {
    return when (priority.lowercase()) {
        "urgent" -> "🔥 긴급"
        "high" -> "⚡ 높음"
        "medium" -> "📌 보통"
        else -> "⏰ 낮음"
    }
}