package com.tukanginAja.solusi.ui.screens.request

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tukanginAja.solusi.data.model.TukangLocation
import com.tukanginAja.solusi.ui.navigation.Screen
import com.tukanginAja.solusi.ui.screens.chat.ChatViewModel

/**
 * Request Screen for customers to send service requests to tukang
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestScreen(
    tukang: TukangLocation,
    customerId: String = "u001", // Should be passed from parent or retrieved from auth
    navController: NavController? = null,
    viewModel: RequestViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Form state
    var description by remember { mutableStateOf("") }
    
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
                title = { Text("Request Tukang") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tukang info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🧑 ${tukang.name}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "📍 ${tukang.lat}, ${tukang.lng}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = if (tukang.isOnline) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (tukang.isOnline) "🔵 Online" else "⚪ Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (tukang.isOnline) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Request form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Kirim Permintaan Layanan",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi Pekerjaan") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6,
                        placeholder = { Text("Contoh: Servis AC bocor di daerah Setiabudi...") }
                    )
                    
                    Button(
                        onClick = {
                            if (description.isNotBlank()) {
                                viewModel.createRequest(customerId, tukang, description)
                                description = "" // Clear form after submission
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && description.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Kirim Permintaan")
                    }
                }
            }
            
            // Info message
            Text(
                text = "Permintaan Anda akan dikirim ke tukang. Tunggu konfirmasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Chat button (shown after successful request)
            if (navController != null && uiState.successMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        chatViewModel.createChatIfNotExists(customerId, tukang.id) { chatId ->
                            navController.navigate("${Screen.Chat.route}/$chatId/$customerId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mulai Chat")
                }
            }
        }
    }
}

