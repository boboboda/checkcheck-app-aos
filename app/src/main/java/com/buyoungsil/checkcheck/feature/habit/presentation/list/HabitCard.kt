package com.buyoungsil.checkcheck.feature.habit.presentation.list

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.core.util.IconConverter
import com.buyoungsil.checkcheck.feature.habit.domain.model.HabitMilestones
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 확장 가능한 습관 카드
 *
 * ✨ 클릭 시 카드가 확장되며 액션 버튼 표시
 * ✅ 오늘 체크하기
 * 📊 상세보기
 */
@Composable
fun HabitCard(
    habitName: String,
    isCompleted: Boolean,
    streak: Int = 0,
    completionRate: Float = 0f,
    habitIcon: String = "📝",
    nextMilestoneInfo: NextMilestoneInfo? = null,
    onCheck: () -> Unit,
    onDetailClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayIcon = IconConverter.convertToEmoji(habitIcon)

    val scale by animateFloatAsState(
        targetValue = if (isCompleted && !isExpanded) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isCompleted && !isExpanded) CheckedBackground else Color.White,
        animationSpec = spring(),
        label = "cardColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable {
                if (onDetailClick != null) {
                    isExpanded = !isExpanded
                } else if (!isCompleted) {
                    onCheck()
                }
            }
            .alpha(if (isCompleted && !isExpanded) 0.7f else 1f),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 4.dp else if (isCompleted) 6.dp else 2.dp
        )
    ) {
        Column {
            // 메인 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 아이콘
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(ComponentShapes.IconBackground)
                                .background(
                                    Brush.linearGradient(
                                        colors = if (isCompleted) {
                                            listOf(
                                                OrangePrimary.copy(alpha = 0.8f),
                                                OrangeSecondary.copy(alpha = 0.8f)
                                            )
                                        } else {
                                            listOf(
                                                OrangePrimary.copy(alpha = 0.15f),
                                                OrangeSecondary.copy(alpha = 0.15f)
                                            )
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayIcon,
                                fontSize = 24.sp
                            )
                        }

                        // 텍스트
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = habitName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (streak > 0) {
                                    Text(
                                        text = "🔥 $streak 일 연속",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (completionRate > 0f) {
                                    Text(
                                        text = "${(completionRate * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )
                                }
                            }
                        }
                    }

                    // 체크박스 (확장 안 되었을 때만)
                    if (!isExpanded) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCompleted) {
                                        Brush.linearGradient(
                                            colors = listOf(OrangePrimary, OrangeSecondary)
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                DividerLight,
                                                DividerLight.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 마일스톤 정보
                if (nextMilestoneInfo != null && !isCompleted) {
                    NextMilestoneInfoCard(info = nextMilestoneInfo)
                }

                // 완료 뱃지
                if (isCompleted && !isExpanded) {
                    CompletedBadge()
                }
            }

            // 액션 버튼 (확장 시)
            if (onDetailClick != null) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = DividerLight, thickness = 1.dp)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    onCheck()
                                    isExpanded = false
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCompleted) Color.Gray.copy(0.3f) else OrangePrimary,
                                    contentColor = if (isCompleted) TextSecondaryLight else Color.White
                                ),
                                enabled = !isCompleted,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (isCompleted) "완료됨" else "오늘 체크", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    onDetailClick()
                                    isExpanded = false
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.BarChart, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("상세보기", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ComponentShapes.Chip,
        color = OrangePrimary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✅ 오늘 완료했습니다!",
                style = MaterialTheme.typography.bodySmall,
                color = OrangePrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NextMilestoneInfoCard(info: NextMilestoneInfo, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ComponentShapes.Chip,
        color = OrangePrimary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${info.daysLeft}일 더 하면",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    fontSize = 11.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💰", fontSize = 14.sp)
                    Text(
                        text = "${info.coinsToEarn}코인",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "획득!",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${info.currentStreak}/${info.targetDays}일",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight,
                    fontSize = 10.sp
                )

                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DividerLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(info.progress)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.horizontalGradient(listOf(OrangePrimary, OrangeSecondary)))
                    )
                }
            }
        }
    }
}

// ✅ NextMilestoneInfo 데이터 클래스 - 절대 건드리지 않음!
data class NextMilestoneInfo(
    val currentStreak: Int,
    val targetDays: Int,
    val daysLeft: Int,
    val coinsToEarn: Int,
    val progress: Float
) {
    companion object {
        fun fromCurrentStreak(currentStreak: Int): NextMilestoneInfo? {
            val nextMilestone = HabitMilestones.getNextMilestone(currentStreak)
                ?: return null

            val daysLeft = nextMilestone.days - currentStreak
            val progress = currentStreak.toFloat() / nextMilestone.days.toFloat()

            return NextMilestoneInfo(
                currentStreak = currentStreak,
                targetDays = nextMilestone.days,
                daysLeft = daysLeft,
                coinsToEarn = nextMilestone.coins,
                progress = progress.coerceIn(0f, 1f)
            )
        }
    }
}