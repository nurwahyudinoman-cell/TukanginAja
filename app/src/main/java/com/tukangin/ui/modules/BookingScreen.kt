package com.tukangin.ui.modules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tukangin.modules.booking.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel = hiltViewModel()
) {
    val state by viewModel.bookings.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Booking List") }) }
    ) { paddingValues ->
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.padding(paddingValues).padding(16.dp))
            error != null -> Text(
                text = "Error: $error",
                modifier = Modifier.padding(paddingValues).padding(16.dp)
            )
            state.isEmpty() -> Text(
                text = "No bookings yet",
                modifier = Modifier.padding(paddingValues).padding(16.dp)
            )
            else -> {
                Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                    state.forEach { booking ->
                        Text("Booking: ${booking.serviceType} — ${booking.status}")
                    }
                }
            }
        }
    }
}
