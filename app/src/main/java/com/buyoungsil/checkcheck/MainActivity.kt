package com.buyoungsil.checkcheck

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.core.domain.usecase.InitializeUserUseCase
import com.buyoungsil.checkcheck.core.domain.usecase.UpdateFcmTokenUseCase
import com.buyoungsil.checkcheck.core.ui.navigation.NavGraph
import com.buyoungsil.checkcheck.core.ui.navigation.Screen
import com.buyoungsil.checkcheck.ui.theme.CheckCheckTheme
import com.buyoungsil.checkcheck.ui.theme.CheckPrimary
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: FirebaseAuthManager

    @Inject
    lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase

    @Inject
    lateinit var initializeUserUseCase: InitializeUserUseCase

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "✅ 알림 권한 허용됨")
        } else {
            Log.d(TAG, "❌ 알림 권한 거부됨")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Firebase 익명 로그인
        lifecycleScope.launch {
            if (authManager.currentUser == null) {
                Log.d(TAG, "⏳ Firebase 익명 로그인 시도...")
                val result = authManager.signInAnonymously()
                result.onSuccess { user ->
                    Log.d(TAG, "✅ Firebase 익명 로그인 성공")
                    Log.d(TAG, "   User ID: ${user.uid}")
                    initializeUserUseCase(user.uid)
                    checkAndSaveFcmToken()
                }.onFailure { error ->
                    Log.e(TAG, "❌ Firebase 로그인 실패: ${error.message}")
                }
            } else {
                Log.d(TAG, "✅ 이미 로그인됨: ${authManager.currentUser?.uid}")
                authManager.currentUserId?.let { initializeUserUseCase(it) }
                checkAndSaveFcmToken()
            }
        }

        requestNotificationPermission()

        setContent {
            CheckCheckTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                val authState by authManager.authStateFlow()
                    .collectAsState(initial = authManager.currentUser)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (authState == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = CheckPrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("로그인 중...", color = CheckPrimary)
                            }
                        }
                    } else {
                        Scaffold(
                            containerColor = Color.Transparent,
                            bottomBar = {
                                if (currentRoute in listOf(
                                        Screen.Home.route,
                                        Screen.GroupList.route,
                                        Screen.Statistics.route
                                    )
                                ) {
                                    MZBottomNavigation(
                                        currentRoute = currentRoute,
                                        onNavigate = { route ->
                                            navController.navigate(route) {
                                                popUpTo(Screen.Home.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        ) { padding ->
                            NavGraph(
                                navController = navController,
                                startDestination = Screen.Home.route,
                                modifier = Modifier.padding(padding)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "✅ 알림 권한 이미 허용됨")
                }
                else -> {
                    Log.d(TAG, "⏳ 알림 권한 요청 중...")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "✅ Android 12 이하 - 알림 권한 자동 허용")
        }
    }

    private fun checkAndSaveFcmToken() {
        val userId = authManager.currentUserId
        if (userId == null) {
            Log.w(TAG, "⚠️ 로그인 안 된 상태 - FCM 토큰 저장 건너뜀")
            return
        }

        Log.d(TAG, "=== FCM 토큰 확인 시작 ===")

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(TAG, "✅ FCM 토큰 생성 성공!")
                    Log.d(TAG, "🔑 토큰: $token")

                    lifecycleScope.launch {
                        try {
                            updateFcmTokenUseCase(userId, token)
                            Log.d(TAG, "✅ FCM 토큰 Firestore 저장 완료")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ FCM 토큰 저장 실패", e)
                        }
                    }
                } else {
                    Log.e(TAG, "❌ FCM 토큰 생성 실패", task.exception)
                    Log.e(TAG, "   에러 메시지: ${task.exception?.message}")
                }
            }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

/**
 * ✨ MZ감성 바텀 네비게이션
 * - 글래스모피즘 효과
 * - 선택된 아이템 강조
 * - 부드러운 애니메이션
 */
@Composable
private fun MZBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // 네비게이션 바 영역만 확보
                .height(64.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MZNavItem(
                icon = if (currentRoute == Screen.Home.route) Icons.Filled.Home else Icons.Outlined.Home,
                label = "홈",
                selected = currentRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) }
            )

            MZNavItem(
                icon = if (currentRoute == Screen.GroupList.route) Icons.Filled.People else Icons.Outlined.People,
                label = "그룹",
                selected = currentRoute == Screen.GroupList.route,
                onClick = { onNavigate(Screen.GroupList.route) }
            )

            MZNavItem(
                icon = if (currentRoute == Screen.Statistics.route) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                label = "통계",
                selected = currentRoute == Screen.Statistics.route,
                onClick = { onNavigate(Screen.Statistics.route) }
            )
        }
    }
}

@Composable
private fun MZNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) CheckPrimary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) CheckPrimary else Color.Gray,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) CheckPrimary else Color.Gray,
                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold
                else androidx.compose.ui.text.font.FontWeight.Normal
            )
        }
    }
}