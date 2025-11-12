package com.buyoungsil.checkcheck.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.buyoungsil.checkcheck.feature.auth.presentation.link.LinkAccountScreen
import com.buyoungsil.checkcheck.feature.group.presentation.create.CreateGroupScreen
import com.buyoungsil.checkcheck.feature.group.presentation.detail.GroupDetailScreen
import com.buyoungsil.checkcheck.feature.group.presentation.join.JoinGroupScreen
import com.buyoungsil.checkcheck.feature.group.presentation.list.GroupListScreen
import com.buyoungsil.checkcheck.feature.habit.presentation.create.CreateHabitScreen
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitListScreen
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
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToHabitCreate = {
                    navController.navigate(Screen.HabitCreate.createRoute())
                },
                onNavigateToGroupList = {
                    navController.navigate(Screen.GroupList.route)
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHabitList = {  // ✅ 추가
                    navController.navigate(Screen.HabitList.route)
                },
                onNavigateToPersonalTaskCreate = {  // ✅ 추가
                    navController.navigate(Screen.PersonalTaskCreate.route)
                }
            )
        }

        // ========== Habit 화면들 ==========
        composable(Screen.HabitList.route) {
            HabitListScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.HabitCreate.createRoute())
                },
                onNavigateBack = {  // ✅ 이미 추가했어야 함
                    navController.popBackStack()
                }
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
                }
            )
        }

        // ========== Task 화면들 ==========
        composable(
            route = Screen.TaskList.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) {
            TaskListScreen(
                onNavigateToCreate = {
                    val groupId = it.arguments?.getString("groupId") ?: ""
                    navController.navigate(Screen.TaskCreate.createRoute(groupId))
                }
            )
        }

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

        // ========== 통계 ==========
        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }

        // ========== 설정 (✨ 추가) ==========
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

        // ========== 계정 연동 (✨ 추가) ==========
        composable(Screen.LinkAccount.route) {
            LinkAccountScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    // 연동 성공 시 설정 화면으로 돌아가기
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PersonalTaskCreate.route) {
            CreateTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}