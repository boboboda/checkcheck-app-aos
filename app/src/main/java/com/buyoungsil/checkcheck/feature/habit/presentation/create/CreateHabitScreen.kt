package com.buyoungsil.checkcheck.feature.habit.presentation.create

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 습관 생성 화면 - Material Icons Extended 사용
 * ✨ 2000+ 세련된 아이콘
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    viewModel: CreateHabitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showIconPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 🎨 배경 그라디언트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "새 습관 만들기",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 😊 아이콘 선택 카드
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIconPicker = true },
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 아이콘 표시
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Material 아이콘 표시
                                val iconVector = HabitIcon.fromKey(uiState.icon).vector
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "아이콘 선택",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "습관을 표현할 아이콘을 골라주세요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 📝 습관 이름 입력
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "습관 이름",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onTitleChange(it) },
                        placeholder = {
                            Text(
                                "예: 물 2L 마시기",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        isError = uiState.error != null && uiState.title.isBlank()
                    )
                }

                // 📄 설명 입력
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "설명 (선택)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        placeholder = {
                            Text(
                                "이 습관에 대해 간단히 설명해주세요",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // 👥 그룹 공유 설정
                if (uiState.availableGroups.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "그룹과 공유하기",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "그룹 멤버들과 함께 실천해요",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.groupShared,
                                    onCheckedChange = { viewModel.onGroupSharedToggle(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }

                        // 그룹 선택
                        AnimatedVisibility(
                            visible = uiState.groupShared,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "공유할 그룹",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                uiState.availableGroups.forEach { group ->
                                    val isSelected = uiState.selectedGroup?.id == group.id

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.onGroupSelect(group) },
                                        shape = MaterialTheme.shapes.large,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
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
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                // 그룹 아이콘도 Material Icons
                                                val groupIconVector = HabitIcon.fromKey(group.icon).vector
                                                Icon(
                                                    imageVector = groupIconVector,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = group.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = "선택됨",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 에러 메시지
                if (uiState.error != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ✨ 생성 버튼
                Button(
                    onClick = { viewModel.onCreateHabit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.loading,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (uiState.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "습관 만들기",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 🎨 아이콘 선택 다이얼로그
    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = uiState.icon,
            onIconSelected = { icon ->
                viewModel.onIconChange(icon)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

/**
 * 아이콘 선택 다이얼로그 - Material Icons Extended
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconPickerDialog(
    currentIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val iconCategories = remember {
        mapOf(
            "생활" to listOf(
                HabitIcon.WATER_DROP,
                HabitIcon.NOTIFICATIONS,
                HabitIcon.CALENDAR,
                HabitIcon.SCHEDULE,
                HabitIcon.HOME,
                HabitIcon.LIGHTBULB,
                HabitIcon.NOTE,
                HabitIcon.PHONE,
                HabitIcon.YARD,
                HabitIcon.BOOK,
                HabitIcon.COFFEE,
                HabitIcon.ECO
            ),
            "건강" to listOf(
                HabitIcon.FAVORITE,
                HabitIcon.MONITOR_HEART,
                HabitIcon.APPLE,
                HabitIcon.LOCAL_HOSPITAL,
                HabitIcon.MEDICATION,
                HabitIcon.HOTEL,
                HabitIcon.PSYCHOLOGY,
                HabitIcon.SENTIMENT_SATISFIED,
                HabitIcon.VISIBILITY,
                HabitIcon.VOLUNTEER_ACTIVISM,
                HabitIcon.THERMOSTAT,
                HabitIcon.VACCINES
            ),
            "운동" to listOf(
                HabitIcon.FITNESS_CENTER,
                HabitIcon.DIRECTIONS_BIKE,
                HabitIcon.DIRECTIONS_RUN,
                HabitIcon.DIRECTIONS_WALK,
                HabitIcon.POOL,
                HabitIcon.SELF_IMPROVEMENT,
                HabitIcon.SPORTS_BASKETBALL,
                HabitIcon.SPORTS_SOCCER,
                HabitIcon.SPORTS_TENNIS,
                HabitIcon.SPORTS_MARTIAL_ARTS,
                HabitIcon.SPORTS_SCORE,
                HabitIcon.TIMER
            ),
            "공부" to listOf(
                HabitIcon.MENU_BOOK,
                HabitIcon.SCHOOL,
                HabitIcon.EDIT,
                HabitIcon.CREATE,
                HabitIcon.BACKPACK,
                HabitIcon.WORKSPACE_PREMIUM,
                HabitIcon.CALCULATE,
                HabitIcon.SCIENCE,
                HabitIcon.PUBLIC,
                HabitIcon.FUNCTIONS,
                HabitIcon.BIOTECH,
                HabitIcon.TRACK_CHANGES
            ),
            "취미" to listOf(
                HabitIcon.PALETTE,
                HabitIcon.MUSIC_NOTE,
                HabitIcon.PIANO,
                HabitIcon.SPORTS_ESPORTS,
                HabitIcon.CAMERA_ALT,
                HabitIcon.MOVIE,
                HabitIcon.BRUSH,
                HabitIcon.HEADPHONES,
                HabitIcon.MIC,
                HabitIcon.EXTENSION,
                HabitIcon.CELEBRATION,
                HabitIcon.INTERESTS
            ),
            "관계" to listOf(
                HabitIcon.GROUPS,
                HabitIcon.PERSON,
                HabitIcon.HANDSHAKE,
                HabitIcon.FORUM,
                HabitIcon.EMAIL,
                HabitIcon.CARD_GIFTCARD,
                HabitIcon.EMOJI_EMOTIONS,
                HabitIcon.WAVING_HAND,
                HabitIcon.VIDEOCAM,
                HabitIcon.CAKE,
                HabitIcon.LOYALTY,
                HabitIcon.DIVERSITY
            )
        )
    }

    var selectedCategory by remember { mutableStateOf(iconCategories.keys.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 제목
                Text(
                    text = "아이콘 선택",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 카테고리 탭
                ScrollableTabRow(
                    selectedTabIndex = iconCategories.keys.indexOf(selectedCategory),
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    indicator = {},
                    divider = {}
                ) {
                    iconCategories.keys.forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { selectedCategory = category },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 아이콘 그리드
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(iconCategories[selectedCategory] ?: emptyList()) { habitIcon ->
                        val isSelected = habitIcon.key == currentIcon

                        Surface(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onIconSelected(habitIcon.key) },
                            shape = MaterialTheme.shapes.large,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = habitIcon.vector,
                                    contentDescription = habitIcon.label,
                                    modifier = Modifier.size(28.dp),
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 닫기 버튼
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("닫기")
                }
            }
        }
    }
}

/**
 * 습관 아이콘 정의 (Material Icons Extended)
 */
data class HabitIcon(
    val key: String,
    val label: String,
    val vector: ImageVector
) {
    companion object {
        // 생활
        val WATER_DROP = HabitIcon("water_drop", "물", Icons.Rounded.WaterDrop)
        val NOTIFICATIONS = HabitIcon("notifications", "알림", Icons.Rounded.Notifications)
        val CALENDAR = HabitIcon("calendar", "달력", Icons.Rounded.CalendarToday)
        val SCHEDULE = HabitIcon("schedule", "시계", Icons.Rounded.Schedule)
        val HOME = HabitIcon("home", "집", Icons.Rounded.Home)
        val LIGHTBULB = HabitIcon("lightbulb", "전구", Icons.Rounded.Lightbulb)
        val NOTE = HabitIcon("note", "노트", Icons.Rounded.Note)
        val PHONE = HabitIcon("phone", "전화", Icons.Rounded.Phone)
        val YARD = HabitIcon("yard", "식물", Icons.Rounded.Yard)
        val BOOK = HabitIcon("book", "책", Icons.Rounded.Book)
        val COFFEE = HabitIcon("coffee", "커피", Icons.Rounded.Coffee)
        val ECO = HabitIcon("eco", "잎", Icons.Rounded.Eco)

        // 건강
        val FAVORITE = HabitIcon("favorite", "하트", Icons.Rounded.Favorite)
        val MONITOR_HEART = HabitIcon("monitor_heart", "활동", Icons.Rounded.MonitorHeart)
        val APPLE = HabitIcon("apple", "음식", Icons.Rounded.LocalDining)
        val LOCAL_HOSPITAL = HabitIcon("local_hospital", "응급", Icons.Rounded.LocalHospital)
        val MEDICATION = HabitIcon("medication", "약", Icons.Rounded.Medication)
        val HOTEL = HabitIcon("hotel", "침대", Icons.Rounded.Hotel)
        val PSYCHOLOGY = HabitIcon("psychology", "뇌", Icons.Rounded.Psychology)
        val SENTIMENT_SATISFIED = HabitIcon("sentiment", "웃음", Icons.Rounded.SentimentSatisfied)
        val VISIBILITY = HabitIcon("visibility", "눈", Icons.Rounded.Visibility)
        val VOLUNTEER_ACTIVISM = HabitIcon("volunteer", "손하트", Icons.Rounded.VolunteerActivism)
        val THERMOSTAT = HabitIcon("thermostat", "체온계", Icons.Rounded.Thermostat)
        val VACCINES = HabitIcon("vaccines", "주사기", Icons.Rounded.Vaccines)

        // 운동
        val FITNESS_CENTER = HabitIcon("fitness_center", "바벨", Icons.Rounded.FitnessCenter)
        val DIRECTIONS_BIKE = HabitIcon("directions_bike", "자전거", Icons.Rounded.DirectionsBike)
        val DIRECTIONS_RUN = HabitIcon("directions_run", "달리기", Icons.Rounded.DirectionsRun)
        val DIRECTIONS_WALK = HabitIcon("directions_walk", "걷기", Icons.Rounded.DirectionsWalk)
        val POOL = HabitIcon("pool", "수영", Icons.Rounded.Pool)
        val SELF_IMPROVEMENT = HabitIcon("self_improvement", "요가", Icons.Rounded.SelfImprovement)
        val SPORTS_BASKETBALL = HabitIcon("basketball", "농구", Icons.Rounded.SportsBasketball)
        val SPORTS_SOCCER = HabitIcon("soccer", "축구", Icons.Rounded.SportsSoccer)
        val SPORTS_TENNIS = HabitIcon("tennis", "테니스", Icons.Rounded.SportsTennis)
        val SPORTS_MARTIAL_ARTS = HabitIcon("martial_arts", "무술", Icons.Rounded.SportsMartialArts)
        val SPORTS_SCORE = HabitIcon("sports_score", "운동화", Icons.Rounded.SportsScore)
        val TIMER = HabitIcon("timer", "타이머", Icons.Rounded.Timer)

        // 공부
        val MENU_BOOK = HabitIcon("menu_book", "책열림", Icons.Rounded.MenuBook)
        val SCHOOL = HabitIcon("school", "졸업", Icons.Rounded.School)
        val EDIT = HabitIcon("edit", "펜", Icons.Rounded.Edit)
        val CREATE = HabitIcon("create", "연필", Icons.Rounded.Create)
        val BACKPACK = HabitIcon("backpack", "가방", Icons.Rounded.Backpack)
        val WORKSPACE_PREMIUM = HabitIcon("workspace_premium", "증명서", Icons.Rounded.WorkspacePremium)
        val CALCULATE = HabitIcon("calculate", "계산기", Icons.Rounded.Calculate)
        val SCIENCE = HabitIcon("science", "플라스크", Icons.Rounded.Science)
        val PUBLIC = HabitIcon("public", "지구본", Icons.Rounded.Public)
        val FUNCTIONS = HabitIcon("functions", "함수", Icons.Rounded.Functions)
        val BIOTECH = HabitIcon("biotech", "생물", Icons.Rounded.Biotech)
        val TRACK_CHANGES = HabitIcon("track_changes", "원자", Icons.Rounded.TrackChanges)

        // 취미
        val PALETTE = HabitIcon("palette", "팔레트", Icons.Rounded.Palette)
        val MUSIC_NOTE = HabitIcon("music_note", "음악", Icons.Rounded.MusicNote)
        val PIANO = HabitIcon("piano", "기타", Icons.Rounded.Piano)
        val SPORTS_ESPORTS = HabitIcon("sports_esports", "게임", Icons.Rounded.SportsEsports)
        val CAMERA_ALT = HabitIcon("camera_alt", "카메라", Icons.Rounded.CameraAlt)
        val MOVIE = HabitIcon("movie", "영화", Icons.Rounded.Movie)
        val BRUSH = HabitIcon("brush", "붓", Icons.Rounded.Brush)
        val HEADPHONES = HabitIcon("headphones", "헤드폰", Icons.Rounded.Headphones)
        val MIC = HabitIcon("mic", "마이크", Icons.Rounded.Mic)
        val EXTENSION = HabitIcon("extension", "퍼즐", Icons.Rounded.Extension)
        val CELEBRATION = HabitIcon("celebration", "연", Icons.Rounded.Celebration)
        val INTERESTS = HabitIcon("interests", "취미", Icons.Rounded.Interests)

        // 관계
        val GROUPS = HabitIcon("groups", "사람들", Icons.Rounded.Groups)
        val PERSON = HabitIcon("person", "사용자", Icons.Rounded.Person)
        val HANDSHAKE = HabitIcon("handshake", "악수", Icons.Rounded.Handshake)
        val FORUM = HabitIcon("forum", "채팅", Icons.Rounded.Forum)
        val EMAIL = HabitIcon("email", "편지", Icons.Rounded.Email)
        val CARD_GIFTCARD = HabitIcon("card_giftcard", "선물", Icons.Rounded.CardGiftcard)
        val EMOJI_EMOTIONS = HabitIcon("emoji_emotions", "웃음", Icons.Rounded.EmojiEmotions)
        val WAVING_HAND = HabitIcon("waving_hand", "포옹", Icons.Rounded.WavingHand)
        val VIDEOCAM = HabitIcon("videocam", "영상", Icons.Rounded.Videocam)
        val CAKE = HabitIcon("cake", "케이크", Icons.Rounded.Cake)
        val LOYALTY = HabitIcon("loyalty", "선물", Icons.Rounded.Loyalty)
        val DIVERSITY = HabitIcon("diversity", "다양성", Icons.Rounded.Diversity3)

        // 기본값
        val DEFAULT = NOTIFICATIONS

        fun fromKey(key: String): HabitIcon {
            return when (key) {
                "water_drop" -> WATER_DROP
                "notifications" -> NOTIFICATIONS
                "calendar" -> CALENDAR
                "schedule" -> SCHEDULE
                "home" -> HOME
                "lightbulb" -> LIGHTBULB
                "note" -> NOTE
                "phone" -> PHONE
                "yard" -> YARD
                "book" -> BOOK
                "coffee" -> COFFEE
                "eco" -> ECO
                "favorite" -> FAVORITE
                "monitor_heart" -> MONITOR_HEART
                "apple" -> APPLE
                "local_hospital" -> LOCAL_HOSPITAL
                "medication" -> MEDICATION
                "hotel" -> HOTEL
                "psychology" -> PSYCHOLOGY
                "sentiment" -> SENTIMENT_SATISFIED
                "visibility" -> VISIBILITY
                "volunteer" -> VOLUNTEER_ACTIVISM
                "thermostat" -> THERMOSTAT
                "vaccines" -> VACCINES
                "fitness_center" -> FITNESS_CENTER
                "directions_bike" -> DIRECTIONS_BIKE
                "directions_run" -> DIRECTIONS_RUN
                "directions_walk" -> DIRECTIONS_WALK
                "pool" -> POOL
                "self_improvement" -> SELF_IMPROVEMENT
                "basketball" -> SPORTS_BASKETBALL
                "soccer" -> SPORTS_SOCCER
                "tennis" -> SPORTS_TENNIS
                "martial_arts" -> SPORTS_MARTIAL_ARTS
                "sports_score" -> SPORTS_SCORE
                "timer" -> TIMER
                "menu_book" -> MENU_BOOK
                "school" -> SCHOOL
                "edit" -> EDIT
                "create" -> CREATE
                "backpack" -> BACKPACK
                "workspace_premium" -> WORKSPACE_PREMIUM
                "calculate" -> CALCULATE
                "science" -> SCIENCE
                "public" -> PUBLIC
                "functions" -> FUNCTIONS
                "biotech" -> BIOTECH
                "track_changes" -> TRACK_CHANGES
                "palette" -> PALETTE
                "music_note" -> MUSIC_NOTE
                "piano" -> PIANO
                "sports_esports" -> SPORTS_ESPORTS
                "camera_alt" -> CAMERA_ALT
                "movie" -> MOVIE
                "brush" -> BRUSH
                "headphones" -> HEADPHONES
                "mic" -> MIC
                "extension" -> EXTENSION
                "celebration" -> CELEBRATION
                "interests" -> INTERESTS
                "groups" -> GROUPS
                "person" -> PERSON
                "handshake" -> HANDSHAKE
                "forum" -> FORUM
                "email" -> EMAIL
                "card_giftcard" -> CARD_GIFTCARD
                "emoji_emotions" -> EMOJI_EMOTIONS
                "waving_hand" -> WAVING_HAND
                "videocam" -> VIDEOCAM
                "cake" -> CAKE
                "loyalty" -> LOYALTY
                "diversity" -> DIVERSITY
                else -> DEFAULT
            }
        }
    }
}