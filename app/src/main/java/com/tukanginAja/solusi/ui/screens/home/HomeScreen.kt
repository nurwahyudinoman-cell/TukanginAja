package com.tukanginAja.solusi.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tukanginAja.solusi.ui.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Home Screen",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Text(
                text = "Map will be displayed here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Buttons for navigation
            if (navController != null) {
                Button(
                    onClick = {
                        navController.navigate(Screen.Map.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Map")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        navController.navigate(Screen.TukangList.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lihat Daftar Tukang")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        navController.navigate(Screen.TukangMap.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lihat Peta Tukang")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        navController.navigate(Screen.TukangCrud.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kelola Tukang")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        navController.navigate(Screen.TukangRequest.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Permintaan Masuk (Tukang)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Placeholder for Google Maps
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Google Maps\n(Placeholder)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

