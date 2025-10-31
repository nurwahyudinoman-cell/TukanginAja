package com.tukanginAja.solusi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tukanginAja.solusi.ui.screens.auth.LoginScreen
import com.tukanginAja.solusi.ui.screens.auth.RegisterScreen
import com.tukanginAja.solusi.ui.screens.chat.ChatScreen
import com.tukanginAja.solusi.ui.screens.home.HomeScreen
import com.tukanginAja.solusi.ui.screens.more.MoreScreen
import com.tukanginAja.solusi.ui.screens.orders.OrdersScreen
import com.tukanginAja.solusi.ui.screens.profile.ProfileScreen
import com.tukanginAja.solusi.ui.screens.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Orders : Screen("orders")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object More : Screen("more")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen()
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen()
        }
        
        composable(Screen.Orders.route) {
            OrdersScreen()
        }
        
        composable(Screen.Chat.route) {
            ChatScreen()
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    onSignOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.More.route) {
            MoreScreen()
        }
    }
}

