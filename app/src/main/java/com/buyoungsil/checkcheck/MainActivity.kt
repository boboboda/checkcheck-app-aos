package com.buyoungsil.checkcheck

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

    companion object {
        private const val TAG = "MainActivity"
    }

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

        Log.d(TAG, "=== MainActivity onCreate 시작 ===")

        // ✨ 핵심: UI를 렌더링하기 전에 로그인을 먼저 완료
        lifecycleScope.launch {
            try {
                Log.d(TAG, "익명 로그인 시도...")
                val result = authManager.signInAnonymously()

                if (result.isSuccess) {
                    val user = result.getOrNull()
                    Log.d(TAG, "✅ 익명 로그인 성공: uid=${user?.uid}")

                    val userId = authManager.currentUserId
                    Log.d(TAG, "currentUserId 확인: $userId")

                    if (userId != null) {
                        Log.d(TAG, "사용자 초기화 중...")
                        initializeUserUseCase(userId)
                        Log.d(TAG, "✅ 사용자 초기화 완료")
                    }
                } else {
                    Log.e(TAG, "❌ 익명 로그인 실패: ${result.exceptionOrNull()?.message}")
                }

                requestNotificationPermission()
                checkAndSaveFcmToken()
            } catch (e: Exception) {
                Log.e(TAG, "❌ 초기화 실패", e)
            }
        }

        // ✨ setContent는 동기적으로 실행 (로그인과 별개)
        setContent {
            CheckCheckTheme {
                Log.d(TAG, "setContent 실행")

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // ✨ authStateFlow는 초기값으로 currentUser를 사용
                // FirebaseAuthManager에서 즉시 emit하도록 수정했으므로
                // 로그인 완료 후 자동으로 업데이트됨
                val authState by authManager.authStateFlow()
                    .collectAsState(initial = authManager.currentUser)

                LaunchedEffect(authState) {
                    Log.d(TAG, "authState 변경: ${authState?.uid}")
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OrangeBackground)
                ) {
                    if (authState == null) {
                        Log.d(TAG, "로딩 화면 표시")
                        // 로딩 화면
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = OrangePrimary
                                )
                                Text(
                                    text = "로그인 중...",
                                    color = TextPrimaryLight
                                )
                            }
                        }
                    } else {
                        Log.d(TAG, "메인 화면 렌더링 (userId=${authState!!.uid})")
                        // 메인 화면
                        Scaffold(
                            bottomBar = {
                                if (shouldShowBottomBar(currentRoute)) {
                                    Log.d(TAG, "BottomBar 표시 중 - currentRoute: $currentRoute") // ✅ 로그 추가

                                    BottomNavigationBar(
                                        currentRoute = currentRoute,
                                        onNavigate = { route ->
                                            Log.d(TAG, "탭바 클릭 - from: $currentRoute, to: $route") // ✅ 로그 추가

                                            if (currentRoute != route) {
                                                Log.d(TAG, "네비게이션 시작: $route") // ✅ 로그 추가

                                                navController.navigate(route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }

                                                Log.d(TAG, "네비게이션 완료: $route") // ✅ 로그 추가
                                            } else {
                                                Log.d(TAG, "같은 탭 재클릭 무시: $route") // ✅ 로그 추가
                                            }
                                        }
                                    )
                                } else {
                                    Log.d(TAG, "BottomBar 숨김 - currentRoute: $currentRoute") // ✅ 로그 추가
                                }
                            },
                            containerColor = OrangeBackground
                        ) { padding ->
                            Log.d(TAG, "NavGraph 렌더링")
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

        Log.d(TAG, "=== MainActivity onCreate 완료 ===")
    }

    private fun shouldShowBottomBar(currentRoute: String?): Boolean {
        return when (currentRoute) {
            Screen.Home.route,
            Screen.GroupList.route,
            Screen.Statistics.route -> true
            else -> false
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
                    Log.d(TAG, "토큰: $token")

                    lifecycleScope.launch {
                        try {
                            updateFcmTokenUseCase(userId, token)
                            Log.d(TAG, "✅ FCM 토큰 Firestore 저장 성공")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ FCM 토큰 저장 실패: ${e.message}", e)
                        }
                    }
                } else {
                    Log.e(TAG, "❌ FCM 토큰 생성 실패", task.exception)
                }
            }
    }
}

/**
 * 🧡 오렌지 테마 하단 네비게이션 바
 */
@Composable
private fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {



    Log.d(TAG, "BottomNavigationBar 렌더링 - currentRoute: $currentRoute") // ✅ 로그 추가

    NavigationBar(
        containerColor = Color.White,
        contentColor = OrangePrimary,
        tonalElevation = 8.dp
    ) {
        NavigationItem(
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            label = "홈",
            selected = currentRoute == Screen.Home.route,
            onClick = {
                Log.d(TAG, "홈 탭 클릭됨") // ✅ 로그 추가
                onNavigate(Screen.Home.route)
            }
        )

        NavigationItem(
            icon = Icons.Outlined.People,
            selectedIcon = Icons.Filled.People,
            label = "그룹",
            selected = currentRoute == Screen.GroupList.route,
            onClick = {
                Log.d(TAG, "그룹 탭 클릭됨") // ✅ 로그 추가
                onNavigate(Screen.GroupList.route)
            }
        )

        NavigationItem(
            icon = Icons.Outlined.BarChart,
            selectedIcon = Icons.Filled.BarChart,
            label = "통계",
            selected = currentRoute == Screen.Statistics.route,
            onClick = {
                Log.d(TAG, "통계 탭 클릭됨") // ✅ 로그 추가
                onNavigate(Screen.Statistics.route)
            }
        )
    }
}








/**
 * 네비게이션 아이템
 */
@Composable
private fun RowScope.NavigationItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = label,
                modifier = Modifier.size(26.dp)
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = OrangePrimary,
            selectedTextColor = OrangePrimary,
            unselectedIconColor = TextSecondaryLight,
            unselectedTextColor = TextSecondaryLight,
            indicatorColor = OrangePrimary.copy(alpha = 0.1f)
        )
    )
}