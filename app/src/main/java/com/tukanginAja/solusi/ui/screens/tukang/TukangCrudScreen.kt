package com.tukanginAja.solusi.ui.screens.tukang

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tukanginAja.solusi.data.model.TukangLocation

/**
 * CRUD Screen for managing Tukang data in Firestore
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TukangCrudScreen(
    viewModel: TukangCrudViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Form state
    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("offline") }
    
    // Edit state
    var editingTukang by remember { mutableStateOf<TukangLocation?>(null) }
    
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
                title = { Text("Manajemen Tukang") },
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
            // Form Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (editingTukang == null) "Tambah Tukang Baru" else "Edit Tukang",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = lat,
                            onValueChange = { lat = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = lng,
                            onValueChange = { lng = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    // Status dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Online") },
                                onClick = {
                                    selectedStatus = "online"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Offline") },
                                onClick = {
                                    selectedStatus = "offline"
                                    expanded = false
                                }
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (editingTukang != null) {
                            OutlinedButton(
                                onClick = {
                                    editingTukang = null
                                    name = ""
                                    lat = ""
                                    lng = ""
                                    selectedStatus = "offline"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Batal")
                            }
                        }
                        
                        Button(
                            onClick = {
                                val latVal = lat.toDoubleOrNull() ?: 0.0
                                val lngVal = lng.toDoubleOrNull() ?: 0.0
                                
                                if (name.isNotBlank() && latVal != 0.0 && lngVal != 0.0) {
                                    if (editingTukang != null) {
                                        // Update existing
                                        val updatedTukang = editingTukang!!.copy(
                                            name = name,
                                            lat = latVal,
                                            lng = lngVal,
                                            status = selectedStatus
                                        )
                                        viewModel.updateTukang(updatedTukang)
                                        editingTukang = null
                                    } else {
                                        // Add new
                                        viewModel.addTukang(name, latVal, lngVal)
                                    }
                                    
                                    // Clear form
                                    name = ""
                                    lat = ""
                                    lng = ""
                                    selectedStatus = "offline"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading && name.isNotBlank()
                        ) {
                            Text(if (editingTukang == null) "Tambah Tukang" else "Update Tukang")
                        }
                    }
                }
            }
            
            // List Section
            if (uiState.isLoading && uiState.tukangList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.tukangList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada tukang. Tambahkan tukang baru.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tukangList) { tukang ->
                        TukangCrudItemCard(
                            tukang = tukang,
                            onEdit = {
                                editingTukang = tukang
                                name = tukang.name
                                lat = tukang.lat.toString()
                                lng = tukang.lng.toString()
                                selectedStatus = tukang.status
                            },
                            onDelete = {
                                viewModel.deleteTukang(tukang.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TukangCrudItemCard(
    tukang: TukangLocation,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🧑 ${tukang.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 ${tukang.lat}, ${tukang.lng}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
            if (tukang.updatedAt > 0) {
                Text(
                    text = "🕒 Updated: ${formatTimestamp(tukang.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

