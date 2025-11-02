package com.buyoungsil.checkcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.buyoungsil.checkcheck.core.data.firebase.FirebaseAuthManager
import com.buyoungsil.checkcheck.core.ui.navigation.NavGraph
import com.buyoungsil.checkcheck.core.ui.navigation.Screen
import com.buyoungsil.checkcheck.ui.theme.CheckcheckTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 자동 로그인 (익명)
        lifecycleScope.launch {
            if (authManager.currentUser == null) {
                authManager.signInAnonymously()
            }
        }

        enableEdgeToEdge()
        setContent {
            CheckcheckTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 로그인 상태 확인
                val authState by authManager.authStateFlow()
                    .collectAsState(initial = authManager.currentUser)

                if (authState == null) {
                    // 로딩 화면
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Scaffold(
                        bottomBar = {
                            if (currentRoute in listOf(
                                    Screen.Home.route,
                                    Screen.GroupList.route,
                                    Screen.Statistics.route
                                )) {
                                NavigationBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Home, "홈") },
                                        label = { Text("홈") },
                                        selected = currentRoute == Screen.Home.route,
                                        onClick = {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Home.route) { inclusive = true }
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.People, "그룹") },
                                        label = { Text("그룹") },
                                        selected = currentRoute == Screen.GroupList.route,
                                        onClick = {
                                            navController.navigate(Screen.GroupList.route) {
                                                popUpTo(Screen.Home.route)
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.BarChart, "통계") },
                                        label = { Text("통계") },
                                        selected = currentRoute == Screen.Statistics.route,
                                        onClick = {
                                            navController.navigate(Screen.Statistics.route) {
                                                popUpTo(Screen.Home.route)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}