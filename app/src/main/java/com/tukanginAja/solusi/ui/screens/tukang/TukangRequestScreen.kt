package com.tukanginAja.solusi.ui.screens.tukang

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tukanginAja.solusi.data.model.ServiceRequest
import com.tukanginAja.solusi.ui.screens.request.RequestViewModel
import com.tukanginAja.solusi.ui.screens.chat.ChatViewModel
import com.tukanginAja.solusi.ui.navigation.Screen

/**
 * Tukang Request Screen - Shows incoming requests for tukang
 * Tukang can accept or decline requests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TukangRequestScreen(
    navController: NavController? = null,
    tukangId: String = "t001", // Should be passed from parent or retrieved from auth
    userLat: Double = -6.2088, // Default to Jakarta
    userLng: Double = 106.8456,
    viewModel: RequestViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Observe requests for current tukang
    LaunchedEffect(tukangId) {
        viewModel.observeRequestsForTukang(tukangId)
    }
    
    // Show success/error messages via Snackbar
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar("Error: $error", duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permintaan Masuk") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.requestList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.requestList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Belum ada permintaan masuk",
                        style = MaterialTheme.typography.bodyLarge,
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
                items(uiState.requestList) { request ->
                    TukangRequestItemCard(
                        request = request,
                        navController = navController,
                        userLat = userLat,
                        userLng = userLng,
                        chatViewModel = chatViewModel,
                        currentUserId = tukangId,
                        onAccept = {
                            viewModel.updateRequestStatus(request.id, "accepted")
                        },
                        onDecline = {
                            viewModel.updateRequestStatus(request.id, "declined")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TukangRequestItemCard(
    request: ServiceRequest,
    navController: NavController? = null,
    userLat: Double = -6.2088,
    userLng: Double = 106.8456,
    chatViewModel: ChatViewModel? = null,
    currentUserId: String = "t001",
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
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
                Text(
                    text = "Pelanggan: ${request.customerId}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Status badge
                Surface(
                    color = when (request.status) {
                        "pending" -> MaterialTheme.colorScheme.tertiaryContainer
                        "accepted" -> MaterialTheme.colorScheme.primaryContainer
                        "declined" -> MaterialTheme.colorScheme.errorContainer
                        "completed" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (request.status) {
                            "pending" -> "⏳ Pending"
                            "accepted" -> "✅ Accepted"
                            "declined" -> "❌ Declined"
                            "completed" -> "✔️ Completed"
                            else -> request.status
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (request.status) {
                            "pending" -> MaterialTheme.colorScheme.onTertiaryContainer
                            "accepted" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "declined" -> MaterialTheme.colorScheme.onErrorContainer
                            "completed" -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                text = request.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Timestamp
            if (request.timestamp > 0) {
                Text(
                    text = "🕒 ${formatTimestamp(request.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons (only show for pending requests)
            if (request.isPending) {
                HorizontalDivider()
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Terima")
                        }
                        
                        OutlinedButton(
                            onClick = onDecline,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Tolak")
                        }
                    }
                    
                    // Navigate to route button for accepted requests (temporary - will be shown after accept)
                }
            }
            
            // Show action buttons for accepted requests
            if (request.isAccepted && navController != null) {
                HorizontalDivider()
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.Route.createRoute(
                                tukangId = request.tukangId,
                                userLat = userLat,
                                userLng = userLng
                            ))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Lihat Rute")
                    }
                    
                    // Chat button
                    if (chatViewModel != null) {
                        Button(
                            onClick = {
                                chatViewModel.createChatIfNotExists(
                                    userId = request.customerId,
                                    tukangId = currentUserId
                                ) { chatId ->
                                    navController.navigate("${Screen.Chat.route}/$chatId/$currentUserId")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Mulai Chat")
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val seconds = timestamp / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "$hours jam yang lalu"
        minutes > 0 -> "$minutes menit yang lalu"
        else -> "Baru saja"
    }
}

