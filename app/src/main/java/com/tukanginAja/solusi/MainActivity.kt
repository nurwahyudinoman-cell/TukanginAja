package com.tukanginAja.solusi

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.tukanginAja.solusi.data.repository.AuthRepository
import com.tukanginAja.solusi.domain.usecase.auth.SignOutUseCase
import com.tukanginAja.solusi.ui.components.BottomNavigationBar
import com.tukanginAja.solusi.ui.navigation.NavGraph
import com.tukanginAja.solusi.ui.navigation.Screen
import com.tukanginAja.solusi.ui.theme.TukanginAjaTheme
import com.tukanginAja.solusi.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var signOutUseCase: SignOutUseCase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
        
        // Initialize FCM token
        initializeFCMToken()
        
        setContent {
            TukanginAjaTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val isLoggedIn = authRepository.isUserLoggedIn()
                
                // Get notification intent data
                val notificationChatId = remember { intent.getStringExtra("chatId") }
                val notificationNavigateTo = remember { intent.getStringExtra("navigateTo") }
                
                // Handle splash screen navigation based on auth state
                LaunchedEffect(isLoggedIn, notificationChatId) {
                    delay(Constants.SPLASH_DELAY) // Show splash for 2 seconds
                    if (!isLoggedIn) {
                        if (currentRoute != Screen.Login.route && currentRoute != Screen.Register.route) {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    } else {
                        // Check if we need to navigate to chat from notification
                        if (notificationNavigateTo == "chat" && notificationChatId != null) {
                            // Navigate to chat screen
                            val currentUserId = authRepository.currentUser?.uid ?: ""
                            navController.navigate("${Screen.Chat.route}/$notificationChatId/$currentUserId") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            if (currentRoute != Screen.Home.route && currentRoute != Screen.Orders.route && 
                                currentRoute != Screen.Chat.route && currentRoute != Screen.Profile.route && 
                                currentRoute != Screen.More.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (isLoggedIn && currentRoute in listOf(
                                Screen.Home.route,
                                Screen.Orders.route,
                                Screen.Chat.route,
                                Screen.Profile.route,
                                Screen.More.route
                            )
                        ) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        onSignOut = {
                            CoroutineScope(Dispatchers.Main).launch {
                                signOutUseCase()
                            }
                        },
                        authRepository = authRepository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    /**
     * Initialize FCM token and save to shared preferences
     */
    private fun initializeFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                
                // Token will be automatically saved to Firestore by MyFirebaseMessagingService
                // when user logs in via onNewToken callback
                
                // Also save to shared preferences for quick access
                val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                prefs.edit().putString("fcmToken", token).apply()
            } else {
                Log.e("FCM", "Failed to get FCM token", task.exception)
            }
        }
    }
    
}
