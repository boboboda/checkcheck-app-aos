package com.buyoungsil.checkcheck.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 개선된 HabitCard - Material Icons 사용
 * - MZ감성 귀여운 디자인
 * - 체크 시 만족스러운 애니메이션
 * - 스트릭 불꽃 그라데이션
 * - 둥글둥글한 모서리
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitWithStats: HabitWithStats,
    onCheck: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStats.habit
    val stats = habitWithStats.statistics
    val isChecked = habitWithStats.isCheckedToday

    // 체크 상태에 따른 애니메이션
    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "card_scale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isChecked) CheckPrimaryLight.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(300),
        label = "card_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = CheckShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) {
                CheckPrimaryLight.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        ),
        onClick = onCheck
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아이콘 (원형 배경) - Material Icon으로 변경
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isChecked) {
                            Brush.linearGradient(
                                colors = listOf(
                                    CheckPrimaryLight,
                                    CheckPrimary
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    CheckGray100,
                                    CheckGray200
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Material Icon 표시
                Icon(
                    imageVector = getHabitIcon(habit.icon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (isChecked) Color.White else CheckPrimary
                )
            }

            // 습관 정보
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) CheckPrimary else MaterialTheme.colorScheme.onSurface
                )

                if (habit.description != null && habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // 통계 정보 (스트릭, 완료 횟수)
                if (stats != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 스트릭
                        if (stats.currentStreak > 0) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${stats.currentStreak}일",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CheckOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 이번 달 달성 횟수
                        Text(
                            text = "이번 달 ${stats.thisMonthChecks}회",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ✨ 체크 버튼 - 만족스러운 디자인
            Surface(
                modifier = Modifier
                    .size(48.dp),
                shape = CircleShape,
                color = if (isChecked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shadowElevation = if (isChecked) 4.dp else 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 체크 아이콘
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isChecked,
                        enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "완료",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 미체크 상태 아이콘
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isChecked,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "미완료",
                            tint = CheckGray400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * habit.icon key에서 Material Icon을 가져오는 함수
 */
private fun getHabitIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        // 생활
        "water_drop" -> Icons.Rounded.WaterDrop
        "notifications" -> Icons.Rounded.Notifications
        "calendar" -> Icons.Rounded.CalendarToday
        "schedule" -> Icons.Rounded.Schedule
        "home" -> Icons.Rounded.Home
        "lightbulb" -> Icons.Rounded.Lightbulb
        "note" -> Icons.Rounded.Note
        "phone" -> Icons.Rounded.Phone
        "yard" -> Icons.Rounded.Yard
        "book" -> Icons.Rounded.Book
        "coffee" -> Icons.Rounded.Coffee
        "eco" -> Icons.Rounded.Eco

        // 건강
        "favorite" -> Icons.Rounded.Favorite
        "monitor_heart" -> Icons.Rounded.MonitorHeart
        "apple" -> Icons.Rounded.LocalDining
        "local_hospital" -> Icons.Rounded.LocalHospital
        "medication" -> Icons.Rounded.Medication
        "hotel" -> Icons.Rounded.Hotel
        "psychology" -> Icons.Rounded.Psychology
        "sentiment" -> Icons.Rounded.SentimentSatisfied
        "visibility" -> Icons.Rounded.Visibility
        "volunteer" -> Icons.Rounded.VolunteerActivism
        "thermostat" -> Icons.Rounded.Thermostat
        "vaccines" -> Icons.Rounded.Vaccines

        // 운동
        "fitness_center" -> Icons.Rounded.FitnessCenter
        "directions_bike" -> Icons.Rounded.DirectionsBike
        "directions_run" -> Icons.Rounded.DirectionsRun
        "directions_walk" -> Icons.Rounded.DirectionsWalk
        "pool" -> Icons.Rounded.Pool
        "self_improvement" -> Icons.Rounded.SelfImprovement
        "basketball" -> Icons.Rounded.SportsBasketball
        "soccer" -> Icons.Rounded.SportsSoccer
        "tennis" -> Icons.Rounded.SportsTennis
        "martial_arts" -> Icons.Rounded.SportsMartialArts
        "sports_score" -> Icons.Rounded.SportsScore
        "timer" -> Icons.Rounded.Timer

        // 공부
        "menu_book" -> Icons.Rounded.MenuBook
        "school" -> Icons.Rounded.School
        "edit" -> Icons.Rounded.Edit
        "create" -> Icons.Rounded.Create
        "backpack" -> Icons.Rounded.Backpack
        "workspace_premium" -> Icons.Rounded.WorkspacePremium
        "calculate" -> Icons.Rounded.Calculate
        "science" -> Icons.Rounded.Science
        "public" -> Icons.Rounded.Public
        "functions" -> Icons.Rounded.Functions
        "biotech" -> Icons.Rounded.Biotech
        "track_changes" -> Icons.Rounded.TrackChanges

        // 취미
        "palette" -> Icons.Rounded.Palette
        "music_note" -> Icons.Rounded.MusicNote
        "piano" -> Icons.Rounded.Piano
        "sports_esports" -> Icons.Rounded.SportsEsports
        "camera_alt" -> Icons.Rounded.CameraAlt
        "movie" -> Icons.Rounded.Movie
        "brush" -> Icons.Rounded.Brush
        "headphones" -> Icons.Rounded.Headphones
        "mic" -> Icons.Rounded.Mic
        "extension" -> Icons.Rounded.Extension
        "celebration" -> Icons.Rounded.Celebration
        "interests" -> Icons.Rounded.Interests

        // 관계
        "groups" -> Icons.Rounded.Groups
        "person" -> Icons.Rounded.Person
        "handshake" -> Icons.Rounded.Handshake
        "forum" -> Icons.Rounded.Forum
        "email" -> Icons.Rounded.Email
        "card_giftcard" -> Icons.Rounded.CardGiftcard
        "emoji_emotions" -> Icons.Rounded.EmojiEmotions
        "waving_hand" -> Icons.Rounded.WavingHand
        "videocam" -> Icons.Rounded.Videocam
        "cake" -> Icons.Rounded.Cake
        "loyalty" -> Icons.Rounded.Loyalty
        "diversity" -> Icons.Rounded.Diversity3

        // 기본값 (이모지 또는 알 수 없는 key)
        else -> Icons.Rounded.Notifications
    }
}