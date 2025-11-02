package com.tukanginAja.solusi.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tukanginAja.solusi.data.model.ServiceRequest
import com.tukanginAja.solusi.data.repository.AuthRepository
import com.tukanginAja.solusi.ui.navigation.Screen
import com.tukanginAja.solusi.ui.screens.user.UserDashboardViewModel
import com.tukanginAja.solusi.utils.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    authRepository: AuthRepository? = null,
    viewModel: UserDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = authRepository?.currentUser?.uid ?: ""
    
    val isRefreshing = uiState.isLoading
    
    // Load user orders when screen appears
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadUserOrders(currentUserId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👤 User Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (navController != null) {
                        IconButton(
                            onClick = { navController.navigate(Screen.TukangList.route) }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Buat Order")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (navController != null && !uiState.isLoading) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.TukangList.route) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Buat Order")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading && uiState.orders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.orders.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📋",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada pesanan yang kamu buat.",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mulai dengan mencari tukang terbaik untuk kebutuhanmu!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        if (navController != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { navController.navigate(Screen.TukangList.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("➕ Buat Order")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "📋 Daftar Pesanan Anda",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = "${uiState.orders.size} pesanan ditemukan",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (navController != null) {
                                    TextButton(
                                        onClick = { navController.navigate(Screen.TukangList.route) }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Buat Order")
                                    }
                                }
                            }
                        }
                        
                        items(uiState.orders) { order ->
                            UserOrderCard(order = order)
                        }
                    }
                }
            }
        }
        
        // Error message
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar or error message
            }
        }
    }
}

@Composable
private fun UserOrderCard(order: ServiceRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pesanan #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (order.tukangName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "👷 Tukang: ${order.tukangName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status badge with improved colors
                StatusBadge(status = order.status)
            }
            
            HorizontalDivider()
            
            if (order.description.isNotEmpty()) {
                Text(
                    text = "📝 ${order.description}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (order.timestamp > 0) {
                Text(
                    text = "🕒 ${DateFormatter.formatTimestamp(order.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (statusText, statusColor) = when (status.lowercase()) {
        "pending", "waiting" -> "⏳ Menunggu" to Color(0xFFFFB300) // Yellow
        "accepted" -> "✅ Diterima" to Color(0xFF4CAF50) // Green
        "in_progress", "inprogress" -> "🚗 Sedang Dikerjakan" to Color(0xFF2196F3) // Blue
        "completed", "done" -> "✅ Selesai" to Color(0xFF4CAF50) // Green
        "declined" -> "❌ Ditolak" to Color(0xFFF44336) // Red
        else -> status to MaterialTheme.colorScheme.surfaceVariant
    }
    
    Surface(
        color = statusColor.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
