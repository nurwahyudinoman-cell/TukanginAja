package com.tukanginAja.solusi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tukanginAja.solusi.data.repository.AuthRepository
import com.tukanginAja.solusi.domain.usecase.auth.SignOutUseCase
import com.tukanginAja.solusi.ui.components.BottomNavigationBar
import com.tukanginAja.solusi.ui.navigation.NavGraph
import com.tukanginAja.solusi.ui.navigation.Screen
import com.tukanginAja.solusi.ui.theme.TukanginAjaTheme
import com.tukanginAja.solusi.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
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
        
        setContent {
            TukanginAjaTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val isLoggedIn = authRepository.isUserLoggedIn()
                
                // Handle splash screen navigation
                LaunchedEffect(Unit) {
                    delay(Constants.SPLASH_DELAY) // Show splash for 2 seconds
                    if (!isLoggedIn) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
