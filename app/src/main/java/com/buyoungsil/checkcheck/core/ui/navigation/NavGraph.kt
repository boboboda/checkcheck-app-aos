package com.buyoungsil.checkcheck.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.buyoungsil.checkcheck.feature.group.presentation.create.CreateGroupScreen
import com.buyoungsil.checkcheck.feature.group.presentation.detail.GroupDetailScreen
import com.buyoungsil.checkcheck.feature.group.presentation.join.JoinGroupScreen
import com.buyoungsil.checkcheck.feature.group.presentation.list.GroupListScreen
import com.buyoungsil.checkcheck.feature.habit.presentation.create.CreateHabitScreen
import com.buyoungsil.checkcheck.feature.habit.presentation.list.HabitListScreen
import com.buyoungsil.checkcheck.feature.home.HomeScreen
import com.buyoungsil.checkcheck.feature.statistics.StatisticsScreen
import com.buyoungsil.checkcheck.feature.task.presentation.create.CreateTaskScreen
import com.buyoungsil.checkcheck.feature.task.presentation.list.TaskListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier  // ← 추가
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
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))  // ✅ 수정
                }
            )
        }

        // ========== Habit 화면들 ==========
        composable(Screen.HabitList.route) {
            HabitListScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.HabitCreate.createRoute())
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
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))  // ✅ 수정
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

        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }
    }
}