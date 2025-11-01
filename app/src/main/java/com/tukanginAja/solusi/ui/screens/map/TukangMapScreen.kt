package com.tukanginAja.solusi.ui.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.tukanginAja.solusi.data.model.TukangLocation
import com.tukanginAja.solusi.utils.GooglePlayServicesUtil

/**
 * Tukang Map Screen with real-time Firestore integration
 * Displays tukang locations on Google Maps with live updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TukangMapScreen(
    viewModel: TukangMapViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val jakarta = LatLng(-6.2088, 106.8456)
    val scope = rememberCoroutineScope()
    
    // UI State from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    // Google Play Services availability
    var isGooglePlayServicesAvailable by remember { mutableStateOf(true) }
    var googlePlayServicesError by remember { mutableStateOf<String?>(null) }
    
    // Location state
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    
    // Camera auto-follow state
    var autoFollowEnabled by remember { mutableStateOf(false) }
    
    // FusedLocationProviderClient
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Marker map for efficient updates (avoids flicker)
    val markerMap = remember { mutableStateMapOf<String, LatLng>() }
    
    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(jakarta, 12f)
    }
    
    // Check Google Play Services availability
    LaunchedEffect(Unit) {
        isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)
        if (!isGooglePlayServicesAvailable) {
            googlePlayServicesError = GooglePlayServicesUtil.getErrorMessage(context)
        }
    }
    
    // Check location permission
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasLocationPermission) {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
            }
        }
    }
    
    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        
        if (hasLocationPermission) {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
            }
        } else {
            userLocation = jakarta // Default to Jakarta
        }
    }
    
    // Update marker map when tukangList changes (realtime)
    LaunchedEffect(uiState.tukangList) {
        markerMap.clear()
        uiState.tukangList.forEach { tukang ->
            if (tukang.lat != 0.0 && tukang.lng != 0.0) {
                markerMap[tukang.id] = LatLng(tukang.lat, tukang.lng)
            }
        }
    }
    
    // Auto-follow camera to first online tukang
    LaunchedEffect(autoFollowEnabled, uiState.tukangList) {
        if (autoFollowEnabled && uiState.tukangList.isNotEmpty()) {
            val firstOnline = uiState.tukangList.firstOrNull { it.isOnline && it.lat != 0.0 && it.lng != 0.0 }
            firstOnline?.let { tukang ->
                val newPosition = CameraPosition.fromLatLngZoom(
                    LatLng(tukang.lat, tukang.lng),
                    14f
                )
                cameraPositionState.position = newPosition
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peta Tukang") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Auto-follow toggle button
                    IconButton(
                        onClick = { autoFollowEnabled = !autoFollowEnabled }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = if (autoFollowEnabled) "Disable Auto-Follow" else "Enable Auto-Follow",
                            tint = if (autoFollowEnabled) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Request location permission button
            if (!hasLocationPermission) {
                FloatingActionButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Request Location")
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Google Play Services not available
                !isGooglePlayServicesAvailable -> {
                    GooglePlayServicesErrorContent(
                        error = googlePlayServicesError,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Loading state
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat data tukang...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Error state
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Empty state
                uiState.tukangList.isEmpty() -> {
                    EmptyStateContent(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Success state - show map with markers
                else -> {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = hasLocationPermission
                        ),
                        uiSettings = MapUiSettings(
                            myLocationButtonEnabled = hasLocationPermission,
                            zoomControlsEnabled = true,
                            compassEnabled = true
                        )
                    ) {
                        // User location marker (if permission granted)
                        userLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Lokasi Saya",
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_AZURE
                                )
                            )
                        }
                        
                        // Tukang markers (realtime from Firestore)
                        uiState.tukangList.forEach { tukang ->
                            if (tukang.lat != 0.0 && tukang.lng != 0.0) {
                                val position = LatLng(tukang.lat, tukang.lng)
                                Marker(
                                    state = MarkerState(position = position),
                                    title = tukang.name,
                                    snippet = "Status: ${if (tukang.isOnline) "Online" else "Offline"}",
                                    icon = BitmapDescriptorFactory.defaultMarker(
                                        if (tukang.isOnline) 
                                            BitmapDescriptorFactory.HUE_BLUE  // 🔵 Blue for online
                                        else 
                                            BitmapDescriptorFactory.HUE_ORANGE  // ⚪ Gray/Orange for offline
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper function to get user location
 */
@SuppressLint("MissingPermission")
private fun getUserLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(LatLng(it.latitude, it.longitude))
            }
        }
    } catch (e: Exception) {
        // Handle error silently
    }
}

/**
 * Google Play Services error content
 */
@Composable
private fun GooglePlayServicesErrorContent(
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Google Play Services Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error ?: "Google Play Services is not available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Please use an emulator with:\n• Google Play ARM64 v8a system image\n• Google Play Services enabled",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Error state content
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $error",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Empty state content
 */
@Composable
private fun EmptyStateContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Belum ada tukang online",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

