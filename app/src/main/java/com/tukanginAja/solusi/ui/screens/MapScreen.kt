package com.tukanginAja.solusi.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.maps.android.compose.*
import com.tukanginAja.solusi.data.model.TukangLocation
import com.tukanginAja.solusi.utils.GooglePlayServicesUtil

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val jakarta = LatLng(-6.2088, 106.8456)
    
    // Google Play Services availability
    var isGooglePlayServicesAvailable by remember { mutableStateOf(true) }
    var googlePlayServicesError by remember { mutableStateOf<String?>(null) }
    var showGooglePlayServicesDialog by remember { mutableStateOf(false) }
    
    // Location state
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Tukang locations from Firestore
    var tukangLocations by remember { mutableStateOf<List<TukangLocation>>(emptyList()) }
    
    // FusedLocationProviderClient
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Check Google Play Services availability
    LaunchedEffect(Unit) {
        isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)
        if (!isGooglePlayServicesAvailable) {
            googlePlayServicesError = GooglePlayServicesUtil.getErrorMessage(context)
            showGooglePlayServicesDialog = true
        }
    }
    
    // Check permission
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        
        if (!hasLocationPermission) {
            showPermissionDialog = true
            userLocation = jakarta // Default to Jakarta
        } else {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
            }
        }
    }
    
    // Request permission on first launch
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission && !showPermissionDialog) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (hasLocationPermission) {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
            }
        } else {
            userLocation = jakarta // Default to Jakarta if permission denied
        }
    }
    
    // Listen to Firestore changes
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val registration = db.collection("tukang_locations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val locations = snapshot.documents.mapNotNull { doc ->
                        try {
                            TukangLocation(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                lat = doc.getDouble("lat") ?: 0.0,
                                lng = doc.getDouble("lng") ?: 0.0,
                                status = doc.getString("status") ?: "offline",
                                updatedAt = doc.getLong("updatedAt") ?: 0L
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    tukangLocations = locations
                }
            }
        
        // Cleanup on dispose
        onDispose {
            registration.remove()
        }
    }
    
    // Camera position - initialize with Jakarta, update when user location is available
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(jakarta, 14f)
    }
    
    // Update camera when user location changes
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            val newPosition = CameraPosition.fromLatLngZoom(location, 14f)
            cameraPositionState.position = newPosition
        }
    }
    
    // Google Play Services error dialog
    if (showGooglePlayServicesDialog && googlePlayServicesError != null) {
        AlertDialog(
            onDismissRequest = { showGooglePlayServicesDialog = false },
            title = { Text("Google Play Services Required") },
            text = { 
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Google Maps requires Google Play Services to function.")
                    Text(
                        text = "Error: $googlePlayServicesError",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Please use an emulator with Google Play Services enabled.\n" +
                        "Recommended: Google Play ARM64 v8a system image."
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showGooglePlayServicesDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Permission denied dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Location Permission Required") },
            text = { Text("Please grant location permission to see your current location on the map.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    userLocation = jakarta
                }) {
                    Text("Use Default")
                }
            }
        )
    }
    
    // Map display - only show if Google Play Services is available
    if (isGooglePlayServicesAvailable) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = hasLocationPermission,
                zoomControlsEnabled = true
            )
        ) {
        // User location marker (blue)
        userLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Your Location",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }
        
        // Tukang markers (red)
        tukangLocations.forEach { tukang ->
            if (tukang.lat != 0.0 && tukang.lng != 0.0 && tukang.isAvailable) {
                Marker(
                    state = MarkerState(position = LatLng(tukang.lat, tukang.lng)),
                    title = tukang.name,
                    snippet = "${tukang.type} - ${if (tukang.isAvailable) "Available" else "Busy"}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }
        }
        }
    } else {
        // Fallback UI when Google Play Services is not available
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
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
                Text(
                    text = "Please use an emulator with:\n• Google Play ARM64 v8a system image\n• Google Play Services enabled",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getUserLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    try {
        // Get last known location (permission already checked before calling)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(LatLng(it.latitude, it.longitude))
            }
        }
    } catch (e: Exception) {
        // Handle error silently, will default to Jakarta
    }
}
