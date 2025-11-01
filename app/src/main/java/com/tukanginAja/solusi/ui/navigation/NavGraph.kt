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
import com.tukanginAja.solusi.ui.screens.MapScreen
import com.tukanginAja.solusi.ui.screens.tukang.TukangListScreen
import com.tukanginAja.solusi.ui.screens.tukang.TukangCrudScreen
import com.tukanginAja.solusi.ui.screens.tukang.TukangRequestScreen
import com.tukanginAja.solusi.ui.screens.tukang.TukangDashboardScreen
import com.tukanginAja.solusi.ui.screens.map.TukangMapScreen
import com.tukanginAja.solusi.ui.screens.request.RequestScreen
import com.tukanginAja.solusi.ui.screens.route.RouteScreen
import com.tukanginAja.solusi.ui.screens.chat.ChatScreen
import com.tukanginAja.solusi.data.model.TukangLocation
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Orders : Screen("orders")
    object Profile : Screen("profile")
    object More : Screen("more")
    object Map : Screen("map")
    object TukangList : Screen("tukang_list")
    object TukangMap : Screen("tukang_map")
    object TukangCrud : Screen("tukang_crud")
    object Request : Screen("request/{tukangId}/{tukangName}/{lat}/{lng}") {
        fun createRoute(tukangId: String, tukangName: String, lat: Double, lng: Double) = "request/$tukangId/$tukangName/$lat/$lng"
    }
    object TukangRequest : Screen("tukang_request")
    object TukangDashboard : Screen("tukang_dashboard/{tukangId}/{tukangName}") {
        fun createRoute(tukangId: String, tukangName: String) = "tukang_dashboard/$tukangId/$tukangName"
    }
    object Route : Screen("route/{tukangId}/{userLat}/{userLng}") {
        fun createRoute(tukangId: String, userLat: Double, userLng: Double) = "route/$tukangId/$userLat/$userLng"
    }
    object Chat : Screen("chat/{chatId}/{currentUserId}")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    onSignOut: () -> Unit,
    authRepository: com.tukanginAja.solusi.data.repository.AuthRepository,
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
                    // Clear back stack and navigate to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
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
                    // Clear back stack and navigate to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
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
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Orders.route) {
            OrdersScreen()
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("currentUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val currentUserId = backStackEntry.arguments?.getString("currentUserId") ?: authRepository.currentUser?.uid ?: ""
            
            ChatScreen(
                chatId = chatId,
                currentUserId = currentUserId
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    onSignOut()
                    // Clear entire back stack and navigate to Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(Screen.More.route) {
            MoreScreen()
        }
        
        composable(Screen.Map.route) {
            MapScreen()
        }
        
        composable(Screen.TukangList.route) {
            TukangListScreen(
                navController = navController,
                customerId = authRepository.currentUser?.uid ?: "u001"
            )
        }
        
        composable(Screen.TukangMap.route) {
            TukangMapScreen()
        }
        
        composable(Screen.TukangCrud.route) {
            TukangCrudScreen()
        }
        
        composable(
            route = Screen.Request.route,
            arguments = listOf(
                navArgument("tukangId") { type = NavType.StringType },
                navArgument("tukangName") { type = NavType.StringType },
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tukangId = backStackEntry.arguments?.getString("tukangId") ?: ""
            val tukangName = backStackEntry.arguments?.getString("tukangName") ?: ""
            val latStr = backStackEntry.arguments?.getString("lat") ?: "0.0"
            val lngStr = backStackEntry.arguments?.getString("lng") ?: "0.0"
            
            val tukang = TukangLocation(
                id = tukangId,
                name = tukangName,
                lat = latStr.toDoubleOrNull() ?: 0.0,
                lng = lngStr.toDoubleOrNull() ?: 0.0
            )
            
            RequestScreen(
                tukang = tukang,
                customerId = authRepository.currentUser?.uid ?: "u001",
                navController = navController
            )
        }
        
        composable(Screen.TukangRequest.route) {
            TukangRequestScreen(
                navController = navController,
                tukangId = "t001", // TODO: Get from auth or tukang profile
                userLat = -6.2088, // Default to Jakarta - TODO: Get from user location
                userLng = 106.8456
            )
        }
        
        composable(
            route = Screen.TukangDashboard.route,
            arguments = listOf(
                navArgument("tukangId") { type = NavType.StringType },
                navArgument("tukangName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tukangId = backStackEntry.arguments?.getString("tukangId") ?: ""
            val tukangName = backStackEntry.arguments?.getString("tukangName") ?: ""
            
            TukangDashboardScreen(
                navController = navController,
                tukangId = tukangId,
                tukangName = tukangName
            )
        }
        
        composable(
            route = Screen.Route.route,
            arguments = listOf(
                navArgument("tukangId") { type = NavType.StringType },
                navArgument("userLat") { type = NavType.StringType },
                navArgument("userLng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tukangId = backStackEntry.arguments?.getString("tukangId") ?: ""
            val userLatStr = backStackEntry.arguments?.getString("userLat") ?: "0.0"
            val userLngStr = backStackEntry.arguments?.getString("userLng") ?: "0.0"
            
            val userLat = userLatStr.toDoubleOrNull() ?: 0.0
            val userLng = userLngStr.toDoubleOrNull() ?: 0.0
            
            RouteScreen(
                userLat = userLat,
                userLng = userLng,
                tukangId = tukangId
            )
        }
    }
}

