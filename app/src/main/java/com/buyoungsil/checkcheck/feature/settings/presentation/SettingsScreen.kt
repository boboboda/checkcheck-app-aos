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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 계정 정보 섹션
            AccountSection(
                isAnonymous = uiState.isAnonymous,
                email = uiState.email,
                onLinkAccount = onNavigateToLogin
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 앱 설정 섹션
            AppSettingsSection()

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 정보 섹션
            InfoSection()

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 로그아웃/회원탈퇴 섹션
            DangerZoneSection(
                onLogout = { showLogoutDialog = true }
            )
        }
    }

    // 로그아웃 확인 다이얼로그
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("정말 로그아웃 하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onLogout()
                        showLogoutDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("로그아웃")
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

@Composable
private fun AccountSection(
    isAnonymous: Boolean,
    email: String?,
    onLinkAccount: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "계정",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isAnonymous) {
            // 익명 사용자
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "게스트 계정",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "계정을 연동하여 데이터를 안전하게 보관하세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onLinkAccount,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Link, "계정 연동")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("계정 연동하기")
                    }
                }
            }
        } else {
            // 정식 계정
            SettingItem(
                icon = Icons.Default.Email,
                title = "이메일",
                subtitle = email ?: "정보 없음"
            )
        }
    }
}

@Composable
private fun AppSettingsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "알림 설정",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        var pushEnabled by remember { mutableStateOf(true) }
        SettingItemWithSwitch(
            icon = Icons.Default.Notifications,
            title = "푸시 알림",
            subtitle = "습관 체크 및 그룹 활동 알림",
            checked = pushEnabled,
            onCheckedChange = { pushEnabled = it }
        )

        var groupNotificationEnabled by remember { mutableStateOf(true) }
        SettingItemWithSwitch(
            icon = Icons.Default.Groups,
            title = "그룹 알림",
            subtitle = "그룹 멤버 활동 알림",
            checked = groupNotificationEnabled,
            onCheckedChange = { groupNotificationEnabled = it }
        )
    }
}

@Composable
private fun InfoSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "정보",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingItem(
            icon = Icons.Default.Info,
            title = "버전 정보",
            subtitle = "1.0.0"
        )

        SettingItem(
            icon = Icons.Default.Description,
            title = "개인정보 처리방침",
            onClick = { /* TODO: 개인정보 처리방침 화면으로 이동 */ }
        )

        SettingItem(
            icon = Icons.Default.Article,
            title = "서비스 이용약관",
            onClick = { /* TODO: 이용약관 화면으로 이동 */ }
        )
    }
}

@Composable
private fun DangerZoneSection(
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "계정 관리",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingItem(
            icon = Icons.Default.Logout,
            title = "로그아웃",
            titleColor = MaterialTheme.colorScheme.error,
            onClick = onLogout
        )
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingItemWithSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}