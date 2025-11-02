package com.buyoungsil.checkcheck.core.ui.navigation

sealed class Screen(val route: String) {
    // Home
    object Home : Screen("home")

    // Habit
    object HabitList : Screen("habit_list")
    object HabitCreate : Screen("habit_create?groupId={groupId}") {  // ← 수정
        fun createRoute(groupId: String? = null) =
            if (groupId != null) "habit_create?groupId=$groupId"
            else "habit_create"
    }
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }

    // Group
    object GroupList : Screen("group_list")
    object GroupCreate : Screen("group_create")
    object GroupJoin : Screen("group_join")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }

    // Task
    object TaskList : Screen("task_list/{groupId}") {
        fun createRoute(groupId: String) = "task_list/$groupId"
    }
    object TaskCreate : Screen("task_create/{groupId}") {
        fun createRoute(groupId: String) = "task_create/$groupId"
    }

    object Statistics : Screen("statistics")
}