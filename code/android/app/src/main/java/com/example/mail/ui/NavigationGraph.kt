package com.example.mail.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mail.ui.screens.*

// 定义导航路由
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object EmailList : Screen("email_list")
    object EmailDetail : Screen("email_detail/{id}") {
        fun createRoute(id: Long) = "email_detail/$id"
    }
    object ComposeEmail : Screen("compose_email/{emailId}?") {
        fun createRoute(emailId: Long? = null) = if (emailId != null) "compose_email/$emailId" else "compose_email"
    }
    object UserProfile : Screen("user_profile")
    // 管理员相关路由
    object UserManagement : Screen("user_management")
    object MassEmail : Screen("mass_email")
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.EmailList.route) {
            EmailListScreen(navController = navController, initialEmailType = com.example.mail.ui.screens.EmailType.INBOX)
        }
        composable(Screen.EmailDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            EmailDetailScreen(navController = navController, emailId = id)
        }
        composable(Screen.ComposeEmail.route) { backStackEntry ->
            val emailId = backStackEntry.arguments?.getString("emailId")?.toLongOrNull()
            ComposeEmailScreen(navController = navController, draftEmailId = emailId)
        }
        // 管理员相关界面
        composable(Screen.UserManagement.route) {
            UserManagementScreen(navController = navController)
        }
        composable(Screen.MassEmail.route) {
            MassEmailScreen(navController = navController)
        }
        composable(Screen.UserProfile.route) {
            UserProfileScreen(navController = navController)
        }
    }
}