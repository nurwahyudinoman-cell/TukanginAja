package com.tukanginAja.solusi.ui.screens.route

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.tukanginAja.solusi.utils.GooglePlayServicesUtil

/**
 * Route Screen - Displays real-time route between user and tukang on Google Maps
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    userLat: Double,
    userLng: Double,
    tukangId: String,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Google Play Services availability
    var isGooglePlayServicesAvailable by remember { mutableStateOf(true) }
    var googlePlayServicesError by remember { mutableStateOf<String?>(null) }
    
    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(userLat, userLng), 13f)
    }
    
    // Check Google Play Services availability
    LaunchedEffect(Unit) {
        isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)
        if (!isGooglePlayServicesAvailable) {
            googlePlayServicesError = GooglePlayServicesUtil.getErrorMessage(context)
        }
    }
    
    // Start tracking when screen is displayed
    LaunchedEffect(tukangId) {
        if (tukangId.isNotEmpty()) {
            viewModel.startTracking(userLat, userLng, tukangId)
        }
    }
    
    // Update camera position when tukang location or route changes
    LaunchedEffect(uiState.tukangLocation, uiState.routeData) {
        val tukang = uiState.tukangLocation
        if (tukang != null && tukang.lat != 0.0 && tukang.lng != 0.0) {
            // Calculate center point between user and tukang
            val centerLat = (userLat + tukang.lat) / 2
            val centerLng = (userLng + tukang.lng) / 2
            
            // Calculate zoom level to fit both markers
            val latDiff = kotlin.math.abs(userLat - tukang.lat)
            val lngDiff = kotlin.math.abs(userLng - tukang.lng)
            val maxDiff = kotlin.math.max(latDiff, lngDiff)
            
            val zoom = when {
                maxDiff > 0.1 -> 10f
                maxDiff > 0.05 -> 12f
                maxDiff > 0.01 -> 13f
                else -> 14f
            }
            
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(centerLat, centerLng),
                zoom
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Rute Tukang")
                        uiState.routeData?.let { route ->
                            Text(
                                text = "${route.formattedDistance} • ${route.formattedDuration}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isGooglePlayServicesAvailable) {
                // Fallback UI when Google Play Services is not available
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Google Play Services Required",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = googlePlayServicesError ?: "Google Play Services is not available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                // Map display
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false
                    )
                ) {
                    // User location marker (green)
                    Marker(
                        state = MarkerState(position = LatLng(userLat, userLng)),
                        title = "Lokasi Anda",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                    
                    // Tukang location marker (blue)
                    uiState.tukangLocation?.let { tukang ->
                        if (tukang.lat != 0.0 && tukang.lng != 0.0) {
                            Marker(
                                state = MarkerState(position = LatLng(tukang.lat, tukang.lng)),
                                title = tukang.name,
                                snippet = "Status: ${if (tukang.isOnline) "Online" else "Offline"}",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                            )
                        }
                    }
                    
                    // Route polyline (blue line)
                    uiState.routeData?.routePoints?.let { routePoints ->
                        if (routePoints.isNotEmpty()) {
                            val polylinePoints = routePoints.map { (lat, lng) -> 
                                LatLng(lat, lng) 
                            }
                            Polyline(
                                points = polylinePoints,
                                color = Color(0xFF2196F3),
                                width = 8f
                            )
                        }
                    }
                }
                
                // Loading, Error states overlay
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Memuat rute...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Error: ${uiState.error}",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Button(
                                    onClick = {
                                        viewModel.retry(userLat, userLng, tukangId)
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                
                // Route info card (bottom sheet)
                uiState.routeData?.let { route ->
                    if (route.routePoints.isNotEmpty() && !uiState.isLoading) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.BottomCenter),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Informasi Rute",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Jarak",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = route.formattedDistance,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Waktu Tempuh",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = route.formattedDuration,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

