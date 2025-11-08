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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitWithStats
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 심플하고 깔끔한 HabitCard
 * - 완료 시: 은은한 배경색 + 초록 체크
 * - 과한 효과 제거
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CheckShapes.HabitCard,
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) {
                CheckSuccess.copy(alpha = 0.08f)  // 은은한 초록 배경
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp  // 고정된 elevation
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
            // 아이콘
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isChecked) {
                            Brush.linearGradient(
                                colors = listOf(
                                    CheckSuccess,
                                    CheckSuccess.copy(alpha = 0.8f)
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
                    color = if (isChecked) {
                        CheckSuccess  // 완료 시 초록색
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (habit.description != null && habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // 통계 정보
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

                        // 이번 달 횟수
                        Text(
                            text = "이번 달 ${stats.thisMonthChecks}회",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 체크 버튼 (심플하게)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isChecked) {
                            CheckSuccess  // 완료: 초록색
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant  // 미완료: 회색
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 체크 아이콘
                androidx.compose.animation.AnimatedVisibility(
                    visible = isChecked,
                    enter = scaleIn(spring(dampingRatio = 0.6f)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "완료",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 빈 원 아이콘
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isChecked,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Circle,
                        contentDescription = "미완료",
                        tint = CheckGray400,
                        modifier = Modifier.size(28.dp)
                    )
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
        "sports_soccer" -> Icons.Rounded.SportsSoccer
        "sports_basketball" -> Icons.Rounded.SportsBasketball
        "sports_tennis" -> Icons.Rounded.SportsTennis
        "self_improvement" -> Icons.Rounded.SelfImprovement
        "sports" -> Icons.Rounded.Sports
        "sports_martial_arts" -> Icons.Rounded.SportsMartialArts
        "hiking" -> Icons.Rounded.Hiking

        // 공부
        "school" -> Icons.Rounded.School
        "menu_book" -> Icons.Rounded.MenuBook
        "edit" -> Icons.Rounded.Edit
        "laptop" -> Icons.Rounded.Laptop
        "code" -> Icons.Rounded.Code
        "quiz" -> Icons.Rounded.Quiz
        "translate" -> Icons.Rounded.Translate
        "science" -> Icons.Rounded.Science
        "calculate" -> Icons.Rounded.Calculate
        "history_edu" -> Icons.Rounded.HistoryEdu
        "auto_stories" -> Icons.Rounded.AutoStories
        "workspace_premium" -> Icons.Rounded.WorkspacePremium

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

        else -> Icons.Rounded.Check
    }
}