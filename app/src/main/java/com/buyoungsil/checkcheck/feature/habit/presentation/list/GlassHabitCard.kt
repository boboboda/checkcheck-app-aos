package com.buyoungsil.checkcheck.feature.habit.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buyoungsil.checkcheck.core.ui.components.GlassCard
import com.buyoungsil.checkcheck.core.ui.components.GlassIconBackground
import com.buyoungsil.checkcheck.core.ui.components.GlassProgressBar
import com.buyoungsil.checkcheck.core.ui.components.GlassBadge
import com.buyoungsil.checkcheck.ui.theme.GlassWhite25
import com.buyoungsil.checkcheck.ui.theme.CheckPrimary

/**
 * 🔥 글라스모피즘 습관 카드
 */
@Composable
fun GlassHabitCard(
    habitWithStats: HabitWithStats,
    onCheck: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStats.habit
    val statistics = habitWithStats.statistics
    val isChecked = habitWithStats.isCheckedToday

    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 체크박스
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onCheck() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.White,
                        uncheckedColor = Color.White.copy(alpha = 0.5f),
                        checkmarkColor = CheckPrimary
                    )
                )

                // 아이콘
                GlassIconBackground {
                    Icon(
                        imageVector = getHabitIcon(habit.icon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 제목
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (habit.description?.isNotBlank() == true) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // 스트릭
            if ((statistics?.currentStreak ?: 0) > 0) {
                GlassBadge(
                    text = "🔥 ${statistics?.currentStreak}일"
                )
            }

            // 삭제 버튼
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // 프로그레스 바
        if (statistics != null && statistics.totalChecks > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            GlassProgressBar(
                progress = statistics.completionRate / 100f
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "달성률 ${statistics.completionRate.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
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

        // 기본값
        else -> Icons.Rounded.Notifications
    }
}