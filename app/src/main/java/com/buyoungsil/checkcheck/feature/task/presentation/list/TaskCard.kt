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
import com.buyoungsil.checkcheck.feature.home.formatDueDateTime
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
    status: String? = null,  // ✨ 추가
    priority: String = "medium",
    dueDate: LocalDate? = null,
    dueTime: LocalTime? = null,
    reminderMinutes: Int? = null,
    assignee: String? = null,
    taskIcon: String = "📋",
    createdBy: String? = null,
    currentUserId: String? = null,
    onCheck: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val priorityColor = getPriorityColor(priority)
    val today = LocalDate.now()
    val now = LocalTime.now()

    // ✨ 승인 대기 상태 확인
    val isWaitingApproval = status == "WAITING_APPROVAL"

    val isOverdue = when {
        dueDate == null -> false
        dueDate < today -> true
        dueDate == today && dueTime != null && dueTime < now -> true
        else -> false
    }

    val daysUntilDue = dueDate?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    val isUrgent = daysUntilDue != null && daysUntilDue <= 2 && daysUntilDue >= 0 && !isOverdue

    val canDelete = createdBy != null && currentUserId != null && createdBy == currentUserId

    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // ✨ 승인 대기 상태일 때 노란색 배경
    val cardColor by animateColorAsState(
        targetValue = when {
            isWaitingApproval -> Color(0xFFFFF9E6)  // 연한 노란색
            isCompleted -> CheckedBackground
            else -> Color.White
        },
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
            // ✨ 승인 대기 시 주황색 사이드 바
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = if (isWaitingApproval) {
                                listOf(
                                    OrangePrimary.copy(alpha = 0.8f),
                                    OrangePrimary
                                )
                            } else {
                                listOf(
                                    priorityColor.copy(alpha = 0.8f),
                                    priorityColor
                                )
                            }
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // 제목 행
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

                        Text(
                            text = taskName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) TextSecondaryLight else TextPrimaryLight,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        )
                    }

                    // ✨ 승인 대기 배지 추가
                    if (isWaitingApproval) {
                        Surface(
                            shape = ComponentShapes.Chip,
                            color = OrangePrimary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🕐",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "승인 대기",
                                    style = CustomTypography.chip,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimary
                                )
                            }
                        }
                    }

                    // 삭제 버튼
                    if (canDelete && onDelete != null && !isCompleted && !isWaitingApproval) {
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
                }

                // 담당자 & 마감일
                if (assignee != null || dueDate != null || reminderMinutes != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 담당자
                        assignee?.let {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TextSecondaryLight
                                )
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }
                        }

                        // 마감일
                        if (dueDate != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isOverdue) ErrorRed else TextSecondaryLight
                                )

                                val deadlineText = formatDueDateTime(dueDate, dueTime)
                                Text(
                                    text = deadlineText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOverdue) ErrorRed else TextSecondaryLight,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        // 알림 설정 배지
                        if (reminderMinutes != null && !isOverdue) {
                            Surface(
                                shape = ComponentShapes.Chip,
                                color = OrangePrimary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = OrangePrimary
                                    )
                                    Text(
                                        text = "${reminderMinutes}분 전",
                                        style = CustomTypography.chip,
                                        color = OrangePrimary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 체크박스
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterVertically)
            ) {
                // ✨ 승인 대기 상태면 체크박스 비활성화
                Checkbox(
                    checked = isCompleted || isWaitingApproval,
                    onCheckedChange = {
                        if (!isWaitingApproval) {  // 승인 대기가 아닐 때만 체크 가능
                            onCheck()
                        }
                    },
                    enabled = !isWaitingApproval,  // 승인 대기 시 비활성화
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isWaitingApproval) OrangePrimary else OrangeSecondary,
                        uncheckedColor = TextSecondaryLight,
                        disabledCheckedColor = OrangePrimary.copy(alpha = 0.6f)  // 비활성화 색상
                    )
                )
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