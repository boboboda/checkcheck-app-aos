package com.buyoungsil.checkcheck.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.buyoungsil.checkcheck.BuildConfig
import com.buyoungsil.checkcheck.feature.auth.presentation.link.LinkAccountScreen
import com.buyoungsil.checkcheck.feature.coin.presentation.wallet.CoinWalletScreen
import com.buyoungsil.checkcheck.feature.debug.DebugTestScreen
import com.buyoungsil.checkcheck.feature.group.presentation.create.CreateGroupScreen
import com.buyoungsil.checkcheck.feature.group.presentation.detail.GroupDetailScreen
import com.buyoungsil.checkcheck.feature.group.presentation.join.JoinGroupScreen
import com.buyoungsil.checkcheck.feature.group.presentation.list.GroupListScreen
import com.buyoungsil.checkcheck.feature.group.presentation.upgrade.UpgradeGroupTierScreen
import com.buyoungsil.checkcheck.feature.habit.presentation.create.CreateHabitScreen
import com.buyoungsil.checkcheck.feature.habit.presentation.detail.HabitDetailScreen
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitListScreen
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitListViewModel
import com.buyoungsil.checkcheck.feature.home.HomeScreen
import com.buyoungsil.checkcheck.feature.settings.presentation.SettingsScreen
import com.buyoungsil.checkcheck.feature.statistics.presentation.StatisticsScreen
import com.buyoungsil.checkcheck.feature.task.presentation.create.CreateTaskScreen
import com.buyoungsil.checkcheck.feature.task.presentation.list.TaskListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ========== 홈 화면 ==========
        composable(Screen.Home.route) { backStackEntry ->
            // ✅ NavGraph 레벨에서 ViewModel 가져오기
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.graph.startDestinationRoute!!)
            }
            val habitViewModel: HabitListViewModel = hiltViewModel(parentEntry)

            HomeScreen(
                habitViewModel = habitViewModel,  // ✅ 공유된 ViewModel 전달
                onNavigateToHabitCreate = {
                    navController.navigate(Screen.HabitCreate.createRoute())
                },
                onNavigateToGroupList = {
                    navController.navigate(Screen.GroupList.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHabitList = {
                    navController.navigate(Screen.HabitList.route)
                },
                onNavigateToPersonalTaskCreate = {
                    navController.navigate(Screen.PersonalTaskCreate.route)
                },
                onNavigateToPersonalTaskList = {
                    navController.navigate(Screen.PersonalTaskList.route)
                },
                onNavigateToCoinWallet = {
                    navController.navigate(Screen.CoinWallet.route)
                },
                onNavigateToDebug = {
                    navController.navigate("debug_test")
                },
                onNavigateToHabitDetail = { habitId ->  // 🆕 추가
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
            )
        }

        // ========== Habit 화면들 ==========
        composable(Screen.HabitList.route) { backStackEntry ->
            // ✅ NavGraph 레벨에서 ViewModel 가져오기
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.graph.startDestinationRoute!!)
            }
            val habitViewModel: HabitListViewModel = hiltViewModel(parentEntry)

            HabitListScreen(
                viewModel = habitViewModel,  // ✅ 공유된 ViewModel 전달
                onNavigateToCreate = {
                    navController.navigate(Screen.HabitCreate.createRoute())
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { habitId ->  // 🆕 추가
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
            )
        }

        composable(
            route = Screen.HabitCreate.route,
            arguments = listOf(
                navArgument("groupId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            CreateHabitScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "habit_detail/{habitId}",
            arguments = listOf(
                navArgument("habitId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== Group 화면들 ==========
        composable(Screen.GroupList.route) {
            GroupListScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.GroupCreate.route)
                },
                onNavigateToJoin = {
                    navController.navigate(Screen.GroupJoin.route)
                },
                onNavigateToDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                }
            )
        }

        composable(Screen.GroupCreate.route) {
            CreateGroupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.GroupJoin.route) {
            JoinGroupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== Group Detail ==========
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHabitCreate = { groupId ->
                    navController.navigate(Screen.HabitCreate.createRoute(groupId))
                },
                onNavigateToTaskCreate = {
                    navController.navigate(Screen.TaskCreate.createRoute(groupId))
                },
                // ✨ 추가
                onNavigateToTaskList = {
                    navController.navigate(Screen.TaskList.createRoute(groupId))
                },
                onNavigateToHabitDetail = { habitId ->  // 🆕 추가
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
                onNavigateToUpgradeTier = { groupId ->  // ✨ 추가
                    navController.navigate(Screen.UpgradeGroupTier.createRoute(groupId))
                }
            )
        }

        // ========== 그룹 티어 업그레이드 ==========
        composable(
            route = Screen.UpgradeGroupTier.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) {
            UpgradeGroupTierScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


        // ========== Task 화면들 ==========

        // ✅ 개인 할일 리스트 - 새로 추가
        composable(Screen.PersonalTaskList.route) {
            TaskListScreen(
                groupId = null,  // null = 개인 모드
                onNavigateToCreate = {
                    navController.navigate(Screen.PersonalTaskCreate.route)
                },
                onNavigateBack = {  // ✅ 뒤로가기 추가
                    navController.popBackStack()
                }
            )
        }

        // 그룹 할일 리스트
        composable(
            route = Screen.TaskList.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            TaskListScreen(
                groupId = groupId,
                onNavigateToCreate = {
                    navController.navigate(Screen.TaskCreate.createRoute(groupId))
                },
                onNavigateBack = {  // ✅ 뒤로가기 추가
                    navController.popBackStack()
                }
            )
        }

        // 그룹 할일 생성
        composable(
            route = Screen.TaskCreate.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) {
            CreateTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ✅ 개인 할일 생성
        composable(Screen.PersonalTaskCreate.route) {
            CreateTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== 통계 ==========
        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }

        // ========== 설정 ==========
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.LinkAccount.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== 계정 연동 ==========
        composable(Screen.LinkAccount.route) {
            LinkAccountScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // ========== 코인 지갑 ==========
        composable(Screen.CoinWallet.route) {
            CoinWalletScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== 디버그 (개발 모드만) ==========
        if (BuildConfig.DEBUG) {
            composable("debug_test") {
                DebugTestScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}