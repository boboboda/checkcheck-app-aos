package com.buyoungsil.checkcheck.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 🧡 오렌지 테마 설정 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "설정",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight,
                    navigationIconContentColor = TextPrimaryLight
                )
            )
        },
        containerColor = OrangeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 계정 정보 섹션
            AccountSection(
                isAnonymous = uiState.isAnonymous,
                email = uiState.email,
                onLinkAccount = onNavigateToLogin
            )

            // 앱 설정 섹션
            AppSettingsSection()

            // 정보 섹션
            InfoSection()

            // 로그아웃 섹션
            DangerZoneSection(
                onLogout = { showLogoutDialog = true }
            )
        }
    }

    // 로그아웃 확인 다이얼로그
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "로그아웃",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("정말 로그아웃 하시겠습니까?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onLogout()
                        showLogoutDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(
                        "로그아웃",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 * 계정 정보 섹션
 */
@Composable
private fun AccountSection(
    isAnonymous: Boolean,
    email: String?,
    onLinkAccount: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "계정 정보",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            HorizontalDivider(color = DividerLight)

            SettingItem(
                icon = Icons.Default.AccountCircle,
                title = if (isAnonymous) "익명 사용자" else "로그인됨",
                subtitle = email ?: "계정을 연동하여 데이터를 보호하세요"
            )

            if (isAnonymous) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLinkAccount,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary
                    ),
                    shape = ComponentShapes.PrimaryButton
                ) {
                    Text(
                        "계정 연동하기",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 앱 설정 섹션
 */
@Composable
private fun AppSettingsSection() {
    var pushEnabled by remember { mutableStateOf(true) }
    var groupNotificationEnabled by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "알림 설정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            HorizontalDivider(color = DividerLight)

            SettingItemWithSwitch(
                icon = Icons.Default.Notifications,
                title = "푸시 알림",
                subtitle = "습관 체크 및 그룹 활동 알림",
                checked = pushEnabled,
                onCheckedChange = { pushEnabled = it }
            )

            HorizontalDivider(color = DividerLight)

            SettingItemWithSwitch(
                icon = Icons.Default.Groups,
                title = "그룹 알림",
                subtitle = "그룹 멤버 활동 알림",
                checked = groupNotificationEnabled,
                onCheckedChange = { groupNotificationEnabled = it }
            )
        }
    }
}

/**
 * 정보 섹션
 */
@Composable
private fun InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "정보",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            HorizontalDivider(color = DividerLight)

            SettingItem(
                icon = Icons.Default.Info,
                title = "버전 정보",
                subtitle = "1.0.0"
            )

            HorizontalDivider(color = DividerLight)

            SettingItem(
                icon = Icons.Default.Description,
                title = "개인정보 처리방침",
                onClick = { /* TODO: 개인정보 처리방침 화면으로 이동 */ }
            )

            HorizontalDivider(color = DividerLight)

            SettingItem(
                icon = Icons.Default.Article,
                title = "서비스 이용약관",
                onClick = { /* TODO: 이용약관 화면으로 이동 */ }
            )
        }
    }
}

/**
 * 위험 영역 섹션
 */
@Composable
private fun DangerZoneSection(
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ComponentShapes.HabitCard,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "계정 관리",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ErrorRed
            )

            HorizontalDivider(color = DividerLight)

            SettingItem(
                icon = Icons.Default.Logout,
                title = "로그아웃",
                titleColor = ErrorRed,
                onClick = onLogout
            )
        }
    }
}

/**
 * 설정 항목
 */
@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = TextPrimaryLight,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TextSecondaryLight
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondaryLight
                )
            }
        }
    }
}

/**
 * 스위치가 있는 설정 항목
 */
@Composable
private fun SettingItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = TextSecondaryLight
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimaryLight
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OrangePrimary
            )
        )
    }
}