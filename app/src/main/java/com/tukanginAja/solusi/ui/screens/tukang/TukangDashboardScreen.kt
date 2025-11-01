package com.tukanginAja.solusi.ui.screens.tukang

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.tukanginAja.solusi.data.model.ServiceRequest
import com.tukanginAja.solusi.service.BackgroundLocationService
import com.tukanginAja.solusi.ui.navigation.Screen
import kotlinx.coroutines.launch

/**
 * Dashboard Tukang Screen
 * Shows active orders for tukang with options to complete orders
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TukangDashboardScreen(
    navController: NavController,
    tukangId: String,
    tukangName: String,
    viewModel: TukangDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Load active orders when screen is displayed
    LaunchedEffect(tukangId) {
        viewModel.loadActiveOrders(tukangId)
    }
    
    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Tukang") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.activeOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.activeOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📋",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "Tidak ada order aktif",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Saat ini tidak ada order yang perlu ditangani",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.activeOrders) { order ->
                    ActiveOrderCard(
                        order = order,
                        navController = navController,
                        onCompleteOrder = { routeData, startLoc, endLoc ->
                            viewModel.completeOrder(order, routeData, startLoc, endLoc)
                        },
                        onStartTracking = {
                            // TAHAP 16: Pass orderId saat start tracking
                            startBackgroundTracking(context, tukangId, tukangName, order.id)
                        },
                        onStopTracking = {
                            stopBackgroundTracking(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveOrderCard(
    order: ServiceRequest,
    navController: NavController,
    onCompleteOrder: (com.tukanginAja.solusi.data.repository.RouteData?, Pair<Double, Double>, Pair<Double, Double>) -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Pelanggan: ${order.customerId.take(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status badge
                Surface(
                    color = when (order.status) {
                        "accepted" -> MaterialTheme.colorScheme.primaryContainer
                        "in_progress" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (order.status) {
                            "accepted" -> "✅ Diterima"
                            "in_progress" -> "🚗 Dalam Perjalanan"
                            else -> order.status
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (order.status) {
                            "accepted" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "in_progress" -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            HorizontalDivider()
            
            // Description
            Text(
                text = "📝 Deskripsi:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = order.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Timestamp
            if (order.timestamp > 0) {
                Text(
                    text = "🕒 ${formatTimestamp(order.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // For accepted orders, show "Mulai Perjalanan" button
                if (order.status == "accepted") {
                    Button(
                        onClick = {
                            onStartTracking()
                            // Navigate to route screen
                            navController.navigate(Screen.Route.createRoute(
                                tukangId = order.tukangId,
                                userLat = -6.2088, // TODO: Get from user location
                                userLng = 106.8456
                            ))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("🚗 Mulai Perjalanan")
                    }
                }
                
                // For in_progress orders, show "Selesaikan Order" button
                if (order.status == "in_progress" || order.status == "accepted") {
                    Button(
                        onClick = { showCompleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("✅ Selesaikan Order")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.Route.createRoute(
                                tukangId = order.tukangId,
                                userLat = -6.2088, // TODO: Get from user location
                                userLng = 106.8456
                            ))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🗺️ Lihat Rute")
                    }
                }
            }
        }
    }
    
    // Complete order confirmation dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Selesaikan Order?") },
            text = {
                Text("Apakah Anda yakin ingin menyelesaikan order ini?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteDialog = false
                        // TODO: Get actual route data, start and end locations
                        onCompleteOrder(null, Pair(-6.2088, 106.8456), Pair(-6.2090, 106.8460))
                        onStopTracking()
                    }
                ) {
                    Text("Ya, Selesaikan")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCompleteDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Start background location tracking
 * TAHAP 16: Tambahkan orderId untuk monitoring status
 */
private fun startBackgroundTracking(context: Context, tukangId: String, tukangName: String, orderId: String? = null) {
    val intent = Intent(context, BackgroundLocationService::class.java).apply {
        action = BackgroundLocationService.ACTION_START_TRACKING
        putExtra(BackgroundLocationService.EXTRA_TUKANG_ID, tukangId)
        putExtra(BackgroundLocationService.EXTRA_TUKANG_NAME, tukangName)
        // TAHAP 16: Pass orderId untuk monitoring status
        if (orderId != null) {
            putExtra(BackgroundLocationService.EXTRA_ORDER_ID, orderId)
        }
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ContextCompat.startForegroundService(context, intent)
    } else {
        context.startService(intent)
    }
}

/**
 * Stop background location tracking
 */
private fun stopBackgroundTracking(context: Context) {
    val intent = Intent(context, BackgroundLocationService::class.java).apply {
        action = BackgroundLocationService.ACTION_STOP_TRACKING
    }
    context.stopService(intent)
}

private fun formatTimestamp(timestamp: Long): String {
    val seconds = timestamp / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "$days hari yang lalu"
        hours > 0 -> "$hours jam yang lalu"
        minutes > 0 -> "$minutes menit yang lalu"
        else -> "Baru saja"
    }
}
