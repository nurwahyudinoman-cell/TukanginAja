package com.tukanginAja.solusi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tukanginAja.solusi.ui.screens.admin.AdminDashboardScreen
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
import com.tukanginAja.solusi.data.model.TukangLocation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tukanginAja.solusi.ui.screens.user.UserOrdersScreen
import com.tukanginAja.solusi.ui.screens.user.UserProfileScreen
import com.tukanginAja.solusi.ui.screens.tukang.TukangOrdersScreen
import com.tukanginAja.solusi.ui.screens.tukang.TukangProfileScreen
import com.tukanginAja.solusi.ui.screens.admin.AdminUserListScreen
import com.tukanginAja.solusi.ui.screens.admin.AdminOrderListScreen
import com.tukanginAja.solusi.ui.screens.admin.AdminReportScreen
import com.tukanginAja.solusi.ui.screens.navigation.RoleErrorScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object UserDashboard : Screen("user_dashboard")
    object TukangDashboard : Screen("tukang_dashboard/{tukangId}/{tukangName}") {
        fun createRoute(tukangId: String, tukangName: String) = "tukang_dashboard/$tukangId/$tukangName"
    }
    object AdminDashboard : Screen("admin_dashboard")
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
    object Route : Screen("route/{tukangId}/{userLat}/{userLng}") {
        fun createRoute(tukangId: String, userLat: Double, userLng: Double) = "route/$tukangId/$userLat/$userLng"
    }
    object Chat : Screen("chat/{chatId}/{currentUserId}")
    object RoleError : Screen("role_error")
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
                onLoginSuccess = { role ->
                    // Navigate based on user role
                    val destination = when (role) {
                        "user" -> Screen.UserDashboard.route
                        "tukang" -> {
                            // Navigate to tukang dashboard with actual user data
                            val tukangId = authRepository.currentUser?.uid ?: "tukang_unknown"
                            val tukangName = authRepository.currentUser?.email?.split("@")?.firstOrNull() ?: "Tukang User"
                            Screen.TukangDashboard.createRoute(tukangId, tukangName)
                        }
                        "admin" -> Screen.AdminDashboard.route
                        else -> Screen.RoleError.route
                    }
                    navController.navigate(destination) {
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
                onRegisterSuccess = { role ->
                    // Navigate based on user role (new registrations default to "user")
                    val destination = when (role) {
                        "user" -> Screen.UserDashboard.route
                        "tukang" -> {
                            // Navigate to tukang dashboard with default values
                            val userId = authRepository.currentUser?.uid ?: "tukang_001"
                            val userName = authRepository.currentUser?.email?.split("@")?.firstOrNull() ?: "Tukang User"
                            Screen.TukangDashboard.createRoute(userId, userName)
                        }
                        "admin" -> Screen.AdminDashboard.route
                        else -> Screen.UserDashboard.route // Default to UserDashboard for new registrations
                    }
                    navController.navigate(destination) {
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
            HomeScreen(
                navController = navController,
                authRepository = authRepository
            )
        }

        composable(Screen.UserDashboard.route) {
            HomeScreen(
                navController = navController,
                authRepository = authRepository
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }

        composable(Screen.RoleError.route) {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "❌ Role tidak ditemukan",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Silakan hubungi administrator",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
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

/**
 * Main Navigation Graph - Routes to role-specific navigation graphs
 */
@Composable
fun MainNavGraph(
    navController: NavHostController,
    role: String,
    onSignOut: () -> Unit,
    authRepository: com.tukanginAja.solusi.data.repository.AuthRepository,
    modifier: Modifier = Modifier
) {
    when (role) {
        "user" -> UserNavGraph(navController, onSignOut, authRepository, modifier)
        "tukang" -> TukangNavGraph(navController, onSignOut, authRepository, modifier)
        "admin" -> AdminNavGraph(navController, onSignOut, authRepository, modifier)
        else -> {
            NavHost(navController, startDestination = "role_error", modifier = modifier) {
                composable("role_error") {
                    RoleErrorScreen()
                }
            }
        }
    }
}

/**
 * User Navigation Graph
 */
@Composable
fun UserNavGraph(
    navController: NavHostController,
    onSignOut: () -> Unit,
    authRepository: com.tukanginAja.solusi.data.repository.AuthRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "user_home",
        modifier = modifier
    ) {
        composable("user_home") {
            HomeScreen(
                navController = navController,
                authRepository = authRepository
            )
        }
        composable("user_orders") {
            UserOrdersScreen(
                navController = navController
            )
        }
        composable("user_profile") {
            UserProfileScreen(
                navController = navController,
                onSignOut = {
                    onSignOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        // Support legacy routes
        composable(Screen.UserDashboard.route) {
            HomeScreen(
                navController = navController,
                authRepository = authRepository
            )
        }
        composable(Screen.Orders.route) {
            UserOrdersScreen(
                navController = navController
            )
        }
        composable(Screen.Profile.route) {
            UserProfileScreen(
                navController = navController,
                onSignOut = {
                    onSignOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.TukangList.route) {
            TukangListScreen(
                navController = navController,
                customerId = authRepository.currentUser?.uid ?: "u001"
            )
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
    }
}

/**
 * Tukang Navigation Graph
 */
@Composable
fun TukangNavGraph(
    navController: NavHostController,
    onSignOut: () -> Unit,
    authRepository: com.tukanginAja.solusi.data.repository.AuthRepository,
    modifier: Modifier = Modifier
) {
    val tukangId = authRepository.currentUser?.uid ?: "tukang_unknown"
    val tukangName = authRepository.currentUser?.email?.split("@")?.firstOrNull() ?: "Tukang User"
    
    NavHost(
        navController = navController,
        startDestination = Screen.TukangDashboard.createRoute(tukangId, tukangName),
        modifier = modifier
    ) {
        composable("tukang_home") {
            // Show tukang dashboard directly
            TukangDashboardScreen(
                navController = navController,
                tukangId = tukangId,
                tukangName = tukangName
            )
        }
        composable("tukang_orders") {
            TukangOrdersScreen(
                navController = navController,
                tukangId = tukangId
            )
        }
        composable("tukang_profile") {
            TukangProfileScreen(
                navController = navController,
                onSignOut = {
                    onSignOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        // Support legacy tukang dashboard route
        composable(
            route = Screen.TukangDashboard.route,
            arguments = listOf(
                navArgument("tukangId") { type = NavType.StringType },
                navArgument("tukangName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("tukangId") ?: tukangId
            val name = backStackEntry.arguments?.getString("tukangName") ?: tukangName
            
            TukangDashboardScreen(
                navController = navController,
                tukangId = id,
                tukangName = name
            )
        }
        composable(Screen.TukangRequest.route) {
            TukangRequestScreen(
                navController = navController,
                tukangId = tukangId,
                userLat = -6.2088,
                userLng = 106.8456
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
            val id = backStackEntry.arguments?.getString("tukangId") ?: tukangId
            val userLatStr = backStackEntry.arguments?.getString("userLat") ?: "0.0"
            val userLngStr = backStackEntry.arguments?.getString("userLng") ?: "0.0"
            
            val userLat = userLatStr.toDoubleOrNull() ?: 0.0
            val userLng = userLngStr.toDoubleOrNull() ?: 0.0
            
            RouteScreen(
                userLat = userLat,
                userLng = userLng,
                tukangId = id
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("currentUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val currentUserId = backStackEntry.arguments?.getString("currentUserId") ?: tukangId
            
            ChatScreen(
                chatId = chatId,
                currentUserId = currentUserId
            )
        }
    }
}

/**
 * Admin Navigation Graph
 */
@Composable
fun AdminNavGraph(
    navController: NavHostController,
    onSignOut: () -> Unit,
    authRepository: com.tukanginAja.solusi.data.repository.AuthRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "admin_home",
        modifier = modifier
    ) {
        composable("admin_home") {
            AdminDashboardScreen(navController = navController)
        }
        composable("admin_users") {
            AdminUserListScreen(navController = navController)
        }
        composable("admin_orders") {
            AdminOrderListScreen(navController = navController)
        }
        composable("admin_reports") {
            AdminReportScreen(navController = navController)
        }
        // Support legacy admin dashboard route
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
    }
}

