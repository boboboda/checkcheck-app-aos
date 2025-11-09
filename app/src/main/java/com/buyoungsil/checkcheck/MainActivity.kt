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
import androidx.compose.ui.graphics.Brush
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
import com.buyoungsil.checkcheck.ui.theme.*
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

        lifecycleScope.launch {
            try {
                authManager.signInAnonymously()
                Log.d(TAG, "익명 로그인 성공")

                val userId = authManager.currentUserId
                if (userId != null) {
                    initializeUserUseCase(userId)
                }

                requestNotificationPermission()
                checkAndSaveFcmToken()
            } catch (e: Exception) {
                Log.e(TAG, "초기화 실패", e)
            }
        }

        setContent {
            CheckCheckTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val authState by authManager.authStateFlow()
                    .collectAsState(initial = authManager.currentUser)

                // 🧡 따뜻한 오렌지 그라데이션 배경
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    CheckBgGradientStart,  // #FFF5F0
                                    CheckBgGradientEnd     // #FFEBE0
                                )
                            )
                        )
                ) {
                    if (authState == null) {
                        // 로딩 화면
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
                                val shouldShowBottomBar = currentRoute in listOf(
                                    Screen.Home.route,
                                    Screen.GroupList.route,
                                    Screen.Statistics.route
                                )

                                if (shouldShowBottomBar) {
                                    WarmBottomNavigation(
                                        currentRoute = currentRoute,
                                        onNavigate = { route ->
                                            navController.navigate(route) {
                                                popUpTo(Screen.Home.route) {
                                                    inclusive = (route == Screen.Home.route)
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
 * 🧡 따뜻한 오렌지 바텀 네비게이션
 */
@Composable
private fun WarmBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        tonalElevation = 8.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WarmNavItem(
                icon = if (currentRoute == Screen.Home.route) Icons.Filled.Home else Icons.Outlined.Home,
                label = "홈",
                selected = currentRoute == Screen.Home.route,
                onClick = {
                    if (currentRoute != Screen.Home.route) {
                        onNavigate(Screen.Home.route)
                    }
                }
            )

            WarmNavItem(
                icon = if (currentRoute == Screen.GroupList.route) Icons.Filled.People else Icons.Outlined.People,
                label = "그룹",
                selected = currentRoute == Screen.GroupList.route,
                onClick = {
                    if (currentRoute != Screen.GroupList.route) {
                        onNavigate(Screen.GroupList.route)
                    }
                }
            )

            WarmNavItem(
                icon = if (currentRoute == Screen.Statistics.route) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                label = "통계",
                selected = currentRoute == Screen.Statistics.route,
                onClick = {
                    if (currentRoute != Screen.Statistics.route) {
                        onNavigate(Screen.Statistics.route)
                    }
                }
            )
        }
    }
}

@Composable
private fun WarmNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) CheckPrimary.copy(alpha = 0.1f)
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
                tint = if (selected) CheckPrimary else CheckGray500,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) CheckPrimary else CheckGray500,
                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold
                else androidx.compose.ui.text.font.FontWeight.Normal
            )
        }
    }
}