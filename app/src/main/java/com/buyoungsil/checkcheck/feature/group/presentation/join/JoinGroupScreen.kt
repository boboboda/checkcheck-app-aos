package com.buyoungsil.checkcheck.feature.group.presentation.join

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.core.ui.components.OrangeGradientButton
import com.buyoungsil.checkcheck.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGroupScreen(
    viewModel: JoinGroupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val inviteCode by viewModel.inviteCode.collectAsState()
    val nickname by viewModel.nickname.collectAsState()  // ✅ 추가
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "그룹 참여하기",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 초대 코드 입력
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.GroupCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔑",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "초대 코드",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }

                    Text(
                        text = "그룹 초대 코드를 입력하세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )

                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { viewModel.onInviteCodeChange(it) },
                        placeholder = { Text("예: ABC123") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ComponentShapes.TextField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerLight,
                            cursorColor = OrangePrimary,
                        ),
                        isError = error?.contains("초대") == true
                    )
                }
            }

            // ✅ 닉네임 입력 추가
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ComponentShapes.GroupCard,
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "그룹 내 닉네임",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }

                    Text(
                        text = "다른 멤버들에게 이 이름으로 보여요",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { viewModel.onNicknameChange(it) },
                        placeholder = { Text("예: 친구, 팀원, 동료...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ComponentShapes.TextField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerLight,
                            cursorColor = OrangePrimary,
                        ),
                        isError = error?.contains("닉네임") == true
                    )
                }
            }

            // 에러 메시지
            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ComponentShapes.GroupCard,
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorRed,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 참여 버튼
            OrangeGradientButton(
                text = if (isLoading) "참여 중..." else "그룹 참여하기",
                onClick = { viewModel.onJoinGroup() },
                enabled = !isLoading && inviteCode.isNotBlank() && nickname.isNotBlank(),
                icon = Icons.Default.ArrowBack  // 적절한 아이콘으로 변경 가능
            )
        }
    }
}